package de.uniks.se19.team_g.project_rbsg.lobby.core.ui;

import de.uniks.se19.team_g.project_rbsg.*;
import de.uniks.se19.team_g.project_rbsg.configuration.*;
import de.uniks.se19.team_g.project_rbsg.lobby.chat.*;
import de.uniks.se19.team_g.project_rbsg.lobby.chat.ui.*;
import de.uniks.se19.team_g.project_rbsg.lobby.core.*;
import de.uniks.se19.team_g.project_rbsg.lobby.game.*;
import de.uniks.se19.team_g.project_rbsg.lobby.model.*;
import de.uniks.se19.team_g.project_rbsg.lobby.system.*;
import de.uniks.se19.team_g.project_rbsg.model.*;
import de.uniks.se19.team_g.project_rbsg.server.rest.*;
import de.uniks.se19.team_g.project_rbsg.server.websocket.*;
import io.rincl.*;
import io.rincl.resourcebundle.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.*;
import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.*;
import org.springframework.context.annotation.*;
import org.springframework.lang.*;
import org.springframework.test.context.*;
import org.springframework.test.context.junit4.*;
import org.springframework.web.client.*;
import org.testfx.framework.junit.*;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        PlayerJoinedGameListTest.ContextConfiguration.class,
        ChatBuilder.class,
        GameProvider.class,
        UserProvider.class,
        SceneManager.class,
        JoinGameManager.class,
        CreateGameFormBuilder.class,
        CreateGameController.class,
        LobbyViewBuilder.class,
        LobbyViewController.class,
        FXMLLoaderFactory.class
        }
)
public class PlayerJoinedGameListTest extends ApplicationTest
{
    @Autowired
    private ApplicationContext context;

    private Lobby lobby;

    @TestConfiguration
    public static class ContextConfiguration implements ApplicationContextAware{
        private ApplicationContext context;

        @Bean
        @Scope("prototype")
        public FXMLLoader fxmlLoader() {
            FXMLLoader loader = new FXMLLoader();
            loader.setControllerFactory(this.context::getBean);
            return loader;
        }

        @Bean
        public GameManager gameManager()
        {
            return new GameManager(new RESTClient(new RestTemplate()), new UserProvider())
            {
                @Override
                public Collection<Game> getGames()
                {
                    ArrayList<Game> games = new ArrayList<>();
                    games.add(new Game("1", "game1", 2, 0));
                    return games;
                }
            };
        }

        @Bean
        public PlayerManager playerManager()
        {
            return new PlayerManager(new RESTClient(new RestTemplate()), new UserProvider())
            {
                @Override
                public Collection<Player> getPlayers()
                {
                    return new ArrayList<Player>();
                }
            };
        }

        @Bean
        public SystemMessageManager systemMessageManager()
        {
            return new SystemMessageManager(new WebSocketClient())
            {
                @Override
                public void startSocket()
                {
                }
            };
        }

        @Bean
        public ChatController chatController()
        {
            return new ChatController(new UserProvider(), new WebSocketClient(), new ChatWebSocketCallback())
            {
                @Override
                public void init(@NonNull final TabPane chatPane)
                {
                }

            };
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
        {
            this.context = applicationContext;
        }
    }

    @Override
    public void start(Stage stage) {
        Rincl.setDefaultResourceI18nConcern(new ResourceBundleResourceI18nConcern());
        LobbyViewBuilder lobbyViewBuilder = context.getBean(LobbyViewBuilder.class);

        Parent parent = (Parent) lobbyViewBuilder.buildLobbyScene();
        Scene scene = new Scene(parent, 1200, 840);
        stage.setScene(scene);
        stage.show();
        stage.toFront();

        lobby = lobbyViewBuilder.getLobbyViewController().getLobby();
    }

    @Test
    public void TestGameList() throws IOException
    {
        Label playersLabel = lookup("#gameCellPlayersLabelgame1").query();
        assertEquals("0/2", playersLabel.textProperty().get());

        assertNotNull(lobby);
        Game gameOne = lobby.getGameOverId("1");

        assertNotNull(gameOne);

        gameOne.setJoinedPlayer(1);

        sleep(100);

        assertEquals("1/2", playersLabel.textProperty().get());

    }
}