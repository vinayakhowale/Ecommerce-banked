package com.socommerce.app.entity;

/**
 * Moderation state of a post a creator uploaded themselves. Posts the admin creates directly
 * have no review status (null) and never need approval.
 */
public enum PostReviewStatus {
    PENDING,
    APPROVED,
    REJECTED
}
