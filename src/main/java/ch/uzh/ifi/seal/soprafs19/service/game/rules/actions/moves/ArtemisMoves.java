package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.moves;

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
import java.util.HashSet;


public class ArtemisMoves extends DefaultMoves {
    public ArtemisMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                        FigureRepository figureRepository, MoveRepository moveRepository,
                        GameRepository gameRepository, GameService gameService, FigureService figureService)
    {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public ArrayList<Position> calculatePossiblePositions() {

        HashSet<Position> adjacentPositionsOfOrigin = new HashSet<>(super.calculatePossiblePositions());
        HashSet<Position> secondMovePositions = new HashSet<>(adjacentPositionsOfOrigin);
        Position initPosition = getOriginPosition(); // Position where the worker initially stands

        for (Position tmp : adjacentPositionsOfOrigin) {
            setOriginPosition(tmp);
            secondMovePositions.addAll(super.calculatePossiblePositions()); // Calculates the positions around tmp
        }

        // He must not move back to it's initial space
        adjacentPositionsOfOrigin.remove(initPosition);

        return new ArrayList<Position>(secondMovePositions);
    }


}


