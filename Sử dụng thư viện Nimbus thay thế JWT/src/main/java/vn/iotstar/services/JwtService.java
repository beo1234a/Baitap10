package vn.iotstar.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    /**
     * Lấy username/email từ JWT
     */
    public String extractUsername(String token) {
        return extractClaim(token, JWTClaimsSet::getSubject);
    }

    /**
     * Lấy một claim bất kỳ
     */
    public <T> T extractClaim(
            String token,
            Function<JWTClaimsSet, T> claimsResolver) {

        final JWTClaimsSet claims = extractAllClaims(token);

        return claimsResolver.apply(claims);
    }

    /**
     * Tạo JWT token từ UserDetails
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Tạo JWT token có thêm extra claims
     */
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {

        return buildToken(
                extraClaims,
                userDetails,
                jwtExpiration
        );
    }

    /**
     * Lấy thời gian hết hạn
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }

    /**
     * Tạo JWT bằng Nimbus
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {

        try {

            Date now = new Date();

            Date expiry = new Date(
                    now.getTime() + expiration
            );

            JWTClaimsSet.Builder claimsBuilder =
                    new JWTClaimsSet.Builder()
                            .subject(userDetails.getUsername())
                            .issueTime(now)
                            .expirationTime(expiry);

            /*
             * Thêm các claims bổ sung
             */
            if (extraClaims != null && !extraClaims.isEmpty()) {

                for (Map.Entry<String, Object> entry
                        : extraClaims.entrySet()) {

                    claimsBuilder.claim(
                            entry.getKey(),
                            entry.getValue()
                    );
                }
            }

            /*
             * Tạo JWT sử dụng thuật toán HS256
             */
            SignedJWT signedJWT =
                    new SignedJWT(
                            new JWSHeader(JWSAlgorithm.HS256),
                            claimsBuilder.build()
                    );

            /*
             * Ký JWT bằng secret key
             */
            signedJWT.sign(
                    new MACSigner(getSignInKey())
            );

            /*
             * Trả JWT dạng String
             */
            return signedJWT.serialize();

        } catch (JOSEException exception) {

            throw new IllegalStateException(
                    "Không thể tạo JWT token",
                    exception
            );
        }
    }

    /**
     * Kiểm tra JWT có hợp lệ hay không
     */
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        try {

            final String username =
                    extractUsername(token);

            final Date expiration =
                    extractExpiration(token);

            return username != null
                    && username.equals(
                            userDetails.getUsername()
                    )
                    && expiration != null
                    && expiration.after(new Date())
                    && verifySignature(token);

        } catch (RuntimeException exception) {

            return false;
        }
    }

    /**
     * Lấy thời gian hết hạn từ JWT
     */
    private Date extractExpiration(String token) {

        return extractClaim(
                token,
                JWTClaimsSet::getExpirationTime
        );
    }

    /**
     * Đọc toàn bộ claims từ JWT
     */
    private JWTClaimsSet extractAllClaims(String token) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            /*
             * Kiểm tra chữ ký trước khi lấy claims
             */
            if (!verifySignature(signedJWT)) {

                throw new IllegalArgumentException(
                        "JWT signature không hợp lệ"
                );
            }

            return signedJWT.getJWTClaimsSet();

        } catch (
                ParseException |
                JOSEException exception) {

            throw new IllegalArgumentException(
                    "JWT không hợp lệ",
                    exception
            );
        }
    }

    /**
     * Kiểm tra chữ ký JWT từ String
     */
    private boolean verifySignature(String token) {

        try {

            SignedJWT signedJWT =
                    SignedJWT.parse(token);

            return verifySignature(signedJWT);

        } catch (
                ParseException |
                JOSEException exception) {

            return false;
        }
    }

    /**
     * Kiểm tra chữ ký JWT bằng Nimbus
     */
    private boolean verifySignature(
            SignedJWT signedJWT)
            throws JOSEException {

        JWSVerifier verifier =
                new MACVerifier(getSignInKey());

        return signedJWT.verify(verifier);
    }

    /**
     * Chuyển secret key thành byte[]
     */
    private byte[] getSignInKey() {

        return secretKey.getBytes(
                StandardCharsets.UTF_8
        );
    }
}