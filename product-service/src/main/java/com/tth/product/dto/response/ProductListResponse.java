package com.tth.product.dto.response;

import com.tth.product.dto.response.supplier.SupplierResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {

    private String id;

    private String name;

    private String image;

    private BigDecimal price;

    private String description;

    private SupplierResponse supplier;

}
