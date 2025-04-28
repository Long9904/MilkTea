package com.src.milkTea.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequest {

    @Schema(description = "ID của sản phẩm", example = "101")
    private Long productId;

    @Min(value = 1, message = "Số lượng sản phẩm phải lớn hơn hoặc bằng 1")
    @Schema(description = "Số lượng sản phẩm", example = "2")
    private int quantity;

    @Schema(description = "Kích thước của sản phẩm", example = "L")
    @Pattern(regexp = "^(S|M|L|XL|NONE)$", message = "Size must be one of S, M, L, XL, NONE")
    private String size;

    private String note;

    @Schema(description = "Xác định xem sản phẩm có phải là combo không", example = "true")
    @JsonProperty("isCombo")
    private boolean isCombo;

    @Schema(description = "Danh sách các sản phẩm con (nếu có)",
            example = "[\n" +
                    "  {\n" +
                    "    \"productId\": 202,\n" +
                    "    \"quantity\": 2,\n" +
                    "    \"size\": \"M\",\n" +
                    "    \"note\": \"nhiều đá\",\n" +
                    "    \"isCombo\": false,\n" +
                    "    \"childItems\": []\n" +
                    "  }\n" +
                    "]")
    private List<OrderItemRequest> childItems = new ArrayList<>();

}
