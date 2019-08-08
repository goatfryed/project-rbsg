package de.uniks.se19.team_g.project_rbsg.skynet.behaviour.exception;

import org.springframework.lang.NonNull;

public class AttackBehaviourException extends BehaviourException {

    public AttackBehaviourException(@NonNull final String message) {
        super(message);
    }
}