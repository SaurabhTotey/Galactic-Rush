package com.prophetsofprofit.galacticrush.graphics.screen.maingame.overlay.planetoverlay.baseoverlay

import com.badlogic.gdx.scenes.scene2d.Group
import com.prophetsofprofit.galacticrush.graphics.screen.maingame.MainGameScreen

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

    /**
     * Sets up all the buttons and panels
     */
    init {
        this.addActor(this.facilitiesPanel)
        this.addActor(this.actionsPanel)
        this.isVisible = false
    }

    /**
     * Become visible if the planet selected has a base
     */
    fun update() {
        this.facilitiesPanel.update()
        this.actionsPanel.update()
        this.isVisible = this.gameScreen.selectedPlanet!!.facilities.any { it.ownerId == this.gameScreen.player.id }
    }

}
