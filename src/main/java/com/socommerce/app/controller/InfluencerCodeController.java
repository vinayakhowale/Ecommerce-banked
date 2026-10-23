package com.socommerce.app.controller;

import com.socommerce.app.entity.Influencer;
import com.socommerce.app.service.InfluencerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public, read-only lookup so the storefront can show "Shopping via {name}'s code" when a
 * customer arrives via an influencer's shared link (?ref=CODE). Deliberately returns only the
 * influencer's public display name -- never internal IDs or sales data -- and 404s for anything
 * invalid/inactive rather than distinguishing "doesn't exist" from "deactivated".
 */
@RestController
@RequestMapping("/api/public/influencer-code")
@RequiredArgsConstructor
public class InfluencerCodeController {

    private final InfluencerService influencerService;

    @GetMapping("/{code}")
    public ResponseEntity<Map<String, String>> lookup(@PathVariable String code) {
        return influencerService.findActiveByCode(code)
                .map(Influencer::getName)
                .map(name -> ResponseEntity.ok(Map.of("name", name)))
                .orElse(ResponseEntity.notFound().build());
    }
}
