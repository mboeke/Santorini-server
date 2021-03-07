package ch.uzh.ifi.seal.soprafs19.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Resource action not allowed")
public class ResourceActionNotAllowedException extends Exception{

    public ResourceActionNotAllowedException() {
        super("Resource action not allowed");
    }

    public ResourceActionNotAllowedException(String message) {
        super(message);
    }
}
