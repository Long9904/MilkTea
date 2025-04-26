package com.src.milkTea.service;

import com.src.milkTea.dto.request.OrderItemRequest;
import com.src.milkTea.dto.request.OrderRequest;
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
import com.src.milkTea.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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


    public Orders createCart(String note) {
        Orders order = new Orders();
        order.setNote(note);
        order.setUser(userUtils.getCurrentUser());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setTotalPrice(0);
        order.setOrderDetails(null);
        return orderRepository.save(order);
    }


    public Orders addItemToCart(OrderRequest orderRequest, Long orderId) {

        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        double totalPrice = 0;


        // Save order details
        for (OrderItemRequest item : orderRequest.getParentItems()) {

            if (item.isCombo()) {
                Product comboProduct = productRepository.findById(item.getProductId()).orElseThrow(() -> new NotFoundException("Combo product not found"));

                if (comboProduct.getProductType() != ProductTypeEnum.COMBO) {
                    throw new ProductException("Product is not a combo");
                }

                // Tạo OrderDetail cha cho combo
                OrderDetail comboDetail = new OrderDetail();
                comboDetail.setOrders(order);
                comboDetail.setProduct(comboProduct);
                comboDetail.setQuantity(item.getQuantity());
                comboDetail.setSize(ProducSizeEnum.valueOf(item.getSize()));
                comboDetail.setUnitPrice(comboProduct.getBasePrice());
                comboDetail.setCombo(true);

                OrderDetail savedComboDetail = orderDetailRepository.save(comboDetail);
                totalPrice += comboProduct.getBasePrice() * item.getQuantity();

                // Lấy danh sách thành phần combo từ bảng ComboDetail
                List<ComboDetail> comboItems = comboDetailRepository.findByCombo(comboProduct);
                for (ComboDetail comboItem : comboItems) {
                    Product childProduct = comboItem.getChildProduct();

                    OrderDetail childDetail = new OrderDetail();
                    childDetail.setOrders(order);
                    childDetail.setProduct(childProduct);
                    childDetail.setParent(savedComboDetail);
                    childDetail.setQuantity(comboItem.getQuantity() * item.getQuantity()); // nhân theo số lượng combo
                    childDetail.setSize(ProducSizeEnum.valueOf(comboItem.getSize()));
                    childDetail.setUnitPrice(0); // miễn phí, vì đã gộp giá trong combo
                    childDetail.setCombo(true);
                    orderDetailRepository.save(childDetail);
                }

            } else {
                // Xử lý cho sản phẩm thông thường
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSize(ProducSizeEnum.valueOf(item.getSize()));
                Product product = productRepository.findById(item.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

                switch (item.getSize()) {
                    case "M" -> orderDetail.setUnitPrice(product.getBasePrice());
                    case "L" -> orderDetail.setUnitPrice(product.getBasePrice() + 5000);
                    case "S" -> orderDetail.setUnitPrice(product.getBasePrice() - 5000);
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
                    Product childProduct = productRepository.findById(childItem.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

                    childOrderDetail.setUnitPrice(childProduct.getBasePrice());

                    childOrderDetail.setProduct(childProduct);
                    childOrderDetail.setOrders(order);
                    childOrderDetail.setParent(savedOrderDetail); // Set parent
                    childOrderDetail.setCombo(false);
                    orderDetailRepository.save(childOrderDetail);

                    // Tính tổng giá trị đơn hàng
                    totalPrice += childOrderDetail.getUnitPrice() * childOrderDetail.getQuantity();
                }
            }
        }
        // Cập nhật tổng giá trị đơn hàng
        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }


    public List<OrderDetail> getOrderDetails(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrdersId(orderId);
        if (orderDetails.isEmpty()) {
            throw new NotFoundException("Order details not found");
        }
        return orderDetails;
    }
}
