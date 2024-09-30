package com.tth.product.controller;

import com.tth.commonlibrary.dto.APIResponse;
import com.tth.commonlibrary.dto.response.product.ProductDetailsResponse;
import com.tth.commonlibrary.dto.response.product.ProductListResponse;
import com.tth.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CrossOrigin
@Transactional
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products", produces = "application/json; charset=UTF-8")
public class APIProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> listProducts(@RequestParam(required = false, defaultValue = "") Map<String, String> params,
                                          @RequestParam(required = false, defaultValue = "1") int page,
                                          @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(this.productService.findAllWithFilter(params, page, size));
    }

    @PostMapping("/batch")
    public ResponseEntity<?> getProductsInBatch(@RequestBody Set<String> productIds) {
        List<ProductListResponse> products = this.productService.findAllInBatch(productIds);

        return ResponseEntity.ok(APIResponse.<List<ProductListResponse>>builder().result(products).build());
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable String productId) {
        ProductDetailsResponse product = this.productService.findById(productId);

        return ResponseEntity.ok(APIResponse.<ProductDetailsResponse>builder().result(product).build());
    }

}