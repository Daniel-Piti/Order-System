package com.pt.ordersystem.ordersystem.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
  private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {

  override fun doFilterInternal(
    request: HttpServletRequest,
    response: HttpServletResponse,
    filterChain: FilterChain
  ) {
    val authHeader = request.getHeader("Authorization")

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      val token = authHeader.substring(7)

      if (jwtUtil.isTokenValid(token)) {
        val email = jwtUtil.extractEmail(token)
        val role = jwtUtil.extractClaim(token, "role")
        val userId = jwtUtil.extractClaim(token, "userId")
        if (email != null && role != null) {
          val auth = UsernamePasswordAuthenticationToken(
            email,
            null,
            listOf(SimpleGrantedAuthority(role))
          )
          auth.details = mapOf(
            "userId" to userId,
            "email" to email,
            "role" to role
          )
          SecurityContextHolder.getContext().authentication = auth
        }
      }
    }

    filterChain.doFilter(request, response)
  }
}