package com.src.milkTea.dto.request;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    // List của các sp chính của order
    @Valid
    private List<OrderMasterRequest> orderMasterRequests;
}
