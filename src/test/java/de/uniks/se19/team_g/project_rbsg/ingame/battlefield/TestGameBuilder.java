package de.uniks.se19.team_g.project_rbsg.ingame.battlefield;

import de.uniks.se19.team_g.project_rbsg.ingame.model.Cell;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Game;
import de.uniks.se19.team_g.project_rbsg.ingame.model.Unit;

import java.util.Arrays;

public class TestGameBuilder {

    /**
        Y -> player, 0 -> passable, X -> blocked
        OYOO
        XXOO
        -OOO
        --O-
     */
    public static Definition sampleGameAlpha() {
        Definition definition = new Definition(new Cell[4][4]);

        Game game = definition.game;
        Unit helicopterDick = definition.playerUnit;
        Unit enemy = definition.otherUnit;
        helicopterDick.setMp(4);
        game.withUnit(helicopterDick);
        Cell[][] cells = definition.cells;
        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 4; column++) {
                final Cell cell = new Cell(String.format("%d:%d", row, column));
                cell.setPassable(true);
                cell.setX(row);
                cell.setY(column);
                cells[row][column] = cell;
                if (row > 0) {
                    cell.setTop(cells[row-1][column]);
                }
                if (column > 0) {
                    cell.setLeft(cells[row][column-1]);
                }
            }
        }
        Cell startCell = cells[0][1];
        helicopterDick.setPosition(startCell);
        cells[1][0].setPassable(false);
        cells[1][1].setPassable(false);

        game.withCells(
                Arrays.stream(cells)
                        .flatMap(Arrays::stream)
                        .toArray(Cell[]::new)
        );

        return definition;
    }

    public static class Definition {
        final public Game game = new Game("game");
        final public Unit playerUnit = new Unit("Helicopter Dick");
        final public Unit otherUnit = new Unit("enemy");
        final public Cell[][] cells;

        public Definition(Cell[][] cells) {
            this.cells = cells;
        }
    }
}
