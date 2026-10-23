package com.socommerce.app.dto;

import com.socommerce.app.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddressDto {

    public record Request(
            @NotBlank String fullName,
            @NotBlank @Pattern(regexp = "^[0-9+\\-\\s()]{7,20}$", message = "Enter a valid phone number") String phone,
            @NotBlank String line1,
            String line2,
            String landmark,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Enter a valid 6-digit pincode") String pincode,
            String label,
            Boolean defaultAddress
    ) {}

    public record Response(
            Long id, String fullName, String phone, String line1, String line2, String landmark,
            String city, String state, String pincode, String label, boolean defaultAddress
    ) {
        public static Response from(Address a) {
            return new Response(a.getId(), a.getFullName(), a.getPhone(), a.getLine1(), a.getLine2(),
                    a.getLandmark(), a.getCity(), a.getState(), a.getPincode(), a.getLabel(), a.isDefaultAddress());
        }
    }
}
