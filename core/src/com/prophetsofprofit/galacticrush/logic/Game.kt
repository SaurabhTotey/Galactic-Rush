package com.prophetsofprofit.galacticrush.logic

import com.prophetsofprofit.galacticrush.logic.drone.Drone
import com.prophetsofprofit.galacticrush.logic.map.Galaxy
import com.prophetsofprofit.galacticrush.logic.player.Player

/**
 * The main game object
 * Handles attributes of the current game, and is serialized for networking
 */
class Game(val players: Array<Int>, galaxySize: Int) {

    /**
     * Empty constructor for serialization
     */
    constructor(): this(arrayOf(), -1)

    //The board or map on which this game is played
    val galaxy = Galaxy(galaxySize, players.toList())
    //The amount of turns that have passed since the game was created
    var turnsPlayed = 0
    //The players who need to submit their changes for the drones to commence
    val waitingOn = this.players.toMutableList()
    //The drones that currently exist in the game
    //Should be ordered in order of creation
    val drones = mutableListOf<Drone>()

    /**
     * A method that collects changes, verifies their integrity, and then applies them to the game
     */
    fun collectChange(change: Change) {
        if (waitingOn.contains(change.ownerId)) {
            waitingOn.remove(change.ownerId)
        }
    }

    /**
     * Performs one action per drone for all drones that can perform an action; won't be callable until game is ready
     */
    fun doDroneTurn() {
        //If waiting on players don't do anything
        if (waitingOn.isNotEmpty()) {
            return
        }
        //Complete the actions of all the drones who can do actions in the queue
        this.drones.filterNot { it.queueFinished }.forEach { it.completeAction() }
        //If all the drones are now finished, wait for players and reset drones
        if (this.drones.all { it.queueFinished }) {
            this.drones.forEach { it.resetQueue() }
            players.mapTo(waitingOn) { it }
        }
    }

}
