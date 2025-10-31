package com.pt.ordersystem.ordersystem.auth

import com.pt.ordersystem.ordersystem.config.SecretsProvider
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(
  private val secrets: SecretsProvider,
) {

  private val EXPIRATION_TIME_MS = 1000 * 60 * 60 * 24 // 24 hours

  fun generateToken(email: String, id: String, roles: List<Roles>): String {
    return Jwts.builder()
      .setSubject(email)
      .claim("userId", id)
      .claim("roles", roles.map { it.name })
      .setIssuedAt(Date())
      .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
      .signWith(secrets.jwtSigningKey, SignatureAlgorithm.HS256)
      .compact()
  }

  fun isTokenValid(token: String): Boolean {
    return try {
      val claims = Jwts.parserBuilder()
        .setSigningKey(secrets.jwtSigningKey)
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
        .setSigningKey(secrets.jwtSigningKey)
        .build()
        .parseClaimsJws(token)
        .body
        .subject
    } catch (e: Exception) {
      null
    }
  }

  fun extractClaim(token: String, claimName: String): Any? {
    return try {
      val claims = Jwts.parserBuilder()
        .setSigningKey(secrets.jwtSigningKey)
        .build()
        .parseClaimsJws(token)
        .body
      claims[claimName]
    } catch (e: Exception) {
      null
    }
  }

}
