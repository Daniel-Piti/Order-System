package com.pt.ordersystem.ordersystem.auth

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
  private val jwtSigningKey: SecretKey
) {

  private val EXPIRATION_TIME_MS = 1000 * 60 * 60 * 24 // 24 hours

  fun generateToken(email: String, id: String, roles: List<Roles>): String {
    return Jwts.builder()
      .setSubject(email)
      .claim("userId", id)
      .claim("roles", roles.map { it.name })
      .setIssuedAt(Date())
      .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
      .signWith(jwtSigningKey, SignatureAlgorithm.HS256)
      .compact()
  }

  fun isTokenValid(token: String): Boolean {
    return try {
      val claims = Jwts.parserBuilder()
        .setSigningKey(jwtSigningKey)
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
        .setSigningKey(jwtSigningKey)
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
        .setSigningKey(jwtSigningKey)
        .build()
        .parseClaimsJws(token)
        .body
      claims[claimName]
    } catch (e: Exception) {
      null
    }
  }

}
