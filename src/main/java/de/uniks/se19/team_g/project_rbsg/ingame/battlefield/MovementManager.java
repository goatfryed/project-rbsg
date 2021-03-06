package de.uniks.se19.team_g.project_rbsg.ingame.battlefield;

import de.uniks.se19.team_g.project_rbsg.ingame.model.Cell;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Unit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * The movement manager should provide information about cells that can be reached for a given unit
 */
@Component
public class MovementManager {

    private final MovementEvaluator movementEvaluator;

    private Map<Unit, Entry> managedUnits = new HashMap<>();

    public MovementManager(MovementEvaluator movementEvaluator) {

        this.movementEvaluator = movementEvaluator;
    }

    @Nullable
    public Tour getTour(final @Nonnull Unit unit, final @Nonnull Cell target) {
        manageUnit(unit);

        if (target.getUnit() != null) {
            return null;
        }

        Entry entry = managedUnits.get(unit);
        if (entry.allowedTours == null) {
            entry.allowedTours = movementEvaluator.getAllowedTours(unit);
        }

        return entry.allowedTours.get(target);
    }

    private void manageUnit(final @Nonnull Unit unit) {
        if (managedUnits.containsKey(unit)) {
            return;
        }
        Entry entry = new Entry();
        WeakChangeListener<Object> listener = new WeakChangeListener<>(entry);
        unit.positionProperty().addListener(listener);
        unit.remainingMovePointsProperty().addListener(listener);

        managedUnits.put(unit, entry);
    }

    private static class Entry
        implements ChangeListener<Object>
    {
        public Map<Cell, Tour> allowedTours = null;

        @Override
        public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
            allowedTours = null;
        }
    }
}
