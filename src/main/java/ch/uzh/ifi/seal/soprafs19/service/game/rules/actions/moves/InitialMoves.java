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

public class InitialMoves extends Action {

    public InitialMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                        FigureRepository figureRepository, MoveRepository moveRepository,
                        GameRepository gameRepository, GameService gameService, FigureService figureService)
    {
        super(figure, board, buildingRepository, figureRepository,
                moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public ArrayList<Position> calculatePossiblePositions()
    {
        ArrayList<Position> possiblePositions = new ArrayList<>();

        // Calculate all possible positions on the 0th level
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                Position possiblePosition = new Position(x, y, 0);
                possiblePositions.add(possiblePosition);
            }
        }

        // Remove all positions which are already occupied.
        stripOccupiedPositions(possiblePositions);

        return possiblePositions;
    }

    @Override
    public void perform()
    {
        getFigure().setPosition(getTargetPosition());
        figureRepository.save(getFigure());

        // Swap the turn if the user has placed 2 figures
        if (!game.getTurn().isPlaceFigureAllowedByUserId(getFigure().getOwnerId())) {
            game.swapTurns();
        }
    }
}
