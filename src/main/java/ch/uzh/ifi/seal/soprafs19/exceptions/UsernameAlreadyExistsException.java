package ch.uzh.ifi.seal.soprafs19.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Username already taken.")
public class UsernameAlreadyExistsException extends Exception{

    public UsernameAlreadyExistsException() {
        super("Username already taken.");
    }
}
