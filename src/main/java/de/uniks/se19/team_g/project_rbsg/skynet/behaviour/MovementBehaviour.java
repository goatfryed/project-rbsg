package de.uniks.se19.team_g.project_rbsg.skynet.behaviour;

import de.uniks.se19.team_g.project_rbsg.ingame.model.Game;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Player;
import de.uniks.se19.team_g.project_rbsg.skynet.action.Action;
import org.springframework.lang.NonNull;

import java.util.Optional;

public class MovementBehaviour implements Behaviour {

    @Override
    public Optional<Action> apply(@NonNull final Game game, @NonNull final Player player) {
        return null;
    }
}
