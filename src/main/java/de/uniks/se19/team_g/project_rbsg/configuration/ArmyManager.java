package de.uniks.se19.team_g.project_rbsg.configuration;

import de.uniks.se19.team_g.project_rbsg.model.Army;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.GetArmiesService;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;


@Component
public class ArmyManager {

    @Nonnull private final GetArmiesService getArmiesService;

    public ArmyManager(@Nonnull GetArmiesService getArmiesService) {
        this.getArmiesService = getArmiesService;
    }

    @Nonnull
    public List<Army> getArmies() {
        return getArmiesService.loadArmies();
    }
}

