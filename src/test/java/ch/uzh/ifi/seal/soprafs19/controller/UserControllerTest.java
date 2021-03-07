package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the UserResource REST resource.
 *
 * @see UserService
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
@AutoConfigureMockMvc
public class UserControllerTest {



    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserController userController;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void fetchAllUsersWithInvalidToken() throws Exception {
        this.mvc.perform(get("/users").header("authorization", "fake_token")).andDo(print()).andExpect(status().is(401));
    }

    @Test
    public void fetchUserWithInvalidToken() throws Exception {
        this.mvc.perform(get("/users/1").header("authorization", "fake_token")).andDo(print()).andExpect(status().is(401));
    }

    @Test
    public void createUser() throws Exception {

        this.mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Userw\",\"username\": \"testUserw\", \"password\": \"testPassword\"}"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.path", notNullValue()));

        userRepository.delete(userRepository.findByUsername("testUserw"));




    }

    @Test
    public void loginUser() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUsere");
        testUser.setName("Test Usere");
        testUser.setPassword("testPassword");
        testUser.setBirthday(new SimpleDateFormat("yy-MM-dd").parse("1948-04-06"));
        String path = userService.postCreateUser(testUser);


        this.mvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"testUsere\", \"password\": \"testPassword\"}"))

                .andExpect(status().is(200))
                .andExpect(jsonPath("$.token", notNullValue()));

        userRepository.delete(userRepository.findByUsername("testUsere"));


    }

    @Test
    public void logoutUser() throws Exception{
        User testUser = new User();
        testUser.setUsername("testUsert");
        testUser.setName("Test Usert");
        testUser.setPassword("testPassword");
        String path = userService.postCreateUser(testUser);
        String token = userService.postLogin(testUser);


        this.mvc.perform(get("/users/logout")
                .header("authorization", token))
                .andExpect(status().is(204));

        userRepository.delete(userRepository.findByUsername("testUsert"));

    }

    @Test
    public void updateUser() throws Exception {
        User testUser = new User();
        testUser.setUsername("testUserz");
        testUser.setName("Test Userz");
        testUser.setPassword("testPassword");
        String path = userService.postCreateUser(testUser);
        String token = userService.postLogin(testUser);
        long id = userRepository.findByUsername(testUser.getUsername()).getId();

        this.mvc.perform(put("/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("authorization", token)
                .content("{\"name\": \"Test User Updated\",\"username\": \"testUserUpdated\", \"password\": \"testPasswordUpdated\"}"))
                .andExpect(status().is(204)); //andDo(print()).

        Assert.assertEquals("testUserUpdated", userRepository.findById(id).getUsername());

        userRepository.delete(userRepository.findByUsername("testUserUpdated"));



    }



}
