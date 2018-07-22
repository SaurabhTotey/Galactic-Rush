package com.prophetsofprofit.galacticrush.logic.drone;

import com.badlogic.gdx.graphics.Texture
import com.prophetsofprofit.galacticrush.logic.map.Planet
import java.util.*

val baseDroneImage = Texture("image/drone/base.png")

/**
 * A class that represent's a player's drone
 * Is the main unit of the game that carries out instructions
 * Is what is used to achieve victory
 * Location stores the id of the planet
 */
class Drone(val ownerId: Int, var locationId: Int) {

    //When the drone was initialized: the game assumes that this is unique for each drone which is kinda hacky
    val creationTime = Date()
    //Which instruction the drone is currently reading
    var pointer = 0
    //How much base damage the drone deals when attacking
    var attack = 5


    //Whether the drone is done completing its command queue
    var queueFinished = false

    /**
     * Empty constructor for serialization
     */
    constructor() : this(-1, -1)

    //The following methods interface with the drone's instruction structure

    /**
     * Adds an instruction to the end of the drone's task list
     */
    /*fun add(instructionMaker: InstructionMaker): Boolean {
        val instruction = instructionMaker.createInstructionInstanceFor(this)
        if (this.memoryAvailable < instruction.memory) return false
        if (!instruction.add()) return false
        this.instructions.add(instruction)
        return true
    }

    /**
     * Swaps the positions of two instructions in the drone's task list
     */
    fun swap(index1: Int, index2: Int): Boolean {
        if (index1 >= this.instructions.size || index2 >= this.instructions.size) return false
        val placeholder = this.instructions[index1]
        this.instructions[index1] = this.instructions[index2]
        this.instructions[index1].location = index1
        this.instructions[index2] = placeholder
        this.instructions[index2].location = index2
        return true
    }

    /**
     * Pops the last instruction of the drone's task list, freeing memory
     */
    fun pop(): Boolean {
        if (this.instructions.size == 0) return false
        this.instructions[this.instructions.size - 1].remove()
        this.instructions.removeAt(this.instructions.size - 1)
        return true
    }

    /**
     * Does the queued action
     */
    fun completeAction() {
        if (this.instructions.isEmpty()) {
            this.queueFinished = true
            return
        }
        this.instructions[this.pointer].act()
        this.advancePointer(1)
    }

    /**
     * Calls startCycle for all instructions in the queue
     */
    fun startCycle() {
        this.instructions.forEach { it.startCycle() }
    }

    /**
     * Calls endCyclye for all instructions in the queue
     */
    fun endCycle() {
        this.instructions.forEach { it.endCycle() }
    }

    /**
     * Advances the pointer by some number of steps, changing what the drone will read next
     */
    fun advancePointer(steps: Int) {
        this.pointer += steps
        if (this.pointer >= this.instructions.size) {
            this.pointer = this.instructions.size - 1
            this.queueFinished = true
        }
    }

    /**
     * Resets the drone's pointer for the next turn
     */
    fun resetQueue() {
        this.pointer = 0
        this.queueFinished = false
    }

    /**
     * Attempts to distribute damage among instructions
     */
    fun takeDamage(damage: Int) {
        /*
         * Attempts to distribute damage among instructions
         * Loops though instructions, damaging them one at a time until all damage has been done
         */
        var instructionToDamage = 0
        for (i in 0 until damage) {
            this.instructions[instructionToDamage].takeDamage()
            //If the instruction's health reaches zero, destroy it and do not increment index
            // (because it is removed from the list and the list's length changes)
            if (this.instructions[instructionToDamage].health <= 0) {
                this.instructions[instructionToDamage].getDestroyed()
            } else {
                //Increment locationId index
                instructionToDamage = (instructionToDamage + 1) % this.instructions.size
            }
        }
        //If the drone has no instructions left it dies a sad and lonely death
        if (this.instructions.isEmpty()) this.getDestroyed()
    }*/

    /**
     * Remove the drone from the world's list of drones so it can be garbage collected
     * TODO
     */
    fun getDestroyed() {

    }

    /**
     * Gets the locationId of the drone among a list of planets, or null if it does not exist
     */
    fun getLocationAmong(planets: Array<Planet>): Planet? {
        return planets.firstOrNull { it.id == this.locationId }
    }

    /**
     * How the drone will be displayed on the planet listing
     */
    override fun toString(): String {
        return "Drone ($ownerId)"
    }
}