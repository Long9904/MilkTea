package com.src.milkTea.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMasterRequest {

    private Long productId;

    // Size của sp khi đặt sp với productType là single
    @Pattern(regexp = "^([SML])?$", message = "Size must be one of S, M, or L")
    private String size;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;

    @Size(max = 100, message = "Note must be less than 100 characters")
    private String note;

    // List của các sp đi kèm cùa order master
    @Valid
    List<OrderDetailRequest> orderDetailRequests;
}
