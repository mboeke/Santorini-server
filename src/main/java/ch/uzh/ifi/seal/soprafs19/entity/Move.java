package ch.uzh.ifi.seal.soprafs19.entity;
import ch.uzh.ifi.seal.soprafs19.utilities.Position;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
public class Move implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue
    private Long id;

	public Figure getFigure() {
		return figure;
	}

	public void setFigure(Figure figure) {
		this.figure = figure;
	}

	public int getTurnId() {
		return turnId;
	}

	public void setTurnId(int turnId) {
		this.turnId = turnId;
	}

	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	@ManyToOne
    private Figure figure;

    private int turnId;

    private long gameId;

    private int x;

    private int y;

    private int z;

	@Column(nullable = false, updatable = false)
	@CreationTimestamp
	private LocalDateTime createdOn;

	/*
	 * Getters and Setters
	 */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getCreatedOn() {return createdOn;}

    public Position getPosition()
    {
        return new Position(this.x, this.y, this.z);
    }

    public void setPosition(Position position)
    {
        this.x = position.getX();
        this.y = position.getY();
        this.z = position.getZ();
    }

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Move)) {
			return false;
		}
		Move user = (Move) o;
		return this.getId().equals(user.getId());
	}
}
