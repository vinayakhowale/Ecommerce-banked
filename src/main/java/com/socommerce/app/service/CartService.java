package com.socommerce.app.service;

import com.socommerce.app.dto.CartDto;
import com.socommerce.app.entity.AddToCartEvent;
import com.socommerce.app.entity.CartItem;
import com.socommerce.app.entity.Product;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.AddToCartEventRepository;
import com.socommerce.app.repository.CartItemRepository;
import com.socommerce.app.repository.ProductRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddToCartEventRepository addToCartEventRepository;
    private final DiscountService discountService;

    @Transactional(readOnly = true)
    public CartDto.CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return toResponse(items);
    }

    @Transactional
    public CartDto.CartResponse addItem(Long userId, CartDto.AddRequest req) {
        // Never trust a price from the frontend: always look the product up server-side.
        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (!product.isActive()) {
            throw new BadRequestException("This product is no longer available");
        }
        User userRef = userRepository.getReferenceById(userId);

        int qtyToAdd = (req.quantity() == null || req.quantity() < 1) ? 1 : req.quantity();

        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, req.productId())
                .orElseGet(() -> CartItem.builder().user(userRef).product(product).quantity(0).build());
        item.setQuantity(item.getQuantity() + qtyToAdd);
        cartItemRepository.save(item);

        addToCartEventRepository.save(AddToCartEvent.builder().product(product).user(userRef).build());

        return getCart(userId);
    }

    @Transactional
    public CartDto.CartResponse updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have access to this cart item");
        }
        if (quantity < 1) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return getCart(userId);
    }

    @Transactional
    public CartDto.CartResponse removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!item.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have access to this cart item");
        }
        cartItemRepository.delete(item);
        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartDto.CartResponse toResponse(List<CartItem> items) {
        List<CartDto.ItemResponse> itemResponses = items.stream()
                .map(ci -> CartDto.ItemResponse.from(ci, discountService.computeEffectivePrice(ci.getProduct())))
                .toList();
        BigDecimal total = itemResponses.stream()
                .filter(CartDto.ItemResponse::productActive)
                .map(CartDto.ItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartDto.CartResponse(itemResponses, total, itemResponses.size());
    }
}
