package com.src.milkTea.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ComboItemResponse {

    private List<Item> itemsResponse;

    @Data
    public static class Item {
        private Long productId;
        private String productName;
        private String size;
        private int quantity;
    }
}
