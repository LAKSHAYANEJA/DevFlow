package com.devflow.dto;

public class LabelResponse {
    public record Summary(
        Long id,
        Long projectId,
        String name,
        String color
    ){}
}
