package com.src.milkTea.service;

import com.src.milkTea.dto.request.PromotionRequest;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.dto.response.PromotionResponse;
import com.src.milkTea.entities.Orders;
import com.src.milkTea.entities.Promotion;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.ProductException;
import com.src.milkTea.exception.StatusException;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.PromotionRepository;
import com.src.milkTea.utils.DateTimeUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OrderRepository orderRepository;


    // Create a new promotion
    public void createPromotion(PromotionRequest promotionRequest) {

        // Check if the promotion code already exists
        if (promotionRepository.existsByCode(promotionRequest.getCode())) {
            throw new DuplicateException(List.of("Promotion code already exists"));
        }

        Promotion promotion = modelMapper.map(promotionRequest, Promotion.class);

        // Convert dateOpen and dateEnd from String to LocalDateTime
        promotion.setDateOpen(DateTimeUtils.convertStringToDate(promotionRequest.getDateOpen()));
        promotion.setDateEnd(DateTimeUtils.convertStringToDate(promotionRequest.getDateEnd()));

        // Check if the promotion open date and end date are valid
        if (promotion.getDateOpen().isAfter(promotion.getDateEnd())) {
            throw new ProductException("Promotion open date must be before end date");
        }

        promotion.setStatus(ProductStatusEnum.ACTIVE);

        promotionRepository.save(promotion);
    }

    // Update an existing promotion
    public void updatePromotion(Long id, PromotionRequest promotionRequest) {

        // Check if the promotion code already exists

        if (promotionRepository.existsByCodeAndIdNot(promotionRequest.getCode(), id)) {
            throw new DuplicateException(List.of("Promotion code already exists"));
        }

        Promotion promotion = promotionRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Promotion not found"));
        modelMapper.map(promotionRequest, promotion);

        // Convert dateOpen and dateEnd from String to LocalDateTime
        promotion.setDateOpen(DateTimeUtils.convertStringToDate(promotionRequest.getDateOpen()));
        promotion.setDateEnd(DateTimeUtils.convertStringToDate(promotionRequest.getDateEnd()));
        promotionRepository.save(promotion);
    }

    // Delete a promotion, change status to DELETED or ACTIVE
    public void deletePromotion(Long id, String status) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Promotion not found"));
        if (status.equalsIgnoreCase("ACTIVE")) {
            promotion.setStatus(ProductStatusEnum.ACTIVE);
        } else if (status.equalsIgnoreCase("DELETED")) {
            promotion.setStatus(ProductStatusEnum.DELETED);
        } else {
            throw new StatusException("Invalid status: " + status);
        }
        promotionRepository.save(promotion);
    }

    // Get a promotion by ID
    public PromotionResponse getPromotionById(Long id) {
        Promotion promotion = promotionRepository.findById(id).orElseThrow(()
                -> new NotFoundException("Promotion not found"));
        return modelMapper.map(promotion, PromotionResponse.class);
    }

    // Get all promotions by Pagination
    public PagingResponse<PromotionResponse> getAllPromotions(Pageable pageable) {
        Page<Promotion> promotions = promotionRepository.findAll(pageable);
        // Convert Page<Promotion> to Page<PromotionResponse>
        List<PromotionResponse> promotionResponses = promotions.getContent().stream()
                .map(promotion -> modelMapper.map(promotion, PromotionResponse.class))
                .toList();
        // Create a PagingResponse object
        PagingResponse<PromotionResponse> response = new PagingResponse<>();
        response.setData(promotionResponses);
        response.setPage(promotions.getNumber());
        response.setSize(promotions.getSize());
        response.setTotalElements(promotions.getTotalElements());
        response.setLast(promotions.isLast());
        response.setTotalPages(promotions.getTotalPages());
        return response;
    }

    public boolean checkPromotion(Long promotionId, Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(()
                -> new NotFoundException("Order not found"));

        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow(()
                -> new NotFoundException("Promotion not found"));

        if (promotion.getStatus() == ProductStatusEnum.DELETED) {
            throw new StatusException("Promotion is not valid: " + promotion.getStatus());
        }

        // Check if the promotion open date and end date are valid
        LocalDateTime now = LocalDateTime.now();
        if (promotion.getDateOpen().isAfter(now) || promotion.getDateEnd().isBefore(now)) {
            throw new ProductException("Promotion is not valid: " + promotion.getDateOpen()
                    + " - " + promotion.getDateEnd());
        }
        // Check if the order total is greater than or equal to the promotion minTotal
        if (order.getTotalPrice() < promotion.getMinTotal()) {
            throw new StatusException("Order total is less than the promotion minTotal");
        }
        return true;
    }
}
