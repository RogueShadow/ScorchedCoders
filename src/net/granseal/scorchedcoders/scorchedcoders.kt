package net.granseal.scorchedcoders

import net.granseal.koLambda.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Image
import java.awt.Shape
import java.awt.event.MouseEvent
import java.awt.geom.GeneralPath
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.geom.Point2D.Float as Float2
import kotlin.concurrent.thread
import kotlin.random.Random

const val PixelScale = 1
const val WIDTH = 1024 / PixelScale
const val HEIGHT = 768 / PixelScale
const val WorldWidth = WIDTH * 2
const val WorldHeight = HEIGHT
const val PLAYERS = 5

val TAKEN = mutableListOf<String>()
val NAMES = listOf(
    "RogueShadow","Danicron","javidx9","UglySwedishFish","Tutas","Ben",
    "Noodles","Nimmy","MaGetzUb","AniCator","Eremiell","Dustyshouri",
    "Jonus","whizard_dragon","Inverness","Diego"
)

val SKY = Color.blue.brighter().brighter().brighter().rgb
val GROUND = Color.green.darker().darker().darker().rgb

fun main(args: Array<String>){
    args.forEach { println(it) }
    ScorchedCoders.playerList = args.toList()
    ScorchedCoders.start()
}

object ScorchedCoders: ApplicationAdapter("Scorched Coders", WIDTH * PixelScale, HEIGHT * PixelScale){

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