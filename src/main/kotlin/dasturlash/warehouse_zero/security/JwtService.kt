package dasturlash.warehouse_zero.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}")private val secret: String,
    @Value("\${jwt.expiration}")private val expiration: Long,
) {
    private fun key() =
        Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret))

    fun generateToken(username: String, role: String): String {
        val claims = mapOf("roles" to role)
        return Jwts.builder()
            .setClaims(claims)
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key(), SignatureAlgorithm.HS512)
            .compact()
    }

    private fun extractAllClaims(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body

    fun extractUsername(token: String): String {
        return extractAllClaims(token).subject
    }

    fun extractRole(token: String): String {
        return extractAllClaims(token).get("role", String::class.java)
    }

    fun isTokenValid(token: String): Boolean {
        return !extractAllClaims(token).expiration.before(Date())
    }
}