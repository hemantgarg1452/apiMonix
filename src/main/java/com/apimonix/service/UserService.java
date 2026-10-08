package com.apimonix.service;

import com.apimonix.model.User;
import com.apimonix.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(UUID userId, String email){
        return userRepository.findById(userId)
                .orElseGet(()-> {
                    log.info("New user detected - creating account for [{}]", email);

                    User newUser = User.builder()
                            .id(userId)
                            .email(email)
                            .plan("FREE")
                            .build();

                    return userRepository.save(newUser);
                });
    }

    public void upgradePlan(UUID userId, String plan){
        userRepository.findById(userId).ifPresent(user -> {
            log.info("Upgrading user [{}] from [{}] to [{}]",
                    userId, user.getPlan(), plan);
            user.setPlan(plan);
            userRepository.save(user);
        });
    }
}
