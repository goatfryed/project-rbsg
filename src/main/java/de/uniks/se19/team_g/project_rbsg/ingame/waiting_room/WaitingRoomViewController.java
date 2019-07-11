package de.uniks.se19.team_g.project_rbsg.ingame.waiting_room;

import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uniks.se19.team_g.project_rbsg.MusicManager;
import de.uniks.se19.team_g.project_rbsg.RootController;
import de.uniks.se19.team_g.project_rbsg.SceneManager;
import de.uniks.se19.team_g.project_rbsg.ViewComponent;
import de.uniks.se19.team_g.project_rbsg.alert.AlertBuilder;
import de.uniks.se19.team_g.project_rbsg.army_builder.army_selection.ArmySelectorController;
import de.uniks.se19.team_g.project_rbsg.chat.ChatController;
import de.uniks.se19.team_g.project_rbsg.chat.ui.ChatBuilder;
import de.uniks.se19.team_g.project_rbsg.configuration.ApplicationState;
import de.uniks.se19.team_g.project_rbsg.login.SplashImageBuilder;
import de.uniks.se19.team_g.project_rbsg.model.Army;
import de.uniks.se19.team_g.project_rbsg.model.GameProvider;
import de.uniks.se19.team_g.project_rbsg.model.IngameGameProvider;
import de.uniks.se19.team_g.project_rbsg.model.UserProvider;
import de.uniks.se19.team_g.project_rbsg.termination.Terminable;
import de.uniks.se19.team_g.project_rbsg.util.JavaFXUtils;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.event.CommandBuilder;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.event.GameEventHandler;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.event.GameEventManager;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.model.Cell;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.model.Game;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.model.ModelManager;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.model.Player;
import de.uniks.se19.team_g.project_rbsg.ingame.waiting_room.preview_map.PreviewMapBuilder;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;


/**
 * @author  Keanu Stückrad
 * @author Jan Müller
 */
@Scope("prototype")
@Controller
public class WaitingRoomViewController implements RootController, Terminable, GameEventHandler {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int ICON_SIZE = 40;

    public Pane player1Pane;
    public Pane player2Pane;
    public Pane player3Pane;
    public Pane player4Pane;
    public Pane chatContainer;
    public Pane mapPreviewPane;
    public Pane miniGamePane; // TODO Tic-Tac-Toe?
    public VBox armySelector;
    public Button soundButton;
    public Button leaveButton;
    public Button showInfoButton;
    public AnchorPane root;

    // TODO: Ask Jan, wether this can be removed
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ChatController chatController;

    private PlayerCardBuilder playerCard;
    private PlayerCardBuilder playerCard2;
    private PlayerCardBuilder playerCard3;
    private PlayerCardBuilder playerCard4;
    private ObservableList<PlayerCardBuilder> playerCardBuilders;

    private final GameProvider gameProvider;
    private final UserProvider userProvider;
    private final SceneManager sceneManager;
    private final GameEventManager gameEventManager;
    private final MusicManager musicManager;
    private final SplashImageBuilder splashImageBuilder;
    private final ApplicationState applicationState;
    private final ChatBuilder chatBuilder;
    private final AlertBuilder alertBuilder;
    @Nonnull
    private final Function<VBox, ArmySelectorController> armySelectorComponent;
    private final PreviewMapBuilder previewMapBuilder;
    public ModelManager modelManager;
    private final IngameGameProvider ingameGameProvider;

    private ObjectProperty<Army> selectedArmy = new SimpleObjectProperty<>();

    /**
     * keep reference for WeakReferences further down the road
     */
    @SuppressWarnings("FieldCanBeLocal")
    private ArmySelectorController armySelectorController;

    @Autowired
    public WaitingRoomViewController(
            @Nonnull final GameProvider gameProvider,
            @Nonnull final UserProvider userProvider,
            @Nonnull final SceneManager sceneManager,
            @Nonnull final GameEventManager gameEventManager,
            @Nonnull final MusicManager musicManager,
            @Nonnull final SplashImageBuilder splashImageBuilder,
            @Nonnull final ApplicationState applicationState,
            @Nonnull final IngameGameProvider ingameGameProvider,
            @Nonnull final ChatBuilder chatBuilder,
            @Nonnull final PreviewMapBuilder previewMapBuilder,
            @Nonnull final AlertBuilder alertBuilder,
            @Nonnull final Function<VBox, ArmySelectorController> armySelectorComponent,
            @Nonnull final ModelManager modelManager
    ) {
        this.gameProvider = gameProvider;
        this.userProvider = userProvider;
        this.sceneManager = sceneManager;
        this.gameEventManager = gameEventManager;
        this.musicManager = musicManager;
        this.splashImageBuilder = splashImageBuilder;
        this.applicationState = applicationState;
        this.chatBuilder = chatBuilder;
        this.alertBuilder = alertBuilder;
        this.armySelectorComponent = armySelectorComponent;
        this.modelManager = modelManager;
        this.previewMapBuilder = previewMapBuilder;
        this.ingameGameProvider = ingameGameProvider;
    }

    public void initialize() throws Exception {
        initPlayerCardBuilders();
        setPlayerCardNodes();
        JavaFXUtils.setButtonIcons(
                leaveButton,
                getClass().getResource("/assets/icons/navigation/arrowBackWhite.png"),
                getClass().getResource("/assets/icons/navigation/arrowBackBlack.png"),
                ICON_SIZE
        );
        JavaFXUtils.setButtonIcons(
                showInfoButton,
                getClass().getResource("/assets/icons/navigation/infoWhite.png"),
                getClass().getResource("/assets/icons/navigation/infoBlack.png"),
                ICON_SIZE
        );
        musicManager.initButtonIcons(soundButton);
        root.setBackground(new Background(splashImageBuilder.getSplashImage()));
        initSocket();

        ObservableList<Player> readyPlayers = FXCollections.observableArrayList(
            player -> new Observable[] {player.isReadyProperty()}
        );

        modelManager.gameProperty().addListener(
            (observable, oldValue, newValue) -> Bindings.bindContent(readyPlayers, newValue.getPlayers())
        );

        Bindings.createBooleanBinding(
            () -> readyPlayers.stream().filter(Player::getIsReady).count() == gameProvider.get().getNeededPlayer(),
            readyPlayers
        ).addListener((observable, oldValue, newValue) -> {
            if (newValue && gameProvider.get().getCreator() == userProvider.get()) {
                logger.debug("trigger game start of our own game");
                gameEventManager.sendMessage(CommandBuilder.startGame());
            }
        });

        configureArmySelection();
    }

    private void initSocket() throws Exception {
        gameEventManager.addHandler(modelManager);
        gameEventManager.addHandler(this);
        withChatSupport();

        gameEventManager.setSceneController(this);
        gameEventManager.startSocket(gameProvider.get().getId(), null);
    }

    private void withChatSupport() throws Exception {
        final ViewComponent<ChatController> chatComponents = chatBuilder.buildChat(gameEventManager);
        chatContainer.getChildren().add(chatComponents.getRoot());
        chatController = chatComponents.getController();
    }

    private void initPlayerCardBuilders() {
        playerCard = new PlayerCardBuilder();
        playerCard2 = new PlayerCardBuilder();
        playerCardBuilders = FXCollections.observableArrayList();
        playerCardBuilders.add(playerCard2);
        if(gameProvider.get().getNeededPlayer() == 4) {
            playerCard3 = new PlayerCardBuilder();
            playerCard4 = new PlayerCardBuilder();
            playerCardBuilders.add(playerCard3);
            playerCardBuilders.add(playerCard4);
        }
    }

    private void setPlayerCardNodes() {
        player1Pane.getChildren().add(playerCard.buildPlayerCard());
        player2Pane.getChildren().add(playerCard2.buildPlayerCard());
        playerCard2.switchColumns();
        if(gameProvider.get().getNeededPlayer() == 4) {
            // if visibility was disabled before for example when leaving game
            player3Pane.setVisible(true);
            player4Pane.setVisible(true);
            AnchorPane.setTopAnchor(player1Pane, 102.0);
            AnchorPane.setTopAnchor(player2Pane, 102.0);
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
    public void terminate() {
        gameEventManager.terminate();
    }

    public void showInfo() {
        sceneManager.setScene(SceneManager.SceneIdentifier.INGAME, true, SceneManager.SceneIdentifier.WAITING_ROOM); // for testing
    }

    public void onConnectionClosed() {
        alertBuilder.error(AlertBuilder.Text.CONNECTION_CLOSED, this::leaveWaitingRoom);
    }

    public void leaveRoom() {
        alertBuilder
                .confirmation(
                        AlertBuilder.Text.EXIT,
                        this::leaveWaitingRoom,
                        null);
    }

    private void leaveWaitingRoom() {
        gameProvider.clear();
        sceneManager.setScene(SceneManager.SceneIdentifier.LOBBY, false, null);
    }

    public void toggleSound() {
        musicManager.updateMusicButtonIcons(soundButton);
    }

    private void showMapPreview(@NonNull final List<Cell> cells) {
        final Node previewMap = previewMapBuilder.buildPreviewMap(cells, mapPreviewPane.getWidth(), mapPreviewPane.getHeight());
        Platform.runLater(() -> mapPreviewPane.getChildren().add(previewMap));
    }

    @Override
    public boolean accepts(@NonNull final ObjectNode message) {
        if (!message.has("action")) return false;

        return message.get("action").asText().equals("gameInitFinished");
    }

    @Override
    public void handle(@NonNull final ObjectNode message) {
        final Game game = modelManager.getGame();
        //game SHOULD (no guarantee) be ready now
        ingameGameProvider.set(game);
        setPlayerCards(ingameGameProvider.get());
        showMapPreview(ingameGameProvider.get().getCells());
    }

    public void setPlayerCards(Game game) {
        // init PlayerCards
        boolean skipped = false;
        Player user = null;
        for(Player p: game.getPlayers()) {
            if(p.getName().equals(userProvider.get().getName())) {
                user = p;
            }
        }
        if (user == null) {
            // Exception
            return;
        }
        playerCard.setPlayer(user, Color.valueOf(user.getColor()));
        for (Player p : game.getPlayers()) {
            if(p.equals(user) && !skipped){
                skipped = true;
                continue;
            }
            for(PlayerCardBuilder playerC: playerCardBuilders){
                if(playerC.isEmpty) {
                    playerC.setPlayer(p, Color.valueOf(p.getColor()));
                    break;
                }
            }
        }
        // ListChangeListener for Player (+ PlayerCards)
        game.getPlayers().addListener((ListChangeListener<Player>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Player p : c.getAddedSubList()) {
                        for(PlayerCardBuilder playerC: playerCardBuilders){
                            if(playerC.isEmpty) {
                                playerC.setPlayer(p, Color.valueOf(p.getColor()));
                                break;
                            }
                        }
                    }
                }
                if (c.wasRemoved()) {
                    for (Player p : c.getRemoved()) {
                        for(PlayerCardBuilder playerC: playerCardBuilders){
                            if(!playerC.isEmpty) {
                                if(playerC.getPlayer().equals(p)) {
                                    playerC.playerLeft();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    protected void configureArmySelection() {
        armySelectorController = armySelectorComponent.apply(armySelector);

        selectedArmy.addListener((observable, oldValue, newValue) -> {
            gameEventManager.sendMessage(CommandBuilder.changeArmy(newValue));
            gameEventManager.sendMessage(CommandBuilder.readyToPlay());
        });

        /*
         * normally, an observable list is only aware of items added and removed
         * we can wrap our armies in a bound observable list with extractor to also receive update events of items in the list
         */
        final ObservableList<Army> playableAwareArmies = FXCollections.observableArrayList(
            army -> new Observable[] {army.isPlayable}
        );
        Bindings.bindContent( playableAwareArmies, applicationState.armies);

        armySelectorController.setSelection(playableAwareArmies.filtered(a -> a.isPlayable.get()), selectedArmy);
    }
}