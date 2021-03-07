package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.FailedAuthenticationException;
import ch.uzh.ifi.seal.soprafs19.exceptions.GameRuleException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceActionNotAllowedException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceNotFoundException;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.service.BuildingService;
import ch.uzh.ifi.seal.soprafs19.utilities.AuthenticationService;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;


@RestController
public class BuildingController {

    private final BuildingService service;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    BuildingController(BuildingService service, GameRepository gameRepository, UserRepository userRepository, AuthenticationService authenticationService, FigureRepository figureRepository)
    {
        this.service = service;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping(value = "/games/{id}/buildings")
    public Iterable<Building> getGameBoardBuildings (@PathVariable long id)
    {
        Game game = gameRepository.findById(id);
        return service.getAllBuildings(game);
    }

    @PostMapping(value = "/games/{id}/buildings")
    public Map<String, String> postGameBoardBuilding (
            @RequestHeader("authorization") String token,
            @PathVariable long id,
            @RequestBody Position position,
            HttpServletResponse response)
            throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException, GameRuleException
    {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, id);
        authenticationService.userTokenIsCurrentTurn(token, id);

        Game game = gameRepository.findById(id);
        User user = userRepository.findByToken(token);
        Building building = new Building();

        building.setPosition(position);
        building.setOwnerId(user.getId());
        building.setGame(game);
        response.setStatus(201);

        HashMap<String, String> pathToBuilding = new HashMap<>();
        pathToBuilding.put("path", service.postBuilding(game, building));

        return pathToBuilding;
    }

    @GetMapping(value = "/games/{id}/buildings/possibleBuilds")
    public Iterable<Position> getGameBoardBuildingsPossibleBuilds (
            @RequestHeader("authorization") String token,
            @PathVariable long id
    ) throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, id);
        authenticationService.userTokenIsCurrentTurn(token, id);

        Game game = gameRepository.findById(id);
        return service.getPossibleBuilds(game);
    }
}
