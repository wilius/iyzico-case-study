package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.repository.UserRepository;
import com.iyzico.challenge.integrator.exception.UserNotFoundException;
import com.iyzico.challenge.integrator.exception.UsernameTakenByAnotherUserException;
import org.apache.commons.lang3.StringUtils;
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

    public Iterable<User> getAllUsers() {
        return repository.findAll();
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public User createUser(String name, String username, String password, boolean admin) {
        if (repository.existsByUsername(username)) {
            throw new UsernameTakenByAnotherUserException(String.format("Username '%s' is taken by another user. Try a different one", username));
        }

        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setPassword(password);
        user.setAdmin(admin);

        return repository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public User updateUser(long id, String name, String password, boolean admin) {
        User user = getById(id);
        user.setName(name);
        user.setAdmin(admin);

        if (!StringUtils.isEmpty(password)) {
            user.setPassword(password);
        }

        return repository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void delete(long id) {
        User user = getById(id);
        user.setActive(false);
        repository.save(user);
    }
}

