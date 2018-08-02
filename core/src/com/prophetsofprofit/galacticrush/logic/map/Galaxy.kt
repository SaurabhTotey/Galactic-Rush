package com.prophetsofprofit.galacticrush.logic.map

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.prophetsofprofit.galacticrush.logic.base.Base
import com.prophetsofprofit.galacticrush.logic.base.Facility
import com.prophetsofprofit.galacticrush.logic.drone.Drone
import java.util.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A class that is basically the map that the game is played on
 * Contains a bunch of planets which are essentially the game 'tiles'
 * Planets are connected as a graph rather than sequentially
 */
class Galaxy(numPlanets: Int, playerIDs: List<Int>) {

    //The planets that are in the galaxy: serve as 'tiles' of the game, but are connected as a graph
    val planets = mutableListOf<Planet>()
    //The cosmic highways that are in the galaxy: serve as the 'paths' or 'connections' of the game
    val highways = mutableListOf<CosmicHighway>()
    //The drones that currently exist in the game; should be ordered in order of creation
    val drones: Array<Drone>
        get() {
            return this.planets.fold(mutableListOf<Drone>()) { list, currentPlanet -> list.addAll(currentPlanet.drones); list }.sortedBy { it.creationTime }.toTypedArray()
        }
    //The bases that currently exist in the game; ordered arbitrarily
    val bases: Array<Base>
        get() = this.planets.mapNotNull { it.base }.toTypedArray()

    /**
     * Empty constructor for serialization
     */
    constructor() : this(-1, listOf())

    /**
     * Galaxy constructor generates all the planets and terrain and values and such
     * Constructor works by evenly distributing planets across a square grid that has one
     * bounding box for each planet to be generated
     * Afterward, edges are generated by making all edges that are within
     * the side length of the bounding boxes used to generate the planets
     */
    init {
        generatePlanets(numPlanets)
        generateEdges(numPlanets)
        connectAllPlanets()
        val pickablePlanets = this.planets.toMutableList()
        var planetChoice: Planet
        for (i in 0 until playerIDs.size) {
            planetChoice = pickablePlanets.shuffled()[0]
            planetChoice.base = Base(playerIDs[i], planetChoice.id, arrayOf(Facility.HOME_BASE, Facility.CONSTUCTION, Facility.PROGRAMMING))
            pickablePlanets.remove(planetChoice)
        }
    }

    /**
     * Generates planets
     */
    private fun generatePlanets(numPlanets: Int) {
        //Calculates the side length of a square that fits a planet
        val sideLength = sqrt(numPlanets.toDouble()).toInt()
        /*
         * Iterates through all indices of x and y for the square and randomly places a planet in each bounding box
         */
        for (i in 0 until sideLength) {
            for (j in 0 until sideLength) {
                //Shifts x by i and y by j, and adds to it in a random number within a range of 0 to 1 / sidelength, the length of each bounding square
                this.planets.add(Planet(((Math.random() + i) / sideLength).toFloat(), ((Math.random() + j) / sideLength).toFloat(), (sideLength / 250.0 + Math.random() * sideLength / 500.0).toFloat() / numPlanets, this.planets.size))
            }
        }
    }

    private fun generateEdges(numPlanets: Int) {
        //Calculates the side length of a square that fits a planet
        val sideLength = sqrt(numPlanets.toDouble()).toInt()
        //Iterate through all planets in a random order
        for (p0 in planets.shuffled()) {
            //Iterate through all other planets in a random order
            for (p1 in planets.shuffled()) {
                //If the distance between the two planets is greater than 1 / sidelength or the planets are the same, go to next planet
                if (sqrt((p0.x - p1.x).pow(2) + (p0.y - p1.y).pow(2)) >= 2.0 / sideLength || p1 === p0) {
                    continue
                }
                //If the current planets can have a path that doesn't intersect an existing highway, or already is an existing highway, or intersect a planet, make a highway
                if (!highways.any { it ->
                    val planet0 = this.getPlanetWithId(it.p0)!!
                    val planet1 = this.getPlanetWithId(it.p1)!!
                            doSegmentsIntersect(p0.x, p0.y, p1.x, p1.y, planet0.x, planet0.y, planet1.x, planet1.y) || //Highways crosses existing highway
                                    (planet0 == p0 && planet1 == p1) || (planet0 == p1 && planet1 == p0) //Highway already exists but with p0 and p1 switched around
                        } && !planets.filter { it != p0 && it != p1 }.any {
                            Intersector.distanceSegmentPoint(p0.x, p0.y, p1.x, p1.y, it.x, it.y) <= it.radius
                        } //Highway doesn't intersect planet
                        && !this.highwaysConnectedTo(p1.id).any {
                            val planet0 = this.getPlanetWithId(it.p0)!!
                            val planet1 = this.getPlanetWithId(it.p1)!!
                            isAngleTooSmall(p0.x, p0.y, p1.x, p1.y, planet0.x, planet0.y, planet1.x, planet1.y)
                        } //Highway angle between others
                        && !this.highwaysConnectedTo(p0.id).any {
                            val planet0 = this.getPlanetWithId(it.p0)!!
                            val planet1 = this.getPlanetWithId(it.p1)!!
                            isAngleTooSmall(p0.x, p0.y, p1.x, p1.y, planet0.x, planet0.y, planet1.x, planet1.y)
                        } //is not too small

                ) {
                    //Add a highway to the galaxy and to the connecting planets
                    val highwayToAdd = CosmicHighway(p0.id, p1.id)
                    highways.add(highwayToAdd)
                }
            }
        }
    }

    /**
     * Ensures that all planets are connected
     * Currently only connects planets which are not connected to any planets
     * TODO: Check for separate clusters of planets using quick-union
     */
    private fun connectAllPlanets() {
        //Iterate through all planets which have no connecting planets
        for (p0 in planets.filter { this.highwaysConnectedTo(it.id).isEmpty() }) {
            //Initialize distance as 0 to start; will check for distance = 0
            var planetDistance = 0f
            //Closest planet starts as itself; will get changed
            var closestPlanet = p0
            //Iterate through all planets to find the planet closest to this one
            for (p1 in planets) {
                //Calculate distance between planets
                val tempDistance = sqrt((p0.x - p1.x).pow(2) + (p0.y - p1.y).pow(2))
                //If the planets are not the same and the distance between them is less, then set it to be so
                if (p0 !== p1 && (planetDistance == 0f || tempDistance < planetDistance)) {
                    planetDistance = tempDistance
                    closestPlanet = p1
                }
            }
            //Make a connection with the planet closest to it
            val highwayToAdd = CosmicHighway(p0.id, closestPlanet.id)
            highways.add(highwayToAdd)
        }
    }

    /**
     * Returns whether an intersection happens that isn't an intersection at the endpoints
     * Segments are p0 -> p1 and p2 -> p3
     */
    private fun doSegmentsIntersect(p0x: Float, p0y: Float, p1x: Float, p1y: Float, p2x: Float, p2y: Float, p3x: Float, p3y: Float): Boolean {
        val intersectionPoint = Vector2()
        val intersect = Intersector.intersectSegments(p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y, intersectionPoint)
        //Returns whether it intersects and that the intersection point isn't an endpoint
        return intersect && !(
                (intersectionPoint.x == p0x && intersectionPoint.y == p0y) ||
                        (intersectionPoint.x == p1x && intersectionPoint.y == p1y) ||
                        (intersectionPoint.x == p2x && intersectionPoint.y == p2y) ||
                        (intersectionPoint.x == p3x && intersectionPoint.y == p3y)
                )
    }

    /**
     * Returns whether or not the angle between the two given segments is too slim
     * The threshhold is defined as pi / 6 radians (30 degrees)
     */
    private fun isAngleTooSmall(p0x: Float, p0y: Float, p1x: Float, p1y: Float, p2x: Float, p2y: Float, p3x: Float, p3y: Float): Boolean {
        var angleBetween = (atan2(p1y - p0y, p1x - p0x) - atan2(p3y - p2y, p3x - p2x)) % PI
        if (angleBetween < 0) angleBetween += PI
        return angleBetween < PI / 6
    }

    /**
     * Returns all highways connected to the planet with the specified ID
     */
    fun highwaysConnectedTo(id: Int): Array<CosmicHighway> {
        return this.highways.filter { it.connects(id) != null }.toTypedArray()
    }

    /**
     * Gets al planets adjacent to the given planet
     */
    fun planetsAdjacentTo(id: Int): Array<Int> {
        return this.highways.mapNotNull { it.connects(id) }.toTypedArray()
    }

    /**
     * Gets the planet with a specified ID
     */
    fun getPlanetWithId(id: Int): Planet? {
        return this.planets.firstOrNull { it.id == id }
    }

    /**
     * Gets the drone with the specified drone information
     */
    fun getDroneWithId(id: Pair<Int, Date>?): Drone? {
        return this.drones.firstOrNull { it.id == id }
    }

}
