package com.prophetsofprofit.galacticrush.graphics.screen.maingame.overlay.planetoverlay.baseoverlay

import com.badlogic.gdx.scenes.scene2d.Group
import com.prophetsofprofit.galacticrush.graphics.screen.maingame.MainGameScreen
import kotlin.math.abs

/**
 * Handles all actors that appear when a planet with a base on it is selected
 */
class BaseOverlay(val gameScreen: MainGameScreen, val yOffset: Float): Group() {

    //Shows all facilities on the planet
    val facilitiesPanel = BaseFacilitiesPanel(this.gameScreen, this.gameScreen.game.camera.viewportWidth / 6, 2 * this.gameScreen.game.camera.viewportHeight / 3, this.yOffset)
    //Handles all the buttons for facility actions
    val actionsPanel = BaseActionButtons(this.gameScreen,
            this.facilitiesPanel.baseLabel.x,
            this.facilitiesPanel.baseLabel.y,
            this.facilitiesPanel.baseLabel.width,
            this.facilitiesPanel.baseLabel.height / 3
            )
    //Whether the panels are inside the camera view
    var isInView = false
    //How fast the panels slide into view
    val zoomSpeed = 0.1f

    /**
     * Sets up all the buttons and panels
     */
    init {
        this.addActor(this.facilitiesPanel)
        this.addActor(this.actionsPanel)
    }

    /**
     * Become visible if the planet selected has a base
     */
    fun update() {
        if (this.gameScreen.selectedPlanet != null && this.gameScreen.selectedPlanet!!.facilities.any { it.ownerId == this.gameScreen.player.id }) {
            //If the overlay is becoming visible
            if (!this.isInView) {
                //Ensure that there is not already a move command for the panels in place
                this.gameScreen.movementHandler.queue.remove(this)
                //Start moving the base overlay
                this.gameScreen.movementHandler.add(this,
                        this.facilitiesPanel.baseLabel.width - this.x,
                        0f,
                        this.zoomSpeed * abs(this.facilitiesPanel.baseLabel.width - this.x) / this.facilitiesPanel.baseLabel.width)
            }
            this.facilitiesPanel.update()
            this.actionsPanel.update()
            this.isInView = true
        } else {
            if (this.isInView) {
                //Ensure that there is not already a move command for the panels in place
                this.gameScreen.movementHandler.queue.remove(this)
                //Start moving the base overlay
                this.gameScreen.movementHandler.add(this,
                        - this.x,
                        0f,
                        this.zoomSpeed * abs( - this.x) / this.facilitiesPanel.baseLabel.width)
            }
            //If the overlay is becoming invisible
            this.isInView = false
        }
    }

}
