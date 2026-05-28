package com.tasktracker.controller;

import com.tasktracker.dto.AssigneeRequest;
import com.tasktracker.dto.StatusRequest;
import com.tasktracker.dto.TaskRequest;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Get all own tasks")
    @GetMapping
    public List<TaskResponse> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return taskService.getAll(userDetails.getUsername());
    }

    @Operation(summary = "Get task by id")
    @GetMapping("/{id}")
    public TaskResponse getOne(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.getOne(id, userDetails.getUsername());
    }

    @Operation(summary = "Create task")
    @PostMapping
    public TaskResponse create(@Valid @RequestBody TaskRequest req,
                               @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.create(req, userDetails.getUsername());
    }

    @Operation(summary = "Update task title and description")
    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id,
                               @Valid @RequestBody TaskRequest req,
                               @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.update(id, req, userDetails.getUsername());
    }

    @Operation(summary = "Change task status")
    @PatchMapping("/{id}/status")
    public TaskResponse changeStatus(@PathVariable Long id,
                                     @RequestBody StatusRequest req,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.changeStatus(id, req, userDetails.getUsername());
    }

    @Operation(summary = "Change task assignee")
    @PatchMapping("/{id}/assignee")
    public TaskResponse changeAssignee(@PathVariable Long id,
                                       @RequestBody AssigneeRequest req,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        return taskService.changeAssignee(id, req, userDetails.getUsername());
    }

    @Operation(summary = "Delete task")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        taskService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
