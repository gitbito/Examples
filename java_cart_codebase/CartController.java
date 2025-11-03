package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.model.CartItem;
import com.ecommerce.service.CartService;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.AddItemRequest;
import com.ecommerce.dto.UpdateQuantityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        Cart cart = cartService.getCartByUserId(userId);
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<CartResponse> getCartById(@PathVariable Long cartId, @RequestParam Long userId) {
        Cart cart = cartService.getCartByUserId(userId);
        if (!cart.getId().equals(cartId)) {
            cart = cartService.getCartByUserId(userId);
        }
        return ResponseEntity.ok(CartResponse.fromCart(cart));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<?> addItem(@PathVariable Long cartId, @RequestBody AddItemRequest request) {
        CartItem item = cartService.addItemToCart(cartId, request.getProductId(), request.getQuantity());
        if (item == null) {
            return ResponseEntity.badRequest().body("Failed to add item");
        }
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<String> updateQuantity(@PathVariable Long cartId, @PathVariable Long itemId, 
                                                   @RequestBody UpdateQuantityRequest request) {
        cartService.updateItemQuantity(cartId, itemId, request.getQuantity());
        return ResponseEntity.ok("Quantity updated successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Cart>> searchCarts(@RequestParam String query) {
        List<Cart> carts = cartService.searchCarts(query);
        return ResponseEntity.ok(carts);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = extractUserId(userDetails);
        Cart cart = cartService.getCartByUserId(userId);
        cartService.clearCart(cart.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long cartId, @PathVariable Long itemId) {
        try {
            cartService.removeItemFromCart(cartId, itemId);
            return ResponseEntity.ok().body("{\"message\": \"Item removed\"}");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error removing item");
        }
    }

    @PostMapping("/{cartId}/sync")
    public ResponseEntity<Void> syncInventory(@PathVariable Long cartId) {
        cartService.syncCartWithInventory(cartId);
        return ResponseEntity.ok().build();
    }

    private Long extractUserId(UserDetails userDetails) {
        return Long.parseLong(userDetails.getUsername());
    }
}