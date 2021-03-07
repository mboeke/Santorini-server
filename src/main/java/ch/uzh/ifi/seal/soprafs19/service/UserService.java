package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.utilities.AuthenticationService;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.FailedAuthenticationException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceNotFoundException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceActionNotAllowedException;
import ch.uzh.ifi.seal.soprafs19.exceptions.UsernameAlreadyExistsException;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.utilities.Utilities;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class UserService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final AuthenticationService authentication;
    private final Utilities utils;


    @Autowired
    public UserService(UserRepository userRepository, AuthenticationService authentication, Utilities utils) {
        this.userRepository = userRepository;
        this.authentication = authentication;
        this.utils = utils;
    }

    public String postCreateUser(User newUser) throws UsernameAlreadyExistsException
    {
        Boolean uniqueUsername = !userRepository.existsByUsername(newUser.getUsername());

        if (!uniqueUsername) {
            throw new UsernameAlreadyExistsException();
        }

        newUser.setStatus(UserStatus.OFFLINE);
        userRepository.save(newUser);

        return "/users/" + newUser.getId().toString();
    }
    public String postLogin(User userToAuthenticate) throws FailedAuthenticationException, ResourceNotFoundException
    {
        String loginUsername = userToAuthenticate.getUsername();
        String loginPassword = userToAuthenticate.getPassword();
        Boolean userExists = userRepository.existsByUsername(loginUsername);

        if (!userExists) {
            throw new ResourceNotFoundException("User does not exist");
        }

        User dbUser = userRepository.findByUsername(loginUsername);
        String dbPassword = dbUser.getPassword();

        if (!loginPassword.equals(dbPassword)) {
            throw new FailedAuthenticationException();
        }

        if (isOffline(dbUser)) {

            userToAuthenticate.setStatus(UserStatus.ONLINE);
            dbUser.setStatus(UserStatus.ONLINE);
            userRepository.save(dbUser);

        }

        dbUser.setToken(createUserToken(dbUser));
        userToAuthenticate.setToken(createUserToken(userToAuthenticate));
        userRepository.save(dbUser);
        return dbUser.getToken();
    }
    // Update a user
    public User putUpdateUser(String token, long userToUpdateId, User userToUpdate) throws
            ResourceActionNotAllowedException,
            FailedAuthenticationException,
            UsernameAlreadyExistsException
    {
        User dbUser = userRepository.findById(userToUpdateId);
        Boolean authenticated = authentication.isAuthenticated(token);
        Boolean tokenBelongsToUser = authentication.tokenOwnsUser(token, userToUpdateId);
        Boolean uniqueUsername = !userRepository.existsByUsername(userToUpdate.getUsername())
                || userToUpdate.getUsername().equals(dbUser.getUsername());

        if (!authenticated) {
            throw new FailedAuthenticationException();
        }

        if (!tokenBelongsToUser) {
            throw new ResourceActionNotAllowedException();
        }

        if (!uniqueUsername) {
            throw new UsernameAlreadyExistsException();
        }

        utils.copyAttributes(userToUpdate, dbUser);
        userRepository.save(dbUser);

        return dbUser;
    }



    public Iterable<User> getAllUsers(String token) throws FailedAuthenticationException
    {
        Boolean authenticated = authentication.isAuthenticated(token);

        if (!authenticated) {
            throw new FailedAuthenticationException();
        }

        return this.userRepository.findAll();
    }

    // Get one particular user. A valid token and a user id must be provided
    public User getUserById(String token, long userId) throws FailedAuthenticationException, NullPointerException
    {
        Boolean authenticated = authentication.isAuthenticated(token);

        if (!authenticated) {
            throw new FailedAuthenticationException();
        }

        return userRepository.findById(userId);
    }

    // Create a token for the user
    private String createUserToken(User user)
    {
        User dbUser = userRepository.findByUsername(user.getUsername());

        // We use a timestamp and the user id to create a hash
        long userId = dbUser.getId();
        Date now = new Date();

        // We create a json object containing user id and creation date of token
        JSONObject json = new JSONObject();
        json.put("user_id", userId);
        json.put("token_created", now);

        // Convert json to String and encode it with b64
        return Base64.getEncoder().encodeToString(json.toString().getBytes());
    }

    public void getLogout(String userToLogoutToken) throws ResourceNotFoundException, ResourceActionNotAllowedException
    {
        Boolean authenticated = authentication.isAuthenticated(userToLogoutToken);

        if (!authenticated) {
            throw new ResourceNotFoundException("User does not exist");
        }

        User dbUser = userRepository.findByToken(userToLogoutToken);

        if (!isOnline(dbUser)) {
            throw new ResourceActionNotAllowedException();
        }

        dbUser.setToken(null);
        dbUser.setStatus(UserStatus.OFFLINE);
        userRepository.save(dbUser);
    }

    public boolean isOnline(User user)
    {
        return user.getStatus() == UserStatus.ONLINE;
    }

    public boolean isPlaying(User user)
    {
        return user.getStatus() == UserStatus.PLAYING;
    }

    public boolean isChallenged(User user)
    {
        return user.getStatus() == UserStatus.CHALLENGED;
    }

    public boolean isOffline(User user)
    {
        return user.getStatus() == UserStatus.OFFLINE;
    }
}