package com.prophetsofprofit.galacticrush.logic.map

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.prophetsofprofit.galacticrush.logic.drone.Drone
import com.prophetsofprofit.galacticrush.logic.facility.ConstructionFacility
import com.prophetsofprofit.galacticrush.logic.facility.HomeBase
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
        var pickablePlanets = this.planets
        var planetChoice: Planet
        for (i in 0 until playerIDs.size){
            planetChoice = planets.shuffled()[0]
            planetChoice.facilities.add(HomeBase(playerIDs[i]))
            planetChoice.facilities.add(ConstructionFacility(playerIDs[i]))
            pickablePlanets.remove(planetChoice)
        }
    }

    private fun generatePlanets(numPlanets: Int) {
        //Calculates the side length of a square that fits a planet
        val sideLength = sqrt(numPlanets.toDouble()).toInt()
        /*
         * Iterates through all indices of x and y for the square and randomly places a planet in each bounding box
         */
        for (i in 0 until sideLength) {
            for (j in 0 until sideLength) {
                //Shifts x by i and y by j, and adds to it in a random number within a range of 0 to 1 / sidelength, the length of each bounding square
                planets.add(Planet(((Math.random() + i) / sideLength).toFloat(), ((Math.random() + j) / sideLength).toFloat(), (sideLength / 250.0 + Math.random() * sideLength / 500.0).toFloat() / numPlanets))
                //TODO: This is a test; remove it for release
                for(i in 0 until (Math.random() * 6).toInt()) planets.last().drones.add(Drone((Math.random() * 1000000).toInt()))
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
                if (!highways.any {
                            doSegmentsIntersect(p0.x, p0.y, p1.x, p1.y, it.p0.x, it.p0.y, it.p1.x, it.p1.y) || //Highways crosses existing highway
                                    (it.p0 == p0 && it.p1 == p1) || (it.p0 == p1 && it.p1 == p0) //Highway already exists but with p0 and p1 switched around
                        } && !planets.filter { it != p0 && it != p1 }.any {
                            Intersector.distanceSegmentPoint(p0.x, p0.y, p1.x, p1.y, it.x, it.y) <= it.radius
                        }//Highway doesn't intersect planet
                        && !p1.connectedHighways.any { isAngleTooSmall(p0.x, p0.y, p1.x, p1.y, it.p0.x, it.p0.y, it.p1.x, it.p1.y) } //Highway angle between others
                        && !p0.connectedHighways.any { isAngleTooSmall(p0.x, p0.y, p1.x, p1.y, it.p0.x, it.p0.y, it.p1.x, it.p1.y) } //is not too small

                ) {
                    //Add a highway to the galaxy and to the connecting planets
                    val highwayToAdd = CosmicHighway(p0, p1)
                    highways.add(highwayToAdd)
                    p0.connectedHighways.add(highwayToAdd)
                    p1.connectedHighways.add(highwayToAdd)
                }
            }
        }
    }

    /**
     * Ensures that all planets are connected
     * Currently only connects planets which are not connected to any planets
     * TODO: Check for seperate clusters of planets using quick-union
     */
    private fun connectAllPlanets() {
        //Iterate through all planets which have no connecting planets
        for (p0 in planets.filter { it.connectedHighways.size == 0 }) {
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
            val highwayToAdd = CosmicHighway(p0, closestPlanet)
            highways.add(highwayToAdd)
            p0.connectedHighways.add(highwayToAdd)
            closestPlanet.connectedHighways.add(highwayToAdd)
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

}
