package com.devflow.dto;

import java.util.Map;
import java.util.List;

public class WorkloadResponse {
    
    public record MemberWorkload(
        Long userId,
        String name,
        Long taskCount
    ) {}

    public record ProjectStats(
        Long projectId,
        long totalTasks,
        long unassignedTasks,
        Map<String, Long> tasksByStatus,
        List<MemberWorkload> workloadByMember
    ) {}
}
