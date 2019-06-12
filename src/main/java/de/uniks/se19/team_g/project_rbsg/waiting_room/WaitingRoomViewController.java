package de.uniks.se19.team_g.project_rbsg.waiting_room;

import de.uniks.se19.team_g.project_rbsg.MusicManager;
import de.uniks.se19.team_g.project_rbsg.SceneManager;
import de.uniks.se19.team_g.project_rbsg.login.SplashImageBuilder;
import de.uniks.se19.team_g.project_rbsg.waiting_room.event.GameEventManager;
import de.uniks.se19.team_g.project_rbsg.lobby.model.Player;
import de.uniks.se19.team_g.project_rbsg.model.GameProvider;
import de.uniks.se19.team_g.project_rbsg.model.UserProvider;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import de.uniks.se19.team_g.project_rbsg.termination.RootController;
import de.uniks.se19.team_g.project_rbsg.termination.Terminable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;

/**
 * @author  Keanu Stückrad
 * @author Jan Müller
 */
@Controller
public class WaitingRoomViewController implements RootController, Terminable {

    private static final int ICON_SIZE = 40;

    public Pane player1Pane;
    public Pane player2Pane;
    public Pane player3Pane;
    public Pane player4Pane;
    public Pane chatPane; // TODO @DerYeger
    public Pane mapPreviewPane; // TODO @DerYeger
    public Pane miniGamePane; // TODO Tic-Tac-Toe?
    public Button soundButton;
    public Button leaveButton;
    public Button showInfoButton;
    public AnchorPane root;

    private PlayerCardBuilder playerCard;
    private PlayerCardBuilder playerCard2;
    private PlayerCardBuilder playerCard3;
    private PlayerCardBuilder playerCard4;

    private final GameProvider gameProvider;
    private final UserProvider userProvider;
    private final SceneManager sceneManager;
    private final GameEventManager gameEventManager;
    private final MusicManager musicManager;
    private final SplashImageBuilder splashImageBuilder;

    @Autowired
    public WaitingRoomViewController(@NonNull final GameProvider gameProvider,
                                     @NonNull final UserProvider userProvider,
                                     @NonNull final SceneManager sceneManager,
                                     @NonNull final GameEventManager gameEventManager,
                                     @NonNull final MusicManager musicManager,
                                     @NonNull final SplashImageBuilder splashImageBuilder) {
        this.gameProvider = gameProvider;
        this.userProvider = userProvider;
        this.sceneManager = sceneManager;
        this.gameEventManager = gameEventManager;
        this.musicManager = musicManager.init();
        this.splashImageBuilder = splashImageBuilder;
    }

    public void init() {
        initPlayerCardBuilders();
        setPlayerCardNodes();
        gameEventManager.startSocket(gameProvider.get().getId());
        setAsRootController();
        setButtonIcons(leaveButton, "/assets/icons/navigation/arrow-back-black.png", "/assets/icons/navigation/arrow-back-white.png");
        setButtonIcons(showInfoButton,"/assets/icons/navigation/info-black.png", "/assets/icons/navigation/info-white.png");
        musicManager.initButtonIcons(soundButton);
        root.setBackground(new Background(splashImageBuilder.getSplashImage()));
    }

    private void initPlayerCardBuilders() {
        playerCard = new PlayerCardBuilder();
        playerCard2 = new PlayerCardBuilder();
        if(gameProvider.get().getNeededPlayer() == 4) {
            playerCard3 = new PlayerCardBuilder();
            playerCard4 = new PlayerCardBuilder();
        }
    }

    private void setPlayerCardNodes() {
        player1Pane.getChildren().add(playerCard.setPlayer(new Player(userProvider.get().getName())));
        player2Pane.getChildren().add(playerCard2.buildPlayerCard());
        playerCard2.switchColumns();
        if(gameProvider.get().getNeededPlayer() == 4) {
            // if visibility was disabled before for example when leaving game
            player3Pane.setVisible(true);
            player4Pane.setVisible(true);
            AnchorPane.setTopAnchor(player1Pane, 110.0);
            AnchorPane.setTopAnchor(player2Pane, 110.0);
            player3Pane.getChildren().add(playerCard3.buildPlayerCard());
            player4Pane.getChildren().add(playerCard4.buildPlayerCard());
            playerCard4.switchColumns();
        } else {
            AnchorPane.setTopAnchor(player1Pane, 180.0);
            AnchorPane.setTopAnchor(player2Pane, 180.0);
            player3Pane.setVisible(false);
            player4Pane.setVisible(false);
        }
    }

    @Override
    public void setAsRootController() {
        sceneManager.setRootController(this);
    }

    @Override
    public void terminate() {
        gameEventManager.terminate();
    }

    public void showInfo(ActionEvent actionEvent) {
        // TODO
    }

    public void leaveRoom(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Leave Game");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.OK)) {
            // WebSocketConfigurator.userKey = userProvider.get().getUserKey();
            sceneManager.setLobbyScene();
            gameProvider.clear();
        } else {
            actionEvent.consume();
        }
    }

    public void toggleSound(ActionEvent actionEvent) {
        musicManager.updateMusicButtonIcons(soundButton);
    }

    private void setButtonIcons(Button button, String hoverIconName, String nonHoverIconName) {
        ImageView hover = new ImageView();
        ImageView nonHover = new ImageView();
        nonHover.fitWidthProperty().setValue(ICON_SIZE);
        nonHover.fitHeightProperty().setValue(ICON_SIZE);
        hover.fitWidthProperty().setValue(ICON_SIZE);
        hover.fitHeightProperty().setValue(ICON_SIZE);
        hover.setImage(new Image(String.valueOf(getClass().getResource(hoverIconName))));
        nonHover.setImage(new Image(String.valueOf(getClass().getResource(nonHoverIconName))));
        button.graphicProperty().bind(Bindings.when(button.hoverProperty())
                .then(hover)
                .otherwise(nonHover));
    }
}
