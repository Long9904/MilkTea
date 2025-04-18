package com.src.milkTea.service;

import com.src.milkTea.dto.CategoryDTO;
import com.src.milkTea.dto.response.CategoryResponse;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.entities.Category;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.PageException;
import com.src.milkTea.exception.StatusException;
import com.src.milkTea.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private final ModelMapper modelMapper;

    public CategoryService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Category createCategory(CategoryDTO categoryDTO) {
        // Check if the category name already exists
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new DuplicateException(List.of("Category name already exists"));
        }
        Category category = modelMapper.map(categoryDTO, Category.class);
        // Set default status to ACTIVE
        category.setStatus(ProductStatusEnum.ACTIVE);
        return  categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryDTO categoryDTO) {
        // Check if the category name already exists
        if (categoryRepository.existsByNameAndIdNot(categoryDTO.getName(), id)) {
            throw new DuplicateException(List.of("Category name already exists"));
        }
        // Find the category by ID
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        modelMapper.map(categoryDTO, existingCategory);
        return categoryRepository.save(existingCategory);
    }

    public void deleteCategory(Long id) {
        // Check if the category exists
        if(categoryRepository.findByIdAndStatus(id, ProductStatusEnum.DELETED).isPresent()) {
            throw new StatusException("Category already deleted");
        }

        Category existingCategory = categoryRepository.findByIdAndStatus(id, ProductStatusEnum.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Set the status to INACTIVE
        existingCategory.setStatus(ProductStatusEnum.DELETED);
        existingCategory.setDeleteAt(LocalDateTime.now());
        categoryRepository.save(existingCategory);
    }

    public PagingResponse<CategoryResponse> getAllCategories(int page, int size) {

        if (page < 0 || size <= 0) {
            throw new PageException("Invalid page or size: page must >= 0 and size > 0");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        if (page >= categoryPage.getTotalPages() && categoryPage.getTotalPages() > 0) {
            throw new PageException("Page number exceeds total pages: " + page + " >= " + categoryPage.getTotalPages());
        }

       List<CategoryResponse> categories = categoryPage.getContent().stream()
                .map(category -> modelMapper.map(category, CategoryResponse.class))
                .toList();

        PagingResponse<CategoryResponse> response = new PagingResponse<>();
        response.setData(categories);
        response.setPage(page);
        response.setSize(size);
        response.setTotalPages(categoryPage.getTotalPages());
        response.setTotalElements(categoryPage.getTotalElements());
        response.setLast(categoryPage.isLast());
        return response;
    }

}
