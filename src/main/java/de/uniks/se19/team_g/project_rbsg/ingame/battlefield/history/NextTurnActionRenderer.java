package de.uniks.se19.team_g.project_rbsg.ingame.battlefield.history;

import de.uniks.se19.team_g.project_rbsg.ingame.model.Game;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Player;
import de.uniks.se19.team_g.project_rbsg.ingame.state.Action;
import de.uniks.se19.team_g.project_rbsg.ingame.state.UpdateAction;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.net.URL;

@Component
public class NextTurnActionRenderer extends DefaultActionRenderer {

    public NextTurnActionRenderer(@Qualifier("fxmlLoader") ObjectFactory<FXMLLoader> loaderFactory) {
        super(loaderFactory);
    }

    @Override
    protected URL getFxmlUrl() {
        return getClass().getResource("/ui/ingame/battleField/nextTurnHistoryCell.fxml");
    }

    @Nonnull
    @Override
    protected HistoryRenderData doRender(Action action) {
        UpdateAction actionImpl = (UpdateAction) action;
        Player player = (Player) actionImpl.getNextValue();
        Game game = player.getCurrentGame();
        int roundCount = Integer.valueOf(game.getRoundCounter());

        Pair<DefaultHistoryCellController, HBox> data = loadCell();

        Color playerColor = Color.web(player.getColor());

        HBox root = data.getValue();
        Label roundCounter = (Label)root.getChildren().get(0);
        roundCounter.setText(String.valueOf(roundCount));
        root.setBackground(new Background(new BackgroundFill(playerColor, null, null)));

        return new HistoryRenderData(root, playerColor);
    }

    @Override
    public boolean supports(Action action) {
        if ( !(action instanceof UpdateAction)) {
            return false;
        }
        UpdateAction actionImpl = (UpdateAction) action;
        return actionImpl.getEntity() instanceof Game
                && "currentPlayer".equals(actionImpl.getFieldName());
    }
}