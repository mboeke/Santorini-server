package ch.uzh.ifi.seal.soprafs19.deserializers;

import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.UserRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GameDeserializer extends JsonDeserializer<Game> {
    private UserRepository userRepository;

    @Autowired
    public GameDeserializer(UserRepository userRepository) {this.userRepository = userRepository;}

    @Override
    public Game deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        /**
         * deserializes JSON body to a game object.
         */
        try {
            ObjectCodec oc = jp.getCodec();
            JsonNode node = oc.readTree(jp);

            final long userId1 = node.get("user1").asLong();
            final long userId2 = node.get("user2").asLong();
            final boolean isGodPower = node.get("isGodPower").asBoolean();

            User user1 = userRepository.findById(userId1);
            User user2 = userRepository.findById(userId2);

            Game newGame = new Game();
            newGame.setUser1(user1);
            newGame.setUser2(user2);
            newGame.setGodPower(isGodPower);

            if (isGodPower) {
                ArrayList<LinkedHashMap<String, Object>> godCardsList = new ObjectMapper().readValue(node.get("godCards").toString(), ArrayList.class);

                ArrayList<String> godCardIdentifiers = new ArrayList<>();

                for (LinkedHashMap<String, Object> godCard : godCardsList) {
                    godCardIdentifiers.add((String) godCard.get("name"));
                }
                newGame.setGodCardsList(godCardIdentifiers);
            }

            return newGame;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Game();
    }
}
