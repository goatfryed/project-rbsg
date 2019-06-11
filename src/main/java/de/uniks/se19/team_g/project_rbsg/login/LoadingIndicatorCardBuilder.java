package de.uniks.se19.team_g.project_rbsg.login;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

import java.io.IOException;

/**
 * @author Keanu Stückrad
 */
public class LoadingIndicatorCardBuilder {

    @FXML
    private Label progressLabel;
    @FXML
    private ProgressIndicator progressIndicator;

    private FXMLLoader fxmlLoader;
    private Node loadingIndicatorView;
    private SimpleDoubleProperty progress;

    public Node buildProgressIndicatorCard(){
        if(fxmlLoader == null) {
            fxmlLoader = new FXMLLoader(getClass().getResource("loading-indicator.fxml"));
            fxmlLoader.setController(this);
            try {
                loadingIndicatorView = fxmlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(progress == null){
            progress = new SimpleDoubleProperty(0.0);
        }
        progressIndicator.progressProperty().bind(progress);
        Timeline progressTimeline = setupTimeline();
        progressTimeline.setCycleCount(Animation.INDEFINITE);
        progressTimeline.play();
        return loadingIndicatorView;
    }

    private Timeline setupTimeline() {
        return new Timeline(
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(progress, 1.0)
                )
        );
    }

}