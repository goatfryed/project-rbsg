package de.uniks.se19.team_g.project_rbsg.server.rest.army.persistance;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uniks.se19.team_g.project_rbsg.model.Army;
import de.uniks.se19.team_g.project_rbsg.model.Unit;
import de.uniks.se19.team_g.project_rbsg.server.rest.RBSGDataResponse;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.GetArmiesService;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.deletion.DeleteArmyService;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.persistance.serverResponses.SaveArmyResponse;
import de.uniks.se19.team_g.project_rbsg.server.rest.army.persistance.requests.PersistArmyRequest;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class PersistentArmyManager {
    private final String url = "/army";
    final RestTemplate restTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private String fileName = "armies.json";
    @NonNull
    private DeleteArmyService deleteArmyService;
    private GetArmiesService getArmiesService;

    public PersistentArmyManager(@NonNull RestTemplate restTemplate,
                                 @NonNull DeleteArmyService deleteArmyService,
                                 @NonNull GetArmiesService getArmiesService) {

        this.restTemplate = restTemplate;
        this.deleteArmyService = deleteArmyService;
        this.getArmiesService = getArmiesService;
    }

    public SaveArmyResponse saveArmyOnline(@NonNull Army army) throws ExecutionException,
            InterruptedException,
            IOException {
        //if (army.id.isEmpty().get()) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            SaveArmyResponse response = createArmy(army).get();
            String message = response.message;
            System.out.println("save online status: " + response.status);
            return response;
        //}
        //return updateArmy(army).get();
    }

    private CompletableFuture<SaveArmyResponse> updateArmy(@NonNull Army army) {
        String putUrl = "/army/" + army.id.get();
        PersistArmyRequest updateArmyRequest = new PersistArmyRequest();
        updateArmyRequest.name = army.name.get();
        updateArmyRequest.units = new LinkedList<String>();

        for (Unit unit : army.units) {
            updateArmyRequest.units.add(unit.id.get());
        }

        return CompletableFuture.supplyAsync(() -> restTemplate.exchange(putUrl,
                HttpMethod.PUT,
                new HttpEntity<>(updateArmyRequest),
                SaveArmyResponse.class))
                .thenApply(updateAnswer -> onUpdateArmyResponseReturned(updateAnswer))
                .exceptionally(this::handleException);
    }

    private SaveArmyResponse onUpdateArmyResponseReturned(
            ResponseEntity<SaveArmyResponse> updateArmyResponse) {

        SaveArmyResponse saveArmyResponse = new SaveArmyResponse();

        if (updateArmyResponse.getBody().message.isEmpty()) {
            logger.debug("Server responded with empty message.");
        }
        if (updateArmyResponse.getBody().status.isEmpty()) {
            logger.debug("Server responded with empty status.");
        }
        if (updateArmyResponse.getBody().data != null) {
            logger.debug("Server responded with data: " + updateArmyResponse.getBody().data.toString());
        }

        saveArmyResponse.message = updateArmyResponse.getBody().message;
        saveArmyResponse.status = updateArmyResponse.getBody().status;
        saveArmyResponse.data = updateArmyResponse.getBody().data;

        return saveArmyResponse;
    }

    private CompletableFuture<SaveArmyResponse> createArmy(@NonNull Army army) {
        PersistArmyRequest createArmyRequest = new PersistArmyRequest();
        ObservableList<Unit> units = army.units;
        createArmyRequest.units = new LinkedList<String>();

        for (Unit unit : units) {
            createArmyRequest.units.add(unit.id.get());
        }

        createArmyRequest.name = army.name.get();

        return CompletableFuture.supplyAsync(() -> restTemplate.postForObject(
                url,
                createArmyRequest,
                SaveArmyResponse.class))
                .thenApply(saveArmyResponse -> onSaveArmyResponseReturned(saveArmyResponse, army))
                .exceptionally(this::handleException);
    }

    private SaveArmyResponse onSaveArmyResponseReturned(
            @NonNull SaveArmyResponse saveArmyResponse,
            Army army) {

        String armyId = saveArmyResponse.data.id;
        if (armyId.equals(null)) {
            logger.debug("Server answer doesnt contain a armyID.");
            return null;
        }
        if (!armyId.isEmpty()) {
            army.id.set(armyId);
        } else {
            logger.debug("Server responded without ID: ");
        }
        if (!saveArmyResponse.message.equals(null)) {
            logger.debug("Server responded with message: " + saveArmyResponse.message);
        }
        return saveArmyResponse;
    }

    private SaveArmyResponse handleException(@NonNull Throwable throwable) {
        logger.debug("Army creation failed with message: " + throwable.getMessage());
        return null;
    }

    public void saveArmiesLocal(ArrayList<Army> armyList) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        ArmyWrapper armyWrapper = new ArmyWrapper(null);
        armyWrapper.armies = new ArrayList<DeserializableArmy>();

        for (Army army : armyList) {
            DeserializableArmy deserializableArmy = new DeserializableArmy(army.id.get(),
                    army.name.get(),
                    new ArrayList<String>());

            for (Unit unit : army.units) {
                deserializableArmy.units.add(unit.id.get());
            }

            if (army.id.isEmpty().get()) {
                deserializableArmy.id = "";;
            }

            armyWrapper.armies.add(deserializableArmy);
        }

        try {
            String osType = System.getProperty("os.name");
            System.out.println(osType);
            File file;
            File directory;

            if (osType.contains("Windows")) {
                //Windows System
                logger.debug("Windows Operating System detected.");
                directory = new File(System.getProperty("user.home") + "/rbsg/");
                directory.mkdirs();
                file = new File(directory, fileName);
                Files.setAttribute(Paths.get(file.getPath()), "dos:hidden", true);
            } else {
                //Unix System
                logger.debug("Unix System detected.");
                directory = new File(System.getProperty("user.home") + "/.local/rbsg/");
                directory.mkdirs();
                file = new File(directory, fileName);
            }
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            } else {
                file.createNewFile();
            }
            objectMapper.writeValue(file, armyWrapper);
            logger.debug("Local saving was successful.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveArmies(ObservableList<Army> armies) {
        try {
            //Generate clean state
            List<Army> armyState = getArmiesService.queryArmies().get();
            armyState.stream().forEach(army -> deleteArmyService.deleteArmy(army));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        ArrayList<Army> armyList = new ArrayList<>();
        SaveArmyResponse saveArmyResponse;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        if (!armyList.isEmpty()) {
            this.saveArmiesLocal(armyList);
        }

        for (Army army : armies) {
            if (army.units.size() == 10) {
                //army is complete
                try {
                    saveArmyResponse = saveArmyOnline(army);
                    army.id.set(saveArmyResponse.data.id);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                } catch (JsonMappingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                army.id.set("");
            }
            armyList.add(army);
            if (!armyList.isEmpty()) {
                this.saveArmiesLocal(armyList);
            }
        }

    }

    public void setTestFileName(String fileName){
        this.fileName=fileName;
    }

    public static class DeserializableArmy{
        @JsonCreator
        public DeserializableArmy(@JsonProperty("id") String id, @JsonProperty("name")String name, @JsonProperty("units")ArrayList<String> units){
            this.id=id;
            this.name=name;
            this.units=units;
        }
        public String id;
        public String name;
        public ArrayList<String>units;
    }

    public static class ArmyWrapper{
        @JsonCreator
        public ArmyWrapper(@JsonProperty("armies") ArrayList<DeserializableArmy> armies){
            this.armies=armies;
        }
        public ArrayList<DeserializableArmy> armies;
    }
}
