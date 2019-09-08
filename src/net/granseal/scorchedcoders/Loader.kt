package net.granseal.scorchedcoders

import net.granseal.koLambda.Entity
import java.util.concurrent.atomic.AtomicBoolean

object Loader: Entity(){
    var text = ""
    var finished = AtomicBoolean(false)
    init {
        pos.x = 200f
        pos.y = 300f
        drawers.add{
            it.drawNamePlate(text)
        }
        updaters.add{
            if (finished.get()){
                ScorchedCoders.sceneRoot = Level
                finished.set(false)
            }
        }
    }
}
