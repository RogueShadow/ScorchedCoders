package net.granseal.scorchedcoders

import net.granseal.koLambda.*
import java.awt.Color
import java.awt.event.MouseEvent
import java.io.File
import kotlin.concurrent.thread

const val PixelScale = 1
const val WIDTH = 1024 / PixelScale
const val HEIGHT = 768 / PixelScale
const val WorldWidth = WIDTH * 2
const val WorldHeight = HEIGHT
const val PLAYERS = 5

val SKY = Color.blue.brighter().brighter().brighter().rgb
val GROUND = Color.green.darker().darker().darker().rgb

fun main(){
    val playerFile = File("players.txt")
    if (playerFile.exists()){
        ScorchedCoders.playerList = playerFile.readLines().filter { it.isNotBlank() }.map{it.trim()}
    }else{
        ScorchedCoders.playerList = listOf(
            "RogueShadow","Danicron","javidx9","UglySwedishFish","Tutas","Ben",
            "Noodles","Nimmy","MaGetzUb","AniCator","Eremiell","Dustyshouri",
            "Jonus","whizard_dragon","Inverness","Diego"
        )
    }

    ScorchedCoders.start()
}

object ScorchedCoders: ApplicationAdapter("Scorched Coders", WIDTH * PixelScale, HEIGHT * PixelScale){
    val launchSnd = Sound(this.javaClass.getResourceAsStream("res/sc_launch.wav").buffered())
    val explodeSnd = Sound(this.javaClass.getResourceAsStream("res/sc_explode1.wav").buffered())

    var playerList = listOf<String>()

    override fun init() {
        backgroundColor = Color(SKY)
        sceneRoot = Loader
        thread {
            Level
            Loader.finished.set(true)
        }
    }

    override fun dispose() {
        super.dispose()
        Level.gfx.dispose()
    }

    override fun mouseReleased(e: MouseEvent) {
        // Hack, because I don't currently capture events properly for individual Entities.
        if (e.id == MouseEvent.MOUSE_RELEASED){
            Level.children.filter{ it is Player}.map{ it as Player}.forEach {
                if (it.selected){
                    it.launch()
                }
            }
        }
    }
}