package de.uniks.se19.team_g.project_rbsg.ingame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uniks.se19.team_g.project_rbsg.SceneManager;
import de.uniks.se19.team_g.project_rbsg.ViewComponent;
import de.uniks.se19.team_g.project_rbsg.alert.AlertBuilder;
import de.uniks.se19.team_g.project_rbsg.ingame.battlefield.BattleFieldController;
import de.uniks.se19.team_g.project_rbsg.ingame.event.GameEventHandler;
import de.uniks.se19.team_g.project_rbsg.ingame.event.GameEventManager;
import de.uniks.se19.team_g.project_rbsg.ingame.model.ModelManager;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.WaitingRoomViewController;
import de.uniks.se19.team_g.project_rbsg.model.Game;
import javafx.scene.layout.StackPane;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
    IngameRootController.class
})
public class IngameRootControllerTest extends ApplicationTest {

    @Autowired
    IngameRootController sut;

    @MockBean
    ViewComponent<WaitingRoomViewController> waitingRoomComponent;

    @MockBean
    ViewComponent<BattleFieldController> battleFieldComponent;

    @MockBean
    IngameContext ingameContext;

    @MockBean
    GameEventManager gameEventManager;

    @MockBean
    ModelManager modelManager;

    @MockBean
    SceneManager sceneManager;

    @MockBean
    AlertBuilder alertBuilder;

    @Test
    public void test() throws Exception {

        Game gameData = new Game("1", 2);

        final WaitingRoomViewController waitingRoomViewController = mock(WaitingRoomViewController.class);
        final BattleFieldController battleFieldController = mock(BattleFieldController.class);

        when(waitingRoomComponent.getController()).thenReturn(waitingRoomViewController);
        when(waitingRoomComponent.getRoot()).thenReturn(new StackPane());
        when(battleFieldComponent.getController()).thenReturn(battleFieldController);
        when(battleFieldComponent.getRoot()).thenReturn(new StackPane());
        when(ingameContext.getGameData()).thenReturn(gameData);

        final ArrayList<GameEventHandler> gameEventHandlers = new ArrayList<>();
        doAnswer(invocation -> {
            gameEventHandlers.add(invocation.getArgument(0));
            return null;
        }).when(gameEventManager).addHandler(any());

        sut.root = new StackPane();
        sut.initialize(null, null);

        InOrder inOrder = inOrder(
                gameEventManager,
                ingameContext,
                waitingRoomViewController,
                battleFieldController
        );

        verify(gameEventManager).addHandler(modelManager);
        verify(ingameContext).setGameEventManager(gameEventManager);
        verify(waitingRoomComponent, atLeastOnce()).getController();
        verify(waitingRoomComponent, atLeastOnce()).getRoot();
        verify(battleFieldComponent, never()).getRoot();
        verify(battleFieldComponent, never()).getController();

        inOrder.verify(gameEventManager).setOnConnectionClosed(any());
        inOrder.verify(gameEventManager).startSocket(eq(gameData.getId()), any(), false);
        inOrder.verify(waitingRoomViewController).configure(ingameContext);

        //Termination

        sut.terminate();
        verify(gameEventManager).terminate();
        verify(ingameContext).tearDown();

        sut.onConnectionClosed();
        verify(alertBuilder).error(any(), any());

        // Game Events require platform run later
        de.uniks.se19.team_g.project_rbsg.ingame.model.Game gameState = new de.uniks.se19.team_g.project_rbsg.ingame.model.Game("1");
        when(modelManager.getGame()).thenReturn(gameState);

        final ObjectNode gameInitFinishedEvent = new ObjectMapper().readValue(
                "{\"action\":\"" + GameEventManager.GAME_INIT_FINISHED + "\"}",
                ObjectNode.class
        );
        gameEventHandlers.forEach(gameEventHandler -> gameEventHandler.handle(gameInitFinishedEvent));
        WaitForAsyncUtils.waitForFxEvents();

        verify(modelManager).getGame();
        verify(ingameContext).gameInitialized(gameState);

        verify(battleFieldComponent, never()).getRoot();

        final ObjectNode gameStartsEvent = new ObjectMapper().readValue(
                "{\"action\":\"" + GameEventManager.GAME_STARTS + "\"}",
                ObjectNode.class
        );
        gameEventHandlers.forEach(gameEventHandler -> gameEventHandler.handle(gameStartsEvent));
        WaitForAsyncUtils.waitForFxEvents();

        verify(battleFieldComponent, atLeastOnce()).getController();
        verify(battleFieldComponent, atLeastOnce()).getRoot();
        inOrder.verify(battleFieldController).configure(ingameContext);

    }
}