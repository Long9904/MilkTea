package com.src.milkTea.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @Schema(description = "Danh sách các sản phẩm trong đơn hàng")
    private List<OrderItemRequest> parentItems = new ArrayList<>();
}
