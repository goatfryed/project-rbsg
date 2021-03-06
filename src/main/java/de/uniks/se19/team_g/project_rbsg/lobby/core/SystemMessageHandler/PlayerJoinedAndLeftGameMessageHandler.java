package de.uniks.se19.team_g.project_rbsg.lobby.core.SystemMessageHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.se19.team_g.project_rbsg.model.Game;
import de.uniks.se19.team_g.project_rbsg.lobby.model.Lobby;
import de.uniks.se19.team_g.project_rbsg.lobby.system.ISystemMessageHandler;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * @author Georg Siebert
 */

public class PlayerJoinedAndLeftGameMessageHandler implements ISystemMessageHandler
{
    private final Lobby lobby;
    private Logger logger = LoggerFactory.getLogger(UserJoinedMessageHandler.class);
    private ObjectMapper objectMapper;

    public PlayerJoinedAndLeftGameMessageHandler(final @NonNull Lobby lobby)
    {
        this.lobby = lobby;
        objectMapper = new ObjectMapper();
    }

    @Override
    public void handleSystemMessage(String message)
    {
        JsonNode messageNode = null;

        try
        {
            messageNode = objectMapper.readTree(message);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (messageNode == null)
        {
            return;
        }
        if (!messageNode.get("action").asText().equals("playerJoinedGame") && !messageNode.get("action").asText().equals("playerLeftGame"))
        {
            return;
        }

        String id = messageNode.get("data").get("id").asText();
        int joinedPlayer = messageNode.get("data").get("joinedPlayer").asInt();

        if (lobby != null)
        {
            Game game = lobby.getGameOverId(id);
            if(game != null) {
                Platform.runLater(() -> game.setJoinedPlayer(joinedPlayer));
            }
            else {
                logger.debug("Didn't found game with id: " + id);
            }
        }
        else
        {
            logger.warn("lobby is null");
        }
    }
}
