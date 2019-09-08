package net.granseal.scorchedcoders

import net.granseal.koLambda.*
import java.awt.Color
import java.awt.geom.Ellipse2D
import kotlin.math.PI
import java.awt.geom.Point2D.Float as Float2
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Missile(start: Float2, var velocity: Float2, val type: Int = 0): Entity(pos = start) {
    var exploded = false
    var smokeFrequency = 0.01f
    var smokeCounter = 0f
    var angle = 0f

    var width = 32
    var height = 8

    init {
        updaters.add{
            if (!exploded) {
                smokeCounter += it
                velocity.y += ParticleManager.GRAVITY * it
                val newPos = pos.plus(velocity.times(it))

                if (Level.outOfBounds(pos.x,newPos.y)){
                    velocity.y = 0f
                }else
                if (!Level.onWall(pos.x, newPos.y)) {
                    pos.y = newPos.y
                }else detonate()

                if (Level.outOfBounds(newPos.x,pos.y)){
                    velocity.x *= -1
                }else
                if (!Level.onWall(newPos.x,pos.y)){
                    pos.x = newPos.x
                }else detonate()

                angle = atan2(velocity.y,velocity.x)

                if (Level.state == Level.MissileFlight){
                    Level.cameraTarget = point(pos.x , pos.y )
                }

                velocity *= 0.99f

                if (smokeCounter > smokeFrequency){
                    ParticleManager.emit(
                        Particle(point(pos.x - cos(angle)*(width/2),pos.y - sin(angle)*(width/2)))
                            .colors(listOf(Color.YELLOW,Color.YELLOW.darker(),Color.RED.brighter(),Color.RED,Color.RED.darker(),Color.DARK_GRAY),.4f,0)
                            .duration(1.5f)
                            .noGravity()
                            .size(1.25f)
                            .rotation(Random.nextFloat()* PI.toFloat())
                            .rndRotation(3f)
                            .rndVelocity(30f)
                            .fadeOut(0.4f)
                            .shape{Ellipse2D.Float(-5f,-5f,10f,10f)}
                    )
                    smokeCounter = 0f
                }

            }
        }
        drawers.add{
            if (!exploded) {
                it.rotate(angle.toDouble())
                it.color = Color.RED.darker().darker()
                it.fillOval(8,-(height/2),16,height)
                it.color = Color.DARK_GRAY.darker()
                it.fillRoundRect(-width/2, -height/2,width, height,4,4)
                it.rotate(-angle.toDouble())
            }
        }

    }

    fun detonate(){
        exploded = true
        ParticleManager.emit(
            Particle(point(pos.x,pos.y))
                .speed(0f)
                .colors(listOf(Color.RED,Color.YELLOW),0.1f,3)
                .sizes(listOf(32f,20f,10f,20f,10f,20f,10f,20f,10f,20f,10f,20f,10f,20f,10f,20f,0f,5f,0f),1.5f,0)
                .duration(1.5f)
                .fadeOut(0f)
                .fadeIn(0f)
                .shape{ Ellipse2D.Float(-1f,-1f,2f,2f) }
                .noGravity()
        )
        ScorchedCoders.explodeSnd.play()
        Level.explosion(pos, 64, 50f)
        Level.nextTurn()
        Level.state = Level.TakingTurn
    }
}