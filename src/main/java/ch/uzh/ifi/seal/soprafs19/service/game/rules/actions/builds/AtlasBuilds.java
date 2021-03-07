package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.builds;
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
import java.util.Iterator;
import java.util.List;


public class AtlasBuilds extends Action {

    public AtlasBuilds(
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
        // If the building is a dome, we must ensure, lower positions are occupied,
        // such that no build or move can be performed at this position
        if (getTargetPosition().isCeil()) {

            // The buildings lower neighbour
            Position tmp = new Position (getTargetPosition().getX(), getTargetPosition().getY(), getTargetPosition().getZ() - 1);

            while (tmp.hasValidAxis() && !getBoard().getBoardMap().containsKey(tmp)) {

                Building tmpBuilding = new Building();
                tmpBuilding.setGame(getBuilding().getGame());
                tmpBuilding.setOwnerId(getBuilding().getOwnerId());
                tmpBuilding.setPosition(tmp);
                buildingRepository.save(tmpBuilding);
                tmp.setZ(tmp.getZ() - 1);
            }
        }
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

     @Override
     public ArrayList<Position> calculateInvalidLowerPositions(ArrayList<Position> candidates)
     {
         ArrayList<Position> invalidPositions = new ArrayList<>();

         // For each candidate we must check, if it is floating, if it is ontop of a figure
         for (Iterator<Position> iterator = candidates.iterator(); iterator.hasNext(); ) {
             Position candidate = iterator.next();

             Position belowCandidate = new Position(
                     candidate.getX(),
                     candidate.getY(),
                     candidate.getZ() - 1 // Lower neighbour
             );

             // We can always build to floor positions
             if (candidate.isFloor()) {
                 continue;
             }

             // Candidates that are not ceil positions must be checked if floating
             if (!candidate.isCeil()) {

                 // If there is no board item at belowCandidate, the candidate is considered floating.
                 if (!getBoard().getBoardMap().containsKey(belowCandidate)) {
                     invalidPositions.add(candidate);
                     continue;
                 }

                 // If there is a board item at the belowCandidate position, it must be a building
                 BoardItem itemBelowCandidate = getBoard().getBoardMap().get(belowCandidate);
                 if (itemBelowCandidate instanceof Figure) {
                     invalidPositions.add(candidate);
                     continue;
                 }
             }

             // Else, ceil positions are treated differently. For each ceil, we must only ensure:
             // No figure is below there at ANY level.
             else {
                 while (belowCandidate.hasValidAxis()) {

                     // If there is a board item below candidate
                     if (getBoard().getBoardMap().containsKey(belowCandidate)) {
                         BoardItem item = getBoard().getBoardMap().get(belowCandidate);

                         // If it is of type figure, we can't build a dome at candidate
                         if (item instanceof Figure) {
                             invalidPositions.add(candidate);
                             break;
                         }
                     }
                     belowCandidate.setZ(belowCandidate.getZ() - 1);
                 }
             }

            // In both cases (ceil and !ceil) we must check that no dome is placed on top of candidate at ANY level
            if (getBoard().getBoardMap().containsKey(new Position (candidate.getX(), candidate.getY(), 3))) {
                invalidPositions.add(candidate);
                continue;
            }
         }

         return invalidPositions;
     }
}
