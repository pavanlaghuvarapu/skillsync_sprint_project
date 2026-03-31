package com.skillsync.groupservice.controller;

import com.skillsync.groupservice.dto.GroupDtos;
import com.skillsync.groupservice.entity.GroupMessage;
import com.skillsync.groupservice.entity.LearningGroup;
import com.skillsync.groupservice.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Peer learning groups and group discussions")
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "Create a learning group", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LearningGroup> create(Authentication auth, @RequestBody GroupDtos.GroupRequest request) {
        return ResponseEntity.ok(groupService.createGroup(auth.getName(), request));
    }

    @Operation(summary = "Get all groups (public)")
    @GetMapping
    public ResponseEntity<List<LearningGroup>> getAll() {
        return ResponseEntity.ok(groupService.getAll());
    }

    @Operation(summary = "Get group by ID (public)")
    @GetMapping("/{id}")
    public ResponseEntity<LearningGroup> getById(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getById(id));
    }

    @Operation(summary = "Join a group", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LearningGroup> join(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(groupService.joinGroup(id, auth.getName()));
    }

    @Operation(summary = "Leave a group", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LearningGroup> leave(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(groupService.leaveGroup(id, auth.getName()));
    }

    @Operation(summary = "Get my groups", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LearningGroup>> myGroups(Authentication auth) {
        return ResponseEntity.ok(groupService.getMyGroups(auth.getName()));
    }

    @Operation(summary = "Search groups by topic (public)")
    @GetMapping("/search")
    public ResponseEntity<List<LearningGroup>> search(@RequestParam String topic) {
        return ResponseEntity.ok(groupService.searchByTopic(topic));
    }

    @Operation(summary = "Send message in group (members only)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GroupMessage> sendMessage(@PathVariable Long id, Authentication auth,
                                                     @RequestBody GroupDtos.MessageRequest request) {
        return ResponseEntity.ok(groupService.sendMessage(id, auth.getName(), request.getMessage()));
    }

    @Operation(summary = "Get group messages", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GroupMessage>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(groupService.getMessages(id));
    }

    @Operation(summary = "Delete group - ADMIN only", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok().build();
    }
}
