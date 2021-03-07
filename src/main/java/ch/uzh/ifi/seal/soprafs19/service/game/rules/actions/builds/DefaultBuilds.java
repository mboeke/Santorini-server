package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.builds;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.Action;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.GameBoard;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;

import java.util.ArrayList;
import java.util.List;


public class DefaultBuilds extends Action {

    public DefaultBuilds(
            Figure figure, GameBoard board, BuildingRepository buildingRepository,
            FigureRepository figureRepository, MoveRepository moveRepository,
            GameRepository gameRepository, GameService gameService, FigureService figureService)
    {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public List<Position> calculatePossiblePositions()
    {
        int [] neighbourhood = {-1, 1, -1, 1, -3, 3}; // LowerX, UpperX, LowerY, UpperY, LowerZ, UpperZ
        ArrayList<Position> adjacentPositionsOfOrigin = calculatePositionsInNeighbourhood(neighbourhood);

        // Strip out positions that are occupied by other board items
        stripOccupiedPositions(adjacentPositionsOfOrigin);

        // Strip out the positions that are floating and have no building below
        stripFloatingPositions(adjacentPositionsOfOrigin);

        return adjacentPositionsOfOrigin;
    }

    @Override
    public void perform()
    {
        buildingRepository.save(getBuilding());
        // The LAF is set to 0 --> no figure is LAF
        game.setLastActiveFigureId(0);
        handleLoseCondition();
        game.swapTurns();
    }

    protected void handleLoseCondition()
    {
        long opponentUserId = game.getCurrentTurn().equals(game.getUser1()) ?
                game.getUser2().getId() : game.getUser1().getId();
        ArrayList<Figure> figuresOfOpponent = (ArrayList<Figure>) figureRepository.findAllByGameAndOwnerId(game, opponentUserId);

        ArrayList<Position> possibleMovesOfOpponent = new ArrayList<>();
        for (Figure figure: figuresOfOpponent) {
            figure = figureService.loadFigure(figure.getId());
            possibleMovesOfOpponent.addAll(figure.getPossibleMoves());
        }

        if (possibleMovesOfOpponent.isEmpty()) {
            gameService.setWinner(game, game.getCurrentTurn().getId());
        }
    }
}
