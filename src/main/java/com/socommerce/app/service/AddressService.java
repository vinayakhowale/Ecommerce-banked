package com.socommerce.app.service;

import com.socommerce.app.dto.AddressDto;
import com.socommerce.app.entity.Address;
import com.socommerce.app.entity.User;
import com.socommerce.app.exception.ResourceNotFoundException;
import com.socommerce.app.repository.AddressRepository;
import com.socommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Address> list(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Address getOwned(Long id, Long userId) {
        return addressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    @Transactional
    public Address create(AddressDto.Request req, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // The very first address a customer saves becomes their default automatically.
        boolean makeDefault = Boolean.TRUE.equals(req.defaultAddress())
                || addressRepository.countByUserId(userId) == 0;
        if (makeDefault) addressRepository.clearDefaultFor(userId);

        Address address = Address.builder()
                .user(user)
                .fullName(req.fullName().trim())
                .phone(req.phone().trim())
                .line1(req.line1().trim())
                .line2(trimToNull(req.line2()))
                .landmark(trimToNull(req.landmark()))
                .city(req.city().trim())
                .state(req.state().trim())
                .pincode(req.pincode().trim())
                .label(trimToNull(req.label()))
                .defaultAddress(makeDefault)
                .build();
        return addressRepository.save(address);
    }

    @Transactional
    public Address update(Long id, AddressDto.Request req, Long userId) {
        Address address = getOwned(id, userId);
        if (Boolean.TRUE.equals(req.defaultAddress()) && !address.isDefaultAddress()) {
            addressRepository.clearDefaultFor(userId);
            address.setDefaultAddress(true);
        }
        address.setFullName(req.fullName().trim());
        address.setPhone(req.phone().trim());
        address.setLine1(req.line1().trim());
        address.setLine2(trimToNull(req.line2()));
        address.setLandmark(trimToNull(req.landmark()));
        address.setCity(req.city().trim());
        address.setState(req.state().trim());
        address.setPincode(req.pincode().trim());
        address.setLabel(trimToNull(req.label()));
        return addressRepository.save(address);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        Address address = getOwned(id, userId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);

        // Never leave a customer without a default -- promote the next one.
        if (wasDefault) {
            addressRepository.findByUserIdOrderByDefaultAddressDescUpdatedAtDesc(userId).stream()
                    .findFirst()
                    .ifPresent(next -> {
                        next.setDefaultAddress(true);
                        addressRepository.save(next);
                    });
        }
    }

    @Transactional
    public Address makeDefault(Long id, Long userId) {
        Address address = getOwned(id, userId);
        addressRepository.clearDefaultFor(userId);
        address.setDefaultAddress(true);
        return addressRepository.save(address);
    }

    private String trimToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
