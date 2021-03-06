package de.uniks.se19.team_g.project_rbsg.ingame.model;

import static org.junit.Assert.*;

import de.uniks.se19.team_g.project_rbsg.configuration.flavor.UnitTypeInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Jan Müller
 */
public class ModelTests {

    private static final String ALICE = "Alice";
    private static final String BOB = "Bob";

    private static final String RED = "RED";
    private static final String BLUE = "BLUE";

    @Test
    public void testAssociations() {
        //init
        final Game game = new Game("");

        final Cell forest = new Cell("")
                .setBiome(Biome.FOREST)
                .setPassable(true)
                .setX(0)
                .setY(0);
        final Cell grass = new Cell("")
                .setBiome(Biome.GRASS)
                .setPassable(true)
                .setX(1)
                .setY(0);
        final Cell mountain = new Cell("")
                .setBiome(Biome.MOUNTAIN)
                .setPassable(false)
                .setX(0)
                .setY(1);
        final Cell water = new Cell("")
                .setBiome(Biome.WATER)
                .setPassable(false)
                .setX(1)
                .setY(1);

        forest.setRight(grass)
                .setBottom(mountain);
        water.setTop(grass)
                .setLeft(mountain);

        game.withCells(forest, grass, null, water);
        mountain.setGame(game);

        final Player alice = new Player("")
                .setName(ALICE)
                .setColor(RED);
        final Player bob = new Player("")
                .setName(BOB)
                .setColor(BLUE);

        game.withPlayers(null, alice);
        bob.setCurrentGame(game);

        final Collection<UnitTypeInfo> canAttack = Arrays.asList(UnitTypeInfo._CHOPPER, UnitTypeInfo._JEEP);

        final Unit aliceChopper5And10 = new Unit("")
                .setUnitType(UnitTypeInfo._CHOPPER)
                .setMp(5)
                .setHp(10)
                .setCanAttack(canAttack);
        final Unit aliceJeep1And5 = new Unit("")
                .setUnitType(UnitTypeInfo._JEEP)
                .setMp(1)
                .setHp(5)
                .setCanAttack(canAttack);
        final Unit bobChopper10And10 = new Unit("")
                .setUnitType(UnitTypeInfo._CHOPPER)
                .setMp(10)
                .setHp(10)
                .setCanAttack(canAttack);

        game.withUnits(aliceChopper5And10, aliceJeep1And5);

        alice.withUnits(aliceChopper5And10, aliceJeep1And5);

        bobChopper10And10.setGame(game)
                .setLeader(bob);

        aliceChopper5And10.setPosition(water);
        aliceJeep1And5.setPosition(grass);
        mountain.setUnit(bobChopper10And10);

        //asserts
        assertTrue(game.getPlayers().containsAll(Arrays.asList(alice, bob)));
        assertEquals(2, game.getPlayers().size());
        assertTrue(game.getUnits().containsAll(Arrays.asList(aliceChopper5And10, aliceJeep1And5, bobChopper10And10)));
        assertEquals(3, game.getUnits().size());
        assertTrue(game.getCells().containsAll(Arrays.asList(forest, grass, mountain, water)));
        assertEquals(4, game.getCells().size());

        assertEquals(game, alice.getCurrentGame());
        assertEquals(game, bob.getCurrentGame());
        assertEquals(game, forest.getGame());
        assertEquals(game, grass.getGame());
        assertEquals(game, mountain.getGame());
        assertEquals(game, water.getGame());
        assertEquals(game, aliceChopper5And10.getGame());
        assertEquals(game, aliceJeep1And5.getGame());
        assertEquals(game, bobChopper10And10.getGame());

        assertEquals(game, forest.getGame());
        assertEquals(Biome.FOREST, forest.getBiome());
        assertTrue(forest.isPassable());
        assertEquals(0, forest.getX());
        assertEquals(0, forest.getY());
        assertNull(forest.getLeft());
        assertNull(forest.getTop());
        assertEquals(grass, forest.getRight());
        assertEquals(mountain, forest.getBottom());
        assertNull(forest.unitProperty().get());
        assertNull(forest.getTopLeft());
        assertNull(forest.getTopRight());
        assertEquals(water, forest.getBottomRight());
        assertNull(forest.getBottomLeft());

        assertEquals(game, grass.getGame());
        assertEquals(Biome.GRASS, grass.getBiome());
        assertTrue(grass.isPassable());
        assertEquals(1, grass.getX());
        assertEquals(0, grass.getY());
        assertEquals(forest, grass.getLeft());
        assertNull(grass.getTop());
        assertNull(grass.getRight());
        assertEquals(water, grass.getBottom());
        assertEquals(aliceJeep1And5, grass.unitProperty().get());
        assertNull(grass.getTopLeft());
        assertNull(grass.getTopRight());
        assertNull(grass.getBottomRight());
        assertEquals(mountain, grass.getBottomLeft());

        assertEquals(game, mountain.getGame());
        assertEquals(Biome.MOUNTAIN, mountain.getBiome());
        assertFalse(mountain.isPassable());
        assertEquals(0, mountain.getX());
        assertEquals(1, mountain.getY());
        assertNull(mountain.getLeft());
        assertEquals(forest, mountain.getTop());
        assertEquals(water, mountain.getRight());
        assertNull(mountain.getBottom());
        assertEquals(bobChopper10And10, mountain.unitProperty().get());
        assertNull(mountain.getTopLeft());
        assertEquals(grass, mountain.getTopRight());
        assertNull(mountain.getBottomRight());
        assertNull(mountain.getBottomLeft());

        assertEquals(game, water.getGame());
        assertEquals(Biome.WATER, water.getBiome());
        assertFalse(water.isPassable());
        assertEquals(1, water.getX());
        assertEquals(1, water.getY());
        assertEquals(mountain, water.getLeft());
        assertEquals(grass, water.getTop());
        assertNull(water.getRight());
        assertNull(water.getBottom());
        assertEquals(aliceChopper5And10, water.unitProperty().get());
        assertEquals(forest, water.getTopLeft());
        assertNull(water.getTopRight());
        assertNull(water.getBottomRight());
        assertNull(water.getBottomLeft());

        assertEquals(game, alice.getCurrentGame());
        assertEquals(ALICE, alice.getName());
        assertEquals(RED, alice.getColor());
        assertTrue(alice.getUnits().containsAll(Arrays.asList(aliceChopper5And10, aliceJeep1And5)));
        assertEquals(2, alice.getUnits().size());

        assertEquals(game, bob.getCurrentGame());
        assertEquals(BOB, bob.getName());
        assertEquals(BLUE, bob.getColor());
        assertTrue(bob.getUnits().contains(bobChopper10And10));
        assertEquals(1, bob.getUnits().size());

        assertEquals(game, aliceChopper5And10.getGame());
        assertEquals(alice, aliceChopper5And10.getLeader());
        assertEquals(water, aliceChopper5And10.positionProperty().get());
        assertEquals(UnitTypeInfo._CHOPPER, aliceChopper5And10.getUnitType());
        assertEquals(5, aliceChopper5And10.getMp());
        assertEquals(10, aliceChopper5And10.getHp());
        assertEquals(canAttack, aliceChopper5And10.getCanAttack());

        assertEquals(game, aliceJeep1And5.getGame());
        assertEquals(alice, aliceJeep1And5.getLeader());
        assertEquals(grass, aliceJeep1And5.positionProperty().get());
        assertEquals(UnitTypeInfo._JEEP, aliceJeep1And5.getUnitType());
        assertEquals(1, aliceJeep1And5.getMp());
        assertEquals(5, aliceJeep1And5.getHp());
        assertEquals(canAttack, aliceJeep1And5.getCanAttack());

        assertEquals(game, bobChopper10And10.getGame());
        assertEquals(bob, bobChopper10And10.getLeader());
        assertEquals(mountain, bobChopper10And10.positionProperty().get());
        assertEquals(UnitTypeInfo._CHOPPER, bobChopper10And10.getUnitType());
        assertEquals(10, bobChopper10And10.getMp());
        assertEquals(10, bobChopper10And10.getHp());
        assertEquals(canAttack, bobChopper10And10.getCanAttack());

        //action
        aliceJeep1And5.remove();

        //asserts
        assertFalse(game.getUnits().contains(aliceJeep1And5));
        assertEquals(2, game.getUnits().size());

        assertNull(grass.unitProperty().get());

        assertFalse(alice.getUnits().contains(aliceJeep1And5));
        assertEquals(1, alice.getUnits().size());

        assertNull(aliceJeep1And5.getGame());
        assertNull(aliceJeep1And5.getLeader());
        assertNull(aliceJeep1And5.positionProperty().get());
        assertEquals(UnitTypeInfo._JEEP, aliceJeep1And5.getUnitType());
        assertEquals(1, aliceJeep1And5.getMp());
        assertEquals(5, aliceJeep1And5.getHp());
        assertEquals(canAttack, aliceJeep1And5.getCanAttack());


        //action
        water.remove();

        //asserts
        assertFalse(game.getCells().contains(water));
        assertEquals(3, game.getCells().size());

        assertNull(water.getGame());
        assertEquals(Biome.WATER, water.getBiome());
        assertFalse(water.isPassable());
        assertEquals(1, water.getX());
        assertEquals(1, water.getY());
        assertNull(water.getLeft());
        assertNull(water.getTop());
        assertNull(water.getRight());
        assertNull(water.getBottom());
        assertNull(water.unitProperty().get());
        assertNull(water.getTopLeft());

        assertNull(mountain.getRight());
        assertNull(grass.getBottom());
        assertNull(forest.getBottomRight());

        //action
        bob.remove();

        //asserts
        assertFalse(game.getPlayers().contains(bob));
        assertEquals(1, game.getPlayers().size());

        assertNull(bobChopper10And10.getLeader());

        //action
        game.remove();

        //asserts
        assertTrue(game.getPlayers().isEmpty());
        assertTrue(game.getUnits().isEmpty());
        assertTrue(game.getCells().isEmpty());

        assertNull(forest.getGame());
        assertNull(grass.getGame());
        assertNull(mountain.getGame());
        assertNull(water.getGame());

        assertNull(alice.getCurrentGame());
        assertNull(bob.getCurrentGame());

        assertNull(aliceChopper5And10.getGame());
        assertNull(aliceJeep1And5.getGame());
        assertNull(bobChopper10And10.getGame());
    }
}
