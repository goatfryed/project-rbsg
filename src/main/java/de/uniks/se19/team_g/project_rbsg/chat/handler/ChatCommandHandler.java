package de.uniks.se19.team_g.project_rbsg.chat.handler;

import de.uniks.se19.team_g.project_rbsg.chat.controller.ChatChannelController;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * @author Jan Müller
 */
@FunctionalInterface
public interface ChatCommandHandler {

    void handleCommand(@NonNull final ChatChannelController callback, @Nullable final String options) throws Exception;
}
