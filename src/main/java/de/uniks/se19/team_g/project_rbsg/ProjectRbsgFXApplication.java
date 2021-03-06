package de.uniks.se19.team_g.project_rbsg;

import de.uniks.se19.team_g.project_rbsg.bots.UserContext;
import de.uniks.se19.team_g.project_rbsg.bots.UserContextHolder;
import de.uniks.se19.team_g.project_rbsg.model.UserProvider;
import de.uniks.se19.team_g.project_rbsg.overlay.alert.AlertBuilder;
import de.uniks.se19.team_g.project_rbsg.server.websocket.WebSocketConfigurator;
import de.uniks.se19.team_g.project_rbsg.scene.DefaultExceptionHandler;
import de.uniks.se19.team_g.project_rbsg.scene.SceneConfiguration;
import de.uniks.se19.team_g.project_rbsg.scene.SceneManager;
import de.uniks.se19.team_g.project_rbsg.termination.Terminator;
import io.rincl.*;
import io.rincl.resourcebundle.*;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.beans.property.Property;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.Objects;

import static de.uniks.se19.team_g.project_rbsg.scene.SceneManager.SceneIdentifier.*;

/**
 * @author Jan Müller
 */
@Component
public class ProjectRbsgFXApplication extends Application implements Rincled {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 840;

    private ConfigurableApplicationContext context;

    public static void main(final String[] args) {
        launch(args);
    }

    /**
     * Initializes the Spring context with passed arguments
     */
    @Override
    public void init() {

        // set a default context. this is inherited by all child threads, unless overwritten in it
        UserContextHolder.setContext(new UserContext());

        ApplicationContextInitializer<AnnotationConfigApplicationContext> contextInitializer = applicationContext -> {
            applicationContext.registerBean(Parameters.class, this::getParameters);
            applicationContext.registerBean(HostServices.class, this::getHostServices);
        };

        //Initialisiert den Resource Loader für Rincl (I18N)
        Rincl.setDefaultResourceI18nConcern(new ResourceBundleResourceI18nConcern());

        this.context = new SpringApplicationBuilder()
                .sources(ProjectRbsgApplication.class)
                .initializers(contextInitializer)
                .web(WebApplicationType.NONE)
                .run(getParameters().getRaw().toArray(new String[0]));

        WebSocketConfigurator.userProvider = context.getBean(UserProvider.class);
    }


    @Override
    public void start(@NotNull final Stage primaryStage) {
        primaryStage.setWidth(WIDTH);
        primaryStage.setHeight(HEIGHT);
        primaryStage.setResizable(false);

        @SuppressWarnings("unchecked") final Property<Locale> selectedLocale = (Property<Locale>) context.getBean("selectedLocale");
        Objects.requireNonNull(selectedLocale).setValue(Locale.ENGLISH);

        setupMusic();

        final AlertBuilder alertBuilder = context.getBean(AlertBuilder.class);

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            alertBuilder
                    .confirmation(
                            AlertBuilder.Text.EXIT,
                            Platform::exit,
                            null);
        });


        context.getBean(SceneManager.class)
                .init(primaryStage)
                .withExceptionHandler(context.getBean(DefaultExceptionHandler.class))
                .setScene(SceneConfiguration.of(LOGIN));

        primaryStage.show();
    }

    protected void setupMusic() {
        MusicManager musicManager = context.getBean(MusicManager.class).init();
        boolean musicOnStartUp = context.getEnvironment().getProperty("defaults.music_enabled", Boolean.class, true);
        musicManager.setMusicRunning(musicOnStartUp);
    }

    @Override
    public void stop() {
        context.getBean(Terminator.class)
                .terminate();
        this.context.close();
    }
}
