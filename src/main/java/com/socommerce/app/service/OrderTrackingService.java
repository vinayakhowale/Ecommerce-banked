package com.socommerce.app.service;

import com.socommerce.app.dto.OrderTrackingDto;
import com.socommerce.app.entity.FulfillmentStatus;
import com.socommerce.app.entity.PaymentOrder;
import com.socommerce.app.entity.PaymentStatus;
import com.socommerce.app.exception.BadRequestException;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.PaymentOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Delivery tracking for paid orders. Customers look up an order by its number and the email
 * they bought with; admins move the order through packing, shipping and delivery.
 */
@Service
@RequiredArgsConstructor
public class OrderTrackingService {

    /** Accepts "TRD-000042", "trd42", "#42" or just "42". */
    private static final Pattern ORDER_NUMBER = Pattern.compile("^(?:#|TRD[-\\s]?)?0*(\\d{1,12})$", Pattern.CASE_INSENSITIVE);

    /** Same message for "no such order" and "wrong email", so the lookup can't be used to probe emails. */
    private static final String NOT_FOUND = "We couldn't find an order with that ID and email. Check both and try again.";

    private final PaymentOrderRepository paymentOrderRepository;

    @Transactional(readOnly = true)
    public OrderTrackingDto.Response track(OrderTrackingDto.TrackRequest req) {
        Long id = parseOrderNumber(req.orderNumber());
        PaymentOrder order = paymentOrderRepository.findById(id)
                .filter(o -> o.getStatus() == PaymentStatus.PAID)
                .filter(o -> o.getUser() != null && o.getUser().getEmail().equalsIgnoreCase(req.email().trim()))
                .orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND));
        return OrderTrackingDto.Response.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderTrackingDto.Response> mine(Long userId) {
        return paymentOrderRepository.findTop20ByUserIdAndStatusOrderByCreatedAtDesc(userId, PaymentStatus.PAID)
                .stream().map(OrderTrackingDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<OrderTrackingDto.Response> adminList(FulfillmentStatus status, Pageable pageable) {
        Page<PaymentOrder> page = status == null
                ? paymentOrderRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PAID, pageable)
                : paymentOrderRepository.findByStatusAndFulfillmentStatusOrderByCreatedAtDesc(PaymentStatus.PAID, status, pageable);
        return page.map(OrderTrackingDto.Response::from);
    }

    @Transactional
    public OrderTrackingDto.Response updateFulfillment(Long id, OrderTrackingDto.FulfillmentUpdate req) {
        PaymentOrder order = paymentOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Only paid orders can be shipped");
        }
        String url = trimToNull(req.trackingUrl());
        if (url != null && !(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new BadRequestException("The tracking link must start with https://");
        }

        LocalDateTime now = LocalDateTime.now();
        FulfillmentStatus s = req.status();
        // Stamp each step the first time the order reaches it (or skips past it).
        if (atLeast(s, FulfillmentStatus.PACKED) && order.getPackedAt() == null) order.setPackedAt(now);
        if (atLeast(s, FulfillmentStatus.SHIPPED) && order.getShippedAt() == null) order.setShippedAt(now);
        if (atLeast(s, FulfillmentStatus.OUT_FOR_DELIVERY) && order.getOutForDeliveryAt() == null) order.setOutForDeliveryAt(now);
        if (s == FulfillmentStatus.DELIVERED && order.getDeliveredAt() == null) order.setDeliveredAt(now);

        order.setFulfillmentStatus(s);
        if (req.courierName() != null) order.setCourierName(trimToNull(req.courierName()));
        if (req.trackingNumber() != null) order.setTrackingNumber(trimToNull(req.trackingNumber()));
        if (req.trackingUrl() != null) order.setTrackingUrl(url);
        if (req.expectedDeliveryDate() != null) order.setExpectedDeliveryDate(req.expectedDeliveryDate());
        if (req.note() != null) order.setFulfillmentNote(trimToNull(req.note()));
        return OrderTrackingDto.Response.from(paymentOrderRepository.save(order));
    }

    private static boolean atLeast(FulfillmentStatus s, FulfillmentStatus step) {
        if (s == FulfillmentStatus.CANCELLED || s == FulfillmentStatus.RETURNED) return false;
        return s.ordinal() >= step.ordinal();
    }

    private Long parseOrderNumber(String raw) {
        Matcher m = ORDER_NUMBER.matcher(raw == null ? "" : raw.trim());
        if (!m.matches()) throw new ResourceNotFoundException(NOT_FOUND);
        return Long.valueOf(m.group(1));
    }

    private String trimToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
