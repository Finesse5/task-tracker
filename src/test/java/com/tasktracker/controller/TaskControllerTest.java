package com.tasktracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.config.SecurityConfig;
import com.tasktracker.dto.TaskRequest;
import com.tasktracker.dto.TaskResponse;
import com.tasktracker.entity.TaskStatus;
import com.tasktracker.exception.GlobalExceptionHandler;
import com.tasktracker.exception.TaskNotFoundException;
import com.tasktracker.security.JwtFilter;
import com.tasktracker.security.JwtService;
import com.tasktracker.security.UserDetailsServiceImpl;
import com.tasktracker.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, JwtFilter.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TaskService taskService;
    @MockBean JwtService jwtService;
    @MockBean UserDetailsServiceImpl userDetailsService;

    private final TaskResponse sampleTask = new TaskResponse(
            1L, "Sample", "desc", TaskStatus.WAITING, Instant.now(), null, 1L, null);

    @Test
    void createTask_noToken_returns403() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("title", "desc"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void createTask_authorized_returns200() throws Exception {
        when(taskService.create(any(), eq("user@example.com"))).thenReturn(sampleTask);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TaskRequest("Sample", "desc"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Sample"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getAll_returns_ownTasks() throws Exception {
        when(taskService.getAll("user@example.com")).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteTask_success_returns204() throws Exception {
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "other@example.com")
    void getTask_notOwned_returns404() throws Exception {
        when(taskService.getOne(eq(1L), eq("other@example.com")))
                .thenThrow(new TaskNotFoundException(1L));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isNotFound());
    }
}
