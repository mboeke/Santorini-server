package ch.uzh.ifi.seal.soprafs19.service;

import ch.uzh.ifi.seal.soprafs19.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.exceptions.*;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.service.BuildingService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@Transactional
public class GameServiceDemo {

    private final Logger log = LoggerFactory.getLogger(GameServiceDemo.class);

    private final GameRepository gameRepository;
    private final FigureRepository figureRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private  final  FigureService figureService;
    private final  GameService gameService;
    private final BuildingService buildingService;

    @Autowired
    public GameServiceDemo(
            GameRepository gameRepository,
            FigureRepository figureRepository,
            UserRepository userRepository,
            UserService userService,
            FigureService figureService,
            GameService gameService,
            BuildingService buildingService
    )
    {
        this.gameRepository = gameRepository;
        this.figureRepository = figureRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.figureService = figureService;
        this.gameService = gameService;
        this.buildingService = buildingService;
    }





    public String postCreateGameDemoXWins(Game newGame) throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException, GameRuleException, UsernameAlreadyExistsException {
        // Get the users by extracting the user id's from the game
        User user1 = newGame.getUser1();
        User user2 = newGame.getUser2();

        // Check if a users are offline
        if (!(userService.isOnline(user1) && userService.isOnline(user2))) {
            return "Both users have to be online and not involved in a game";
        }

        // Check if user1 and user2 are different
        if (user1.equals(user2)) {
            return "You can't play against yourself";
        }
        newGame.setDemo(1);
        newGame.setStatus(GameStatus.INITIALIZED);
        newGame.setCurrentTurn(user2);
        gameRepository.save(newGame);

        user1.setGame(newGame);
        user1.setStatus(UserStatus.CHALLENGED);
        userRepository.save(user1);

        user2.setGame(newGame);
        user2.setStatus(UserStatus.CHALLENGED);
        userRepository.save(user2);



        return newGame.getId().toString();
    }


        public Game postAcceptDemoGameRequestByUser(long gameId, User acceptingUser) throws FailedAuthenticationException, ResourceNotFoundException, ResourceActionNotAllowedException, GameRuleException, UsernameAlreadyExistsException {

        Game newGame = gameRepository.findById(gameId);
            newGame.setStatus(GameStatus.STARTED);
            gameRepository.save(newGame);

            User testUser = newGame.getUser1();
            User testUser2 = newGame.getUser2();

            testUser.setStatus(UserStatus.PLAYING);
            testUser2.setStatus(UserStatus.PLAYING);

            userRepository.save(testUser);
            userRepository.save(testUser2);


        Figure figure11 = new Figure();
        Figure figure12 = new Figure();
        Figure figure21 = new Figure();
        Figure figure22 = new Figure();

        Position p220 = new Position(2,2,0); //testUser2
        Position p330 = new Position(3,3,0); // testUser2
        Position p110 = new Position(1,1,0); // testUser
        Position p320 = new Position(3,2,0); // testUser
        Position p210 = new Position(2,1,0);
        Position p221 = new Position(2,2,1);
        Position p230 = new Position(2,3,0);
        Position p231 = new Position(2,3,1);
        Position p120 = new Position(1,2,0);
        Position p310 = new Position(3,1,0);
        Position p321 = new Position(3,2,1);
        Position p211 = new Position(2,1,1);
        Position p121 = new Position(1,2,1);
        Position p222 = new Position(2,2,2);
        Position p020 = new Position(0,2,0);
        Position p420 = new Position(4,2,0);
        Position p430 = new Position(4,3,0);
        Position p213 = new Position(2,1,3);
        Position p241 = new Position(2,4,1);
        Position p212 = new Position(2,1,2);
        Position p240 = new Position(2,4,0);
        Position p232 = new Position(2,3,2);
        Position p021 = new Position(0,2,1);
        Position p122 = new Position(1,2,2);
        Position p123 = new Position(1,2,3);
        Position p322 = new Position(3,2,2);
        Position p421 = new Position(4,2,1);



        figure11.setPosition(p220);
        figure11.setOwnerId(testUser2.getId());
        figure11.setGame(newGame);

        figureService.postFigure(newGame, figure11);

        figure12.setPosition(p330);
        figure12.setOwnerId(testUser2.getId());
        figure12.setGame(newGame);
        figureService.postFigure(newGame, figure12);

        figure21.setPosition(p110);
        figure21.setOwnerId(testUser.getId());
        figure21.setGame(newGame);
        figureService.postFigure(newGame, figure21);

        figure22.setPosition(p320);
        figure22.setOwnerId(testUser.getId());
        figure22.setGame(newGame);
        figureService.postFigure(newGame,figure22);

        figureService.putFigure(figure11.getId(),p210 );

        Building building = new Building();
        building.setPosition(p220);
        building.setOwnerId(testUser2.getId());
        building.setGame(newGame);
        buildingService.postBuilding(newGame, building);

        figureService.putFigure(figure21.getId(), p221);

        Building building2 = new Building();
        building2.setPosition(p230);
        building2.setOwnerId(testUser.getId());
        building2.setGame(newGame);
        buildingService.postBuilding(newGame, building2);

        figureService.putFigure(figure12.getId(), p231);

        Building building3 = new Building();
        building3.setPosition(p120);
        building3.setOwnerId(testUser2.getId());
        building3.setGame(newGame);
        buildingService.postBuilding(newGame, building3);

        figureService.putFigure(figure22.getId(), p310);


        Building building4 = new Building();
        building4.setPosition(p320);
        building4.setOwnerId(testUser.getId());
        building4.setGame(newGame);
        buildingService.postBuilding(newGame, building4); //move

        figureService.putFigure(figure11.getId(), p321);

        Building building5 = new Building();
        building5.setPosition(p210);
        building5.setOwnerId(testUser2.getId());
        building5.setGame(newGame);
        buildingService.postBuilding(newGame, building5);

        figureService.putFigure(figure22.getId(), p211);

        Building building6 = new Building();
        building6.setPosition(p121);
        building6.setOwnerId(testUser.getId());
        building6.setGame(newGame);

        buildingService.postBuilding(newGame, building6);
        figureService.putFigure(figure12.getId(), p122);

        Building building7 = new Building();
        building7.setPosition(p231);
        building7.setOwnerId(testUser2.getId());
        building7.setGame(newGame);
        buildingService.postBuilding(newGame, building7);
        figureService.putFigure(figure21.getId(), p232);

        Building building8 = new Building();
        building8.setPosition(p221);
        building8.setOwnerId(testUser.getId());
        building8.setGame(newGame);

        buildingService.postBuilding(newGame, building8);
        figureService.putFigure(figure11.getId(), p222);

        Building building9 = new Building();
        building9.setPosition(p321);
        building9.setOwnerId(testUser2.getId());
        building9.setGame(newGame);
        buildingService.postBuilding(newGame, building9);
        figureService.putFigure(figure22.getId(), p322);

        Building building10 = new Building();
        building10.setPosition(p420);
        building10.setOwnerId(testUser.getId());
        building10.setGame(newGame);
        buildingService.postBuilding(newGame, building10);
        figureService.putFigure(figure12.getId(), p110);

        Building building11 = new Building();
        building11.setPosition(p020);
        building11.setOwnerId(testUser2.getId());
        building11.setGame(newGame);
        buildingService.postBuilding(newGame, building11);
        figureService.putFigure(figure22.getId(), p430);

        Building building12 = new Building();
        building12.setPosition(p421);
        building12.setOwnerId(testUser.getId());
        building12.setGame(newGame);
        buildingService.postBuilding(newGame, building12);
        figureService.putFigure(figure11.getId(), p322);

        Building building13 = new Building();
        building13.setPosition(p211);
        building13.setOwnerId(testUser2.getId());
        building13.setGame(newGame);
        buildingService.postBuilding(newGame, building13);
        figureService.putFigure(figure21.getId(), p222);

        Building building14 = new Building();
        building14.setPosition(p122);
        building14.setOwnerId(testUser.getId());
        building14.setGame(newGame);
        buildingService.postBuilding(newGame, building14);
        figureService.putFigure(figure12.getId(), p021);

        Building building15 = new Building();
        building15.setPosition(p123);
        building15.setOwnerId(testUser2.getId());
        building15.setGame(newGame);
        buildingService.postBuilding(newGame, building15);
        figureService.putFigure(figure21.getId(), p232);

        Building building16 = new Building();
        building16.setPosition(p240);
        building16.setOwnerId(testUser.getId());
        building16.setGame(newGame);
        buildingService.postBuilding(newGame, building16);

        figureService.putFigure(figure11.getId(), p222);

        Building building17 = new Building();
        building17.setPosition(p212);
        building17.setOwnerId(testUser2.getId());
        building17.setGame(newGame);
        buildingService.postBuilding(newGame, building17);

            newGame.setGodPower(true);
            ArrayList<String> godCards = new ArrayList<>();
            godCards.add("apollo");
            godCards.add("minotaur");
            newGame.setGodCardsList(godCards);
            newGame.setGod1(godCards.get(0));
            newGame.setGod2(godCards.get(1));

        newGame.setDemo(1);
        gameRepository.save(newGame);

        return newGame;

    }
}