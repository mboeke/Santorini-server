package ch.uzh.ifi.seal.soprafs19.repository;
import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.Move;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;

@Repository
public interface MoveRepository extends CrudRepository<Move, Long> {
	Move findById(long id);
	boolean existsById(long id);
	Iterable<Move> findAllByTurnId(int turnId);
    Iterable<Move> findAllByFigure(Figure figure);
    Iterable<Move> findAllByGameId(long gameId);
}
