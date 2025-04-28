package com.src.milkTea.service;

import com.src.milkTea.dto.request.OrderItemRequest;
import com.src.milkTea.dto.request.OrderRequest;
import com.src.milkTea.entities.OrderDetail;
import com.src.milkTea.entities.Orders;
import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.OrderStatusEnum;
import com.src.milkTea.enums.ProducSizeEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.ProductException;
import com.src.milkTea.repository.ComboDetailRepository;
import com.src.milkTea.repository.OrderDetailRepository;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.utils.UserUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceV2 {

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
                        if (toppingProduct.getProductUsage() == ProductUsageEnum.MAIN) {
                            throw new ProductException("Product is not a EXTRA product");
                        } // Không cho combo như là 1 sản phẩm SINGLE

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
                if(product.getProductType() == ProductTypeEnum.COMBO) {
                    throw new ProductException("Product is not a SINGLE product");
                } // Không cho combo như là 1 sản phẩm SINGLE

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

                    // Kiểm tra xem sản phẩm con có phải là combo hoặc ly trà sữa không
                    if ( childProduct.getProductUsage() == ProductUsageEnum.MAIN) {
                        throw new ProductException("Product is not a EXTRA product");
                    } // Không cho combo như là 1 sản phẩm SINGLE

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

    public Orders addItemToExistingOrder(Long orderId, OrderRequest orderRequest) {
        Orders existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Only allow adding items to PENDING orders
        if (existingOrder.getStatus() != OrderStatusEnum.PENDING) {
            throw new ProductException("Can only add items to orders in PENDING status");
        }

        double totalPrice = existingOrder.getTotalPrice();

        // Save order details
        for (OrderItemRequest item : orderRequest.getParentItems()) {
            if (item.isCombo()) {
                Product comboProduct = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Combo product not found"));

                if (comboProduct.getProductType() != ProductTypeEnum.COMBO) {
                    throw new ProductException("Product is not a combo");
                }

                // Create parent OrderDetail for combo
                OrderDetail comboDetail = new OrderDetail();
                comboDetail.setOrders(existingOrder);
                comboDetail.setProduct(comboProduct);
                comboDetail.setQuantity(item.getQuantity());
                comboDetail.setSize(ProducSizeEnum.NONE);
                comboDetail.setUnitPrice(comboProduct.getBasePrice());
                comboDetail.setCombo(true);
                comboDetail.setNote(item.getNote());
                OrderDetail savedComboDetail = orderDetailRepository.save(comboDetail);

                totalPrice += comboProduct.getBasePrice() * item.getQuantity();

                // Handle child items in combo (milk tea cups in combo)
                for (OrderItemRequest childItem : item.getChildItems()) {
                    Product childProduct = productRepository.findById(childItem.getProductId())
                            .orElseThrow(() -> new NotFoundException("Child product not found"));

                    if (childProduct.getProductType() == ProductTypeEnum.COMBO) {
                        throw new ProductException("Product is not a SINGLE product");
                    }

                    OrderDetail childDetail = new OrderDetail();
                    childDetail.setOrders(existingOrder);
                    childDetail.setProduct(childProduct);
                    childDetail.setParent(savedComboDetail);
                    childDetail.setQuantity(childItem.getQuantity());
                    childDetail.setSize(ProducSizeEnum.valueOf(childItem.getSize()));
                    childDetail.setUnitPrice(0);
                    childDetail.setCombo(false);
                    childDetail.setNote(childItem.getNote());
                    OrderDetail savedChildDetail = orderDetailRepository.save(childDetail);

                    // Handle toppings if any
                    for (OrderItemRequest toppingItem : childItem.getChildItems()) {
                        Product toppingProduct = productRepository.findById(toppingItem.getProductId())
                                .orElseThrow(() -> new NotFoundException("Topping product not found"));

                        if (toppingProduct.getProductType() == ProductTypeEnum.COMBO) {
                            throw new ProductException("Product is not a SINGLE product");
                        }
                        if (toppingProduct.getProductUsage() == ProductUsageEnum.MAIN) {
                            throw new ProductException("Product is not a EXTRA product");
                        }

                        OrderDetail toppingDetail = new OrderDetail();
                        toppingDetail.setOrders(existingOrder);
                        toppingDetail.setProduct(toppingProduct);
                        toppingDetail.setParent(savedChildDetail);
                        toppingDetail.setQuantity(toppingItem.getQuantity());
                        toppingDetail.setSize(ProducSizeEnum.NONE);
                        toppingDetail.setUnitPrice(toppingProduct.getBasePrice());
                        toppingDetail.setCombo(false);
                        toppingDetail.setNote(toppingItem.getNote());
                        orderDetailRepository.save(toppingDetail);

                        totalPrice += toppingProduct.getBasePrice() * toppingItem.getQuantity();
                    }
                }
            } else {
                // Handle regular products
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setQuantity(item.getQuantity());
                orderDetail.setSize(ProducSizeEnum.valueOf(item.getSize()));
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product not found"));

                if(product.getProductType() == ProductTypeEnum.COMBO) {
                    throw new ProductException("Product is not a SINGLE product");
                }

                switch (item.getSize()) {
                    case "M" -> orderDetail.setUnitPrice(product.getBasePrice());
                    case "L" -> orderDetail.setUnitPrice(product.getBasePrice() + 5000);
                    case "S" -> orderDetail.setUnitPrice(product.getBasePrice() - 5000);
                    case "XL" -> orderDetail.setUnitPrice(product.getBasePrice() + 10000);
                }

                orderDetail.setProduct(product);
                orderDetail.setOrders(existingOrder);
                orderDetail.setCombo(false);

                OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);

                totalPrice += orderDetail.getUnitPrice() * orderDetail.getQuantity();

                // Handle child products (if any)
                for (OrderItemRequest childItem : item.getChildItems()) {
                    OrderDetail childOrderDetail = new OrderDetail();
                    childOrderDetail.setQuantity(childItem.getQuantity());
                    childOrderDetail.setSize(ProducSizeEnum.valueOf(childItem.getSize()));
                    Product childProduct = productRepository.findById(childItem.getProductId())
                            .orElseThrow(() -> new NotFoundException("Product not found"));

                    if (childProduct.getProductUsage() == ProductUsageEnum.MAIN) {
                        throw new ProductException("Product is not a EXTRA product");
                    }

                    childOrderDetail.setUnitPrice(childProduct.getBasePrice());
                    childOrderDetail.setProduct(childProduct);
                    childOrderDetail.setOrders(existingOrder);
                    childOrderDetail.setParent(savedOrderDetail);
                    childOrderDetail.setCombo(false);
                    orderDetailRepository.save(childOrderDetail);

                    totalPrice += childOrderDetail.getUnitPrice() * childOrderDetail.getQuantity();
                }
            }
        }

        existingOrder.setTotalPrice(totalPrice);
        return orderRepository.save(existingOrder);
    }


    public void deleteOrderDetail(Long orderId, Long orderDetailId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép sửa đơn hàng ở trạng thái PENDING
        if (order.getStatus() != OrderStatusEnum.PENDING) {
            throw new ProductException("Chỉ có thể sửa đơn hàng ở trạng thái chờ");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chi tiết đơn hàng"));

        // Kiểm tra xem chi tiết đơn hàng có thuộc về đơn hàng không
        if (!orderDetail.getOrders().getId().equals(orderId)) {
            throw new ProductException("Chi tiết đơn hàng không thuộc về đơn hàng này");
        }

        double priceToSubtract = 0;

        // Nếu là combo
        if (orderDetail.isCombo()) {
            // Tính giá của combo
            priceToSubtract += orderDetail.getUnitPrice() * orderDetail.getQuantity();
            
            // Xử lý các sản phẩm con trong combo
            if (orderDetail.getChildren() != null && !orderDetail.getChildren().isEmpty()) {
                for (OrderDetail child : orderDetail.getChildren()) {
                    // Xử lý các topping của từng sản phẩm con
                    if (child.getChildren() != null && !child.getChildren().isEmpty()) {
                        for (OrderDetail topping : child.getChildren()) {
                            priceToSubtract += topping.getUnitPrice() * topping.getQuantity();
                            orderDetailRepository.delete(topping);
                        }
                    }
                    orderDetailRepository.delete(child);
                }
            }
            orderDetailRepository.delete(orderDetail);
        }
        // Nếu là sản phẩm đơn với topping
        else if (orderDetail.getParent() == null) {
            // Tính giá sản phẩm chính
            priceToSubtract += orderDetail.getUnitPrice() * orderDetail.getQuantity();
            
            // Xử lý các topping
            if (orderDetail.getChildren() != null && !orderDetail.getChildren().isEmpty()) {
                for (OrderDetail topping : orderDetail.getChildren()) {
                    priceToSubtract += topping.getUnitPrice() * topping.getQuantity();
                    orderDetailRepository.delete(topping);
                }
            }
            orderDetailRepository.delete(orderDetail);
        }
        // Nếu là topping hoặc sản phẩm con trong combo
        else {
            // Nếu là topping, chỉ cần xóa nó và tính giá của nó
            if (orderDetail.getParent() != null && !orderDetail.getParent().isCombo()) {
                priceToSubtract = orderDetail.getUnitPrice() * orderDetail.getQuantity();
            }
            // Nếu là sản phẩm con trong combo, xóa nó và các topping của nó
            else {
                if (orderDetail.getChildren() != null && !orderDetail.getChildren().isEmpty()) {
                    for (OrderDetail topping : orderDetail.getChildren()) {
                        priceToSubtract += topping.getUnitPrice() * topping.getQuantity();
                        orderDetailRepository.delete(topping);
                    }
                }
            }
            orderDetailRepository.delete(orderDetail);
        }

        // Cập nhật tổng giá đơn hàng
        order.setTotalPrice(order.getTotalPrice() - priceToSubtract);
        orderRepository.save(order);
    }
}
