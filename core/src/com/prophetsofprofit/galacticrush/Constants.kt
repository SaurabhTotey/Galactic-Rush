package com.prophetsofprofit.galacticrush

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.Json
import com.badlogic.gdx.utils.JsonWriter
import com.esotericsoftware.kryo.Kryo
import com.prophetsofprofit.galacticrush.logic.Game
import com.prophetsofprofit.galacticrush.logic.GamePhase
import com.prophetsofprofit.galacticrush.logic.base.Base
import com.prophetsofprofit.galacticrush.logic.base.Facility
import com.prophetsofprofit.galacticrush.logic.change.Change
import com.prophetsofprofit.galacticrush.logic.change.PlayerChange
import com.prophetsofprofit.galacticrush.logic.drone.Drone
import com.prophetsofprofit.galacticrush.logic.drone.DroneId
import com.prophetsofprofit.galacticrush.logic.drone.instruction.Instruction
import com.prophetsofprofit.galacticrush.logic.drone.instruction.InstructionInstance
import com.prophetsofprofit.galacticrush.logic.loot.MoneyLoot
import com.prophetsofprofit.galacticrush.logic.map.CosmicHighway
import com.prophetsofprofit.galacticrush.logic.map.Galaxy
import com.prophetsofprofit.galacticrush.logic.map.Planet
import com.prophetsofprofit.galacticrush.logic.map.PlanetAttribute
import com.prophetsofprofit.galacticrush.networking.player.LocalPlayer
import com.prophetsofprofit.galacticrush.networking.player.NetworkPlayer
import com.prophetsofprofit.galacticrush.networking.player.Player
import ktx.scene2d.Scene2DSkin
import java.util.*
import kotlin.collections.LinkedHashSet

//The object that handles reading/writing JSON
val jsonObject = Json(JsonWriter.OutputType.json).also { it.setUsePrototypes(false) }
//The object that handles serializing and deserializing objects; is used to clone objects as well
val kryo = Kryo()

//The file where the user options are stored
val optionsFile = Gdx.files.local("UserOptions.json")!!

//The default tcp port
const val defaultTcpPort = 6669

//The size of the buffers for clients and servers
const val bufferSize = Int.MAX_VALUE / 1024

//How to scale each planet's radius
const val planetRadiusScale = 25

//A map of instructions to sprites
//val instructionTextures = mutableMapOf(
//        Instruction.SELECT_HIGHEST_TEMPERATURE to Texture("instruction/SELECT_HOTTEST.png"),
//        Instruction.SELECT_WEAKEST to Texture("instruction/SELECT_WEAKEST.png"),
//        Instruction.RESET_SELECTABLES to Texture("instruction/RESET_SELECTABLES.png"),
//        Instruction.MOVE_SELECTED to Texture("instruction/MOVE_SELECTED.png"),
//        Instruction.LOOP_3 to Texture("instruction/LOOP_3.png"),
//        Instruction.CONSTRUCT_BASE to Texture("instruction/CONSTRUCT_BASE.png"),
//        Instruction.REPRODUCTIVE_VIRUS to Texture("instruction/reproductive-virus.png"),
//        Instruction.ATTACK_SELECTED to Texture("instruction/ATTACK_SELECTED.png"),
//        Instruction.ATTACK_BASE to Texture("instruction/ATTACK_BASE.png"),
//        Instruction.RELEASE_CFCS to Texture("instruction/RELEASE_CFCS.png")
//).also {
//    Instruction.values().filter { instruction -> !it.containsKey(instruction) }.forEach { instruction -> it[instruction] = Texture("instruction/PLACEHOLDER.png") }
//}

//How the drone looks by default
//val baseDroneImage = Texture("image/drone/base.png")

//Default colors for the game players
val PLAYER_ONE_COLOR = Color.RED
val PLAYER_TWO_COLOR = Color.BLUE

//How much money each player starts with
val startingMoney = 5000
//How much it costs to buy a drone
val droneCost = 500

//How much loot each planet has at minimum TODO: move to host options
val minLoot = 5
//How much loot each planet has at maximum TODO: move to host options
val maxLoot = 10

//A list of default drone names; current default names are names of Roman Emperors (super edgy)
val defaultDroneNames = arrayOf(
        "Tiberius",
        "Caligula",
        "Claudius",
        "Nero",
        "Galba",
        "Otho",
        "Aulus Vitellius",
        "Vespasian",
        "Titus",
        "Domitian",
        "Nerva",
        "Trajan",
        "Hadrian",
        "Antoninus Pius",
        "Marcus Aurelius",
        "Lucius Verus",
        "Commodus",
        "Publius Helvius Pertinax",
        "Marcus Didius Severus Julianus",
        "Septimius Severus",
        "Caracalla",
        "Publius Septimius Geta",
        "Macrinus",
        "Elagabalus",
        "Severus Alexander",
        "Maximinus",
        "Gordian I",
        "Gordian II",
        "Pupienus Maximus",
        "Balbinus",
        "Gordian III",
        "Philip",
        "Decius",
        "Hostilian",
        "Gallus",
        "Aemilian",
        "Valerian",
        "Gallienus",
        "Claudius II Gothicus",
        "Quintillus",
        "Aurelian",
        "Tacitus",
        "Florian",
        "Probus",
        "Carus",
        "Numerian",
        "Carinus",
        "Diocletian",
        "Maximian",
        "Constantius I",
        "Galerius",
        "Severus",
        "Maxentius",
        "Constantine I",
        "Galerius Valerius Maximinus",
        "Licinius",
        "Constantine II",
        "Constantius II",
        "Constans I",
        "Gallus Caesar",
        "Julian",
        "Jovian",
        "Valentinian I",
        "Valens",
        "Gratian",
        "Valentinian II",
        "Theodosius I",
        "Arcadius",
        "Magnus Maximus",
        "Honorius",
        "Theodosius II",
        "Constantius III",
        "Valentinian III",
        "Marcian",
        "Petronius Maximus",
        "Avitus",
        "Majorian",
        "Libius Severus",
        "Anthemius",
        "Olybrius",
        "Glycerius",
        "Julius Nepos",
        "Romulus Augustulus",
        "Leo I",
        "Leo II",
        "Zeno"
)

//A list of default base names; current default names are names of places in anime
val defaultBaseNames = arrayOf(
        "Fire Nation",
        "Earth Kingdom",
        "Water Tribe",
        "Ba Sing Se",
        "Shiganshina",
        "Amestris",
        "Xing",
        "Orth",
        "Dark Continent",
        "Republic of East Gorteau",
        "NGL",
        "Sidonia",
        "Namek",
        "Kingdom of Fiore",
        "Alvarez Empire",
        "Village Hidden in the Leaf",
        "Village Hidden in the Sand",
        "Village Hidden in the Mist",
        "Village Hidden in the Cloud",
        "Village Hidden in the Rock",
        "Village Hidden in the Sound",
        "Death Academy",
        "Celadon City",
        "Pallet Town",
        "Aincrad",
        "Alfheim",
        "Grand Line",
        "Kamina City"
)

/**
 * Registers all of the classes that KryoNet will need to serialize and send over the network
 * Registered classes must have a constructor that takes in no arguments or an empty constructor
 */
fun registerAllClasses(kryo: Kryo) {
    kryo.register(Array<Int>::class.java)
    kryo.register(Array<Player>::class.java)
    kryo.register(ArrayList::class.java)
    kryo.register(PlanetAttribute::class.java)
    kryo.register(Base::class.java)
    kryo.register(Change::class.java)
    kryo.register(Color::class.java)
    kryo.register(CosmicHighway::class.java)
    kryo.register(Date::class.java)
    kryo.register(Drone::class.java)
    kryo.register(DroneId::class.java)
    kryo.register(Facility::class.java)
    kryo.register(Galaxy::class.java)
    kryo.register(Game::class.java)
    kryo.register(GamePhase::class.java)
    kryo.register(Instruction::class.java)
    kryo.register(InstructionInstance::class.java)
    kryo.register(LinkedHashMap::class.java)
    kryo.register(LinkedHashSet::class.java)
    kryo.register(LocalPlayer::class.java)
    kryo.register(MoneyLoot::class.java)
    kryo.register(NetworkPlayer::class.java)
    kryo.register(Planet::class.java)
    kryo.register(PlayerChange::class.java)
}