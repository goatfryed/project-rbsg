package de.uniks.se19.team_g.project_rbsg.lobby.core.ui;

import de.uniks.se19.team_g.project_rbsg.MusicManager;
import de.uniks.se19.team_g.project_rbsg.scene.SceneManager;
import de.uniks.se19.team_g.project_rbsg.scene.ViewComponent;
import de.uniks.se19.team_g.project_rbsg.overlay.alert.AlertBuilder;
import de.uniks.se19.team_g.project_rbsg.chat.ChatClient;
import de.uniks.se19.team_g.project_rbsg.chat.ChatController;
import de.uniks.se19.team_g.project_rbsg.chat.command.ChatCommandManager;
import de.uniks.se19.team_g.project_rbsg.chat.ui.ChatBuilder;
import de.uniks.se19.team_g.project_rbsg.chat.ui.ChatTabManager;
import de.uniks.se19.team_g.project_rbsg.configuration.ApplicationState;
import de.uniks.se19.team_g.project_rbsg.configuration.FXMLLoaderFactory;
import de.uniks.se19.team_g.project_rbsg.configuration.LocaleConfig;
import de.uniks.se19.team_g.project_rbsg.configuration.SceneManagerConfig;
import de.uniks.se19.team_g.project_rbsg.lobby.chat.LobbyChatClient;
import de.uniks.se19.team_g.project_rbsg.lobby.core.EmailManager;
import de.uniks.se19.team_g.project_rbsg.lobby.core.PlayerManager;
import de.uniks.se19.team_g.project_rbsg.overlay.credits.Credits;
import de.uniks.se19.team_g.project_rbsg.overlay.credits.CreditsBuilder;
import de.uniks.se19.team_g.project_rbsg.lobby.game.CreateGameController;
import de.uniks.se19.team_g.project_rbsg.lobby.game.CreateGameFormBuilder;
import de.uniks.se19.team_g.project_rbsg.lobby.game.GameManager;
import de.uniks.se19.team_g.project_rbsg.lobby.model.Lobby;
import de.uniks.se19.team_g.project_rbsg.lobby.model.Player;
import de.uniks.se19.team_g.project_rbsg.lobby.system.SystemMessageManager;
import de.uniks.se19.team_g.project_rbsg.model.Game;
import de.uniks.se19.team_g.project_rbsg.model.GameProvider;
import de.uniks.se19.team_g.project_rbsg.model.UserProvider;
import de.uniks.se19.team_g.project_rbsg.overlay.menu.MenuBuilder;
import de.uniks.se19.team_g.project_rbsg.server.rest.DefaultLogoutManager;
import de.uniks.se19.team_g.project_rbsg.server.rest.JoinGameManager;
import de.uniks.se19.team_g.project_rbsg.server.rest.LogoutManager;
import de.uniks.se19.team_g.project_rbsg.server.rest.RESTClient;
import de.uniks.se19.team_g.project_rbsg.server.websocket.WebSocketClient;
import io.rincl.Rincl;
import io.rincl.resourcebundle.ResourceBundleResourceI18nConcern;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Georg Siebert
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        FXMLLoaderFactory.class,
        GameListTest.ContextConfiguration.class,
        ChatBuilder.class,
        GameProvider.class,
        UserProvider.class,
        SceneManager.class,
        JoinGameManager.class,
        CreateGameFormBuilder.class,
        MenuBuilder.class,
        LocaleConfig.class,
        CreditsBuilder.class,
        LobbyViewController.class,
        MusicManager.class,
        ApplicationState.class,
        SceneManagerConfig.class,
        AlertBuilder.class,
        GameListViewCell.class,
        LocaleConfig.class,
        FXMLLoaderFactory.class,
        EmailManager.class
})
public class GameListTest extends ApplicationTest
{

    public static final Game GAME_OF_HELLO = new Game("1", "GameOfHello", 4, 2);
    public static final Game DEFENCE_OF_THE_ANCIENT = new Game("2", "DefenceOfTheAncient", 10, 7);
    @Autowired
    private ApplicationContext context;

    private LobbyViewController lobbyViewController;

    @Override
    public void start(Stage stage)
    {
        Rincl.setDefaultResourceI18nConcern(new ResourceBundleResourceI18nConcern());
        @SuppressWarnings("unchecked")
        ViewComponent<LobbyViewController> components = (ViewComponent<LobbyViewController>) context.getBean("lobbyScene");

        final Scene scene = new Scene(components.getRoot(),1200 ,840);

        stage.setScene(scene);
        stage.show();
        stage.toFront();

        lobbyViewController = components.getController();
    }

    @Override
    public void stop() throws Exception
    {
        FxToolkit.hideStage();
    }

    @Test
    public void addItemsAndRemove()
    {

        ListView<Game> gamesListView = lookup("#lobbyGamesListView").queryListView();
        assertNotNull(gamesListView);

        ObservableList<Game> games = gamesListView.getItems();
        assertNotNull(games);
        assertEquals(2, games.size());

        ListCell<Game> gameOfHello = lookup("#lobbyGamesListView .list-cell").nth(0).query();
        ListCell<Game> gameOfDota = lookup("#lobbyGamesListView .list-cell").nth(1).query();

        assertEquals(GAME_OF_HELLO, gameOfHello.getItem());
        assertEquals(DEFENCE_OF_THE_ANCIENT, gameOfDota.getItem());

        Lobby lobby = lobbyViewController.getLobby();

        final Game starWars1 = new Game("3", "StarWars", 2, 2);
        lobby.addGame(starWars1);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(3, games.size());

        ListCell<Game> gameStarWars = lookup("#lobbyGamesListView .list-cell").nth(2).query();

        assertEquals(starWars1, gameStarWars.getItem());


        lobby.removeGame(DEFENCE_OF_THE_ANCIENT);
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(2, games.size());
    }

    @TestConfiguration
    public static class ContextConfiguration {

        @Bean
        public CreateGameController createGameController()
        {
            return Mockito.mock(CreateGameController.class);
        }

        @Bean
        public Credits creditsController()
        {
            return Mockito.mock(Credits.class);
        }

        @Bean
        public LogoutManager logoutManager() {
            return new DefaultLogoutManager(new RESTClient(new RestTemplate()));
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
                    games.add(GAME_OF_HELLO);
                    games.add(DEFENCE_OF_THE_ANCIENT);
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
        public ChatController chatController() {
            return  new ChatController(new UserProvider(), new ChatCommandManager(), new ChatTabManager()) {
                @Override
                public void init(@NonNull final TabPane chatPane, @NonNull final ChatClient chatClient)
                {
                }
            };
        }

        @Bean
        public LobbyChatClient lobbyChatClient() {
            return new LobbyChatClient(new WebSocketClient(), new UserProvider()) {
                @Override
                public void startChatClient(@NonNull final ChatController chatController) {
                }
            };
        }
    }

}
