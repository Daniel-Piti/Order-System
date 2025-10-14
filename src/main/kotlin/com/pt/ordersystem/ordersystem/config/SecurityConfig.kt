package com.pt.ordersystem.ordersystem.config

import com.pt.ordersystem.ordersystem.auth.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
  private val jwtAuthFilter: JwtAuthFilter,
  private val corsConfigurationSource: CorsConfigurationSource,
) {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() }
      .cors { it.configurationSource(corsConfigurationSource) }
      .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
      .authorizeHttpRequests {
        it.requestMatchers(
          "/api/auth/**",
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/swagger-ui.html",
          "/swagger-resources/**",
          "/webjars/**",
          "/api/products/user/**",
          "/api/products/product/**",
          "/api/products/order/**",
          "/api/orders/*"
        ).permitAll()
        it.anyRequest().authenticated()
      }
      .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
      .build()
  }
}