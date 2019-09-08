package net.granseal.scorchedcoders

import net.granseal.koLambda.*
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.geom.Point2D.Float as Float2
import java.util.*
import kotlin.math.*
import kotlin.random.Random

class Player(startX: Float, var name: String = ""): Entity() {
    val color = hsb(Random.nextFloat(),1f,0.65f)
    var velocity = point()
    var selected = false
    val maxHealth = 100
    var health = maxHealth
    var dead = false
    val id = Level.getAll().filterIsInstance<Player>().size + 1

    init {
        bounds = rect(-16f,-16f,32f,32f)
        pos.x = startX

        drawers.add {
            if (!dead) {
                it.color = if (!selected) color else Color.YELLOW.darker()
                it.fillOval(-16, -16, 32, 32)
                it.color = Color.BLACK
                it.drawOval(-16, -16, 32, 32)
                it.color = Color.WHITE

                it.drawNamePlate(
                    name = name,
                    healthBar = health.toFloat() / maxHealth.toFloat(),
                    outline = if (id == Level.playerTurn) Color.MAGENTA else Color.WHITE,
                    foreground = if (id == Level.playerTurn) Color.MAGENTA else Color.WHITE
                )

                if (selected) {
                    drawFiringLine(it)
                }
            }else{
                it.color = Color.red.darker().darker().darker()
                it.fillOval(-16,-4,32,8)
                it.drawNamePlate(
                    name = name,
                    healthBar = health.toFloat() / maxHealth.toFloat()
                )
            }
        }

        updaters.add{
            velocity.y += ParticleManager.GRAVITY * it
            val newPos = pos + (velocity.times(it))

            if (!Level.onWall(pos.x,newPos.y)){
                pos.y = newPos.y
            }
            if (!Level.onWall(newPos.x,pos.y))pos.x = newPos.x
            velocity *= 0.99f

            if (Level.playerTurnRef == this)Level.cameraTarget = point(pos.x,pos.y)
        }

        clickHandler = {e, pos ->
            if (!dead && Level.playerTurn == id && Level.state == Level.TakingTurn) {
                when (e.id) {
                    MouseEvent.MOUSE_PRESSED -> {
                        selected = true
                        true
                    }
                    MouseEvent.MOUSE_RELEASED -> {
                        selected = false
                        launch()
                        true
                    }
                    MouseEvent.MOUSE_EXITED -> {
                        selected = false
                        true
                    }
                    else -> false
                }
                true
            }else false
        }
    }

    fun damagePlayer(value: Float){
        if (dead)return
        health -= value.toInt()
        repeat(30) {
            ParticleManager.emit(
                Particle(point(pos.x,pos.y),1f,Color.RED)
                    .size(1.5f)
                    .speed(26f)
                    .rndVelocity(60f)
                    .rndHue(.1f)
                    .fadeOut(.1f)
                    .rotation(Random.nextFloat()* PI.toFloat(),(-1f +Random.nextFloat() * 2)*10f).apply {
                        velocity.y -= 100
                    }
            )
        }
        if (health <= 0){
            health = 0
            dead = true
        }
    }

    fun launch() {
        val parent = parent
        val mPos = if (parent != null) {
            parent.getWorldTransform().inverseTransform(mousePos(), null)
        } else getWorldTransform().inverseTransform(mousePos(),null)

        selected = false
        val dist = pos.distance(mPos).toFloat()
        val angle = atan2(pos.y - mPos.y,pos.x - mPos.x).toFloat()

        val mVel = point(-cos(angle) * 3 * dist,-sin(angle) * 3 * dist)
        Level.waitingForLaunch.add(Missile(point(pos.x,pos.y),mVel))
    }

    fun drawFiringLine(g: Graphics2D){
        val parent = parent
        val mPos = if (parent != null) {
            parent.getWorldTransform().inverseTransform(mousePos(), null)
        } else getWorldTransform().inverseTransform(mousePos(),null)
        g.color = Color.RED.darker()
        val oldStroke = g.stroke
        g.stroke = BasicStroke(8f, BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND)
        g.drawLine(point(),point(mPos.x,mPos.y) - pos)
        g.stroke = oldStroke
    }

}

