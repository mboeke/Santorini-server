package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.FailedAuthenticationException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceNotFoundException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceActionNotAllowedException;
import ch.uzh.ifi.seal.soprafs19.exceptions.UsernameAlreadyExistsException;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.json.JSONException;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;


@RestController
public class UserController {

    private final UserService service;

    UserController(UserService service) {
        this.service = service;
    }

    // Create new user

    @PostMapping("/users")
    public Map<String, String> postCreateUser(
            @Valid @RequestBody User newUser,
            HttpServletResponse response) throws UsernameAlreadyExistsException, JSONException
    {
        HashMap<String, String> pathToUser = new HashMap<>();
        pathToUser.put("path", this.service.postCreateUser(newUser));

        // Upon success return the path to the created usr
        response.setStatus(201);
        return pathToUser;
    }

    // Login an existing user
    @PostMapping("users/login")
    public Map<String, String> postLogin (
            @RequestBody User userToAuthenticate)
            throws ResourceNotFoundException, FailedAuthenticationException, JSONException
    {
        HashMap<String, String> map = new HashMap<>();
        map.put("token", this.service.postLogin(userToAuthenticate));

        return map;
    }

    // Logout user
    @GetMapping("users/logout")
    public void getLogout (
            @RequestHeader("authorization") String token,
            HttpServletResponse response) throws ResourceNotFoundException, ResourceActionNotAllowedException {
        response.setStatus(204);
        this.service.getLogout(token);
    }

    // Fetch all users
    @GetMapping("/users") //users
    public Iterable<User> getAllUsers (
            @RequestHeader("authorization") String token) throws FailedAuthenticationException
    {
        return service.getAllUsers(token);
    }

    // Fetch one particular user
    @GetMapping("/users/{userId}") //users
    public User getUserById (
            @RequestHeader("authorization") String token,
            @PathVariable(value="userId") long userId) throws ResourceNotFoundException, FailedAuthenticationException
    {
        return service.getUserById(token, userId);
    }

    // Update one particular user
    @PutMapping("/users/{userId}") //users
    public User putUser (
            @RequestHeader("authorization") String token,
            @PathVariable(value="userId") long userId,
            @RequestBody User userToUpdate, HttpServletResponse response) throws ResourceNotFoundException,
            FailedAuthenticationException, ResourceActionNotAllowedException, UsernameAlreadyExistsException {
        response.setStatus(204);
        return service.putUpdateUser(token, userId, userToUpdate);
    }
}
