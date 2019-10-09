package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.UserRepository;
import com.iyzico.challenge.integrator.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = {
            UserNotFoundException.class
    })
    public User getById(long userId) {
        Optional<User> user = repository.findById(userId);
        if (!user.isPresent()) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
        return user.get();
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = {
            UserNotFoundException.class
    })
    public User getUserByUsername(String username) {
        User user = repository.findFirstByUsername(username);
        if (user == null) {
            throw new UserNotFoundException(String.format("User with username '%s' not found", username));
        }
        return user;
    }
}
