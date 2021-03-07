package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.moves;

import ch.uzh.ifi.seal.soprafs19.entity.Building;
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

public class PrometheusMoves extends Action {

    public PrometheusMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                           FigureRepository figureRepository, MoveRepository moveRepository,
                           GameRepository gameRepository, GameService gameService, FigureService figureService)
    {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public ArrayList<Position> calculatePossiblePositions()
    {
        int [] neighbourhood = {-1, 1, -1, 1, -3, 1}; // LowerX, UpperX, LowerY, UpperY, LowerZ, UpperZ

        // If athena moved up, we restrict moving
        if (game.getAthenaMovedUp()) {
            neighbourhood[5] = 0;
        }

        ArrayList<Position> adjacentPositionsOfOrigin = calculatePositionsInNeighbourhood(neighbourhood);

        // If he built before moving, the figure is not allowed to move up anymore.
        if (firstBuildCompleted()) {
            neighbourhood[5] = 0;
        }


        // Strip out positions that are occupied by other board items
        stripOccupiedPositions(adjacentPositionsOfOrigin);

        // Strip out the positions that are floating and have no building below
        stripFloatingPositions(adjacentPositionsOfOrigin);

        return adjacentPositionsOfOrigin;
    }

    @Override
    public void perform()
    {
        getFigure().setPosition(getTargetPosition());
        figureRepository.save(getFigure());

        game.setLastActiveFigureId(getFigure().getId());
        gameRepository.save(game);

        handleWindCondition();
    }

    public void handleWindCondition()
    {
        if (getFigure().getPosition().isCeil()) {
            gameService.setWinner(getGame(), getFigure().getOwnerId());
        }
    }

    private boolean firstBuildCompleted()
    {
        ArrayList<Building> lastBuildings = (ArrayList<Building>) buildingRepository
                .findTop2ByGameOrderByCreatedOnDesc(getGame());

        // If it's the first build
        if (lastBuildings.isEmpty()) {
            return false;
        }
        // Get the last inserted building
        Building lastBuilding = lastBuildings.get(0);

        return lastBuilding.getOwnerId() == game.getCurrentTurn().getId();
    }
}
