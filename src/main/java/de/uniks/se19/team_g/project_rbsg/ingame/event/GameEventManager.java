package de.uniks.se19.team_g.project_rbsg.ingame.event;

import de.uniks.se19.team_g.project_rbsg.server.websocket.IWebSocketCallback;
import de.uniks.se19.team_g.project_rbsg.server.websocket.WebSocketClient;
import de.uniks.se19.team_g.project_rbsg.termination.Terminable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * @author Jan Müller
 */
@Component
@Scope("prototype")
public class GameEventManager implements IWebSocketCallback, Terminable {

    private static final String ENDPOINT = "/game?gameId=";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ArrayList<GameEventHandler> gameEventHandlers;

    private WebSocketClient webSocketClient;

    public GameEventManager(@NonNull final WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;

        gameEventHandlers = new ArrayList<>();
        gameEventHandlers.add(new DefaultGameEventHandler());
    }

    public GameEventManager addHandler(@NonNull final GameEventHandler gameEventHandler) {
        gameEventHandlers.add(gameEventHandler);
        return this;
    }

    public ArrayList<GameEventHandler> getHandlers() {
        return gameEventHandlers;
    }

    public void startSocket(@NonNull final String gameID) {
        webSocketClient.start(ENDPOINT + gameID, this);
    }

    @Override
    public void handle(@NonNull final String message) {
        gameEventHandlers.forEach(handler -> handler.handle(message));
    }

    @Override
    public void terminate() {
        webSocketClient.stop();
        logger.debug("Terminated " + this);
    }
}