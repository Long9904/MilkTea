package com.src.milkTea.service;

import com.src.milkTea.dto.request.OrderItemRequest;
import com.src.milkTea.dto.request.OrderRequest;
import com.src.milkTea.dto.response.OrderDetailResponse;
import com.src.milkTea.dto.response.OrderResponse;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.entities.ComboDetail;
import com.src.milkTea.entities.OrderDetail;
import com.src.milkTea.entities.Orders;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.OrderStatusEnum;
import com.src.milkTea.enums.ProducSizeEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.ProductException;
import com.src.milkTea.repository.ComboDetailRepository;
import com.src.milkTea.repository.OrderDetailRepository;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.specification.OrderSpecification;
import com.src.milkTea.utils.UserUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ComboDetailRepository comboDetailRepository;

    @Autowired
    private ModelMapper modelMapper;


    public Orders addItemToCart(OrderRequest orderRequest) {

        Orders order = new Orders();
        order.setUser(userUtils.getCurrentUser());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setTotalPrice(0);
        Orders savedOrder = orderRepository.save(order);
        boolean isSuccessOrder = false;

        double totalPrice = 0;


        // Save order details
        for (OrderItemRequest item : orderRequest.getParentItems()) {

            if (item.isCombo()) {
                Product comboProduct = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Combo product not found"));

                if (comboProduct.getProductType() != ProductTypeEnum.COMBO) {
                    throw new ProductException("Product is not a combo");
                }

                // Tạo OrderDetail cha cho combo
                OrderDetail comboDetail = new OrderDetail();
                comboDetail.setOrders(savedOrder);
                comboDetail.setProduct(comboProduct);
                comboDetail.setQuantity(item.getQuantity());
                comboDetail.setSize(ProducSizeEnum.NONE);
                comboDetail.setUnitPrice(comboProduct.getBasePrice());
                comboDetail.setCombo(true);
                comboDetail.setNote(item.getNote());
                OrderDetail savedComboDetail = orderDetailRepository.save(comboDetail);

                totalPrice += comboProduct.getBasePrice() * item.getQuantity();

                // Xử lý các childItems trong combo (ly trà sữa trong combo)
                for (OrderItemRequest childItem : item.getChildItems()) {
                    Product childProduct = productRepository.findById(childItem.getProductId())
                            .orElseThrow(() -> new NotFoundException("Child product not found"));
                    //
                    if (childProduct.getProductType() == ProductTypeEnum.COMBO) {
                        throw new ProductException("Product is not a SINGLE product");
                    }// Không cho combo như là 1 sản phẩm SINGLE

                    OrderDetail childDetail = new OrderDetail();
                    childDetail.setOrders(savedOrder);
                    childDetail.setProduct(childProduct);
                    childDetail.setParent(savedComboDetail); // Parent là combo
                    childDetail.setQuantity(childItem.getQuantity());
                    childDetail.setSize(ProducSizeEnum.valueOf(childItem.getSize()));
                    childDetail.setUnitPrice(0); // Sản phẩm con trong combo giá 0
                    childDetail.setCombo(false);
                    childDetail.setNote(childItem.getNote());
                    OrderDetail savedChildDetail = orderDetailRepository.save(childDetail);

                    // Xử lý tiếp topping nếu có (childItems của ly trà sữa)
                    for (OrderItemRequest toppingItem : childItem.getChildItems()) {
                        Product toppingProduct = productRepository.findById(toppingItem.getProductId())
                                .orElseThrow(() -> new NotFoundException("Topping product not found"));

                        if (toppingProduct.getProductType() == ProductTypeEnum.COMBO) {
                            throw new ProductException("Product is not a SINGLE product");
                        } // Không cho combo như là topping

                        OrderDetail toppingDetail = new OrderDetail();
                        toppingDetail.setOrders(savedOrder);
                        toppingDetail.setProduct(toppingProduct);
                        toppingDetail.setParent(savedChildDetail); // Parent là ly trà sữa
                        toppingDetail.setQuantity(toppingItem.getQuantity());
                        toppingDetail.setSize(ProducSizeEnum.NONE);
                        toppingDetail.setUnitPrice(toppingProduct.getBasePrice());
                        toppingDetail.setCombo(false);
                        toppingDetail.setNote(toppingItem.getNote());
                        orderDetailRepository.save(toppingDetail);

                        // Topping có thể tính phí
                        totalPrice += toppingProduct.getBasePrice() * toppingItem.getQuantity();
                    }
                }

            } else {
                // Xử lý cho sản phẩm thông thường
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSize(ProducSizeEnum.valueOf(item.getSize()));
                Product product = productRepository.findById(item.getProductId()).orElseThrow(()
                        -> new NotFoundException("Product not found"));

                switch (item.getSize()) {
                    case "M" -> orderDetail.setUnitPrice(product.getBasePrice());
                    case "L" -> orderDetail.setUnitPrice(product.getBasePrice() + 5000);
                    case "S" -> orderDetail.setUnitPrice(product.getBasePrice() - 5000);
                    case "XL" -> orderDetail.setUnitPrice(product.getBasePrice() + 10000);
                }

                orderDetail.setProduct(product);
                orderDetail.setOrders(order);
                orderDetail.setCombo(false);

                // Xử lí size cho sản phẩm thông thường
                OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);

                // Tính tổng giá trị đơn hàng
                totalPrice += orderDetail.getUnitPrice() * orderDetail.getQuantity();

                // Xử lý cho sản phẩm con (nếu có)
                for (OrderItemRequest childItem : item.getChildItems()) {
                    OrderDetail childOrderDetail = new OrderDetail();
                    childOrderDetail.setQuantity(childItem.getQuantity());
                    childOrderDetail.setSize(ProducSizeEnum.valueOf(childItem.getSize()));
                    Product childProduct = productRepository.findById(childItem.getProductId()).orElseThrow(()
                            -> new RuntimeException("Product not found"));

                    childOrderDetail.setUnitPrice(childProduct.getBasePrice());

                    childOrderDetail.setProduct(childProduct);
                    childOrderDetail.setOrders(savedOrder);
                    childOrderDetail.setParent(savedOrderDetail); // Set parent
                    childOrderDetail.setCombo(false);
                    orderDetailRepository.save(childOrderDetail);

                    // Tính tổng giá trị đơn hàng
                    totalPrice += childOrderDetail.getUnitPrice() * childOrderDetail.getQuantity();
                }
            }
        }
        // Cập nhật tổng giá trị đơn hàng
        savedOrder.setTotalPrice(totalPrice);
        return orderRepository.save(savedOrder);
    }


    public PagingResponse<OrderResponse> getAllOrders(Double minPrice,
                                                      Double maxPrice,
                                                      String status,
                                                      String staffName,
                                                      Pageable pageable) {
        Specification<Orders> spec = Specification.where(OrderSpecification.priceBetween(minPrice, maxPrice))
                .and(OrderSpecification.staffNameContains(staffName));
        if (status != null && !status.isEmpty()) {
            spec = spec.and(OrderSpecification.orderStatus(OrderStatusEnum.valueOf(status)));
        } else {
            spec = spec.and(OrderSpecification.orderStatus(OrderStatusEnum.PENDING));
            spec = spec.or(OrderSpecification.orderStatus(OrderStatusEnum.CONFIRMED));
            spec = spec.or(OrderSpecification.orderStatus(OrderStatusEnum.CANCELLED));
        }
        Page<Orders> orderPage = orderRepository.findAll(spec, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent()
                .stream()
                .map(this::convertToOrderResponse).toList();
        PagingResponse<OrderResponse> response = new PagingResponse<>();
        response.setData(orderResponses);
        response.setPage(orderPage.getNumber());
        response.setSize(orderPage.getSize());
        response.setTotalElements(orderPage.getTotalElements());
        response.setTotalPages(orderPage.getTotalPages());
        response.setLast(orderPage.isLast());
        return response;
    }

    private OrderResponse convertToOrderResponse(Orders order) {
        OrderResponse orderResponse = modelMapper.map(order, OrderResponse.class);
        if (order.getUser() != null) {
            orderResponse.setUserId(order.getUser().getId());
            orderResponse.setUserName(order.getUser().getFullName());
        }
        return orderResponse;
    }

    public List<OrderDetailResponse> buildOrderDetailTree(List<OrderDetail> orderDetails) {
        Map<Long, OrderDetailResponse> map = new LinkedHashMap<>();
        List<OrderDetailResponse> roots = new ArrayList<>();

        // Bước 1: Đổi từng OrderDetail thành DTO
        for (OrderDetail detail : orderDetails) {
            OrderDetailResponse dto = new OrderDetailResponse();
            dto.setId(detail.getId());
            dto.setProductName(detail.getProduct().getName());
            dto.setQuantity(detail.getQuantity());
            dto.setUnitPrice(detail.getUnitPrice());
            dto.setSize(detail.getSize().toString());
            dto.setNote(detail.getNote());
            dto.setCombo(detail.isCombo());

            map.put(detail.getId(), dto);

            // Nếu không có parent -> là root
            if (detail.getParent() == null) {
                roots.add(dto);
            }
        }

        // Bước 2: Gán child vào parent
        for (OrderDetail detail : orderDetails) {
            if (detail.getParent() != null) {
                OrderDetailResponse parentDto = map.get(detail.getParent().getId());
                OrderDetailResponse childDto = map.get(detail.getId());
                parentDto.getChildItems().add(childDto);
            }
        }
        return roots;
    }

    public List<OrderDetailResponse> getOrderDetails(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrdersOrderByParentIdAscIdAsc(orderId);
        return buildOrderDetailTree(orderDetails);
    }

    public void updateOrderStatus(Long id, String status) {
        Orders order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        OrderStatusEnum orderStatus = OrderStatusEnum.valueOf(status);
        if (orderStatus == OrderStatusEnum.CANCELLED) {
            order.setStatus(OrderStatusEnum.CANCELLED);
        } else if (orderStatus == OrderStatusEnum.CONFIRMED) {
            order.setStatus(OrderStatusEnum.CONFIRMED);
        } else if (orderStatus == OrderStatusEnum.PAID) {
            order.setStatus(OrderStatusEnum.PAID);
        } else {
            throw new ProductException("Invalid order status");
        }
        orderRepository.save(order);
    }
}
