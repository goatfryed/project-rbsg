package de.uniks.se19.team_g.project_rbsg.ingame.battlefield;

import de.uniks.se19.team_g.project_rbsg.RootController;
import de.uniks.se19.team_g.project_rbsg.SceneManager;
import de.uniks.se19.team_g.project_rbsg.ViewComponent;
import de.uniks.se19.team_g.project_rbsg.alert.AlertBuilder;
import de.uniks.se19.team_g.project_rbsg.configuration.FXMLLoaderFactory;
import de.uniks.se19.team_g.project_rbsg.ingame.IngameConfig;
import de.uniks.se19.team_g.project_rbsg.ingame.event.GameEventManager;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Biome;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Cell;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Game;
import de.uniks.se19.team_g.project_rbsg.model.GameProvider;
import de.uniks.se19.team_g.project_rbsg.model.IngameGameProvider;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testfx.framework.junit.ApplicationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author  Keanu Stückrad
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        FXMLLoaderFactory.class,
        IngameViewTests.ContextConfiguration.class,
        BattleFieldController.class,
        IngameConfig.class,
        GameEventManager.class,
        AlertBuilder.class
})
public class IngameViewTests extends ApplicationTest implements ApplicationContextAware {  // TODO Online Test ? for better coverage



    @TestConfiguration
    static class ContextConfiguration {
        @Bean
        public IngameGameProvider ingameGameProvider() {
            return new IngameGameProvider(){
                @Override
                public Game get(){
                    Game game = new Game("game");
                    BufferedReader in = null;
                    try {
                        String path = "game.txt";
                        in = new BufferedReader(new FileReader(new File(path)));
                        String zeile = null;
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (in != null)
                            try {
                                in.close();
                            } catch (IOException e) {
                            }
                    }
                    return game;
                }
            };
        }
        @Bean
        public SceneManager sceneManager(){
            return new SceneManager() {
//                @Override
//                public void setLobbyScene(@NonNull final boolean useCache, @Nullable final SceneIdentifier cacheIdentifier) {
//
//                }
            };
        }
        @Bean
        public GameProvider gameProvider() {
            return new GameProvider(){
                @Override
                public de.uniks.se19.team_g.project_rbsg.model.Game get(){
                    de.uniks.se19.team_g.project_rbsg.model.Game game = new de.uniks.se19.team_g.project_rbsg.model.Game("test", 4);
                    return game;
                }
            };
        }
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }


    private Scene scene;

    @Override
    public void start(@NonNull final Stage stage) {
        @SuppressWarnings("unchecked")
        final Scene buffer = new Scene(((ViewComponent<RootController>) applicationContext.getBean("battleFieldScene")).getRoot());
        scene = buffer;
        stage.setScene(scene);
        stage.setX(0);
        stage.setY(0);
        stage.show();
    }

    @Test
    public void testBuildIngameView() {
        Node ingameView = scene.getRoot();
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
    }

    @Override
    public void stop() throws Exception {
        scene.getWindow().centerOnScreen();
    }
}
