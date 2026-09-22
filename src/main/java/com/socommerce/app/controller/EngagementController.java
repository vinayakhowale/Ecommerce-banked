package com.socommerce.app.controller;

import com.socommerce.app.security.UserPrincipal;
import com.socommerce.app.service.EngagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class EngagementController {

    private final EngagementService engagementService;

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Boolean>> like(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        boolean liked = engagementService.toggleLike(id, principal.getId());
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<Map<String, Boolean>> save(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        boolean saved = engagementService.toggleSave(id, principal.getId());
        return ResponseEntity.ok(Map.of("saved", saved));
    }
}
