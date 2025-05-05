package com.novel.vippro.Controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.google.api.Page;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.GroupMember.GroupMemberDTO;
import com.novel.vippro.Payload.Response.ControllerResponse;
import com.novel.vippro.Payload.Response.PageResponse;
import com.novel.vippro.Services.GroupMemberService;
import com.novel.vippro.Services.GroupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Group Management", description = "APIs for managing groups")
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupMemberService groupMemberService;

    @Operation(summary = "Get all groups", description = "Retrieve all groups in the system")
    @GetMapping()
    public ControllerResponse<List<GroupDTO>> getAllGroups(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ControllerResponse.success("Groups retrieved successfully", groupService.getAllGroups(pageable));
    }

    @Operation(summary = "Get group members by group ID", description = "Retrieve all members of a specific group")
    @GetMapping("/{id}/members")
    public ControllerResponse<PageResponse<GroupMemberDTO>> getMembersByGroupId(@PathVariable UUID id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return ControllerResponse.success("Group members retrieved successfully",
                groupMemberService.getAllGroupMembers(pageable));
    }

    @GetMapping("/{id}")
    public ControllerResponse<GroupDTO> getGroupById(@PathVariable UUID id) {
        return ControllerResponse.success("Group retrieved successfully", groupService.getGroupById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ControllerResponse<GroupDTO> createGroup(@RequestBody GroupDTO groupDTO) {
        return ControllerResponse.success("Group created successfully", groupService.createGroup(groupDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<GroupDTO> updateGroup(@PathVariable UUID id, @RequestBody GroupDTO groupDTO) {
        return ControllerResponse.success("Group updated successfully", groupService.updateGroup(id, groupDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ControllerResponse.success("Group deleted successfully", null);
    }
}