package ch.uzh.ifi.seal.soprafs19.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Game rule violation")
public class GameRuleException extends Exception{

    public GameRuleException() {
        super("Game rule violation");
    }

//    public GameRuleException(String message) {
//        super(message);
//    }
}
