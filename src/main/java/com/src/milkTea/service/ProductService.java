package com.src.milkTea.service;

import com.src.milkTea.dto.request.ComboItemRequest;
import com.src.milkTea.dto.request.ComboItemRequestV2;
import com.src.milkTea.dto.request.ProductRequest;
import com.src.milkTea.dto.response.ComboItemResponse;
import com.src.milkTea.dto.response.ComboItemResponseV2;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.entities.Category;
import com.src.milkTea.entities.ComboDetail;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.StatusException;
import com.src.milkTea.repository.CategoryRepository;
import com.src.milkTea.repository.ComboDetailRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ComboDetailRepository comboDetailRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        // Check duplicate name and product code
        List<String> duplicates = new ArrayList<>();
        if (productRepository.existsByName(productRequest.getName())) {
            duplicates.add("Product name already exists");
        }
        if (productRepository.existsByProductCode(productRequest.getProductCode())) {
            duplicates.add("Product code already exists");
        }
        if (!duplicates.isEmpty()) {
            throw new DuplicateException(duplicates);
        }
        // Check if category exists or not
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(() -> new NotFoundException("Category not found"));

        // Convert ProductRequest to Product entity and map the category
        Product product = modelMapper.map(productRequest, Product.class);
        product.setCategory(category);
        product.setStatus(ProductStatusEnum.ACTIVE);

        // Save the product
        Product savedProduct = productRepository.save(product);

        // Map to response
        ProductResponse productResponse = modelMapper.map(savedProduct, ProductResponse.class);
        productResponse.setCategoryId(category.getId());
        productResponse.setCategoryName(category.getName());

        return productResponse;
    }


    public void softDeleteProduct(Long productId) {
        // Check if the product is already deleted
        if (productRepository.findByIdAndStatus(productId, ProductStatusEnum.DELETED).isPresent()) {
            throw new StatusException("Product already deleted");
        }
        // Delete the product
        // Set the status to DELETED
        Product existingProduct = productRepository.findByIdAndStatus(productId, ProductStatusEnum.ACTIVE).orElseThrow(()
                -> new NotFoundException("Product not found"));
        existingProduct.setStatus(ProductStatusEnum.DELETED);
        existingProduct.setDeleteAt(LocalDateTime.now());
        productRepository.save(existingProduct);
    }

    public ProductResponse updateProduct(Long productId, ProductRequest productRequest) {
        // Check if the product exists
        Product existingProduct = productRepository.findById(productId).orElseThrow(()
                -> new NotFoundException("Product not found"));
        // Check duplicate name and product code
        List<String> duplicates = new ArrayList<>();
        if (productRepository.existsByNameAndIdNot(productRequest.getName(), productId)) {
            duplicates.add("Product name already exists");
        }
        if (productRepository.existsByProductCodeAndIdNot(productRequest.getProductCode(), productId)) {
            duplicates.add("Product code already exists");
        }
        if (!duplicates.isEmpty()) {
            throw new DuplicateException(duplicates);
        }

        // Check if category exists or not
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(()
                -> new NotFoundException("Category not found"));

        // Update the product
        modelMapper.map(productRequest, existingProduct);
        existingProduct.setCategory(category);
        existingProduct.setUpdateAt(LocalDateTime.now());
        existingProduct.setStatus(ProductStatusEnum.valueOf(productRequest.getStatus()));
        Product updatedProduct = productRepository.save(existingProduct);

        // Map to response
        ProductResponse productResponse = modelMapper.map(updatedProduct, ProductResponse.class);
        productResponse.setCategoryId(category.getId());
        productResponse.setCategoryName(category.getName());

        return productResponse;
    }

    private ProductResponse convertToProductResponse(Product product) {
        // Convert Product to ProductResponse
        ProductResponse response = modelMapper.map(product, ProductResponse.class);
        // Map id and name of the category to ProductResponse
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        return response;
    }

    public PagingResponse<ProductResponse> getAllProducts(Pageable pageable, String productType, String productUsage) {
        Specification<Product> spec = null;

        if (productType != null && !productType.isEmpty()) {
            ProductTypeEnum type = ProductTypeEnum.valueOf(productType.toUpperCase());
            spec = Specification.where(ProductSpecification.productTypeEquals(type));
        }

        if (productUsage != null && !productUsage.isEmpty()) {
            ProductUsageEnum usage = ProductUsageEnum.valueOf(productUsage.toUpperCase());
            if (spec == null) {
                spec = Specification.where(ProductSpecification.productUsageEquals(usage));
            } else {
                spec = spec.or(ProductSpecification.productUsageEquals(usage));
            }
        }


        // Find all products with pagination
        Page<Product> products = productRepository.findAll(spec, pageable);

        // Convert the Page<Product> to List<ProductResponse>
        List<ProductResponse> productResponses = products.getContent()
                .stream()
                .map(this::convertToProductResponse).toList();

        // Create a PagingResponse object
        PagingResponse<ProductResponse> pagingResponse = new PagingResponse<>();
        pagingResponse.setData(productResponses);
        pagingResponse.setPage(products.getNumber());
        pagingResponse.setSize(products.getSize());
        pagingResponse.setTotalPages(products.getTotalPages());
        pagingResponse.setTotalElements(products.getTotalElements());
        pagingResponse.setLast(products.isLast());
        // Return the PagingResponse
        return pagingResponse;
    }


    public PagingResponse<ProductResponse> filterProducts(String name,
                                                          Double minPrice,
                                                          Double maxPrice,
                                                          String categoryName,
                                                          String productType,
                                                          String productUsage,
                                                          Pageable pageable) {
        // Create a Specification to filter products
        Specification<Product> spec = Specification.
                where(ProductSpecification.nameContains(name))
                .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                .and(ProductSpecification.categoryNameContains(categoryName));

        if (productType != null && !productType.isEmpty()) {
            ProductTypeEnum type = ProductTypeEnum.valueOf(productType.toUpperCase());
            spec = spec.and(ProductSpecification.productTypeEquals(type));
        }

        if (productUsage != null && !productUsage.isEmpty()) {
            ProductUsageEnum usage = ProductUsageEnum.valueOf(productUsage.toUpperCase());
            spec = spec.and(ProductSpecification.productUsageEquals(usage));
        }

        // Find all products with pagination and filtering
        Page<Product> products = productRepository.findAll(spec, pageable);
        // Convert the Page<Product> to List<ProductResponse>
        List<ProductResponse> productResponses = products.getContent()
                .stream()
                .map(this::convertToProductResponse).toList();

        // Create a PagingResponse object
        PagingResponse<ProductResponse> pagingResponse = new PagingResponse<>();
        pagingResponse.setData(productResponses);
        pagingResponse.setPage(products.getNumber());
        pagingResponse.setSize(products.getSize());
        pagingResponse.setTotalPages(products.getTotalPages());
        pagingResponse.setTotalElements(products.getTotalElements());
        pagingResponse.setLast(products.isLast());
        // Return the PagingResponse
        return pagingResponse;
    }

    // Update combo details áp dụng cho delete, update, add combo item
    @Transactional
    public void updateComboItem(Long comboId, ComboItemRequest comboItemRequest) {
        // Check if the product is a combo
        Product comboProduct = productRepository.findById(comboId)
                .orElseThrow(() -> new NotFoundException("Combo not found"));
        if (comboProduct.getProductType() != ProductTypeEnum.COMBO) {
            throw new StatusException("Product is not a combo");
        }
        // Xóa tất cả combo detail cũ --> có nghĩa là ghi đè lên
        comboDetailRepository.deleteByComboId(comboId);

        // Update the combo details
        for (ComboItemRequest.Item item : comboItemRequest.getComboItems()) {
            // Check if the child product exists
            Product childProduct = productRepository.findById(Long.valueOf(item.getProductId()))
                    .orElseThrow(() -> new NotFoundException("Child product not found"));
            // Check if the child product is a combo
            if (childProduct.getProductType() == ProductTypeEnum.COMBO) {
                throw new StatusException("Child product is a combo");
            }
            // Create a new ComboDetail entity
            ComboDetail comboDetail = new ComboDetail();
            comboDetail.setCombo(comboProduct);
            comboDetail.setChildProduct(childProduct);
            comboDetail.setQuantity(item.getQuantity());
            comboDetail.setSize(item.getSize());
            // Save the combo detail
            comboDetailRepository.save(comboDetail);
        }
        // Do toList() trả về danh sách read-only nên không thể thêm vào db
        // Hibernate cần một danh sách có thể thay đổi được
        productRepository.save(comboProduct);

    }

    public ComboItemResponse getComboByProductId(Long productId) {

        // Check if the product is a combo
        Product comboProduct = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Combo not found"));
        if (comboProduct.getProductType() != ProductTypeEnum.COMBO) {
            throw new StatusException("Product is not a combo");
        }

        // Get all combo details by combo id
        List<ComboDetail> comboDetails = comboDetailRepository.findByComboId(comboProduct.getId());
        if (comboDetails.isEmpty()) {
            throw new NotFoundException("Combo details not found");
        }

        // Convert ComboDetail to ComboItemResponse.Item
        List<ComboItemResponse.Item> items = comboDetails.stream().map(detail -> {
            ComboItemResponse.Item item = new ComboItemResponse.Item();
            item.setProductId(detail.getChildProduct().getId());
            item.setProductName(detail.getChildProduct().getName());
            item.setSize(detail.getSize());
            item.setQuantity(detail.getQuantity());
            return item;
        }).collect(Collectors.toList());

        ComboItemResponse response = new ComboItemResponse();
        response.setItemsResponse(items);
        return response;
    }

    public ProductResponse getProductById(Long productId) {
        Optional<Product> products = productRepository.findById(productId);
        //
        if (products.isEmpty()) {
            throw new NotFoundException("Product not found");
        }
        Product product = products.get();
        // Convert Product to ProductResponse
        return convertToProductResponse(product);
    }

    @Transactional
    public ProductResponse updateComboWithDetail(Long comboId, ComboItemRequestV2 request) {
        // 1. Tạo ProductRequest từ ComboItemRequestV2
        ProductRequest productRequest = getProductRequest(request);

        // 2. Gọi hàm cập nhật sản phẩm có sẵn
        ProductResponse productResponse = updateProduct(comboId, productRequest);

        // 3. Tạo ComboItemRequest từ ComboItemRequestV2
        ComboItemRequest comboItemRequest = new ComboItemRequest();
        comboItemRequest.setComboItems(request.getComboItems());

        // 4. Gọi hàm cập nhật combo detail có sẵn
        updateComboItem(comboId, comboItemRequest);

        // 5. Trả lại response từ hàm updateProduct
        return productResponse;
    }

    private static ProductRequest getProductRequest(ComboItemRequestV2 request) {
        ProductRequest productRequest = new ProductRequest();
        productRequest.setName(request.getName());
        productRequest.setBasePrice(request.getBasePrice());
        productRequest.setProductCode(request.getProductCode());
        productRequest.setImageUrl(request.getImageUrl());
        productRequest.setDescription(request.getDescription());
        productRequest.setProductType(request.getProductType());
        productRequest.setStatus(request.getStatus());
        productRequest.setProductUsage(request.getProductUsage());
        productRequest.setCategoryId(request.getCategoryId());
        return productRequest;
    }

    public ComboItemResponseV2 getProductByIdV2(Long productId) {
        // Check if the product is a combo
        Product comboProduct = productRepository.findByIdWithCategory(productId)
                .orElseThrow(() -> new NotFoundException("Combo not found"));

        if (comboProduct.getProductType() != ProductTypeEnum.COMBO) {
            throw new StatusException("Product is not a combo");
        }

        // Get all combo details by combo id
        List<ComboDetail> comboDetails = comboDetailRepository.findByComboId(comboProduct.getId());
        if (comboDetails.isEmpty()) {
            throw new NotFoundException("Combo details not found");
        }
        // Map product to ComboResponseV2
        ComboItemResponseV2 comboItemResponseV2 = getComboItemResponseV2(comboProduct);

        // Convert ComboDetail to ComboItemResponse.Item
        for (ComboDetail detail : comboDetails) {
            ComboItemResponseV2.Item item = new ComboItemResponseV2.Item();
            item.setProductId(detail.getChildProduct().getId());
            item.setProductName(detail.getChildProduct().getName());
            item.setSize(detail.getSize());
            item.setQuantity(detail.getQuantity());
            if (comboItemResponseV2.getItemsResponse() == null) {
                comboItemResponseV2.setItemsResponse(new ArrayList<>());
            }
            comboItemResponseV2.getItemsResponse().add(item);
        }
        return comboItemResponseV2;
    }

    private static ComboItemResponseV2 getComboItemResponseV2(Product comboProduct) {
        ComboItemResponseV2 comboItemResponseV2 = new ComboItemResponseV2();
        comboItemResponseV2.setId(comboProduct.getId());
        comboItemResponseV2.setName(comboProduct.getName());
        comboItemResponseV2.setBasePrice(comboProduct.getBasePrice());
        comboItemResponseV2.setProductCode(comboProduct.getProductCode());
        comboItemResponseV2.setImageUrl(comboProduct.getImageUrl());
        comboItemResponseV2.setDescription(comboProduct.getDescription());
        comboItemResponseV2.setProductType(comboProduct.getProductType());
        comboItemResponseV2.setProductUsage(comboProduct.getProductUsage());
        comboItemResponseV2.setStatus(comboProduct.getStatus());
        comboItemResponseV2.setCategoryId(comboProduct.getCategory().getId());
        comboItemResponseV2.setCategoryName(comboProduct.getCategory().getName());
        comboItemResponseV2.setCreateAt(comboProduct.getCreateAt());
        comboItemResponseV2.setUpdateAt(comboProduct.getUpdateAt());
        comboItemResponseV2.setDeleteAt(comboProduct.getDeleteAt());
        return comboItemResponseV2;
    }
}
