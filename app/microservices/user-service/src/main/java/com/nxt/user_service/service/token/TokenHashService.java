package com.nxt.user_service.service.token;

import com.nxt.user_service.exception.TokenHashingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class TokenHashService {

    private final String secret;

    @Autowired
    public TokenHashService(@Value("${token.hash.secret}") String secret) {
        this.secret = secret;
    }

    public String hashToken(String token) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal(token.getBytes());
            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            throw new TokenHashingException("HmacSHA256 algorithm not found", e);
        } catch (InvalidKeyException e) {
            throw new TokenHashingException("Invalid HMAC secret key configuration", e);
        }
    }
}