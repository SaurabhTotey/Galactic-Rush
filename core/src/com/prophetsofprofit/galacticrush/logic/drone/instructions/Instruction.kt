package com.prophetsofprofit.galacticrush.logic.drone.instructions

import com.badlogic.gdx.graphics.g2d.Sprite
import com.prophetsofprofit.galacticrush.logic.drone.Drone
import com.prophetsofprofit.galacticrush.logic.map.Planet

/**
 * There are eight types of instructions
 * They are described as integer powers of 2 for the use of Kotlin infix operators (bitwise logic)
 * Distinguishing between the types is useful for gameplay and for other instructions to affect
 * To create an instruction with multiple types, use the bitwise or (e.g. MINING or UPGRADE)
 * To check if an instruction is of a certain type, use the bitwise and (e.g. instruction.type and ORDER)
 */
enum class InstructionType(val value: Int) {
    NONE(0),            //Miscellaneous
    COMBAT(1),          //Instructs the drone to do something combat-related, like attack another drone
    MOVEMENT(2),        //Instructs the drone to travel to another planet
    CONSTRUCTION(4),    //Instructs the drone to modify the world or construct facilities
    MINING(8),          //Instructs the drone to gather resources
    MODIFICATION(16),   //Modifies the effects of other instructions in the queue
    UPGRADE(32),        //Modifies the properties of the drone
    ORDER(64)           //Modifies the order in which commands are executed in the queue
}

/**
 * An instruction is a slottable modification to a drone that either defines its behavior or modifies its stats
 * This class defines an instruction and provides default implementations for the necessary methods
 * It also contains the definitions of instruction
 */
enum class Instruction(var maxHealth: Int, val memory: Int, val type: InstructionType, var location: Int, val sprite: Sprite, val drone: Drone) {

    NOTHING(0, 0, InstructionType.NONE, 0, Sprite(), Drone(0));

    //-----------METHODS BELOW----------------------------------------------------------------------

    //If these are null, they should be chosen when making a related action
    //Otherwise another activity might change them in order to control selection
    var selectedPlanet: Planet? = null
    var selectedDrone: Drone? = null

    /**
     * What the instruction does when added to an instruction queue
     */
    fun add(): Boolean {
        return true
    }

    /**
     * What the instruction does when removed from an instruction queue
     */
    fun remove(): Boolean {
        return true
    }

    /**
     * What the instruction does at the end of every turn when its turn in the queue arrives
     */
    fun act(): Boolean {
        this.selectedPlanet = null
        this.selectedDrone = null
        return true
    }

    /**
     * What happens when the instruction sustains enough damage to take its health to 0
     */
    fun getDestroyed(): Boolean {
        this.remove()
        return true
    }

}