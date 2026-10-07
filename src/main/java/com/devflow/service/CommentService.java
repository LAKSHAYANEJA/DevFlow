package com.devflow.service;

import com.devflow.dto.CommentRequest;
import com.devflow.dto.CommentResponse;
import com.devflow.entity.Comment;
import com.devflow.entity.Task;
import com.devflow.entity.User;
import com.devflow.repository.CommentRepository;
import com.devflow.repository.ProjectMemberRepository;
import com.devflow.repository.TaskRepository;
import com.devflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Task getTaskAndVerifyAccess(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Long projectId = task.getProject().getId();
        boolean hasAccess = task.getProject().getOwner().getId().equals(user.getId())
                || projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId());

        if (!hasAccess) throw new RuntimeException("Access denied");
        return task;
    }

    @Transactional
    public CommentResponse.Summary addComment(Long taskId, CommentRequest.Create request) {
        User user = getCurrentUser();
        Task task = getTaskAndVerifyAccess(taskId, user);

        Comment comment = Comment.builder()
                .task(task)
                .author(user)
                .content(request.content())
                .build();

        return toSummary(commentRepository.save(comment), user.getId());
    }

    public CommentResponse.Paged getComments(Long taskId, int page, int size) {
        User user = getCurrentUser();
        getTaskAndVerifyAccess(taskId, user);

        Page<Comment> commentPage = commentRepository.findByTaskIdActive(
                taskId, PageRequest.of(page, size));

        return new CommentResponse.Paged(
                commentPage.getContent().stream()
                        .map(c -> toSummary(c, user.getId()))
                        .toList(),
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages(),
                commentPage.isLast()
        );
    }

    @Transactional
    public CommentResponse.Summary updateComment(Long commentId, CommentRequest.Update request) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own comments");
        }
        if (comment.isDeleted()) {
            throw new RuntimeException("Comment has been deleted");
        }

        comment.setContent(request.content());
        return toSummary(commentRepository.save(comment), user.getId());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        User user = getCurrentUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }

        comment.setDeletedAt(Instant.now());
        commentRepository.save(comment);
    }

    private CommentResponse.Summary toSummary(Comment c, Long currentUserId) {
        return new CommentResponse.Summary(
                c.getId(),
                c.getTask().getId(),
                c.getAuthor().getId(),
                c.getAuthor().getName(),
                c.getContent(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getAuthor().getId().equals(currentUserId)
        );
    }
}