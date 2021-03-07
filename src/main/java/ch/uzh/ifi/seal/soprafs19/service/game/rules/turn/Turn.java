package ch.uzh.ifi.seal.soprafs19.service.game.rules.turn;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.utilities.GameBoard;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public abstract class Turn {

    protected final GameBoard board;
    protected final MoveRepository moveRepository;
    protected final BuildingRepository buildingRepository;
    protected final FigureRepository figureRepository;
    protected final GameRepository gameRepository;
    protected final Game game;

    @Autowired
    public Turn(GameBoard board, MoveRepository moveRepository, BuildingRepository buildingRepository, FigureRepository figureRepository, GameRepository gameRepository) {
        this.board = board;
        this.moveRepository = moveRepository;
        this.buildingRepository = buildingRepository;
        this.figureRepository = figureRepository;
        this.gameRepository = gameRepository;
        this.game = board.getGame();
    }
    public abstract boolean isBuildAllowedByUserId(long userId);
    public abstract boolean isMoveAllowedByUserId(long userId);
    public boolean isPlaceFigureAllowedByUserId(long userId)
    {
        ArrayList<Figure> figures = (ArrayList<Figure>) figureRepository.findAllByGameAndOwnerId(game, userId);
        return isCurrentTurn(userId)
               && figures.size() < 2;
    }

    public void swap()
    {
        if (game.getCurrentTurn().equals(game.getUser1())) {
            game.setCurrentTurn(game.getUser2());
        }
        else {
            game.setCurrentTurn(game.getUser1());
        }

        gameRepository.save(game);
    }

    public boolean isCurrentTurn(long userId)
    {
        return game.getCurrentTurn().getId() == userId;
    }
}
