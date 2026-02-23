package com.pt.ordersystem.ordersystem.domains.agent.controllers

import com.pt.ordersystem.ordersystem.auth.AuthRole.AUTH_MANAGER
import com.pt.ordersystem.ordersystem.auth.AuthUser
import com.pt.ordersystem.ordersystem.domains.agent.AgentService
import com.pt.ordersystem.ordersystem.domains.agent.helpers.AgentValidators
import com.pt.ordersystem.ordersystem.domains.agent.models.AgentDto
import com.pt.ordersystem.ordersystem.domains.agent.models.NewAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.UpdateAgentRequest
import com.pt.ordersystem.ordersystem.domains.agent.models.toDto
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Agents", description = "Manager agent management API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/agents")
@PreAuthorize(AUTH_MANAGER)
class AgentManagerController(
  private val agentService: AgentService,
) {

  @GetMapping
  fun getAgentsForManager(
    @AuthenticationPrincipal manager: AuthUser,
  ): ResponseEntity<List<AgentDto>> =
    ResponseEntity.ok(agentService.getAgentsForManager(manager.id))

  @PostMapping
  fun createAgent(
    @AuthenticationPrincipal manager: AuthUser,
    @RequestBody request: NewAgentRequest,
  ): ResponseEntity<AgentDto> {
    val normalizedRequest = request.normalize()
    agentService.validateCreateAgent(normalizedRequest, manager.id)
    val agentDto = agentService.createAgent(manager.id, normalizedRequest).toDto()
    return ResponseEntity.status(HttpStatus.CREATED).body(agentDto)
  }

  @PutMapping("/{agentId}")
  fun updateAgentForManager(
    @AuthenticationPrincipal manager: AuthUser,
    @PathVariable agentId: String,
    @RequestBody request: UpdateAgentRequest,
  ): ResponseEntity<AgentDto> {
    agentService.validateAgentOfManager(agentId, manager.id)
    val normalizedRequest = request.normalize()
    AgentValidators.validateUpdateAgentRequest(normalizedRequest)
    return ResponseEntity.ok(agentService.updateAgent(agentId, normalizedRequest).toDto())
  }

  @DeleteMapping("/{agentId}")
  fun deleteAgent(
    @AuthenticationPrincipal manager: AuthUser,
    @PathVariable agentId: String,
  ): ResponseEntity<String> {
    agentService.deleteAgent(manager.id, agentId)
    return ResponseEntity.ok("Agent deleted successfully")
  }
}

