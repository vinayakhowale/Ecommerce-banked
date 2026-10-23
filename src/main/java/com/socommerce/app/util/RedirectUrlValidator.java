package com.socommerce.app.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validates outbound "Buy Now" redirect URLs before they are ever returned to the frontend.
 *
 * The frontend is NEVER allowed to supply an arbitrary redirect target — every redirect
 * originates from a Product record already stored (and admin-approved) in the database.
 * This validator is a defense-in-depth layer that re-checks the stored URL at click time,
 * so that even a compromised/malformed DB value can't be used to attack users.
 */
@Component
public class RedirectUrlValidator {

    @Value("#{'${app.redirect.blocked-hosts:localhost,127.0.0.1,0.0.0.0,169.254.169.254}'.split(',')}")
    private List<String> blockedHosts;

    private static final Pattern DANGEROUS_SCHEME = Pattern.compile(
            "^(javascript|data|vbscript|file|blob|about):", Pattern.CASE_INSENSITIVE);

    public boolean isSafe(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return false;
        }
        String trimmed = rawUrl.trim();

        // Reject control characters, whitespace-smuggling, and obviously dangerous schemes.
        if (trimmed.chars().anyMatch(Character::isISOControl)) {
            return false;
        }
        if (DANGEROUS_SCHEME.matcher(trimmed).find()) {
            return false;
        }

        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            return false;
        }

        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return false;
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return false;
        }

        String normalizedHost;
        try {
            normalizedHost = IDN.toASCII(host).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // Block loopback / metadata / internal hosts to prevent SSRF-style abuse via the redirect flow.
        if (blockedHosts.stream().anyMatch(h -> normalizedHost.equals(h.trim().toLowerCase(Locale.ROOT)))) {
            return false;
        }
        if (normalizedHost.startsWith("10.") || normalizedHost.startsWith("192.168.")
                || normalizedHost.startsWith("172.16.") || normalizedHost.endsWith(".local")) {
            return false;
        }

        // Must have a proper public-looking hostname (contains a dot, e.g. example.com) unless it's an IP literal
        // we've already screened above. This rejects bare hostnames used for internal network pivoting.
        return normalizedHost.contains(".");
    }

    /**
     * Returns the URL to use, preferring the affiliate URL when present and safe,
     * otherwise falling back to the canonical product URL. Returns null if neither is safe.
     */
    public String resolveSafeRedirect(String affiliateUrl, String productUrl) {
        if (affiliateUrl != null && !affiliateUrl.isBlank() && isSafe(affiliateUrl)) {
            return affiliateUrl;
        }
        if (isSafe(productUrl)) {
            return productUrl;
        }
        return null;
    }
}
