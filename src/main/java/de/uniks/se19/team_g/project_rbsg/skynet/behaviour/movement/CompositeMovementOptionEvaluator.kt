package de.uniks.se19.team_g.project_rbsg.skynet.behaviour.movement

import de.uniks.se19.team_g.project_rbsg.ingame.model.Cell
import de.uniks.se19.team_g.project_rbsg.ingame.model.Unit

class CompositeMovementOptionEvaluator : MovementOptionEvaluator {

    override fun compare(first: MovementOption, second: MovementOption): Int = when {
            first.tour === second.tour -> compareEnemies(first, second)
            first.enemy === second.enemy -> compareTours(first, second)
            else -> compareMovementOptions(first, second)
        }

    private fun compareMovementOptions(first: MovementOption, second: MovementOption) : Int =
            //prefer tourComparison, unless tours are equally viable
            when (val tourComparison = compareTours(first, second)) {
                0 -> compareEnemies(first, second)
                else -> tourComparison
            }

    private fun compareEnemies(first: MovementOption, second: MovementOption) : Int {
        val firstAttackValue = first.unit.getAttackValue(first.enemy.unit)
        val secondAttackValue = second.unit.getAttackValue(second.enemy.unit)

        val firstEnemyThreats = first.enemy.threats().size
        val secondEnemyThreats = second.enemy.threats().size

        return when {
            firstEnemyThreats != secondEnemyThreats -> preferSmaller(firstEnemyThreats, secondEnemyThreats) //prefer enemies which are not fighting already
            firstAttackValue != secondAttackValue -> preferBigger(firstAttackValue, secondAttackValue) //prefer enemies which take more damage
            first.enemy.threatLevel != second.enemy.threatLevel -> preferBigger(first.enemy.threatLevel, second.enemy.threatLevel) //prefer enemies with higher threat level
            first.unit.hp != second.unit.hp -> preferSmaller(first.unit.hp, second.unit.hp) //prefer enemies with less health
            else -> 0
        }
    }

    private fun compareTours(first: MovementOption, second: MovementOption) : Int =  when {
        first.distanceToEnemy != second.distanceToEnemy -> preferSmaller(first.distanceToEnemy, second.distanceToEnemy)
        else -> compareDestinations(first, second)
    }

    private fun compareDestinations(first: MovementOption, second: MovementOption) : Int {
        val firstTargets = first.destination.attackableNeighbors(first.unit).size
        val secondTargets = second.destination.attackableNeighbors(second.unit).size

        val firstThreats = first.destination.threateningNeighbors(first.unit).size
        val secondThreats = second.destination.threateningNeighbors(second.unit).size

        return when {
            firstTargets != secondTargets -> preferBigger(firstTargets, secondTargets) //prefer destinations with more targets
            firstThreats != secondThreats -> preferSmaller(firstThreats, secondThreats) //prefer destinations with less threats
            else -> 0
        }
    }

    private fun preferSmaller(first : Int, second : Int) : Int = (first - second)

    private fun preferBigger(first : Int, second : Int) : Int = (second - first)

    private fun Cell.attackableNeighbors(unit : Unit) : List<Unit> =
            this.neighbors.mapNotNull { it.unit }.filter { unit.canAttack(it) }

    private fun Cell.threateningNeighbors(unit : Unit): List<Unit> =
            this.neighbors.mapNotNull { it.unit }.filter { it.canAttack(unit) }

    private fun Enemy.threats() : List<Unit> =
            this.position.threateningNeighbors(this.unit)
}
