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
import com.src.milkTea.exception.OrderException;
import com.src.milkTea.exception.ProductException;
import com.src.milkTea.repository.ComboDetailRepository;
import com.src.milkTea.repository.OrderDetailRepository;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.utils.UserUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xử lý các thao tác liên quan đến đơn hàng phiên bản 2
 * Hỗ trợ các chức năng:
 * - Thêm sản phẩm vào giỏ hàng mới
 * - Thêm sản phẩm vào đơn hàng có sẵn
 * - Xóa chi tiết đơn hàng
 * - Cập nhật chi tiết đơn hàng
 */
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

    /**
     * Thêm sản phẩm vào giỏ hàng mới
     * @param orderRequest Request chứa thông tin các sản phẩm cần thêm
     * @return Đơn hàng mới được tạo
     */
    @Transactional
    public Orders addItemToCart(OrderRequest orderRequest) {
        Orders order = createNewOrder();
        double totalPrice = processOrderItems(orderRequest, order);
        order.setTotalPrice(totalPrice);
        return orderRepository.save(order);
    }

    /**
     * Thêm sản phẩm vào đơn hàng có sẵn
     * @param orderId ID của đơn hàng cần thêm sản phẩm
     * @param orderRequest Request chứa thông tin các sản phẩm cần thêm
     * @return Đơn hàng sau khi được cập nhật
     */
    @Transactional
    public Orders addItemToExistingOrder(Long orderId, OrderRequest orderRequest) {
        Orders existingOrder = findAndValidateOrder(orderId);
        double additionalPrice = processOrderItems(orderRequest, existingOrder);
        existingOrder.setTotalPrice(existingOrder.getTotalPrice() + additionalPrice);
        return orderRepository.save(existingOrder);
    }

    /**
     * Tạo đơn hàng mới với trạng thái PENDING
     * @return Đơn hàng mới được tạo
     */
    private Orders createNewOrder() {
        Orders order = new Orders();
        order.setUser(userUtils.getCurrentUser());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setTotalPrice(0);
        return orderRepository.save(order);
    }

    /**
     * Tìm và kiểm tra trạng thái của đơn hàng
     * @param orderId ID của đơn hàng cần kiểm tra
     * @return Đơn hàng nếu tìm thấy và hợp lệ
     * @throws NotFoundException nếu không tìm thấy đơn hàng
     * @throws ProductException nếu đơn hàng không ở trạng thái PENDING
     */
    private Orders findAndValidateOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));
        validateOrderStatus(order);
        return order;
    }

    /**
     * Kiểm tra trạng thái của đơn hàng có phải là PENDING không
     * @param order Đơn hàng cần kiểm tra
     * @throws ProductException nếu đơn hàng không ở trạng thái PENDING
     */
    private void validateOrderStatus(Orders order) {
        if (order.getStatus() != OrderStatusEnum.PENDING) {
            throw new ProductException("Chỉ có thể thao tác với đơn hàng ở trạng thái chờ");
        }
    }

    /**
     * Xử lý danh sách các sản phẩm cần thêm vào đơn hàng
     * @param orderRequest Request chứa thông tin các sản phẩm
     * @param order Đơn hàng cần thêm sản phẩm
     * @return Tổng giá trị của các sản phẩm được thêm vào
     */
    private double processOrderItems(OrderRequest orderRequest, Orders order) {
        double totalPrice = 0;
        for (OrderItemRequest item : orderRequest.getParentItems()) {
            // Xử lý sản phẩm đơn hoặc combo
            if (item.isCombo()) {
                totalPrice += processComboItem(item, order);
            } else {
                totalPrice += processSingleItem(item, order);
            }
        }
        return totalPrice;
    }

    /**
     * Xử lý thêm combo vào đơn hàng
     * @param item Thông tin combo cần thêm
     * @param order Đơn hàng cần thêm combo
     * @return Giá của combo
     */
    private double processComboItem(OrderItemRequest item, Orders order) {
        Product comboProduct = findAndValidateComboProduct(item.getProductId());
        OrderDetail comboDetail = createComboDetail(item, order, comboProduct);
        double price = comboProduct.getBasePrice() * item.getQuantity();
        
        // Xử lý các sản phẩm con trong combo
        for (OrderItemRequest childItem : item.getChildItems()) {
            createComboChildDetail(childItem, order, comboDetail, item.getQuantity());
        }
        
        return price;
    }

    /**
     * Tìm và kiểm tra sản phẩm combo
     * @param productId ID của sản phẩm cần kiểm tra
     * @return Sản phẩm combo nếu hợp lệ
     * @throws NotFoundException nếu không tìm thấy sản phẩm
     * @throws ProductException nếu sản phẩm không phải là combo
     */
    private Product findAndValidateComboProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy combo"));
        if (product.getProductType() != ProductTypeEnum.COMBO) {
            throw new ProductException("Sản phẩm không phải là combo");
        }
        return product;
    }

    /**
     * Tạo chi tiết đơn hàng cho combo
     * @param item Thông tin combo
     * @param order Đơn hàng
     * @param comboProduct Sản phẩm combo
     * @return Chi tiết đơn hàng của combo
     */
    private OrderDetail createComboDetail(OrderItemRequest item, Orders order, Product comboProduct) {
        OrderDetail comboDetail = new OrderDetail();
        comboDetail.setOrders(order);
        comboDetail.setProduct(comboProduct);
        comboDetail.setQuantity(item.getQuantity());
        comboDetail.setSize(ProducSizeEnum.NONE);
        comboDetail.setUnitPrice(comboProduct.getBasePrice());
        comboDetail.setCombo(true);
        comboDetail.setNote(item.getNote());
        return orderDetailRepository.save(comboDetail);
    }

    /**
     * Tạo chi tiết đơn hàng cho sản phẩm con trong combo
     * @param childItem Thông tin sản phẩm con
     * @param order Đơn hàng
     * @param parent Chi tiết đơn hàng của combo cha
     * @param parentQuantity Số lượng combo cha
     */
    private void createComboChildDetail(OrderItemRequest childItem, Orders order, OrderDetail parent, int parentQuantity) {
        Product childProduct = findAndValidateChildProduct(childItem.getProductId());
        OrderDetail childDetail = new OrderDetail();
        childDetail.setOrders(order);
        childDetail.setProduct(childProduct);
        childDetail.setParent(parent);
        // Số lượng = số lượng yêu cầu * số lượng combo
        childDetail.setQuantity(childItem.getQuantity() * parentQuantity);
        childDetail.setSize(ProducSizeEnum.valueOf(childItem.getSize()));
        childDetail.setUnitPrice(0); // Sản phẩm con trong combo có giá = 0
        childDetail.setCombo(false);
        childDetail.setNote(childItem.getNote());
        orderDetailRepository.save(childDetail);
    }

    /**
     * Xử lý thêm sản phẩm đơn vào đơn hàng
     * @param item Thông tin sản phẩm đơn
     * @param order Đơn hàng
     * @return Tổng giá của sản phẩm và topping
     */
    private double processSingleItem(OrderItemRequest item, Orders order) {
        Product product = findAndValidateSingleProduct(item.getProductId());
        OrderDetail mainDetail = createMainDetail(item, order, product);
        double price = mainDetail.getUnitPrice() * mainDetail.getQuantity();

        // Xử lý các topping
        for (OrderItemRequest toppingItem : item.getChildItems()) {
            price += createToppingDetail(toppingItem, order, mainDetail, item.getQuantity());
        }

        return price;
    }

    /**
     * Tạo chi tiết đơn hàng cho sản phẩm chính
     * @param item Thông tin sản phẩm
     * @param order Đơn hàng
     * @param product Sản phẩm chính
     * @return Chi tiết đơn hàng của sản phẩm chính
     */
    private OrderDetail createMainDetail(OrderItemRequest item, Orders order, Product product) {
        OrderDetail mainDetail = new OrderDetail();
        mainDetail.setQuantity(item.getQuantity());
        mainDetail.setSize(ProducSizeEnum.valueOf(item.getSize()));
        mainDetail.setUnitPrice(calculatePriceBySize(product.getBasePrice(), item.getSize()));
        mainDetail.setProduct(product);
        mainDetail.setOrders(order);
        mainDetail.setCombo(false);
        mainDetail.setNote(item.getNote());
        return orderDetailRepository.save(mainDetail);
    }

    /**
     * Tính giá sản phẩm theo size
     * @param basePrice Giá gốc của sản phẩm
     * @param size Size của sản phẩm (S, M, L, XL)
     * @return Giá sau khi tính theo size
     */
    private double calculatePriceBySize(double basePrice, String size) {
        return switch (size) {
            case "L" -> basePrice + 5000;  // Size L: +5000
            case "S" -> basePrice - 5000;  // Size S: -5000
            case "XL" -> basePrice + 10000; // Size XL: +10000
            default -> basePrice;          // Size M: giá gốc
        };
    }

    /**
     * Tạo chi tiết đơn hàng cho topping
     * @param toppingItem Thông tin topping
     * @param order Đơn hàng
     * @param parent Chi tiết đơn hàng của sản phẩm chính
     * @param parentQuantity Số lượng sản phẩm chính
     * @return Giá của topping
     */
    private double createToppingDetail(OrderItemRequest toppingItem, Orders order, OrderDetail parent, int parentQuantity) {
        Product toppingProduct = findAndValidateTopping(toppingItem.getProductId());
        OrderDetail toppingDetail = new OrderDetail();
        // Số lượng topping = số lượng yêu cầu * số lượng sản phẩm chính
        toppingDetail.setQuantity(toppingItem.getQuantity() * parentQuantity);
        toppingDetail.setSize(ProducSizeEnum.NONE);
        toppingDetail.setUnitPrice(toppingProduct.getBasePrice());
        toppingDetail.setProduct(toppingProduct);
        toppingDetail.setOrders(order);
        toppingDetail.setParent(parent);
        toppingDetail.setCombo(false);
        toppingDetail.setNote(toppingItem.getNote());
        orderDetailRepository.save(toppingDetail);

        return toppingDetail.getUnitPrice() * toppingDetail.getQuantity();
    }

    private Product findAndValidateChildProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm con"));
        if (product.getProductType() == ProductTypeEnum.COMBO) {
            throw new ProductException("Sản phẩm con không thể là combo");
        }
        return product;
    }

    private Product findAndValidateSingleProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));
        if (product.getProductType() == ProductTypeEnum.COMBO) {
            throw new ProductException("Sản phẩm không thể là combo");
        }
        return product;
    }

    private Product findAndValidateTopping(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy topping"));
        if (product.getProductUsage() != ProductUsageEnum.EXTRA) {
            throw new ProductException("Sản phẩm không phải là topping");
        }
        return product;
    }

    @Transactional
    public void deleteOrderDetail(Long orderId, Long orderDetailId) {
        // 1. Kiểm tra order tồn tại và trạng thái
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng"));

        // Chỉ cho phép xóa item trong đơn hàng PENDING
        if (order.getStatus() != OrderStatusEnum.PENDING) {
            throw new ProductException("Chỉ có thể sửa đơn hàng ở trạng thái chờ");
        }

        // 2. Kiểm tra orderDetail tồn tại và thuộc về order
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy chi tiết đơn hàng"));

        if (!orderDetail.getOrders().getId().equals(orderId)) {
            throw new ProductException("Chi tiết đơn hàng không thuộc về đơn hàng này");
        }

        double priceToSubtract = 0;

        // 3. Xử lý 3 trường hợp xóa:
        // TRƯỜNG HỢP 1: Xóa combo
        if (orderDetail.isCombo()) {
            // Tính giá combo (không tính giá sản phẩm con vì giá = 0)
            priceToSubtract += orderDetail.getUnitPrice() * orderDetail.getQuantity();

            // Xóa theo thứ tự từ trong ra ngoài: topping -> sản phẩm con -> combo
            if (orderDetail.getChildren() != null) {
                for (OrderDetail child : orderDetail.getChildren()) {
                    // Xóa topping của sản phẩm con (nếu có)
                    if (child.getChildren() != null) {
                        for (OrderDetail topping : child.getChildren()) {
                            orderDetailRepository.delete(topping);
                        }
                    }
                    // Xóa sản phẩm con
                    orderDetailRepository.delete(child);
                }
            }
            // Cuối cùng xóa combo
            orderDetailRepository.delete(orderDetail);
        }

        // TRƯỜNG HỢP 2: Xóa sản phẩm đơn (có thể có topping)
        else if (orderDetail.getParent() == null) {
            // Tính giá sản phẩm chính
            priceToSubtract += orderDetail.getUnitPrice() * orderDetail.getQuantity();

            // Xóa và tính giá các topping
            if (orderDetail.getChildren() != null && !orderDetail.getChildren().isEmpty()) {
                for (OrderDetail topping : orderDetail.getChildren()) {
                    priceToSubtract += topping.getUnitPrice() * topping.getQuantity();
                    orderDetailRepository.delete(topping);
                }
            }
            // Xóa sản phẩm chính
            orderDetailRepository.delete(orderDetail);
        }

        // TRƯỜNG HỢP 3: Xóa topping hoặc sản phẩm con trong combo
        else {
            // 3.1: Nếu là topping của sản phẩm đơn
            if (orderDetail.getParent() != null && !orderDetail.getParent().isCombo()) {
                // Chỉ cần tính và trừ giá topping
                priceToSubtract = orderDetail.getUnitPrice() * orderDetail.getQuantity();
            }
            // 3.2: Nếu là sản phẩm con trong combo
            else {
                // Xóa topping của sản phẩm con (nếu có)
                if (orderDetail.getChildren() != null && !orderDetail.getChildren().isEmpty()) {
                    for (OrderDetail topping : orderDetail.getChildren()) {
                        // Không cần trừ giá vì sản phẩm con trong combo có giá = 0
                        orderDetailRepository.delete(topping);
                    }
                }
            }
            // Xóa topping hoặc sản phẩm con
            orderDetailRepository.delete(orderDetail);
        }

        // 4. Cập nhật lại tổng giá của order
        order.setTotalPrice(order.getTotalPrice() - priceToSubtract);
        orderRepository.save(order);
    }

    // ----------------------------------------------------------------------------------------- //

    // Cập nhật chi tiết đơn hàng

}
