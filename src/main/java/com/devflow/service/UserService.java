package com.devflow.service;

import com.devflow.dto.UserProfileRequest;
import com.devflow.dto.UserProfileResponse;
import com.devflow.entity.User;
import com.devflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                             .orElseThrow(() -> new RuntimeException("User not found"));
    }

     public UserProfileResponse.Profile getMyProfile() {
        return toProfile(getCurrentUser());
    }

    public UserProfileResponse.Profile getUserById(Long id) {
        User user = userRepository.findById(id)
                                  .orElseThrow(() -> new RuntimeException("User not found"));

                                  return toProfile(user);
    }
    @Transactional
    public UserProfileResponse.Profile updateMyProfile(UserProfileRequest.Update request) {
        User user = getCurrentUser();
        user.setName(request.name());

        if(request.bio() != null) user.setBio(request.bio());

        return toProfile(userRepository.save(user));
    }

    private UserProfileResponse.Profile toProfile(User u) {
        return new UserProfileResponse.Profile(
            u.getId(),
            u.getName(),
            u.getEmail(),
            u.getRole().name(),
            u.getBio(),
            u.getAvatarUrl(),
            u.getCreatedAt()
        );
    }
}
