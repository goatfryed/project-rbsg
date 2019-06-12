package de.uniks.se19.team_g.project_rbsg;

import de.uniks.se19.team_g.project_rbsg.waiting_room.WaitingRoomSceneBuilder;
import de.uniks.se19.team_g.project_rbsg.lobby.core.LobbySceneBuilder;
import de.uniks.se19.team_g.project_rbsg.login.StartSceneBuilder;
import de.uniks.se19.team_g.project_rbsg.termination.Terminable;
import io.rincl.*;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Jan Müller
 */
@Component
public class SceneManager implements ApplicationContextAware, Terminable, Rincled {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext context;

    private Stage stage;

    private Object rootController;

    private AudioClip audioClip;
    public boolean audioPlayed = true;

    public SceneManager init(@NonNull final Stage stage) {
        this.stage = stage;
        stage.setTitle(String.format("%s - %s", getResources().getString("mainTitle"), getResources().getString("subTitle")));
        stage.getIcons().add(new Image(SceneManager.class.getResourceAsStream("/assets/icons/icon.png")));
        audioClip = new AudioClip(getClass().getResource("/assets/music/simple-8bit-loop.mp3").toString());
        audioClip.setCycleCount(AudioClip.INDEFINITE);
        playAudio();
        return this;
    }

    private void setScene(@NonNull final Scene scene) {
        Platform.runLater(() -> stage.setScene(scene));
    }

    public void setStartScene() {
        if (stage == null) {
            logger.debug("Stage not initialised");
            return;
        }
        terminateRootController();
        try {
            final Scene startScene = context.getBean(StartSceneBuilder.class).getStartScene();
            stage.setResizable(false);
            setScene(startScene);
        } catch (IOException e) {
            logger.error("Unable to set start scene");
            e.printStackTrace();
        }
    }

    public void setLobbyScene() {
        if (stage == null) {
            logger.debug("Stage not initialised");
            return;
        }
        terminateRootController();
        try {
            final Scene lobbyScene = context.getBean(LobbySceneBuilder.class).getLobbyScene();
            stage.setResizable(false);
            setScene(lobbyScene);
        } catch (Exception e) {
            System.out.println("Unable to set lobby scene");
            e.printStackTrace();
        }
    }

    public void setWaitingRoomScene() {
        if (stage == null) {
            System.out.println("Not yet initialised");
            return;
        }
        try {
            final Scene waitingRoomScene = context.getBean(WaitingRoomSceneBuilder.class).getWaitingRoomScene();
            stage.setMinHeight(670);
            stage.setMinWidth(1250);
            stage.setResizable(true);
            setScene(waitingRoomScene);
        } catch (Exception e) {
            System.out.println("Unable to set waiting_room scene");
            e.printStackTrace();
        }
    }

    public void terminateRootController() {
        if (rootController != null && rootController instanceof Terminable) {
            ((Terminable) rootController).terminate();
            rootController = null;
        }
    }

    public Object getRootController() {
        return rootController;
    }

    public void setRootController(@NonNull final Object rootController) {
        if (this.rootController != null) {
            terminateRootController();
        }
        this.rootController = rootController;
    }

    @Override
    public void setApplicationContext(@NonNull final ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void terminate() {
        terminateRootController();
    }

    public void playAudio() {
        audioClip.play();
        audioPlayed = true;
    }

    public void stopAudio() {
        audioClip.stop();
        audioPlayed = false;
    }

    public void setArmyBuilderScene() {
        @SuppressWarnings("unchecked") ViewComponent<Object> component
                = (ViewComponent<Object>) context.getBean("armyBuilderScene");
        showSceneFromViewComponent(component);
    }

    private void showSceneFromViewComponent(ViewComponent<Object> component) {
        Platform.runLater(() -> {
            stage.setScene(sceneFromParent(component.getRoot()));
            setRootController(component.getController());
        });
    }

    public Scene sceneFromParent(Parent parent)
    {
        return new Scene(parent);
    }
}
