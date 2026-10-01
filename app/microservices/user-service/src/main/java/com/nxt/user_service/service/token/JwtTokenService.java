package com.nxt.user_service.service.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nxt.user_service.config.JwtConfig;
import com.nxt.user_service.entity.User;
import com.nxt.user_service.exception.KeyResourceAccessException;
import com.nxt.user_service.exception.PrivateKeyLoadException;
import com.nxt.user_service.exception.PublicKeyLoadException;
import com.nxt.user_service.model.TokenPair;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenService {
    private final JwtConfig jwtConfig;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @Autowired
    public JwtTokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void init() {
        String alg = jwtConfig.getAlgorithm() == null ? "HS256" : jwtConfig.getAlgorithm().toUpperCase();

        if ("RS256".equals(alg)) {
            // load RSA keys from PEM files and build Algorithm.RSA256(public, private)
            RSAPublicKey publicKey = loadPublicKeyFromPem(jwtConfig.getRsaPublicKeyPath());
            RSAPrivateKey privateKey = loadPrivateKeyFromPem(jwtConfig.getRsaPrivateKeyPath());
            this.algorithm = Algorithm.RSA256(publicKey, privateKey);
        } else {
            // default to HMAC using secret
            String secret = jwtConfig.getSecret();
            if (secret == null || secret.isBlank()) {
                throw new IllegalArgumentException("jwt.secret must be configured for H256");
            }
            this.algorithm = Algorithm.HMAC256(secret);
        }
        this.verifier = JWT.require(algorithm).build();
    }

    public TokenPair generateTokens(User user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        return new TokenPair(accessToken, refreshToken);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtConfig.getAccessExpirationSeconds());
        String jti = UUID.randomUUID().toString();

        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withIssuer("nxt.user_service")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withJWTId(jti)
                // custom claims
                .withClaim("email", user.getEmail())
                .sign(algorithm);
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(jwtConfig.getRefreshExpirationSeconds());
        String jti = UUID.randomUUID().toString();

        return JWT.create()
                .withSubject(String.valueOf(user.getId()))
                .withIssuer("nxt.user_service")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(exp))
                .withJWTId(jti)
                // custom claims
                .withClaim("email", user.getEmail())
                .sign(algorithm);
    }

    public DecodedJWT decodeJWT(String token) {
        return JWT.decode(token);
    }

    public DecodedJWT validateJWT(String token) {
        return verifier.verify(token);
    }

    private RSAPublicKey loadPublicKeyFromPem(String path) {
        try (Reader r = new InputStreamReader(getResourceAsStream(path), StandardCharsets.UTF_8);
             PemReader pemReader = new PemReader(r)
             ) {
            byte[] content = pemReader.readPemObject().getContent();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(content);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(keySpec);
        }
        catch (IOException e) {
            throw new PublicKeyLoadException("Unable to read public key from path: " + path, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PublicKeyLoadException("RSA Algorithm not supported on this JVM: ", e);
        } catch (InvalidKeySpecException e) {
            throw new PublicKeyLoadException("Invalid public key specification for file: ", e);
        }
    }

    private RSAPrivateKey loadPrivateKeyFromPem(String path) {
        try (Reader r = new InputStreamReader(getResourceAsStream(path), StandardCharsets.UTF_8);
             PemReader pemReader = new PemReader(r)
        ) {
            byte[] content = pemReader.readPemObject().getContent();
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(content);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        }  catch (IOException e) {
            throw new PrivateKeyLoadException("Unable to read public key from path: " + path, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PrivateKeyLoadException("RSA Algorithm not supported on this JVM ", e);
        } catch (InvalidKeySpecException e) {
            throw new PrivateKeyLoadException("Invalid private key format or corrupted PEM file: ", e);
        }
    }

    private InputStream getResourceAsStream(String path) {
        if (path == null) return null;
        if (path.startsWith("classpath:")) {
            String cp = path.substring("classpath:".length());
            return this.getClass().getClassLoader().getResourceAsStream(cp);
        } else {
            try {
                return new FileInputStream(path);
            } catch (Exception e) {
                throw new KeyResourceAccessException("Unable to open key path: " + path, e);
            }
        }
    }
}