package de.uniks.se19.team_g.project_rbsg.waiting_room.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.uniks.se19.team_g.project_rbsg.util.Tuple;
import de.uniks.se19.team_g.project_rbsg.waiting_room.event.GameEventHandler;
import de.uniks.se19.team_g.project_rbsg.waiting_room.model.util.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.lang.NonNull;

import java.util.HashMap;

/**
 * @author Jan Müller
 */
public class ModelManager implements GameEventHandler {

    private static final String GAME_INIT_OBJECT = "gameInitObject";
    private static final String GAME_NEW_OBJECT = "gameNewObject";
    private static final String GAME_REMOVE_OBJECT = "gameRemoveObject";
    public static final String GAME_CHANGE_OBJECT = "gameChangeObject";

    @NonNull
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NonNull
    private final HashMap<String, Object> objectMap;

    private ObjectProperty<Game> gameProperty = new SimpleObjectProperty<>();

    public ModelManager() {
        objectMap = new HashMap<>();
    }

    public ReadOnlyObjectProperty<Game> gameProperty() {
        return gameProperty;
    }

    public Game getGame() {
        return gameProperty.get();
    }

    @Override
    public boolean accepts(@NonNull final ObjectNode message) {
        if (!message.has("action")) return false;

        return message.has("data") && message.get("data").has("id");
    }

    @Override
    public void handle(@NonNull final ObjectNode node) {
        switch (node.get("action").asText()) {
            case GAME_INIT_OBJECT:
            case GAME_NEW_OBJECT:
                //may change for future server releases
                handleInit(node);
                break;
            case GAME_REMOVE_OBJECT:
                handleRemove(node);
                break;
            case GAME_CHANGE_OBJECT:
                handleChange(node.get("data"));
                break;
            default:
                logger.error("Unknown model message: " + node);
        }
    }

    private void handleChange(JsonNode data) {
        final String id = data.get("id").asText();

        final Object entity = objectMap.get(id);

        if (entity == null) {
            logger.error("unknown identity {} changed", id);
            return;
        }

        String changedProperty = data.get("fieldName").asText();
        final JsonNode newValue = data.get("newValue");

        if (entity instanceof Game) {
            if (handleSpecialGameFields((Game) entity, changedProperty, newValue)) {
                return;
            }
        }

        if (newValue.isValueNode()) {
            try {
                String nextValue = newValue.textValue();
                final BeanWrapperImpl beanWrapper = new BeanWrapperImpl(entity);
                beanWrapper.setPropertyValue(changedProperty, nextValue);
                return;
            } catch (BeansException e) {
                logger.error("entity update failed", e);
            }
        }

        logger.error("can't update entity of type {}", entity.getClass());
    }

    private boolean handleSpecialGameFields(Game entity, String changedProperty, JsonNode newValue) {

        if (changedProperty.equals("currentPlayer")) {
            final Player player = (Player) objectMap.get(newValue.asText());
            entity.setCurrentPlayer(player);

            return true;
        }

        return false;
    }

    private void handleInit(@NonNull final ObjectNode node) {
        final JsonNode data = node.get("data");

        final String identifier = data.get("id").asText();
        final Tuple<String, String> typeAndId = splitIdentifier(identifier);
        final String type = typeAndId.first;

        switch (type) {
            case "Game":
                gameProperty.set(GameUtil.buildGame(this, identifier, data, true));
                break;
            case "Player":
                PlayerUtil.buildPlayer(this, identifier, data, true);
                break;
            case "Unit":
                UnitUtil.buildUnit(this, identifier, data, true);
                break;
            case "Forest":
            case "Grass":
            case "Mountain":
            case "Water":
                CellUtil.buildCell(this, identifier, StringToEnum.biome(type), data, false);
                break;
            default:
                logger.error("Unknown init class: " + type);
        }
    }

    private void handleRemove(@NonNull final ObjectNode node) {
        final JsonNode data = node.get("data");

        final String identifier = data.get("id").asText();
        final Tuple<String, String> typeAndId = splitIdentifier(identifier);
        final String type = typeAndId.first;

        if (!data.has("from") || !data.has("fieldName")) {
            logger.error("Unknown message format: " + node);
            return;
        }

        final String from = data.get("from").asText();
        final String fieldName = data.get("fieldName").asText();

        switch (type) {
            case "Player":
                PlayerUtil.removePlayerFrom(this, identifier, from, fieldName, true);
                break;
            case "Unit":
                UnitUtil.removeUnitFrom(this, identifier, from, fieldName, true);
                break;
            default:
                logger.error("Unknown removal class: " + type);
        }
    }

    public Game gameWithId(@NonNull final String id) {
        return (Game) objectMap.computeIfAbsent(id, g -> new Game(id));
    }

    public Player playerWithId(@NonNull final String id) {
        return (Player) objectMap.computeIfAbsent(id, p -> new Player(id));
    }

    public Unit unitWithId(@NonNull final String id) {
        return (Unit) objectMap.computeIfAbsent(id, u -> new Unit(id));
    }

    public Cell cellWithId(@NonNull final String id) {
        return (Cell) objectMap.computeIfAbsent(id, c -> new Cell(id));
    }

    @NonNull
    private Tuple<String, String> splitIdentifier(@NonNull final String identifier) {
        final int at_index = identifier.indexOf('@');
        final String clazz = identifier.substring(0, at_index);
        final String code = identifier.substring(at_index + 1);
        return new Tuple<>(clazz, code);
    }
}
