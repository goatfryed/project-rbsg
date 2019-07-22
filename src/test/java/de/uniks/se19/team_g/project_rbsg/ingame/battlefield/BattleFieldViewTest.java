package de.uniks.se19.team_g.project_rbsg.ingame.battlefield;

import de.uniks.se19.team_g.project_rbsg.SceneManager;
import de.uniks.se19.team_g.project_rbsg.ViewComponent;
import de.uniks.se19.team_g.project_rbsg.alert.AlertBuilder;
import de.uniks.se19.team_g.project_rbsg.configuration.FXMLLoaderFactory;
import de.uniks.se19.team_g.project_rbsg.ingame.IngameConfig;
import de.uniks.se19.team_g.project_rbsg.ingame.IngameContext;
import de.uniks.se19.team_g.project_rbsg.ingame.event.CommandBuilder;
import de.uniks.se19.team_g.project_rbsg.ingame.event.GameEventManager;
import de.uniks.se19.team_g.project_rbsg.ingame.model.*;
import de.uniks.se19.team_g.project_rbsg.model.GameProvider;
import de.uniks.se19.team_g.project_rbsg.model.IngameGameProvider;
import de.uniks.se19.team_g.project_rbsg.model.User;
import de.uniks.se19.team_g.project_rbsg.model.UserProvider;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.robot.Motion;
import org.testfx.util.WaitForAsyncUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author  Keanu Stückrad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        FXMLLoaderFactory.class,
        BattleFieldController.class,
        IngameConfig.class
})
public class BattleFieldViewTest extends ApplicationTest {


    public static final int BASE_X = 150;
    public static final int BASE_Y = 50;
    private ViewComponent<BattleFieldController> battleFieldComponent;

    @SpyBean
    SceneManager sceneManager;

    @MockBean
    AlertBuilder alertBuilder;

    @MockBean
    MovementManager movementManager;

    @Autowired
    ObjectFactory<ViewComponent<BattleFieldController>> battleFieldFactory;


    @Override
    public void start(@NonNull final Stage ignored) {
        battleFieldComponent = battleFieldFactory.getObject();
    }

    @Test
    public void testBuildIngameView() throws IOException, ExecutionException, InterruptedException {

        IngameGameProvider ingameGameProvider= new IngameGameProvider();
        ingameGameProvider.set(buildComplexTestGame());


        Node ingameView = battleFieldComponent.getRoot();

        GameProvider gameDataProvider = new GameProvider();
        gameDataProvider.set(new de.uniks.se19.team_g.project_rbsg.model.Game("test", 4));


        UserProvider userProvider = new UserProvider();
        userProvider.set(new User().setName("TestUser"));

        IngameContext context = new IngameContext(userProvider, gameDataProvider, ingameGameProvider);

        revealBattleField(context);

        Assert.assertNotNull(ingameView);
        Canvas canvas = lookup("#canvas").query();
        Assert.assertNotNull(canvas);
        Button leave = lookup("#leaveButton").query();
        Assert.assertNotNull(leave);
        clickOn("#leaveButton");
        Button zoomOut = lookup("#zoomOutButton").query();
        Assert.assertNotNull(zoomOut);
        clickOn("#zoomOutButton");
        clickOn("#zoomOutButton");
        Button zoomIn = lookup("#zoomInButton").query();
        Assert.assertNotNull(zoomIn);
        clickOn("#zoomInButton");
        clickOn("#zoomInButton");
        clickOn("#zoomInButton");
        clickOn("#zoomOutButton");

        Button endPhaseButton = lookup("#endPhaseButton").query();
        Assert.assertNotNull(endPhaseButton);


        Game game = ingameGameProvider.get();
        Unit unit = new Unit("10");
        unit.setHp(10);
        unit.setMp(10);
        unit.setUnitType(UnitType.CHOPPER);
        unit.setGame(game);
        unit.setPosition(game.getCells().get(11));

        game.getUnits().get(0).setPosition(ingameGameProvider.get().getCells().get(12));

        click(25, 100);
        click(25, 150);
        click(75, 150);

        Button button = lookup("#mapButton").query();
        clickOn("#mapButton");
        clickOn("#mapButton");
    }


    private void click(double x, double y) {
        clickOn(BASE_X + x, BASE_Y + y, Motion.DIRECT);
    }

    @Test
    public void testMovement() throws ExecutionException, InterruptedException {
        TestGameBuilder.Definition definition = TestGameBuilder.sampleGameAlpha();
        Game game = definition.game;
        Unit playerUnit = definition.playerUnit;

        GameEventManager gameEventManager = Mockito.mock(GameEventManager.class);

        User user = new User();
        user.setName("Karli");
        Player player = new Player("Karli").setName("Karli");
        game.withPlayer(player);
        playerUnit.setLeader(player);
        game.setCurrentPlayer(null);

        playerUnit.setMp(3);
        playerUnit.setRemainingMovePoints(0);

        IngameContext context = new IngameContext(
                new UserProvider().set(user),
                new GameProvider(),
                new IngameGameProvider()
        );
        context.gameInitialized(game);
        context.setGameEventManager(gameEventManager);

        revealBattleField(context);

        CompletableFuture.runAsync(
            () -> game.setCurrentPlayer(player),
            Platform::runLater
        ).get();
        Assert.assertEquals(playerUnit.getMp(), playerUnit.getRemainingMovePoints());

        // test unit selection
        Assert.assertNull(game.getSelectedUnit());
        click(25, 100);
        Assert.assertNull(game.getSelectedUnit());
        click(75, 100);
        Assert.assertSame(playerUnit, game.getSelectedUnit());

        verifyZeroInteractions(movementManager);
        when(movementManager.getTour(playerUnit, definition.cells[1][0]))
                .thenReturn(null);

        // test unit selection removed if not reachable terrain is clicked
        click(25, 150);
        verify(movementManager).getTour(playerUnit, definition.cells[1][0]);
        Assert.assertNull(game.getSelectedUnit());
        click(75, 100);
        Assert.assertSame(playerUnit, game.getSelectedUnit());

        verifyNoMoreInteractions(movementManager);
        verifyZeroInteractions(gameEventManager);

        Tour tour = new Tour();
        tour.setCost(2);
        tour.setPath(Collections.singletonList(definition.cells[0][0]));
        when(movementManager.getTour(playerUnit, definition.cells[0][0]))
                .thenReturn(tour);
        doAnswer(
                invocation -> {
                    CommandBuilder.MoveUnitData data = (CommandBuilder.MoveUnitData) ((Map) invocation.getArgument(0)).get("data");
                    Assert.assertEquals(data.unitId, playerUnit.getId());
                    Assert.assertEquals(
                            Collections.singletonList("0:0"),
                            data.path
                    );
                    return null;
                }
        ).when(gameEventManager).sendMessage(any());

        // test move action fired, if reachable terrain is clicked
        click(25, 100);

        Assert.assertTrue(game.getInitiallyMoved());
        Assert.assertEquals(1, playerUnit.getRemainingMovePoints());

        verify(movementManager, times(2)).getTour(any(), any());
        verify(gameEventManager).sendMessage(any());

        // test no action, if user is not current player
        game.setCurrentPlayer(null);
        click(25, 100);
    }

    @Test
    public void testButtonDisabled() throws ExecutionException, InterruptedException{

        BattleFieldController battleFieldController = battleFieldComponent.getController();

        TestGameBuilder.Definition definition = TestGameBuilder.sampleGameAlpha();
        Game game = definition.game;
        Unit playerUnit = definition.playerUnit;

        GameEventManager gameEventManager = Mockito.mock(GameEventManager.class);

        User user = new User();
        user.setName("Bob");
        Player player = new Player("Bob").setName("Bob");
        game.withPlayer(player);
        playerUnit.setLeader(player);
        game.setCurrentPlayer(player);

        IngameContext context = new IngameContext(
                new UserProvider().set(user),
                new GameProvider(),
                new IngameGameProvider()
        );
        context.gameInitialized(game);
        context.setGameEventManager(gameEventManager);

        context.getUser().setName("Bob");

        revealBattleField(context);

        Button endPhaseButton = lookup("#endPhaseButton").query();
        Assert.assertTrue(endPhaseButton.isDisabled());

        game.setInitiallyMoved(true);

        WaitForAsyncUtils.waitForFxEvents();

        Assert.assertFalse(endPhaseButton.isDisabled());



    }

    protected void revealBattleField(IngameContext context) throws ExecutionException, InterruptedException {
        // doing it like this saves the call to WaitForAsyncUtils and ensures that exceptions
        // in Platform.runLater() will result in failing tests right away
        CompletableFuture.runAsync(
                ()-> {
                    battleFieldComponent.getController().configure(context);
                    Stage stage = new Stage();
                    stage.setScene(new Scene(battleFieldComponent.getRoot()));
                    stage.setX(BASE_X);
                    stage.setY(BASE_Y);
                    stage.show();
                },
                Platform::runLater
        ).get();
    }

    public static Game buildComplexTestGame() throws IOException {
        Game game = new Game("AmazingGame24");
        BufferedReader in;

        String path = "game.txt";
        in = new BufferedReader(new FileReader(new File(path)));
        String zeile;
        while ((zeile = in.readLine()) != null) {
            int idAnf = zeile.indexOf('@')+1;
            int idEnd = zeile.indexOf('\"', idAnf);
            int xAnf = zeile.indexOf("\"x\":")+4;
            int xEnd = zeile.indexOf(",\"y");
            int yAnf = zeile.indexOf("\"y\":")+4;
            int yEnd = zeile.indexOf(",\"isPas");
            int bAnf = 7;
            int bEnd = zeile.indexOf('@');
            String biome = zeile.substring( bAnf, bEnd);
            Biome b = biome.equals("Grass") ? Biome.GRASS :
                    biome.equals("Forest") ? Biome.FOREST :
                            biome.equals("Mountain") ? Biome.MOUNTAIN :
                                    biome.equals("Water") ? Biome.WATER : null;
            Objects.requireNonNull(b);
            game.withCell(new Cell(zeile.substring( idAnf, idEnd))
                    .setX(Integer.parseInt(zeile.substring( xAnf, xEnd)))
                    .setY(Integer.parseInt(zeile.substring( yAnf, yEnd)))
                    .setBiome(b));
        }
        in = new BufferedReader(new FileReader(new File(path)));
        while((zeile = in.readLine()) != null){
            int idAnf = zeile.indexOf('@')+1;
            int idEnd = idAnf+8;
            int leAnf = zeile.indexOf('@', zeile.indexOf("\"left\""))+1;
            int leEnd = zeile.indexOf('\"', leAnf);
            int riAnf = zeile.indexOf('@', zeile.indexOf("\"right\""))+1;
            int riEnd = zeile.indexOf('\"', riAnf);
            int boAnf = zeile.indexOf('@', zeile.indexOf("\"bottom\""))+1;
            int boEnd = zeile.indexOf('\"', boAnf);
            int toAnf = zeile.indexOf('@', zeile.indexOf("\"top\""))+1;
            int toEnd = zeile.indexOf('\"', toAnf);
            for(Cell c: game.getCells()) {
                if(c.getId().equals(zeile.substring( idAnf, idEnd))) {
                    if(leAnf > 0){
                        for(Cell ce: game.getCells()){
                            if(ce.getId().equals(zeile.substring( leAnf, leEnd))) {
                                c.setLeft(ce);
                            }
                        }
                    }
                    if(riAnf > 0){
                        for(Cell ce: game.getCells()){
                            if(ce.getId().equals(zeile.substring( riAnf, riEnd))) {
                                c.setRight(ce);
                            }
                        }
                    }
                    if(boAnf > 0){
                        for(Cell ce: game.getCells()){
                            if(ce.getId().equals(zeile.substring( boAnf, boEnd))) {
                                c.setBottom(ce);
                            }
                        }
                    }
                    if(toAnf > 0){
                        for(Cell ce: game.getCells()){
                            if(ce.getId().equals(zeile.substring( toAnf, toEnd))) {
                                c.setTop(ce);
                            }
                        }
                    }
                }
            }
        }

        in.close();


        for (int i = 0; i < 10; i++)
        {
            Unit unit = new Unit(String.valueOf(i));
            unit.setGame(game);
            unit.setPosition(game.getCells().get(i));
            unit.setHp(10);
            unit.setMp(10);
            unit.setUnitType(UnitType.CHOPPER);
        }

        return game;
    }
}