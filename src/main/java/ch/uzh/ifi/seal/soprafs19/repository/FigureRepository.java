package ch.uzh.ifi.seal.soprafs19.repository;

import ch.uzh.ifi.seal.soprafs19.entity.Figure;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FigureRepository extends CrudRepository<Figure, Long> {
	Figure findById(long id);
	boolean existsById(long id);
	Iterable<Figure> findAllByGame(Game game);

    Iterable<Figure> findAllByGameAndOwnerId(Game game, long ownerId);
}
