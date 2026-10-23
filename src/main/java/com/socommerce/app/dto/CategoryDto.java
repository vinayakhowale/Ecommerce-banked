package com.socommerce.app.dto;

/** One entry in the storefront's Shop menu: a category, how many products it has, and a cover image. */
public record CategoryDto(String name, long productCount, String imageUrl) {}
