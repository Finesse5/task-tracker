package com.tasktracker.dto;

import com.tasktracker.entity.TaskStatus;

import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Instant createdAt,
        Instant doneAt,
        Long ownerId,
        Long assigneeId
) {}
