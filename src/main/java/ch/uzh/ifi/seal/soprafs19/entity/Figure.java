package ch.uzh.ifi.seal.soprafs19.entity;
import ch.uzh.ifi.seal.soprafs19.service.game.rules.actions.Action;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Collection;

@Entity
@DynamicUpdate
public class Figure extends BoardItem implements Serializable {

	@Transient
	private Action moves;

	@Transient
	private Action builds;

	private static final long serialVersionUID = 1L;

	@JsonIgnore
	public Collection<Position> getPossibleMoves() {
		return moves.calculatePossiblePositions();
	}

	@JsonIgnore
	public void moveTo(Position destination)
	{
		moves.setTargetPosition(destination);
		moves.perform();
	}

	@JsonIgnore
	public void build(Building building) {
		builds.setTargetPosition(building.getPosition());
		builds.setBuilding(building);
		builds.perform();
	}

	@JsonIgnore
	public Collection<Position> getPossibleBuilds() { return builds.calculatePossiblePositions(); }

	@JsonIgnore
	public void setMoves(Action moves) {
		this.moves = moves;
	}

	@JsonIgnore
	public Action getMoveAction()
	{return this.moves;}

	@JsonIgnore
	public Action getBuildAction()
	{return this.builds;}

	@JsonIgnore
	public void setBuilds(Action builds) {
		this.builds = builds;
	}
}
