package de.uniks.se19.team_g.project_rbsg.server.rest.army.deletion;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uniks.se19.team_g.project_rbsg.configuration.ApplicationState;
import de.uniks.se19.team_g.project_rbsg.model.Army;
import de.uniks.se19.team_g.project_rbsg.model.User;
import de.uniks.se19.team_g.project_rbsg.model.UserProvider;
import de.uniks.se19.team_g.project_rbsg.server.ServerConfig;
import de.uniks.se19.team_g.project_rbsg.server.rest.LoginManager;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.ArmyAdapter;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.ArmyUnitAdapter;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.GetArmiesService;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.deletion.serverResponses.DeleteArmyResponse;
import de.uniks.se19.team_g.project_rbsg.server.rest.config.ApiClientErrorInterceptor;
import de.uniks.se19.team_g.project_rbsg.server.rest.config.UserKeyInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        ServerConfig.class,
        LoginManager.class,
        UserProvider.class,
        UserKeyInterceptor.class,
        ApiClientErrorInterceptor.class,
        ArmyAdapter.class,
        ObjectMapper.class,
        ArmyUnitAdapter.class,
        ApplicationState.class,
        GetArmiesService.class,
        DeleteArmyService.class
})


public class DeleteArmyTest {


    @Autowired
    GetArmiesService getArmiesService;
    @Autowired
    DeleteArmyService deleteArmyService;
    @Autowired
    LoginManager loginManager;
    @Autowired
    UserProvider userProvider;
    @Autowired
    ArmyAdapter armyAdapter;
    @Autowired
    RestTemplate rbsgTemplate;

    @Test
    public void deleteArmy() throws ExecutionException, InterruptedException {
        User user = new User("test123", "test123");
        user.setUserKey(
                loginManager.onLogin(user).get().getBody().get("data").get("userKey").asText()
        );
        userProvider.set(user);

        CompletableFuture<List<Army>> armyFuture = getArmiesService.queryArmies();
        ArrayList<Army> armyList = null;
        try {
            armyList = (ArrayList<Army>) armyFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        deleteArmyService.deleteArmy(armyList.get(0));
        //CompletableFuture<DeleteArmyResponse> deletionResponse =
        //DeleteArmyResponse response = deletionResponse.get();
        //System.out.println(response.message);
    }
}
