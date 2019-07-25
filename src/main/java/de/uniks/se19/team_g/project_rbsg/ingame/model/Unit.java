package de.uniks.se19.team_g.project_rbsg.ingame.model;

import de.uniks.se19.team_g.project_rbsg.configuration.flavor.UnitTypeInfo;
import javafx.beans.property.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jan Müller
 */
public class Unit {

    @NonNull
    private final String id;

    private Game game;

    private Player leader;

    private SimpleObjectProperty<Cell> position = new SimpleObjectProperty<>();

    private SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

    private SimpleBooleanProperty attackable = new SimpleBooleanProperty(false);

    private UnitTypeInfo unitType;

    final private SimpleIntegerProperty remainingMovePoints = new SimpleIntegerProperty(0);

    private int mp;
    private int hp;

    private ArrayList<UnitTypeInfo> canAttack;

    public Unit(@NonNull final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public Player getLeader() {
        return leader;
    }

    public ReadOnlyObjectProperty<Cell> positionProperty() {
        return position;
    }

    public Cell getPosition() {
        return position.get();
    }

    public UnitTypeInfo getUnitType() {
        return unitType;
    }

    public int getMp() {
        return mp;
    }

    public int getHp() {
        return hp;
    }

    public ArrayList<UnitTypeInfo> getCanAttack() {
        return canAttack;
    }

    public Unit setGame(@Nullable final Game game) {
        if (this.game == game) return this;
        if (this.game != null) this.game.doRemoveUnit(this);
        doSetGame(game);
        if (game != null) game.doAddUnit(this);
        return this;
    }

    Unit doSetGame(@Nullable final Game game) {
        this.game = game;
        return this;
    }

    public Unit setLeader(@Nullable final Player leader) {
        if (this.leader == leader) return this;
        if (this.leader != null) this.leader.doRemoveUnit(this);
        doSetLeader(leader);
        if (leader != null) leader.doAddUnit(this);
        return this;
    }

    void doSetLeader(@Nullable final Player leader) {
        this.leader = leader;
    }

    public Unit setPosition(@Nullable final Cell position) {
        if (this.position.get() == position) return this;
        if (this.position.get() != null) this.position.get().doSetUnit(null);
        if (position != null) position.doSetUnit(this);
        doSetPosition(position);
        return this;
    }

    void doSetPosition(@Nullable final Cell position) {
        this.position.set(position);
    }

    public Unit setUnitType(@NonNull final UnitTypeInfo unitType) {
        this.unitType = unitType;
        return this;
    }

    public Unit setMp(@NonNull final int mp) {
        this.mp = mp;
        return this;
    }

    public Unit setHp(@NonNull final int hp) {
        this.hp = hp;
        return this;
    }

    public Unit setCanAttack(@NonNull final Collection<UnitTypeInfo> canAttack) {
        this.canAttack = new ArrayList<>(canAttack);
        return this;
    }

    public void remove() {
        setGame(null);
        setLeader(null);
        setPosition(null);
    }

    @Override
    public String toString() {
        return "(" + unitType + ", mp : " + mp + ", hp: " + hp + ")";
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }


    public int getRemainingMovePoints() {
        return remainingMovePoints.get();
    }

    public ReadOnlyIntegerProperty remainingMovePointsProperty() {
        return remainingMovePoints;
    }

    public void setRemainingMovePoints(int remainingMovePoints) {
        this.remainingMovePoints.set(remainingMovePoints);
    }

    public boolean isAttackable() {
        return attackable.get();
    }

    public SimpleBooleanProperty attackableProperty() {
        return attackable;
    }

    public void setAttackable(boolean attackable) {
        this.attackable.set(attackable);
    }
}