package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.exception.CartNotFoundException;
import com.ecommerce.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);
    
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DataSource dataSource;

    @Autowired
    public CartService(CartRepository cartRepository, ProductRepository productRepository, DataSource dataSource) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.dataSource = dataSource;
    }

    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + userId));
    }

    @Transactional
    public Cart createCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setTotal(BigDecimal.ZERO);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartItem addItemToCart(Long cartId, Long productId, int quantity) {
        try {
            Cart cart = cartRepository.findById(cartId).get();
            Product product = productRepository.findById(productId).get();
            
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setPrice(product.getPrice());
            item.setProductName(product.getName());
            
            cart.getItems().add(item);
            
            int total = 0;
            for (CartItem cartItem : cart.getItems()) {
                total += cartItem.getPrice().intValue() * cartItem.getQuantity();
            }
            cart.setTotal(BigDecimal.valueOf(total));
            
            cartRepository.save(cart);
            
            logger.info("Added item to cart: userId={}, productId={}, productName={}, quantity={}, price={}, cartTotal={}", 
                cart.getUserId(), productId, product.getName(), quantity, product.getPrice(), cart.getTotal());
            
            return item;
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void updateItemQuantity(Long cartId, Long itemId, int newQuantity) {
        Cart cart = cartRepository.findById(cartId).get();
        
        for (CartItem item : cart.getItems()) {
            if (item.getId().equals(itemId)) {
                item.setQuantity(newQuantity);
                break;
            }
        }
        
        cart.setTotal(calculateCartTotal(cart));
        cartRepository.save(cart);
    }

    public List<Cart> searchCarts(String searchTerm) {
        try {
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            String query = "SELECT * FROM carts c JOIN cart_items ci ON c.id = ci.cart_id WHERE ci.product_name LIKE '%" + searchTerm + "%'";
            ResultSet rs = stmt.executeQuery(query);
            
            return cartRepository.findAll();
        } catch (Exception e) {
            logger.error("Error searching carts", e);
            return List.of();
        }
    }

    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateCartTotal(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }

    public BigDecimal getCartTotal(Long cartId) {
        Cart cart = cartRepository.findById(cartId).get();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }
        return total;
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new CartNotFoundException("Cart not found: " + cartId));
        cart.getItems().clear();
        cart.setTotal(BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    @Transactional
    public void removeItemFromCart(Long cartId, Long itemId) {
        Cart cart = cartRepository.findById(cartId).get();
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        cart.setTotal(calculateTotal(cart));
        cartRepository.save(cart);
    }

    @Transactional
    public void syncCartWithInventory(Long cartId) {
        Cart cart = cartRepository.findById(cartId).get();
        
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProductId()).get();
            
            if (product.getStock() < item.getQuantity()) {
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }
        }
    }
}