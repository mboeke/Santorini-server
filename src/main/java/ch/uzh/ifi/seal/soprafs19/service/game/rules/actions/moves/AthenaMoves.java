package ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.moves;

import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.repository.GameRepository;
import ch.uzh.ifi.seal.soprafs19.repository.MoveRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.service.FigureService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.GameBoard;

public class AthenaMoves extends DefaultMoves {

    public AthenaMoves(Figure figure, GameBoard board, BuildingRepository buildingRepository,
                       FigureRepository figureRepository, MoveRepository moveRepository,
                       GameRepository gameRepository, GameService gameService, FigureService figureService) {
        super(figure, board, buildingRepository, figureRepository, moveRepository, gameRepository, gameService, figureService);
    }

    @Override
    public void perform()
    {
        if (getTargetPosition().getZ() - getOriginPosition().getZ() == 1) {
            game.setAthenaMovedUp(true);
        }

        else {
            game.setAthenaMovedUp(false);
        }


        getFigure().setPosition(getTargetPosition());
        figureRepository.save(getFigure());

        game.setLastActiveFigureId(getFigure().getId());
        gameRepository.save(game);

        handleWindCondition();
    }
}