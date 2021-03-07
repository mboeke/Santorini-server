package ch.uzh.ifi.seal.soprafs19.service.game.rules.turn;
import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.utilities.GameBoard;

import java.util.ArrayList;

public class DefaultTurn extends Turn {


    public DefaultTurn(GameBoard board, MoveRepository moveRepository, BuildingRepository buildingRepository, FigureRepository figureRepository, GameRepository gameRepository) {
        super(board, moveRepository, buildingRepository, figureRepository, gameRepository);

        // For Demter and Hephaestus we need to set the flag canFinishTurn
        ArrayList<String> godsCanFinishTurn = new ArrayList<>();
        godsCanFinishTurn.add("demeter");
        godsCanFinishTurn.add("hephaestus");
        String currentGod = game.getCurrentTurn().equals(game.getUser1()) ? game.getGod1() : game.getGod2();

        // If the current turn has either demeter or hephaestus and they completed the first build
        if (godsCanFinishTurn.contains(currentGod) && firstBuildCompleted()) {
            game.setCanFinishTurn(true);
            gameRepository.save(game);
        }
        // Else, we must reset it.
        else {
            game.setCanFinishTurn(false);
            gameRepository.save(game);
        }
    }

    @Override
    public boolean isBuildAllowedByUserId(long userId) {
        return isCurrentTurn(userId);
    }

    @Override
    public boolean isMoveAllowedByUserId(long userId)
    {
        return isCurrentTurn(userId) && game.getLastActiveFigureId() == 0;
    }

    private boolean firstBuildCompleted()
    {
        ArrayList<Building> lastBuildings = (ArrayList<Building>) buildingRepository
                .findTop2ByGameOrderByCreatedOnDesc(game);

        // If it's the first build
        if (lastBuildings.isEmpty()) {
            return false;
        }
        // Get the last inserted building
        Building lastBuilding = lastBuildings.get(0);

        return lastBuilding.getOwnerId() == game.getCurrentTurn().getId();
    }
}
