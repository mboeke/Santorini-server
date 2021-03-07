package ch.uzh.ifi.seal.soprafs19;

import ch.uzh.ifi.seal.soprafs19.constant.GameStatus;
import ch.uzh.ifi.seal.soprafs19.constant.UserStatus;
import ch.uzh.ifi.seal.soprafs19.entity.Game;
import ch.uzh.ifi.seal.soprafs19.entity.User;
import ch.uzh.ifi.seal.soprafs19.repository.*;
import ch.uzh.ifi.seal.soprafs19.service.UserService;
import ch.uzh.ifi.seal.soprafs19.service.game.service.GameService;
import ch.uzh.ifi.seal.soprafs19.utilities.AuthenticationService;
import ch.uzh.ifi.seal.soprafs19.utilities.Utilities;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.SimpleDateFormat;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
            }
        };
    }

    @Bean
    public CommandLineRunner demo(UserRepository userRepository, GameRepository gameRepository, FigureRepository figureRepository, MoveRepository moveRepository, BuildingRepository buildingRepository, AuthenticationService authentication, Utilities utils) {
        return (args) -> {
            UserService userService = new UserService(userRepository, authentication, utils);
            // save a couple of Users
            String[] testUsers = {"julius", "areg", "max", "tobi", "stewie", "peter", "brian", "louise", "wilson", "doris"};
            for (String username : testUsers) {
                User user = new User();
                user.setUsername(username);
                user.setName(username.toUpperCase());
                user.setPassword("admin");
                user.setBirthday(new SimpleDateFormat("yy-MM-dd").parse("1948-04-06"));
                userService.postCreateUser(user);
            }

            GameService gameService = new GameService(gameRepository, figureRepository, moveRepository, buildingRepository, userRepository, userService);
            // save a few games
            GameStatus[] gameStatuses = {GameStatus.INITIALIZED, GameStatus.STARTED, GameStatus.CANCELED};
            for (int i = 0; i < 3; i++) {
                Game game = new Game();
                User user1 = userRepository.findByUsername(testUsers[i]);
                User user2 = userRepository.findByUsername(testUsers[i+2]);
                user1.setStatus(UserStatus.ONLINE);
                user2.setStatus(UserStatus.ONLINE);
                game.setUser1(user1);
                game.setUser2(user2);
                game.setCurrentTurn(user2);
                game.setGodPower(false);
                gameService.postCreateGame(game);
            }
        };
    }
}
