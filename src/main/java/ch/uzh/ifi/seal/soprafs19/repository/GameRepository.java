package ch.uzh.ifi.seal.soprafs19.repository;

import ch.uzh.ifi.seal.soprafs19.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends CrudRepository<Game, Long> {
	Game findById(long id);
	boolean existsById(long id);
	Iterable<Game> findByUser2AndStatus(User user2, GameStatus status);
	Iterable<Game> findAllByUser1OrUser2(User user1, User user2);
}
