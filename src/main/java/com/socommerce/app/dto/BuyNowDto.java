package com.socommerce.app.dto;

import jakarta.validation.constraints.NotNull;

public class BuyNowDto {
    public record Request(@NotNull Long productId, Long postId, String influencerCode) {}
    public record Response(String redirectUrl, Long clickId) {}
}
