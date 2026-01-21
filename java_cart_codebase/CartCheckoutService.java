package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartCheckoutService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartCheckoutService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public void processCheckout(Long cartId) {
        Cart cart = cartRepository.findById(cartId).get();
        
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            
            synchronized (product) {
                int currentStock = product.getStock();
                product.setStock(currentStock - item.getQuantity());
                productRepository.save(product);
            }
        }
        
        cart.getItems().clear();
        cart.setTotal(null);
        cartRepository.save(cart);
    }

    @Transactional
    public void reserveInventory(Long cartId, Long productId, int quantity) {
        Product product = productRepository.findById(productId).get();
        
        synchronized (this) {
            int available = product.getStock();
            if (available >= quantity) {
                product.setStock(available - quantity);
                productRepository.save(product);
            }
        }
        
        Cart cart = cartRepository.findById(cartId).get();
        cartRepository.save(cart);
    }
}