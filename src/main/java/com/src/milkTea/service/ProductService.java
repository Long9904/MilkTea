package com.src.milkTea.service;

import com.src.milkTea.dto.request.ProductRequest;
import com.src.milkTea.dto.response.CategoryResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.repository.CategoryRepository;
import com.src.milkTea.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private final ModelMapper modelMapper;

    public ProductService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

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
        if (!categoryRepository.existsById(productRequest.getCategoryId())) {
            throw new NotFoundException("Category not found");
        }

        // Convert ProductRequest to Product entity
        Product product = modelMapper.map(productRequest, Product.class);
        product.setCategory(categoryRepository.findById(productRequest.getCategoryId()).orElse(null));

        // Set default status to ACTIVE
        product.setStatus(ProductStatusEnum.ACTIVE);

        // Save the product to the database
        Product savedProduct = productRepository.save(product);
        // Convert the saved product to ProductResponse
        ProductResponse productResponse = modelMapper.map(savedProduct, ProductResponse.class);

        // Map the category to CategoryResponse
        productResponse.setCategory(modelMapper.map(savedProduct.getCategory(), CategoryResponse.class));
        return productResponse;
    }
}
