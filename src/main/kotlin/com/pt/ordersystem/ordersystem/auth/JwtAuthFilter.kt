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
        val roles = jwtUtil.extractClaim(token, "roles") as? List<String>
        val userId = jwtUtil.extractClaim(token, "userId") as? String

        if (email != null && !roles.isNullOrEmpty() && userId != null) {
          val principal = AuthUser(
            id = userId.toString(),
            email = email.toString(),
            roles = roles
          )

          // Create authentication with AuthUser as the principal
          val auth = UsernamePasswordAuthenticationToken(
            principal,
            null,
            principal.roles.map { SimpleGrantedAuthority("ROLE_$it") }
          )

          SecurityContextHolder.getContext().authentication = auth
        }
      }
    }

    filterChain.doFilter(request, response)
  }
}