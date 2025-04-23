package com.src.milkTea.service;

import com.src.milkTea.repository.OrderDetailRepository;
import com.src.milkTea.repository.OrderMasterRepository;
import com.src.milkTea.repository.OrdersRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.utils.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private OrderMasterRepository orderMasterRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserUtils userUtils;

    public void createOrder() {
    /*
    1. Lấy thông tin user từ token JWT
       - Sử dụng SecurityContextHolder để lấy email → truy DB tìm User entity.
       - Kiểm tra trạng thái ACTIVE.

    2. Set status cho đơn hàng là "PENDING"
       - Tạo đối tượng Orders mới.
       - Gán status PENDING, user, createAt, v.v.

    3. Lưu đơn hàng vào DB để lấy ID
       - orderRepository.save(order)
       - Sau bước này ta có thể gán order_id cho OrderMaster.

    4. Duyệt từng orderMaster trong orderRequest.getOrderMasterRequests()

        4.1 Kiểm tra sản phẩm CHÍNH có tồn tại trong DB không
            - productRepository.findById(productId)
            - Kiểm tra: productType phải là SINGLE hoặc COMBO

        4.2 Tạo đối tượng OrderMaster mới

        4.3 Nếu là single thì gán size (S/M/L)

        4.4 Tính giá sản phẩm chính:
            - price = basePrice * quantity

        4.5 Cộng giá sản phẩm chính vào totalPrice của Order:
            - Nếu size = S → giảm 5k
            - Nếu size = M → giữ nguyên
            - Nếu size = L → tăng 5k
            (Giá size có thể lấy từ cấu hình hoặc enum, hoặc hard-code)

        4.6 Lưu OrderMaster vào DB

    5. Xử lý orderDetailList (danh sách sp phụ của từng master)

        5.1 Với mỗi OrderDetailRequest:
            - Kiểm tra productId có tồn tại không
            - Kiểm tra productUsage = MAIN hoặc EXTRA

        5.2 Tạo OrderDetail mới

        5.3 Nếu sản phẩm là trà trong combo → gán size (nếu có)

        5.4 Tính giá sp phụ: basePrice * quantity

            5.4.1 Nếu là topping → không thay đổi gia
            5.4.2 Nếu là trà trong combo:
                - Nếu size = S → giảm 5k
                - Nếu size = M → giữ nguyên
                - Nếu size = L → tăng 5k

        5.5 Cộng vào tổng giá đơn hàng

        5.6 Gán orderMasterId cho OrderDetail → lưu vào DB

    6. Sau khi duyệt xong:
        - Gán lại order.setTotalPrice(...)
        - orderRepository.save(order)
    */
    }

}
