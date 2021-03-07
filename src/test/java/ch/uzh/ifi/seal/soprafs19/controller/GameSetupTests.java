package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceActionNotAllowedException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceNotFoundException;
import ch.uzh.ifi.seal.soprafs19.repository.*;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.BuildingService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.AuthenticationService;
import ch.uzh.ifi.seal.soprafs19.utilities.Utilities;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
@AutoConfigureMockMvc
public class GameSetupTests {

    @Autowired
    private MockMvc mvc;


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FigureRepository figureRepository;
    @Autowired
    private BuildingRepository buildingRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private MoveRepository moveRepository;

    private String user1;
    private String token1;

    private String user2;
    private String token2;

    private String gameId;

    @Autowired
    Utilities utils;
    @Autowired
    AuthenticationService authentication;

    @Autowired
    UserService userService = new UserService(userRepository, authentication, utils);
    @Autowired
    GameService gameService = new GameService(gameRepository, figureRepository, moveRepository ,buildingRepository, userRepository, userService);
    @Autowired
    FigureService figureService = new FigureService(figureRepository, buildingRepository, moveRepository, gameRepository, gameService  );
    @Autowired
    BuildingService buildingService = new BuildingService(buildingRepository, figureService, gameService);

    /*
     * creates 2 users, which are online
     */
    @Before
    public void setup() throws Exception
    {

        // Create user 1
        MvcResult response = this.mvc
                .perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"User 1\",\"username\": \"user1\", \"password\": \"admin\"}"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.path", notNullValue()))
                .andReturn();

        String responseString = response.getResponse().getContentAsString();
        HashMap<String,String> result = new ObjectMapper().readValue(responseString, HashMap.class);
        user1 = result.get("path").split("/")[2];


        // Create user 2
        response = this.mvc
                .perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"User 2\",\"username\": \"user2\", \"password\": \"admin\"}"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.path", notNullValue()))
                .andReturn();

        responseString = response.getResponse().getContentAsString();
        result = new ObjectMapper().readValue(responseString, HashMap.class);
        user2 = result.get("path").split("/")[2];

        // Login user 1
        response = this.mvc
                .perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user1\", \"password\": \"admin\"}"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        responseString = response.getResponse().getContentAsString();
        result = new ObjectMapper().readValue(responseString, HashMap.class);
        token1 = result.get("token");

        // Login user 2
        response = this.mvc
                .perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"user2\", \"password\": \"admin\"}"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.token", notNullValue())).andReturn();
        responseString = response.getResponse().getContentAsString();
        result = new ObjectMapper().readValue(responseString, HashMap.class);
        token2 = result.get("token");


        // Create a game
        HashMap<String, Object> body = new HashMap<>();

        // Athena god card
        HashMap<String, Object> athena = new HashMap<>();
        athena.put("name", "athena");
        athena.put("selected", true);
        athena.put("user", null);

        // Demeter god card
        HashMap<String, Object> demeter = new HashMap<>();
        athena.put("name", "demeter");
        athena.put("selected", true);
        athena.put("user", null);

        ArrayList<HashMap<String, Object>> godCards = new ArrayList<>();

        godCards.add(athena);
        godCards.add(demeter);




        body.put("user1", user1);
        body.put("user2", user2);
        body.put("isGodPower", "true");
        body.put("godCards", godCards);

        JSONObject jsonBody = new JSONObject(body);

        response = this.mvc
                .perform(post("/games")
                        .header("authorization", token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody.toString()))
                .andExpect(status().is(201))
                .andReturn();

        responseString = response.getResponse().getContentAsString();
        result = new ObjectMapper().readValue(responseString, HashMap.class);
        gameId = result.get("path").split("/")[1];
    }

    @Test
    public void acceptGameInvitation() throws Exception
    {
        // Accept the game invitation
        this.mvc.perform(post("/games/" + gameId + "/accept")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"selectedGodPower\": \"demeter\"}"))
                .andExpect(status().is(200));
    }

    @Test
    public void rejectGameInvitation() throws Exception
    {
        // Rejects / surrenders a game
        this.mvc.perform(post("/games/" + gameId + "/reject")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));
    }

    @After
    public void tearDown() throws ResourceNotFoundException, ResourceActionNotAllowedException {
        long game = Long.parseLong(gameId);
        long user1Id = Long.parseLong(user1);
        long user2Id = Long.parseLong(user2);

        gameService.postCancelGameRequestByUser(game, userRepository.findById(user2Id));

        User user1 = userRepository.findById(user1Id);
        User user2 = userRepository.findById(user2Id);

        user1.setUsername(user1.getUsername() + new java.util.Date().getTime());
        user2.setUsername(user2.getUsername() + new java.util.Date().getTime());

        userRepository.save(user1);
        userRepository.save(user2);
    }
}

