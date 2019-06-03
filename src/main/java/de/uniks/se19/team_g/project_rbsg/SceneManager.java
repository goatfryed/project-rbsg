package de.uniks.se19.team_g.project_rbsg;

import de.uniks.se19.team_g.project_rbsg.ingame.IngameSceneBuilder;
import de.uniks.se19.team_g.project_rbsg.lobby.core.LobbySceneBuilder;
import de.uniks.se19.team_g.project_rbsg.login.LoginSceneBuilder;
import de.uniks.se19.team_g.project_rbsg.termination.RootController;
import de.uniks.se19.team_g.project_rbsg.termination.Terminable;
import de.uniks.se19.team_g.project_rbsg.termination.Terminator;
import javafx.scene.Scene;
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
public class SceneManager implements ApplicationContextAware, Terminable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext context;

    private Stage stage;

    private RootController rootController;

    public SceneManager(@NonNull final Terminator terminator) {
        terminator.register(this);
    }

    @Autowired
    private IngameSceneBuilder ingameSceneBuilder;

    public SceneManager init(@NonNull final Stage stage) {
        this.stage = stage;

        return this;
    }

    public void setLoginScene() {
        if (stage == null) {
            logger.debug("Stage not initialised");
            return;
        }
        terminateRootController();
        try {
            final Scene loginScene = context.getBean(LoginSceneBuilder.class).getLoginScene();
            stage.setScene(loginScene);
        } catch (IOException e) {
            logger.error("Unable to set login scene");
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
            stage.setScene(lobbyScene);
        } catch (Exception e) {
            System.out.println("Unable to set lobby scene");
            e.printStackTrace();
        }
    }

    public void setIngameScene() {
        if (stage == null) {
            System.out.println("Not yet initialised");
            return;
        }
        try {
            final Scene ingameScene = ingameSceneBuilder.getIngameScene();
            stage.setMinHeight(670);
            stage.setMinWidth(900);
            stage.setResizable(true);
            stage.setScene(ingameScene);
        } catch (Exception e) {
            System.out.println("Unable to set ingame scene");
            e.printStackTrace();
        }
    }

    public void terminateRootController() {
        if (rootController != null && rootController instanceof Terminable) {
            ((Terminable) rootController).terminate();
            rootController = null;
        }
    }

    public RootController getRootController() {
        return rootController;
    }

    public void setRootController(@NonNull final RootController rootController) {
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
}
