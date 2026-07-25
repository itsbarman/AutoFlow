package com.autoflow.vehicle;

import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Fetches a Maskinporten access token that grants access to the Statens Vegvesen
 * Autosys vehicle data API (akfell-datautlevering).
 *
 * <p>Tokens are cached until 30 seconds before expiry to avoid unnecessary
 * round trips to Maskinporten.</p>
 *
 * <p>This bean is only created when {@code autoflow.vegvesen.maskinporten.client-id}
 * is configured, so development environments without credentials still work
 * (the fallback kjoretoyoppslag endpoint is used instead).</p>
 *
 * <h2>Setup for production</h2>
 * <ol>
 *   <li>Register an integration at <a href="https://sjolvbetjening.samarbeid.digdir.no/">
 *       Digdir selvbetjening</a> with scope
 *       {@code vegvesen:kjoretoy/basisopplysninger/hent}.</li>
 *   <li>Generate an RSA key pair, upload the public key to Digdir.</li>
 *   <li>Set environment variables:<br>
 *       {@code VEGVESEN_MASKINPORTEN_CLIENT_ID} — the client ID from Digdir<br>
 *       {@code VEGVESEN_MASKINPORTEN_PRIVATE_KEY_PEM} — the PKCS#8 private key
 *       (full PEM including headers, or just the base64 body)<br>
 *       {@code VEGVESEN_MASKINPORTEN_URL} — defaults to
 *       {@code https://maskinporten.no/token}</li>
 * </ol>
 */
@Service
@ConditionalOnProperty("autoflow.vegvesen.maskinporten.client-id")
public class MaskinportenTokenService {

    private static final Logger log = LoggerFactory.getLogger(MaskinportenTokenService.class);
    private static final String SCOPE = "vegvesen:kjoretoy/basisopplysninger/hent";
    private static final long EXPIRY_BUFFER_SECONDS = 30;

    private final String clientId;
    private final PrivateKey privateKey;
    private final String maskinportenUrl;
    private final RestClient restClient;

    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;

    public MaskinportenTokenService(
            @Value("${autoflow.vegvesen.maskinporten.client-id}") String clientId,
            @Value("${autoflow.vegvesen.maskinporten.private-key-pem}") String privateKeyPem,
            @Value("${autoflow.vegvesen.maskinporten.url:https://maskinporten.no/token}") String maskinportenUrl) {
        this.clientId = clientId;
        this.maskinportenUrl = maskinportenUrl;
        this.privateKey = loadPrivateKey(privateKeyPem);
        this.restClient = RestClient.builder().baseUrl(maskinportenUrl).build();
        log.info("Maskinporten authentication configured for client '{}'", clientId);
    }

    /**
     * Returns a valid access token, fetching a new one from Maskinporten if the
     * cached token is expired or nearly expired.
     */
    public String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }
        return fetchNewToken();
    }

    private synchronized String fetchNewToken() {
        // Double-checked locking: another thread may have refreshed while we waited.
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry)) {
            return cachedToken;
        }

        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .audience().add(maskinportenUrl.replace("/token", "/")).and()
                .issuer(clientId)
                .claim("scope", SCOPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(120)))
                .id(UUID.randomUUID().toString())
                .signWith(privateKey)
                .compact();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        form.add("assertion", jwt);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Maskinporten returned no access_token");
        }

        cachedToken = (String) response.get("access_token");
        int expiresIn = ((Number) response.getOrDefault("expires_in", 120)).intValue();
        tokenExpiry = now.plusSeconds(expiresIn - EXPIRY_BUFFER_SECONDS);
        log.debug("Obtained Maskinporten token, expires in {} s", expiresIn);
        return cachedToken;
    }

    private static PrivateKey loadPrivateKey(String pem) {
        try {
            // Strip PEM header/footer if present.
            String base64 = pem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] keyBytes = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not load Maskinporten private key. "
                    + "Make sure VEGVESEN_MASKINPORTEN_PRIVATE_KEY_PEM is a PKCS#8 PEM key.", e);
        }
    }
}
