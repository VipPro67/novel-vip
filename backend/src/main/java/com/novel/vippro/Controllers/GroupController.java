package com.novel.vippro.Controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.novel.vippro.DTO.Group.CreateGroupDTO;
import com.novel.vippro.DTO.Group.GroupDTO;
import com.novel.vippro.DTO.Group.UpdateGroupDTO;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ControllerResponse.success("Groups retrieved successfully", groupService.getAllGroups(pageable));
    }

    @Operation(summary = "Get my group", description = "Retrieve groups the authenticated user is a member of")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<List<GroupDTO>> getMyGroups() {
        List<GroupDTO> myGroup = groupService.getMyGroups();
        return ControllerResponse.success("User's groups retrieved successfully", myGroup);
    }

    @Operation(summary = "Get group members by group ID", description = "Retrieve all members of a specific group")
    @GetMapping("/{id}/members")
    public ControllerResponse<PageResponse<GroupMemberDTO>> getMembersByGroupId(@PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ControllerResponse.success("Group members retrieved successfully",
                groupMemberService.getAllGroupMembers(pageable));
    }

    @Operation(summary = "Add a member to a group", description = "Add a new member to a specific group")
    @PostMapping("/{id}/members")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<GroupMemberDTO> addMemberToGroup(@PathVariable UUID id,
            @RequestBody GroupMemberDTO groupMemberDTO) {
        return ControllerResponse.success("Group member added successfully",
                groupMemberService.addGroupMember(id, groupMemberDTO));
    }

    // remove a member from a group
    @Operation(summary = "Remove a member from a group", description = "Remove a member from a specific group")
    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<Void> removeMemberFromGroup(@PathVariable UUID id, @PathVariable UUID memberId) {
        groupMemberService.removeGroupMember(id, memberId);
        return ControllerResponse.success("Group member removed successfully", null);
    }

    // leave a group
    @Operation(summary = "Leave a group", description = "Leave a specific group")
    @DeleteMapping("/{id}/leave")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<Void> leaveGroup(@PathVariable UUID id) {
        groupMemberService.leaveGroup(id);
        return ControllerResponse.success("Left group successfully", null);
    }

    @GetMapping("/{id}")
    public ControllerResponse<GroupDTO> getGroupById(@PathVariable UUID id) {
        return ControllerResponse.success("Group retrieved successfully", groupService.getGroupById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ControllerResponse<GroupDTO> createGroup(@RequestBody CreateGroupDTO groupDTO) {
        return ControllerResponse.success("Group created successfully", groupService.createGroup(groupDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<GroupDTO> updateGroup(@PathVariable UUID id, @RequestBody UpdateGroupDTO groupDTO) {
        return ControllerResponse.success("Group updated successfully", groupService.updateGroup(id, groupDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ControllerResponse<Void> deleteGroup(@PathVariable UUID id) {
        groupService.deleteGroup(id);
        return ControllerResponse.success("Group deleted successfully", null);
    }
}