package ch.uzh.ifi.seal.soprafs19.utilities;

import ch.uzh.ifi.seal.soprafs19.entity.BoardItem;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.repository.BuildingRepository;
import ch.uzh.ifi.seal.soprafs19.repository.FigureRepository;
import ch.uzh.ifi.seal.soprafs19.service.game.rules.turn.Turn;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameBoard {

	private final FigureRepository figureRepository;
	private final BuildingRepository buildingRepository;
	private Game game;

	private Map<Position, BoardItem> boardMap = new HashMap<>();

	private Map<Long, ArrayList<Figure>> figureMap = new HashMap<>();

	private Turn turn;

	public Game getGame() {return game;}

	public void setGame(Game game) {this.game = game;}

	@JsonIgnore
	public Map<Position, BoardItem> getBoardMap()
	{
		return boardMap;
	}

	@JsonProperty("board")
	public ArrayList<BoardItem> getBoardValues()
	/*
	 	returns the boardMap as HashMap with id as key and item as value.
	*/
	{
		ArrayList<BoardItem> items = new ArrayList<>();
		items.addAll(boardMap.values());
		return items;
	}

	public void setBoardMap(Map<Position, BoardItem> boardMap) {this.boardMap = boardMap;}

	@Autowired
	public GameBoard(Game game, FigureRepository figureRepository, BuildingRepository buildingRepository)
	{
		this.game = game;
		this.figureRepository = figureRepository;
		this.buildingRepository = buildingRepository;
		// Add all figures to the boardMap list
		figureRepository.findAllByGame(game).forEach(figure->{
			this.getBoardMap().put(figure.getPosition(), figure);
		});

		// Add all buildings to the boardMap list
		buildingRepository.findAllByGame(game).forEach(building->{
			this.getBoardMap().put(building.getPosition(), building);
		});
	}

	public Map<Long, ArrayList<Figure>> getFigureMap() {
		return figureMap;
	}

	public int figureCountPerOwner(long ownerId)
	{
		int count = 0;
		for (BoardItem item : getBoardMap().values()) {
			if (item instanceof Figure && item.getOwnerId() == ownerId) {
				count++;
			}
		}
		return count;
	}
}
