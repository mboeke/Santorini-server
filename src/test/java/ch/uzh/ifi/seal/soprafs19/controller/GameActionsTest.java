package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.Application;
import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceActionNotAllowedException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceNotFoundException;
import ch.uzh.ifi.seal.soprafs19.repository.*;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.BuildingService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.AuthenticationService;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes= Application.class)
@AutoConfigureMockMvc
public class GameActionsTest {

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
    private String figure2;

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

        // Accept the game invitation
        this.mvc.perform(post("/games/" + gameId + "/accept")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"selectedGodPower\": \"demeter\"}"))
                .andExpect(status().is(200));
    }

    @Test
    public void getPossiblePosts() throws Exception
    {
        this.mvc.perform(get("/games/" + gameId + "/figures/possiblePosts")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200));
    }

    @Test
    public void postFigures() throws Exception
    {

        this.mvc.perform(get("/games/" + gameId + "/figures/possiblePosts")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andReturn();

        HashMap<String, Object> body = new HashMap<>();
        body.put("x", 1);
        body.put("y", 1);
        body.put("z", 0);

        JSONObject jsonBody = new JSONObject(body);

        // Post Figure 1
        MvcResult response = this.mvc.perform(post("/games/" + gameId + "/figures")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().is(201))
                .andReturn();

        String responseString = response.getResponse().getContentAsString();
        HashMap<String,String> result = new ObjectMapper().readValue(responseString, HashMap.class);
        figure2 = result.get("path").split("/")[1];

        body.clear();
        body.put("x", 1);
        body.put("y", 2);
        body.put("z", 0);

        jsonBody = new JSONObject(body);

        // Post Figure 2
        this.mvc.perform(post("/games/" + gameId + "/figures")
                .header("authorization", token2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().is(201));

        body.clear();
        body.put("x", 1);
        body.put("y", 3);
        body.put("z", 0);

        jsonBody = new JSONObject(body);

        // Post Figure 3
        this.mvc.perform(post("/games/" + gameId + "/figures")
                .header("authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().is(201));

        body.clear();
        body.put("x", 1);
        body.put("y", 4);
        body.put("z", 0);

        jsonBody = new JSONObject(body);

        // Post Figure 4
        this.mvc.perform(post("/games/" + gameId + "/figures")
                .header("authorization", token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody.toString()))
                .andExpect(status().is(201));
    }

    @Test
    public void putFigure() throws Exception {
        postFigures();

        // Fetch possible moves for each godcard
        String [] godCards = {"apollo", "artemis", "athena", "atlas", "demeter", "hephaestus", "hermes", "minotaur", "pan", "prometheus"};
        long currentGame = Long.parseLong(gameId);
        Game game = gameRepository.findById(currentGame);
        long figure2Id = Long.parseLong(figure2);
        Figure figure = figureRepository.findById(figure2Id);

        HashMap<String, Object> body = new HashMap<>();
        body.put("x", 2);
        body.put("y", 2);
        body.put("z", 0);
        JSONObject jsonBody = new JSONObject(body);


        for (int i = 0; i < godCards.length; i++) {
            game.setGod1(godCards[i]);
            game.setGod2(godCards[i]);
            gameRepository.save(game);

            this.mvc.perform(put("/games/" + gameId + "/figures/" + figure2)
                    .header("authorization", token2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody.toString()))
                    .andExpect(status().is(200));
            figure.setPosition(new Position(1, 1, 0));
            figureRepository.save(figure);
        }
    }

    @Test
    public void getPossibleMoves() throws Exception {
        postFigures();

        // Fetch possible moves for each godcard
        String [] godCards = {"apollo", "artemis", "athena", "atlas", "demeter", "hephaestus", "hermes", "minotaur", "pan", "prometheus"};
        long currentGame = Long.parseLong(gameId);
        Game game = gameRepository.findById(currentGame);

        for (int i = 0; i < godCards.length; i++) {
            game.setGod1(godCards[i]);
            game.setGod2(godCards[i]);
            gameRepository.save(game);

            this.mvc.perform(get("/games/" + gameId + "/figures/" + figure2 + "/possibleMoves")
                    .header("authorization", token2)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(200));
        }
    }

    @Test
    public void postBuilding() throws  Exception
    {
        postFigures();

        // Fetch possible moves for each godcard
        String [] godCards = {"apollo", "artemis", "athena", "atlas", "demeter", "hephaestus", "hermes", "minotaur", "pan", "prometheus"};
        long currentGame = Long.parseLong(gameId);
        Game game = gameRepository.findById(currentGame);
        long figure2Id = Long.parseLong(figure2);
        long buildingId = 0;
        Figure figure = figureRepository.findById(figure2Id);


        HashMap<String, Object> body = new HashMap<>();
        body.put("x", 2);
        body.put("y", 2);
        body.put("z", 0);
        JSONObject jsonBody = new JSONObject(body);


        for (int i = 0; i < godCards.length; i++) {
            game.setGod1(godCards[i]);
            game.setGod2(godCards[i]);
            game.setLastActiveFigureId(figure2Id);
            gameRepository.save(game);

            MvcResult response = this.mvc.perform(post("/games/" + gameId + "/buildings")
                    .header("authorization", token2)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonBody.toString()))
                    .andExpect(status().is(201))
                    .andReturn();

            String responseString = response.getResponse().getContentAsString();
            HashMap<String,String> result = new ObjectMapper().readValue(responseString, HashMap.class);
            buildingId = Long.parseLong(result.get("path").split("/")[1]);
            Building building = buildingRepository.findById(buildingId);
            buildingRepository.delete(building);

        }
    }

    @Test
    public void getPossibleBuilds() throws Exception {
        postFigures();

        // Fetch possible moves for each godcard
        String [] godCards = {"apollo", "artemis", "athena", "atlas", "demeter", "hephaestus", "hermes", "minotaur", "pan", "prometheus"};
        long currentGame = Long.parseLong(gameId);
        Game game = gameRepository.findById(currentGame);
        long figure2Id = Long.parseLong(figure2);

        for (int i = 0; i < godCards.length; i++) {
            game.setGod1(godCards[i]);
            game.setGod2(godCards[i]);
            game.setLastActiveFigureId(figure2Id);
            gameRepository.save(game);

            this.mvc.perform(get("/games/" + gameId + "/buildings/possibleBuilds")
                    .header("authorization", token2)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(200));
        }
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

