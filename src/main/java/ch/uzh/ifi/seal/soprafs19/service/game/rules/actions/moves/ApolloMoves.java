package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.moves;

import ch.uzh.ifi.seal.soprafs19.entity.BoardItem;
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
import java.util.List;

public class ApolloMoves extends Action {

    public ApolloMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                       FigureRepository figureRepository, MoveRepository moveRepository,
                       GameRepository gameRepository, GameService gameService, FigureService figureService) {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public List<Position> calculatePossiblePositions() {
        int[] neighbourhood = {-1, 1, -1, 1, -3, 1}; // LowerX, UpperX, LowerY, UpperY, LowerZ, UpperZ

        // If athena moved up, we restrict moving
        if (game.getAthenaMovedUp()) {
            neighbourhood[5] = 0;
        }

        ArrayList<Position> adjacentPositionsOfOrigin = calculatePositionsInNeighbourhood(neighbourhood);

        // Strip out positions that are occupied by other board items
        stripOccupiedPositions(adjacentPositionsOfOrigin);

        // Strip out the positions that are floating and have no building below
        stripFloatingPositions(adjacentPositionsOfOrigin);

        return adjacentPositionsOfOrigin;
    }

    // Apollo can move into a space of the opponent figure.
    // It swaps the positions with the opponent's figure.
    @Override
    public void perform() {
        // Move to the target position
        getFigure().setPosition(getTargetPosition());
        figureRepository.save(getFigure());

        // If the target position was occupied by an opponent's figure,
        // we must move it to the origin position.
        if (getBoard().getBoardMap().containsKey(getTargetPosition())) {
            BoardItem item = getBoard().getBoardMap().get(getTargetPosition());

            // Check if the occupied position is an opponent's figure
            if (item instanceof Figure && item.getOwnerId() != game.getCurrentTurn().getId()) {
                Figure opponentFigure = (Figure)item;
                // Place the opponent's figure to the originPosition
                opponentFigure.setPosition(getOriginPosition());
                figureRepository.save(opponentFigure);
            }
        }

        game.setLastActiveFigureId(getFigure().getId());
        gameRepository.save(game);

        handleWindCondition();
    }

    // Apollo may move to a position occupied by an opponent's figure.
    // Thus we must calculate only positions occupied by buildings and
    // figures of the current user
    @Override
    protected void stripOccupiedPositions(ArrayList<Position> candidates) {
        ArrayList<Position> positionsToRemove = new ArrayList<>();

        // Iterate over the board and add to positionsToRemove if building or own figure
        for (Position position : getBoard().getBoardMap().keySet()) {

            // Fetch the item at the position
            BoardItem item = getBoard().getBoardMap().get(position);

            // If it's a building we must add it to the list
            if (item instanceof Building) {
                positionsToRemove.add(position);
            }

            // If the figure is owned by the current user we must add it to the list
            if (item.getOwnerId() == game.getCurrentTurn().getId()) {
                positionsToRemove.add(position);
            }
        }

        candidates.removeAll(positionsToRemove);
    }

    public void handleWindCondition()
    {
        if (getFigure().getPosition().isCeil()) {
            gameService.setWinner(getGame(), getFigure().getOwnerId());
        }
    }
}
