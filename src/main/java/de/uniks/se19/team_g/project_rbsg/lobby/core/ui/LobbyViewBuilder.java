package de.uniks.se19.team_g.project_rbsg.lobby.core.ui;

import de.uniks.se19.team_g.project_rbsg.configuration.*;
import javafx.fxml.*;
import javafx.scene.*;
import org.springframework.context.annotation.*;
import org.springframework.lang.*;
import org.springframework.stereotype.*;

import java.io.*;

/**
 * @author Georg Siebert
 */

@Component
@Scope("prototype")
public class LobbyViewBuilder
{
    private LobbyViewController lobbyViewController;
    private FXMLLoaderFactory loaderFactory;

    public LobbyViewController getLobbyViewController() {
        return lobbyViewController;
    }

    public LobbyViewBuilder(FXMLLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
    }

    private FXMLLoader getLoader() {
        FXMLLoader loader = loaderFactory.fxmlLoader();
        loader.setLocation(getClass().getResource("/ui/lobby/core/lobbyView.fxml"));

        return loader;
    }

    public @NonNull Node buildLobbyScene() {
        FXMLLoader fxmlLoader = getLoader();
        Node lobbyView;
        try
            {
                lobbyView = fxmlLoader.load();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }

            lobbyViewController = fxmlLoader.getController();
            lobbyViewController.init();

        return lobbyView;
    }
}