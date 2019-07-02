package de.uniks.se19.team_g.project_rbsg.army_builder;

import de.uniks.se19.team_g.project_rbsg.model.Unit;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

@SuppressWarnings("unused")
public class ArmyBuilderState {

    public final SimpleObjectProperty<Unit> selectedUnit = new SimpleObjectProperty<>();

    public final BooleanProperty unsavedUpdates = new SimpleBooleanProperty(false);

    public void setUnsavedUpdates(boolean isDirty)
    {
        unsavedUpdates.setValue(isDirty);
    }

    public boolean hasUnsavedUpdates()
    {
        return unsavedUpdates.get();
    }
}
