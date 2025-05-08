package com.src.milkTea.service;

import com.src.milkTea.dto.request.ProductRequest;
import com.src.milkTea.dto.request.ProductRequestV2;
import com.src.milkTea.dto.response.DefaultToppingResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.dto.response.ProductResponseV2;
import com.src.milkTea.entities.DefaultTopping;
import com.src.milkTea.entities.Product;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.repository.DefaultToppingRepository;
import com.src.milkTea.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultToppingService {

    @Autowired
    private ProductService productService;

    @Autowired
    private DefaultToppingRepository defaultToppingRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    // Tạo 1 product kèm default topping
    @Transactional
    public void createProductWithDefaultTopping(ProductRequestV2 productRequestV2) {

        // Tạo product từ productRequestV2, sử dụng lại hàm tạo product của ProductService
        ProductResponse productResponse =
                productService.createProduct(modelMapper.map(productRequestV2, ProductRequest.class));

        // Lưu default topping từ productRequestV2
        productRequestV2.getDefaultToppings().forEach(defaultTopping -> {
            Product product = productRepository.findById(productResponse.getId()).
                    orElseThrow(() -> new NotFoundException("Product not found"));
            Product topping = productRepository.findById(defaultTopping.getToppingId()).
                    orElseThrow(() -> new NotFoundException("Topping not found"));
            // Lưu default topping vào database
            DefaultTopping defaultToppingEntity = new DefaultTopping();
            defaultToppingEntity.setProduct(product);
            defaultToppingEntity.setTopping(topping);
            defaultToppingEntity.setQuantity(defaultTopping.getQuantity());
            defaultToppingRepository.save(defaultToppingEntity);
        });

    }

    // Update product kèm default topping, dùng hàm updateProduct của ProductService
    // Update default topping bằng cách xóa default topping cũ và thêm default topping mới
    @Transactional
    public void updateProductWithDefaultTopping(Long productId, ProductRequestV2 productRequestV2) {
        // Update product
        ProductResponse productResponse =
                productService.updateProduct(productId, modelMapper.map(productRequestV2, ProductRequest.class));

        // Xóa default topping cũ
        defaultToppingRepository.deleteAllByProductId(productId);
        // Lưu default topping mới giống như hàm tạo product ở trên
        productRequestV2.getDefaultToppings().forEach(defaultTopping -> {
            Product product = productRepository.findById(productResponse.getId()).
                    orElseThrow(() -> new NotFoundException("Product not found"));
            Product topping = productRepository.findById(defaultTopping.getToppingId()).
                    orElseThrow(() -> new NotFoundException("Topping not found"));
            // Lưu default topping vào database
            DefaultTopping defaultToppingEntity = new DefaultTopping();
            defaultToppingEntity.setProduct(product);
            defaultToppingEntity.setTopping(topping);
            defaultToppingEntity.setQuantity(defaultTopping.getQuantity());
            defaultToppingRepository.save(defaultToppingEntity);
        });
    }


    public List<DefaultToppingResponse> getDefaultToppingByProductId(Long productId) {
        List<DefaultTopping> defaultToppings = defaultToppingRepository.findAllByProductId(productId);
        // Chuyển đổi defaultTopping sang DefaultToppingResponse

        return defaultToppings.stream().map(defaultTopping -> {
            DefaultToppingResponse defaultToppingResponse = new DefaultToppingResponse();
            defaultToppingResponse.setToppingId(defaultTopping.getTopping().getId());
            defaultToppingResponse.setToppingName(defaultTopping.getTopping().getName());
            defaultToppingResponse.setQuantity(defaultTopping.getQuantity());
            Product product = productRepository.findById(defaultTopping.getTopping().getId()).
                    orElseThrow(() -> new NotFoundException("Topping not found"));
            defaultToppingResponse.setToppingImage(product.getImageUrl());
            return defaultToppingResponse;
        }).toList();
    }

    public ProductResponseV2 getProductWithDefaultToppingByProductId(Long productId) {
        Product product = productRepository.findById(productId).
                orElseThrow(() -> new NotFoundException("Product not found"));
        ProductResponseV2 productResponseV2 =  convertToProductResponse(product);
        // Lấy default topping của product
        List<DefaultTopping> defaultToppings = defaultToppingRepository.findAllByProductId(productId);
        // Chuyển đổi defaultTopping sang DefaultToppingResponse
        List<DefaultToppingResponse> defaultToppingResponses = defaultToppings.stream().map(defaultTopping -> {
            DefaultToppingResponse defaultToppingResponse = new DefaultToppingResponse();
            defaultToppingResponse.setToppingId(defaultTopping.getTopping().getId());
            defaultToppingResponse.setToppingName(defaultTopping.getTopping().getName());
            defaultToppingResponse.setQuantity(defaultTopping.getQuantity());
            defaultToppingResponse.setToppingImage(defaultTopping.getTopping().getImageUrl());
            return defaultToppingResponse;
        }).toList();
        productResponseV2.setDefaultToppings(defaultToppingResponses);

        return productResponseV2;
    }

    private ProductResponseV2 convertToProductResponse(Product product) {
        // Convert Product to ProductResponse
        ProductResponseV2 response = modelMapper.map(product, ProductResponseV2.class);
        // Map id and name of the category to ProductResponse
        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }
        return response;
    }
}
