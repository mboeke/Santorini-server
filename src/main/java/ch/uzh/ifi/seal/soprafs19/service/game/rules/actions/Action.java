package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions;

import ch.uzh.ifi.seal.soprafs19.entity.BoardItem;
import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.GameBoard;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Action {

    private Figure figure;
    private Building building;
    private GameBoard board;
    protected Game game;
    private Position originPosition;
    private Position targetPosition;
    protected final BuildingRepository buildingRepository;
    protected final FigureRepository figureRepository;
    protected final MoveRepository moveRepository;
    protected final GameRepository gameRepository;
    protected final GameService gameService;
    protected final FigureService figureService;

    public Action(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                  FigureRepository figureRepository, MoveRepository moveRepository,
                  GameRepository gameRepository, GameService gameService, FigureService figureService)
    {
        this.figure = figure;
        this.board = board;
        this.game = board.getGame();
        this.originPosition = figure.getPosition();
        this.buildingRepository = buildingRepository;
        this.figureRepository = figureRepository;
        this.moveRepository = moveRepository;
        this.gameRepository = gameRepository;
        this.gameService = gameService;
        this.figureService = figureService;
    }

    public abstract List<Position> calculatePossiblePositions();
    public abstract void perform();

    /* @param inBounds: lowerX, upperX, lowerY, upperY, lowerZ, upperZ
     * returns a list of adjacent positions that lay in the area around origin spanned by inBounds
     */
    protected ArrayList<Position> calculatePositionsInNeighbourhood(int [] inBounds)
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

        return adjacentPositions;
    }

    /*
     * returns a list of invalid positions where one can not move-to or build on, given a list of positions.
     */
    public ArrayList<Position> calculateInvalidLowerPositions(ArrayList<Position> candidates) {
        ArrayList<Position> invalidPositions = new ArrayList<>();

        // For all positions higher than level 0 z in {1,2,3} check if the field below has a building, else remove the original field
        for (Iterator<Position> iterator = candidates.iterator(); iterator.hasNext(); ) {
            Position candidate = iterator.next();

            // We can always move to floor positions
            if (candidate.isFloor()) {
                continue;
            }

            Position belowCandidate = new Position(
                    candidate.getX(),
                    candidate.getY(),
                    candidate.getZ() - 1 // Lower neighbour
            );

            // If there is no board item at belowCandidate, the candidate is considered invalid.
            if (!getBoard().getBoardMap().containsKey(belowCandidate)) {
                invalidPositions.add(candidate);
            }

            // If there is a board item at the belowCandidate position, it must be a building
            BoardItem itemBelowCandidate = getBoard().getBoardMap().get(belowCandidate);
            if (itemBelowCandidate instanceof Figure) {
                invalidPositions.add(candidate);
            }
        }

        return invalidPositions;
    }


    /*
     * deletes all positions which are already occupied by a board item
     */
    protected void stripOccupiedPositions(ArrayList<Position> candidates)
    {
        candidates.removeAll(getBoard().getBoardMap().keySet());
    }

    /*
     * deletes all positions which don't are floor positions or don't have a building below them
     */
    protected void stripFloatingPositions(ArrayList<Position> candidates)
    {
        candidates.removeAll(calculateInvalidLowerPositions(candidates));
    }

    protected Figure getFigure() {
        return figure;
    }

    protected GameBoard getBoard() {
        return board;
    }

    protected Position getOriginPosition() {
        return originPosition;
    }

    protected void setOriginPosition(Position position) {
        this.originPosition = position;
    }

    public Position getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(Position targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setBuilding(Building building)
    {
        this.building = building;
    }

    public Building getBuilding() {
        return building;
    }
}
