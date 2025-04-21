package com.src.milkTea.service;

import com.src.milkTea.dto.request.CategoryRequest;
import com.src.milkTea.dto.response.CategoryResponse;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.entities.Category;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.StatusException;
import com.src.milkTea.repository.CategoryRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.specification.CategorySpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ProductRepository productRepository;

    public Category createCategory(CategoryRequest categoryRequest) {
        // Check if the category name already exists
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new DuplicateException(List.of("Category name already exists"));
        }
        Category category = modelMapper.map(categoryRequest, Category.class);
        // Set default status to ACTIVE
        category.setStatus(ProductStatusEnum.ACTIVE);
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryRequest categoryRequest) {
        // Find the category by ID
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Check if the category name already exists
        if (categoryRepository.existsByNameAndIdNot(categoryRequest.getName(), id)) {
            throw new DuplicateException(List.of("Category name already exists"));
        }

        modelMapper.map(categoryRequest, existingCategory);
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        // Check if the category is already deleted
        if (categoryRepository.findByIdAndStatus(id, ProductStatusEnum.DELETED).isPresent()) {
            throw new StatusException("Category already deleted");
        }

        // Find the category by ID
        Category existingCategory = categoryRepository.findByIdAndStatus(id, ProductStatusEnum.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Set the status to INACTIVE
        existingCategory.setStatus(ProductStatusEnum.DELETED);
        existingCategory.setDeleteAt(LocalDateTime.now());
        categoryRepository.save(existingCategory);
    }

    public PagingResponse<CategoryResponse> getAllCategories(Pageable pageable) {

        Specification<Category> spec = Specification
                .where(CategorySpecification.hasStatus(ProductStatusEnum.ACTIVE));

        // Find all categories
        Page<Category> categories = categoryRepository.findAll(spec, pageable);
        // Check if there are any categories
        List<CategoryResponse> categoryResponses = categories.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();

        // Create a response object
        PagingResponse<CategoryResponse> response = new PagingResponse<>();
        response.setData(categoryResponses);
        response.setPage(categories.getNumber());
        response.setSize(categories.getSize());
        response.setTotalElements(categories.getTotalElements());
        response.setTotalPages(categories.getTotalPages());
        response.setLast(categories.isLast());
        return response;
    }

    public PagingResponse<CategoryResponse> getCategoryByName(String name, Pageable pageable) {
        //Find categories by name
        Page<Category> categories = categoryRepository.findByNameContainingIgnoreCaseAndStatus(name, pageable, ProductStatusEnum.ACTIVE);
        // Check if there are any categories
        List<CategoryResponse> categoryResponses = categories.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();

        // Create a response object
        PagingResponse<CategoryResponse> response = new PagingResponse<>();
        response.setData(categoryResponses);
        response.setPage(categories.getNumber());
        response.setSize(categories.getSize());
        response.setTotalElements(categories.getTotalElements());
        response.setTotalPages(categories.getTotalPages());
        response.setLast(categories.isLast());
        return response;
    }


    public PagingResponse<ProductResponse> getProductsByCategoryIdAndStatus(Long id, ProductStatusEnum status, Pageable pageable) {
        // Find the category by ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Check if the category is deleted
        if (category.getStatus() == ProductStatusEnum.DELETED) {
            throw new StatusException("Category is deleted");
        }

        // Find products by category ID and status
        Page<Product> products = productRepository.findByCategoryIdAndStatus(id, status, pageable);
        // Check if there are any products
        List<ProductResponse> productResponses = products.getContent().stream()
                .map(product -> modelMapper.map(product, ProductResponse.class))
                .toList();

        // Map CategoryId and CategoryName to ProductResponse
        productResponses.forEach(productResponse -> {
            CategoryResponse categoryResponse = modelMapper.map(category, CategoryResponse.class);
            productResponse.setCategoryId(categoryResponse.getId());
            productResponse.setCategoryName(categoryResponse.getName());
        });

        // Create a response object
        PagingResponse<ProductResponse> response = new PagingResponse<>();
        response.setData(productResponses);
        response.setPage(products.getNumber());
        response.setSize(products.getSize());
        response.setTotalElements(products.getTotalElements());
        response.setTotalPages(products.getTotalPages());
        response.setLast(products.isLast());
        return response;
    }
}
