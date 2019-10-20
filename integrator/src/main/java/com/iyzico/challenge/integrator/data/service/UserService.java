package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserProfile;
import com.iyzico.challenge.integrator.data.repository.UserProfileRepository;
import com.iyzico.challenge.integrator.data.repository.UserRepository;
import com.iyzico.challenge.integrator.dto.user.request.CreateUserRequest;
import com.iyzico.challenge.integrator.exception.UserNotFoundException;
import com.iyzico.challenge.integrator.exception.UserProfileNotFoundException;
import com.iyzico.challenge.integrator.exception.UsernameTakenByAnotherUserException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository repository;
    private final UserProfileRepository profileRepository;

    public UserService(UserRepository repository,
                       UserProfileRepository profileRepository) {
        this.repository = repository;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, noRollbackFor = {
            UserNotFoundException.class
    })
    public User getById(long userId) {
        Optional<User> user = repository.findById(userId);
        if (!user.isPresent()) {
            throw new UserNotFoundException(String.format("User with id %s not found", userId));
        }
        return user.get();
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS, noRollbackFor = {
            UserNotFoundException.class
    })
    public UserProfile getProfileById(long profileId) {
        Optional<UserProfile> profile = profileRepository.findById(profileId);
        if (!profile.isPresent()) {
            throw new UserProfileNotFoundException(String.format("Profile with id %s not found", profileId));
        }
        return profile.get();
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

    public Iterable<User> getAll() {
        return repository.findAll();
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public User createUser(CreateUserRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new UsernameTakenByAnotherUserException(String.format("Username '%s' is taken by another user. Try a different one", request.getUsername()));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setAdmin(request.isAdmin());

        UserProfile profile = new UserProfile();

        profile.setName(request.getName());
        profile.setSurname(request.getSurname());
        profile.setIdentityNo(request.getIdentityNo());
        profile.setCity(request.getCity());
        profile.setCountry(request.getCountry());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());
        profile.setAddress(request.getAddress());
        profile.setZipCode(request.getZipCode());
        profile.setRegistrationDate(LocalDateTime.now());

        user.setProfile(profile);

        return repository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public User updateUser(long id, String password, boolean admin) {
        User user = getById(id);
        user.setAdmin(admin);

        if (!StringUtils.isEmpty(password)) {
            user.setPassword(password);
        }

        return repository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void inactivate(long id) {
        User user = getById(id);
        user.setActive(false);
        repository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public void markAsLoggedIn(User user, String sessionKey) {
        user.setLastLoginDate(LocalDateTime.now());
        user.setLastSessionKey(sessionKey);
        repository.save(user);
    }
}

