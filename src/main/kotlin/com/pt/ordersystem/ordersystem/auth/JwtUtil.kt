package com.pt.ordersystem.ordersystem.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil {

  private val SECRET_KEY: SecretKey = Keys.hmacShaKeyFor("your-very-secret-and-long-key-123123123123123".toByteArray())
  private val EXPIRATION_TIME_MS = 1000 * 60 * 60 * 24 // 24 hours

  fun generateToken(email: String, id: String, role: Roles): String {
    return Jwts.builder()
      .setSubject(email)
      .claim("userId", id)
      .claim("role", role.name)
      .setIssuedAt(Date())
      .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
      .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
      .compact()
  }

  fun isTokenValid(token: String): Boolean {
    return try {
      val claims = Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
      !claims.body.expiration.before(Date())
    } catch (e: Exception) {
      false
    }
  }

  fun extractEmail(token: String): String? {
    return try {
      Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .body
        .subject
    } catch (e: Exception) {
      null
    }
  }

  fun extractClaim(token: String, claimName: String): String? {
    return try {
      val claim = Jwts.parserBuilder()
        .setSigningKey(SECRET_KEY)
        .build()
        .parseClaimsJws(token)
        .body
        .get(claimName, String::class.java)
      claim
    } catch (e: Exception) {
      null
    }
  }
}
