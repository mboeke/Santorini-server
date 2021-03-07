package ch.uzh.ifi.seal.soprafs19.repository;

import ch.uzh.ifi.seal.soprafs19.entity.Building;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends CrudRepository<Building, Long> {
	Building findById(long id);
	boolean existsById(long id);
	Iterable<Building> findAllByGame(Game game);
	Iterable<Building> findTop2ByGameOrderByCreatedOnDesc(Game game);
}
