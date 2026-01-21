package com.ecommerce.service;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CachedCartService {

    private final ProductRepository productRepository;

    public CachedCartService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Cacheable(value = "productPrices", key = "#productId")
    public BigDecimal getProductPrice(Long productId) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getPrice() : BigDecimal.ZERO;
    }

    public BigDecimal calculateBulkCartTotal(List<Long> productIds) {
        BigDecimal total = BigDecimal.ZERO;
        for (Long productId : productIds) {
            total = total.add(getProductPrice(productId));
        }
        return total;
    }
}