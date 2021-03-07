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


public class HermesMoves extends DefaultMoves {


    public HermesMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                       FigureRepository figureRepository, MoveRepository moveRepository,
                       GameRepository gameRepository, GameService gameService, FigureService figureService) {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);

    }

    @Override
    public ArrayList<Position> calculatePossiblePositions() {

        int[] neighbourhood = {-1, 1, -1, 1, -3, 1};
        int[] neighbourhoodD = {-1, 1, -1, 1, 0, 0};// LowerX, UpperX, LowerY, UpperY, LowerZ, UpperZ
        HashSet<Position> adjacentPositionsOfOrigin =  new HashSet<>();

        // If athena moved up, we restrict moving
        if (game.getAthenaMovedUp()) {
            neighbourhood[5] = 0;
        }

        adjacentPositionsOfOrigin.addAll(calculatePositionsInNeighbourhoodOfHermes(neighbourhoodD));

        // positions around origin
        int i = 0;
        while (i < 15) {
            adjacentPositionsOfOrigin.addAll(calculatePossiblePositions2(adjacentPositionsOfOrigin));
            i++;
        }

        adjacentPositionsOfOrigin.addAll(calculatePositionsInNeighbourhoodOfHermes(neighbourhood));
        adjacentPositionsOfOrigin.add(getOriginPosition());

        return new ArrayList<Position>(adjacentPositionsOfOrigin);
    }

    public HashSet<Position> calculatePossiblePositions2(HashSet<Position> candidates) {
        HashSet<Position>tmp =new HashSet<>();

        for (Position candidate: candidates )
        {tmp.addAll((calculatePositionsOfCandidates(candidate)));
        }

        return tmp;
    }

    public ArrayList<Position> calculatePositionsOfCandidates(Position position) {
        ArrayList<Position> adjacentPositions = new ArrayList<>();

        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                if (dx != 0 || dy != 0) {
                    if (dx == 0 && dy == 0) { // moving up/down along the z-axis ONLY is not allowed
                        continue;
                    }

                    Position tmp = new Position(
                            position.getX() + dx,
                            position.getY() + dy,
                            position.getZ()
                    );

                    if (tmp.hasValidAxis()) {
                        adjacentPositions.add(tmp);
                    }
                }
            }
        }
        stripOccupiedPositions(adjacentPositions);

        // Strip out the positions that are floating and have no building below
        stripFloatingPositions(adjacentPositions);


        return adjacentPositions;
    }

    public ArrayList<Position> calculatePositionsInNeighbourhoodOfHermes(int [] inBounds)
    {
        ArrayList<Position> adjacentPositions = new ArrayList<>();

        for (int dx = inBounds[0]; dx <= inBounds[1]; ++dx) {
            for (int dy = inBounds[2]; dy <= inBounds[3]; ++dy) {
                for (int dz = inBounds[4]; dz <= inBounds[5]; ++dz) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        if (dx == 0 && dy == 0) { // moving up/down along the z-axis ONLY is not allowed
                            continue;
                        }

                        Position tmp = new Position(
                                getOriginPosition().getX() + dx,
                                getOriginPosition().getY() + dy,
                                getOriginPosition().getZ() + dz
                        );

                        if (tmp.hasValidAxis()) {
                            adjacentPositions.add(tmp);
                        }
                    }
                }
            }
        }
        stripOccupiedPositions(adjacentPositions);

        // Strip out the positions that are floating and have no building below
        stripFloatingPositions(adjacentPositions);

        return adjacentPositions;
    }
}