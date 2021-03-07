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

public class MinotaurMoves extends Action {

    public MinotaurMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
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

    // Minotaur can move into a space of the opponent figure, if the position behind it is free
    // If the position behind it is free, it pushes the opponent figure back and occupies its space
    @Override
    public void perform() {
        // Move to the target position
        getFigure().setPosition(getTargetPosition());
        figureRepository.save(getFigure());

        // If the target position was occupied by an opponent's figure,
        // we must move it to the escape position.
        if (getBoard().getBoardMap().containsKey(getTargetPosition())) {
            BoardItem item = getBoard().getBoardMap().get(getTargetPosition());

            // Check if the occupied position is an opponent's figure
            if (item instanceof Figure && item.getOwnerId() != game.getCurrentTurn().getId()) {
                Figure opponentFigure = (Figure)item;

                // Place the opponent's figure to the escape position
                opponentFigure.setPosition(calculateEscapePosition(getTargetPosition(), moveDirection(getOriginPosition(), getTargetPosition())));
                figureRepository.save(opponentFigure);
            }
        }

        game.setLastActiveFigureId(getFigure().getId());
        gameRepository.save(game);

        handleWindCondition();
    }

    // Minotaur may move to a position occupied by an opponent's figure, if the position behind it is free
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
                continue;
            }

            // If the figure is owned by the current user we must add it to the list
            if (item.getOwnerId() == game.getCurrentTurn().getId()) {
                positionsToRemove.add(position);
                continue;
            }

            // If the figure's opponent is not  north, east, south or west of origin,
            // We must add it to the list.
            if (!isNESWOfOrigin(position)) {
                positionsToRemove.add(position);
                continue;
            }

            // The position occupied by the opponent's figure is either N,E,S,W of origin.
            // For each N,E,S,W we must check if the opponent's figure can move one step in the according direction.
            // The position where the opponent's figure would be moved to is called posOfEscape
            Position posOfEscape = null;

            posOfEscape = calculateEscapePosition(position, moveDirection(getOriginPosition(), position));

            if (posOfEscape == null) {
                positionsToRemove.add(position);
                continue;
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

    private boolean isNESWOfOrigin(Position position)
    {
        Position p = position;
        Position o = getOriginPosition();
        return p.isNorthTo(o) || p.isSouthTo(o) || p.isEastTo(o) || p.isWestTo(o);
    }


    // Calculates an escape position in a direction [N, E, S, W]
    private Position calculateEscapePosition(Position position, String direction)
    {
        direction = direction.toUpperCase();
        Position posOfEscape = new Position(position.getX(), position.getY(), position.getZ());

        switch (direction) {
            case "N":
                posOfEscape.setY(posOfEscape.getY() + 1 );
                break;
            case "E":
                posOfEscape.setX(posOfEscape.getX() + 1 );
                break;
            case "S":
                posOfEscape.setY(posOfEscape.getY() - 1 );
                break;
            case "W":
                posOfEscape.setX(posOfEscape.getX() - 1 );
                break;
            default:
                return null;
        }

        // Now we must assign the first free z to the escape position
        posOfEscape.setZ(findFirstFreeZ(posOfEscape.getX(), posOfEscape.getY()));

        // Now that we have our escape position we must validate it

        // Check if it's in the game board
        if (!posOfEscape.hasValidAxis()) {
            return null;
        }

        // Check if we can move onto the position by using the default moves object
        DefaultMoves tmpMoves = new DefaultMoves(getFigure(), getBoard(), buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
        ArrayList<Position> posOfEscapeList = new ArrayList<>();
        posOfEscapeList.add(posOfEscape);

        // We must ensure that the escape position is not ontop another figure.
        posOfEscapeList.removeAll(tmpMoves.calculateInvalidLowerPositions(posOfEscapeList));

        // If the escape position is still in the list, return it
       return posOfEscapeList.contains(posOfEscape) ? posOfEscape : null;
    }

    private int findFirstFreeZ(int x, int y)
    {
        int z = 0;
        Position tmp = new Position(x, y, z);

        // While the board contains the cand position, it is not free and we must increase z
        while (getBoard().getBoardMap().containsKey(tmp)) {
            z++;
            tmp.setZ(z);
        }

        return z;
    }

    private String moveDirection(Position origin, Position target)
    {
        Position t = target;
        Position o = origin;
        String direction = t.isNorthTo(o) ? "N" : t.isEastTo(o) ? "E" :
                           t.isSouthTo(o) ? "S" : t.isWestTo(o) ? "W" : null;

        return direction;
    }
}
