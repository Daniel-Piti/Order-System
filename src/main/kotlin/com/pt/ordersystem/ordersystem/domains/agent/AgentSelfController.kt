package com.pt.ordersystem.ordersystem.domains.agent

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_AGENT
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDto
import com.pt.ordersystem.ordersystem.domains.agent.models.UpdateAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Agent Profile", description = "Endpoints for authenticated agents")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/agents/me")
@PreAuthorize(AUTH_AGENT)
class AgentSelfController(
  private val agentService: AgentService,
) {

  @GetMapping
  fun getCurrentAgent(
    @AuthenticationPrincipal agent: AuthUser,
  ): ResponseEntity<AgentDto> =
    ResponseEntity.ok(agentService.getAgentProfile(agent.id))

  @PutMapping
  fun updateCurrentAgentProfile(
    @AuthenticationPrincipal agent: AuthUser,
    @RequestBody request: UpdateAgentRequest,
  ): ResponseEntity<AgentDto> =
    ResponseEntity.ok(agentService.updateAgent(agent.id, request).toDto())

  @PutMapping("/update-password")
  fun updateCurrentAgentPassword(
    @RequestParam("old_password") oldPassword: String,
    @RequestParam("new_password") newPassword: String,
    @RequestParam("new_password_confirmation") newPasswordConfirmation: String,
    @AuthenticationPrincipal agent: AuthUser
  ): ResponseEntity<String> {
    agentService.updatePassword(agent.id, oldPassword, newPassword, newPasswordConfirmation)
    return ResponseEntity.ok("Password updated successfully")
  }
}
