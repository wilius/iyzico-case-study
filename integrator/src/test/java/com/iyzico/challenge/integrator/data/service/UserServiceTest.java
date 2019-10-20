package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.User;
import com.iyzico.challenge.integrator.data.entity.UserProfile;
import com.iyzico.challenge.integrator.data.repository.UserProfileRepository;
import com.iyzico.challenge.integrator.data.repository.UserRepository;
import com.iyzico.challenge.integrator.dto.user.request.CreateUserRequest;
import com.iyzico.challenge.integrator.exception.UserNotFoundException;
import com.iyzico.challenge.integrator.exception.UserProfileNotFoundException;
import mockit.Deencapsulation;
import mockit.Injectable;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@RunWith(JMockit.class)
public class UserServiceTest {
    @Tested
    private UserService tested;

    @Injectable
    private UserRepository repository;

    @Injectable
    private UserProfileRepository profileRepository;

    @Test(expected = UserNotFoundException.class)
    public void getById_ProductNotFoundException() {
        long id = 1;

        Optional<User> optional = Optional.empty();
        new StrictExpectations() {{
            repository.findById(id);
            result = optional;
        }};

        tested.getById(id);
    }

    @Test
    public void getById(@Mocked User user) {
        long id = 1;

        Optional<User> optional = Optional.of(user);
        new StrictExpectations() {{
            repository.findById(id);
            result = optional;
        }};

        User result = tested.getById(id);
        Assert.assertNotNull(result);
        Assert.assertEquals(user, result);
    }

    @Test(expected = UserProfileNotFoundException.class)
    public void getProfileById_UserProfileNotFoundException() {
        long id = 1;

        Optional<UserProfile> optional = Optional.empty();
        new StrictExpectations() {{
            profileRepository.findById(id);
            result = optional;
        }};

        tested.getProfileById(id);
    }

    @Test
    public void getProfileById(@Mocked UserProfile profile) {
        long id = 1;

        Optional<UserProfile> optional = Optional.of(profile);
        new StrictExpectations() {{
            profileRepository.findById(id);
            result = optional;
        }};

        UserProfile result = tested.getProfileById(id);
        Assert.assertNotNull(result);
        Assert.assertEquals(profile, result);
    }

    @Test(expected = UserNotFoundException.class)
    public void getUserByUsername_UserProfileNotFoundException() {
        String username = "username";

        new StrictExpectations() {{
            repository.findFirstByUsername(username);
            result = null;
        }};

        tested.getUserByUsername(username);
    }

    @Test
    public void getUserByUsername(@Mocked User user) {
        String username = "username";

        new StrictExpectations() {{
            repository.findFirstByUsername(username);
            result = user;
        }};

        User result = tested.getUserByUsername(username);
        Assert.assertNotNull(result);
        Assert.assertEquals(user, result);
    }

    @Test
    public void getAll() {
        Iterable<User> users = Collections.unmodifiableList(Collections.emptyList());
        new StrictExpectations() {{
            repository.findAll();
            result = users;
        }};

        Iterable<User> result = tested.getAll();
        Assert.assertNotNull(result);
        Assert.assertEquals(users, result);
    }

    @Test
    public void createUser() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("name");
        request.setSurname("surname");
        request.setAddress("address");
        request.setAdmin(true);
        request.setCity("city");
        request.setCountry("country");
        request.setEmail("email");
        request.setIdentityNo("identityNo");
        request.setPassword("password");
        request.setPhoneNumber("phoneNumber");
        request.setUsername("username");
        request.setZipCode("zipCode");

        LocalDateTime now = LocalDateTime.now();

        final UserRepository repository = new MockUp<UserRepository>() {
            @Mock
            <S> S save(S user1) {
                return user1;
            }
        }.getMockInstance();


        new StrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;

            repository.save(withInstanceOf(User.class));
        }};

        Deencapsulation.setField(tested, repository);

        User result = tested.createUser(request);
        Assert.assertNotNull(result);
        Assert.assertEquals(request.getUsername(), result.getUsername());
        Assert.assertEquals(request.getPassword(), result.getPassword());
        Assert.assertTrue(result.isActive());
        Assert.assertTrue(result.isAdmin());
        Assert.assertNull(result.getLastSessionKey());
        Assert.assertNull(result.getLastLoginDate());
        Assert.assertNotNull(result.getProfile());

        UserProfile profile = result.getProfile();
        Assert.assertEquals(request.getAddress(), profile.getAddress());
        Assert.assertEquals(request.getZipCode(), profile.getZipCode());
        Assert.assertEquals(request.getSurname(), profile.getSurname());
        Assert.assertEquals(request.getPhoneNumber(), profile.getPhoneNumber());
        Assert.assertEquals(request.getName(), profile.getName());
        Assert.assertEquals(request.getCity(), profile.getCity());
        Assert.assertEquals(request.getCountry(), profile.getCountry());
        Assert.assertEquals(request.getEmail(), profile.getEmail());
        Assert.assertEquals(request.getIdentityNo(), profile.getIdentityNo());
        Assert.assertEquals(now, profile.getRegistrationDate());
    }

    @Test
    public void updateUser_PasswordNotUpdated(@Mocked User user) {
        long id = 1;
        String password = "";
        boolean admin = true;

        final UserRepository repository = new MockUp<UserRepository>() {
            @Mock
            <S> S save(S user1) {
                return user1;
            }
        }.getMockInstance();

        new StrictExpectations(tested) {{
            tested.getById(id);
            result = user;

            user.setAdmin(admin);

            repository.save(user);
        }};

        Deencapsulation.setField(tested, repository);

        User result = tested.updateUser(id, password, admin);
        Assert.assertNotNull(result);
        Assert.assertEquals(user, result);
    }

    @Test
    public void updateUser(@Mocked User user) {
        long id = 1;
        String password = "password";
        boolean admin = true;

        final UserRepository repository = new MockUp<UserRepository>() {
            @Mock
            <S> S save(S user1) {
                return user1;
            }
        }.getMockInstance();

        new StrictExpectations(tested) {{
            tested.getById(id);
            result = user;

            user.setAdmin(admin);
            user.setPassword(password);

            repository.save(user);
        }};

        Deencapsulation.setField(tested, repository);

        User result = tested.updateUser(id, password, admin);
        Assert.assertNotNull(result);
        Assert.assertEquals(user, result);
    }

    @Test
    public void inactivate(@Mocked User user) {
        long id = 1;
        new StrictExpectations(tested) {{
            tested.getById(id);
            result = user;

            user.setActive(false);

            repository.save(user);
        }};
        tested.inactivate(id);
    }

    @Test
    public void markAsLoggedIn(@Mocked User user) {
        String sessionKey = "sessionKey";

        LocalDateTime now = LocalDateTime.now();
        new StrictExpectations(LocalDateTime.class) {{
            LocalDateTime.now();
            result = now;

            user.setLastLoginDate(now);
            user.setLastSessionKey(sessionKey);
            repository.save(user);
        }};

        tested.markAsLoggedIn(user, sessionKey);
    }
}