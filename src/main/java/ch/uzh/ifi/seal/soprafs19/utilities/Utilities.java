package ch.uzh.ifi.seal.soprafs19.utilities;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional

public class Utilities {


    public void copyAttributes(User fromUser, User toUser) {
        String username = fromUser.getUsername();
        String password = fromUser.getPassword();

        if (!(username == null || username.isEmpty())) {
            toUser.setUsername(username);
        }

        if (!(password == null || password.isEmpty())) {
            toUser.setPassword(password);
        }

        toUser.setName(fromUser.getName());
        toUser.setBirthday(fromUser.getBirthday());
    }
}
