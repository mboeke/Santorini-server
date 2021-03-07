package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.moves;

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
import java.util.Iterator;

public class AtlasMoves extends Action {

    public AtlasMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
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

        // Strip out positions that are occupied by other board items
        stripOccupiedPositions(adjacentPositionsOfOrigin);

        // Strip out the positions that are floating and have no building below
        stripFloatingPositions(adjacentPositionsOfOrigin);

        // Strip out the positions where there is a dome at ANY level
        stripDomePositions(adjacentPositionsOfOrigin);

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

    private void stripDomePositions(ArrayList<Position> candidates)
    {
        candidates.removeAll(calculateDomePositions(candidates));
    }

    private ArrayList<Position> calculateDomePositions(ArrayList<Position> candidates)
    {
        ArrayList<Position> invalidPositions = new ArrayList<>();
        for (Iterator<Position> iterator = candidates.iterator(); iterator.hasNext(); ) {
            Position candidate = iterator.next();
            Position dome = new Position(candidate.getX(), candidate.getY(), 3);

            // If there is a dome at ANY level, the figure can't move below it.
            if (getBoard().getBoardMap().containsKey(dome)) {
                invalidPositions.add(candidate);
            }
        }

        return invalidPositions;
    }
}
