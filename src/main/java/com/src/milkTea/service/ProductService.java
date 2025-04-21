package com.src.milkTea.service;

import com.src.milkTea.dto.request.ProductRequest;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.entities.Category;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.repository.CategoryRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.specification.ProductSpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

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
            throw new NotFoundException("Product already deleted");
        }
        // Delete the product
        // Set the status to DELETED
        Product existingProduct = productRepository.findByIdAndStatus(productId, ProductStatusEnum.ACTIVE).orElseThrow(() -> new NotFoundException("Product not found"));
        existingProduct.setStatus(ProductStatusEnum.DELETED);
        existingProduct.setDeleteAt(LocalDateTime.now());
        productRepository.save(existingProduct);
    }

    public ProductResponse updateProduct(Long productId, ProductRequest productRequest) {
        // Check if the product exists
        Product existingProduct = productRepository.findById(productId).orElseThrow(() -> new NotFoundException("Product not found"));

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

    public PagingResponse<ProductResponse> getAllProducts(Pageable pageable) {

        Page<Product> products = productRepository.findAll(pageable);

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


    public PagingResponse<ProductResponse> filterProducts(String name, Double minPrice, Double maxPrice, String categoryName, Pageable pageable) {

        Specification<Product> spec = Specification.
                where(ProductSpecification.nameContains(name))
                .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                .and(ProductSpecification.categoryNameContains(categoryName));


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

}
