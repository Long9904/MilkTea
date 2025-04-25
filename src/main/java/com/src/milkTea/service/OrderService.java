package com.src.milkTea.service;

import com.src.milkTea.dto.request.OrderItemRequest;
import com.src.milkTea.dto.request.OrderRequest;
import com.src.milkTea.entities.OrderDetail;
import com.src.milkTea.entities.Orders;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.OrderStatusEnum;
import com.src.milkTea.enums.ProducSizeEnum;
import com.src.milkTea.repository.OrderDetailRepository;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void createOrder(OrderRequest orderRequest) {

        Orders order = new Orders();
        order.setNote(orderRequest.getNote());
        order.setUser(userUtils.getCurrentUser());
        order.setStatus(OrderStatusEnum.PENDING);

        double totalPrice = 0;
        Orders savedOrder = orderRepository.save(order);

        // Save order details
        for (OrderItemRequest item : orderRequest.getParentItems()) {

            if (item.isCombo()) {
                // Xử lí trường hợp là combo
                return;
            } else {
                // Xử lý cho sản phẩm thông thường
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSize(ProducSizeEnum.valueOf(item.getSize()));
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                orderDetail.setUnitPrice(product.getBasePrice()); // Xử lý giá sản phẩm ở single product
                orderDetail.setProduct(product);
                orderDetail.setOrders(savedOrder);
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
                    Product childProduct = productRepository.findById(childItem.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));

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
        orderRepository.save(savedOrder);
    }
}
