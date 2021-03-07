package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.builds;
import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.GameBoard;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;

import java.util.ArrayList;
import java.util.List;


public class DemeterBuilds extends DefaultBuilds {

    public DemeterBuilds(
            Figure figure, GameBoard board, BuildingRepository buildingRepository,
            FigureRepository figureRepository, MoveRepository moveRepository,
            GameRepository gameRepository, GameService gameService, FigureService figureService)
    {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public List<Position> calculatePossiblePositions()
    {
        ArrayList<Position> adjacentPositionsOfOrigin = (ArrayList<Position>) super.calculatePossiblePositions();

        // If the player built before, he is not allowed to build
        // on the upper neighbour position of the position of the previous build \_O_/.
        if (firstBuildCompleted()) {
            ArrayList<Building> lastBuildings = (ArrayList<Building>) buildingRepository
                    .findTop2ByGameOrderByCreatedOnDesc(getGame());

            Position lastPosition = lastBuildings.get(0).getPosition();
            Position upperNeighbour = new Position(lastPosition.getX(), lastPosition.getY(), lastPosition.getZ() + 1);

            adjacentPositionsOfOrigin.remove(upperNeighbour);
        }

        return adjacentPositionsOfOrigin;
    }

    @Override
    public void perform()
    {
        // This god power allows for a second build
        if (!firstBuildCompleted()) {
            buildingRepository.save(getBuilding());
            return;
        }

        buildingRepository.save(getBuilding());

        // The LAF is set to 0 --> no figure is LAF
        game.setLastActiveFigureId(0);
        super.handleLoseCondition();
        game.swapTurns();
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
