package ch.uzh.ifi.seal.soprafs19.controller;

import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.FailedAuthenticationException;
import ch.uzh.ifi.seal.soprafs19.exceptions.GameRuleException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceActionNotAllowedException;
import ch.uzh.ifi.seal.soprafs19.exceptions.ResourceNotFoundException;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.utilities.AuthenticationService;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RestController
public class FigureController {

    private final FigureService service;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final FigureRepository figureRepository;
    private final AuthenticationService authenticationService;

    FigureController(FigureService service, GameRepository gameRepository, UserRepository userRepository, AuthenticationService authenticationService, FigureRepository figureRepository)
    {
        this.service = service;
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.figureRepository = figureRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping(value = "/games/{id}/figures")
    public Iterable<Figure> getGameBoardFigures (
            @PathVariable long id,
            @RequestHeader("authorization") String token) throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, id);
        Game game = gameRepository.findById(id);
        return service.getAllFigures(game);
    }

    @PostMapping(value = "/games/{id}/figures")
    public Map<String, String> postGameBoardFigure (
            @RequestHeader("authorization") String token,
            @PathVariable long id,
            @RequestBody Position position,
            HttpServletResponse response)
            throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException, GameRuleException {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, id);
        authenticationService.userTokenIsCurrentTurn(token, id);

        Game game = gameRepository.findById(id);
        User user = userRepository.findByToken(token);
        Figure figure = new Figure();

        figure.setPosition(position);
        figure.setOwnerId(user.getId());
        figure.setGame(game);
        response.setStatus(201);

        HashMap<String, String> pathToFigure = new HashMap<>();
        pathToFigure.put("path", service.postFigure(game, figure));

        return pathToFigure;
    }

    @PutMapping(value = "/games/{gameId}/figures/{figureId}")
    public Map<String, String> putFigure (
            @RequestHeader("authorization") String token,
            @PathVariable long gameId,
            @PathVariable long figureId,
            @RequestBody Position destination,
            HttpServletResponse response)
            throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException, GameRuleException {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, gameId);
        authenticationService.userTokenIsCurrentTurn(token,gameId);
        response.setStatus(200);

        HashMap<String, String> pathToFigure = new HashMap<>();
        pathToFigure.put("path", service.putFigure(figureId, destination));

        return pathToFigure;
    }

    @GetMapping(value = "/games/{gameId}/figures/{figureId}/possibleMoves")
    public Iterable<Position> getGameBoardFigurePossiblePuts(
            @PathVariable long gameId,
            @PathVariable long figureId,
            @RequestHeader("authorization") String token) throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, gameId);
        authenticationService.userTokenIsCurrentTurn(token,gameId);

        Figure figure = figureRepository.findById(figureId);
        return figure != null ? service.getPossibleMoves(figureId) : new ArrayList<Position>();
    }

    @GetMapping(value = "/games/{gameId}/figures/possiblePosts")
    public Iterable<Position> getGameBoardFigurePossiblePosts (
            @PathVariable long gameId,
            @RequestHeader("authorization") String token) throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException {
        authenticationService.authenticateUser(token);
        authenticationService.userTokenInGameById(token, gameId);
        authenticationService.userTokenIsCurrentTurn(token,gameId);

        Game game = gameRepository.findById(gameId);
        return service.getPossibleInitialMoves(game);
    }

}
