package net.granseal.scorchedcoders

import net.granseal.koLambda.*
import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.Point2D.Float as Float2
import kotlin.concurrent.thread
import kotlin.math.*
import kotlin.random.Random

object Level: Entity() {
    val timer = Application.Timer()
    val camSpeed = 550f
    val terrain = ScorchedCoders.getGraphics().deviceConfiguration.createCompatibleImage(WorldWidth, WorldHeight)
    val gfx = terrain.createGraphics()
    val waitingForLaunch = mutableListOf<Missile>()

    var playerTurn = 0
    var playerTurnRef: Player? = null

    val TakingTurn = 0
    val MissileFlight = 1
    var state = TakingTurn
    var cameraTarget = point()

    init {
        timer.start()
        scale = PixelScale.toDouble()

        restart(ScorchedCoders.playerList)

        bounds = rect(0f,0f, WorldWidth.toFloat(), WorldHeight.toFloat())
        drawers.add{ g ->
            g.drawImage(terrain,0,0,null)
            g.color = Color.BLACK
            g.drawString("It's ${playerTurnRef?.name ?: "Null's"}'s turn!   Click and drag to aim, release to fire.  Hold Alt to move camera.",
                20-pos.x.toInt(),20-pos.y.toInt()
            )
        }

        updaters.add{ delta ->

            if (!keyHeld(KeyEvent.VK_ALT)){
                focusCamera(delta)
            }else{
                val target = getWorldTransform().inverseTransform(mousePos(),null)
                cameraTarget = point(target.x,target.y)
                focusCamera(delta)
            }

            waitingForLaunch.forEach {
                add(it)
                state = MissileFlight
            }
            waitingForLaunch.clear()

            when (state){
                TakingTurn -> {
                    if (keyPressed('g')) {
                        restart()
                    }
                    if (keyPressed('f')){
                        val p = children.filterIsInstance<Player>().random()
                        explosion(p.pos,128)
                    }

                    if (keyHeld('w'))cameraTarget.y += camSpeed * delta
                    if (keyHeld('s'))cameraTarget.y -= camSpeed * delta
                    if (keyHeld('a'))cameraTarget.x += camSpeed * delta
                    if (keyHeld('d'))cameraTarget.x -= camSpeed * delta


                }
                MissileFlight -> {

                }
                else -> {}
            }



        }

        clickHandler = {it,mPos ->
            when (it.id){
                MouseEvent.MOUSE_PRESSED -> {
                    when (it.button){
                        MOUSE_BTN_LEFT -> {}
                        MOUSE_BTN_MIDDLE -> { }
                        MOUSE_BTN_RIGHT -> {
                            explosion(point(mPos.x,mPos.y),256)
                        }
                    }
                    true
                }
                MouseEvent.MOUSE_RELEASED -> {

                    true
                }
                else -> false
            }
        }
        println("Initialized Level Object in ${timer.delta()} seconds.")
    }

    fun focusCamera(delta: Float){
        var targetX = cameraTarget.x - WIDTH/2
        var targetY = cameraTarget.y - HEIGHT/2


        val dx = pos.x + targetX
        val dy = pos.y + targetY
        val angle = atan2(dy,dx)

        if (abs(dx) > 32f)pos.x -= cos(angle) * delta * camSpeed
        if (abs(dy) > 32f)pos.y -= sin(angle) * delta * camSpeed

        if (pos.x < -WIDTH){
            pos.x = -WIDTH.toFloat()
        }
        if (pos.x > 0f){
            pos.x = 0f
        }
        if (pos.y < 0f){
            pos.y = 0f
        }
    }

    fun nextTurn(){
        val players = children.filterIsInstance<Player>().filter { !it.dead && !(it.id == playerTurn)}.map{ it.id }
        if (players.isEmpty()){
            restart()
        }else{
            playerTurn++
            while (children.filterIsInstance<Player>().none { it.id == playerTurn && !it.dead }){
                playerTurn++
                if (playerTurn > PLAYERS)playerTurn = 0
            }
        }
        playerTurnRef = children.filterIsInstance<Player>().firstOrNull { it.id == playerTurn }
        cameraTarget = point(playerTurnRef!!.pos.x, playerTurnRef!!.pos.y)
    }
    fun restart(players: List<String> = listOf()){
        clearPlayers()
        ScorchedCoders.sceneRoot = Loader
        thread {
            generateTerrain()
            settleTerrain()
            Loader.finished.set(true)
        }
        if (players.isNullOrEmpty()){
            repeat(PLAYERS){addPlayer()}
        }else{
            players.forEach {
                addPlayer(it)
            }
        }
        nextTurn()
    }
    fun clearPlayers(){
        TAKEN.clear()
        children.removeAll{it is Player}
    }
    fun addPlayer(name: String = ""): Entity {
        val player = Player(Random.nextFloat()* WorldWidth,name)
        add(player)
        return player
    }

    fun onWall(p: Float2) = onWall(p.x,p.y)
    fun onWall(x: Float, y: Float) = onWall(x.toInt(),y.toInt())
    fun onWall(x: Int, y: Int):Boolean {
        if (outOfBounds(x,y))return true
        if (y < 0)return false
        return terrain.getRGB(x,y) == GROUND
    }
    fun outOfBounds(p: Float2) = outOfBounds(p.x,p.y)
    fun outOfBounds(x: Float, y: Float) = outOfBounds(x.toInt(),y.toInt())
    fun outOfBounds(x: Int, y: Int): Boolean {
        if (x < 0) return true
        if (x >= terrain.width)return true
        if (y < 0)return false
        if (y >= terrain.height)return true
        return false
    }

    fun explosion(ePos: Float2, size: Int = 16, damage: Float = 50f){
        val players = children.filterIsInstance<Player>().filter { !it.dead }
        val hSize = size/2
        val hitPlayers = players.filter{
            it.pos.distance(ePos) < ((size/2) + (16))
        }
        val dmg = hitPlayers.map{1f - (it.pos.distance(ePos) / (size/2 + 16)).toFloat() to it}

        dmg.forEach {
            println("Damaging player ${it.second.name} for ${it.first*damage}, he now has ${it.second.health} health left.")
            it.second.damagePlayer(it.first * damage)
            val dx = it.second.pos.x - ePos.x
            val dy = it.second.pos.y - ePos.y
            val a = atan2(dy,dx)
            it.second.velocity plusAssign point(cos(a),sin(a) + -5f).times(it.first * 250)
        }

        val circle = Ellipse2D.Float(
            -hSize + ePos.x,
            -hSize + ePos.y,
            size.toFloat(),
            size.toFloat()
        )
        gfx.color = Color(SKY)
        gfx.fill(circle)

        repeat(40){
            ParticleManager.emit(
                Particle(point(ePos.x,ePos.y))
                    .speed(30f)
                    .color(Color.ORANGE.darker().darker().darker().darker())
                    .duration(3f)
                    .size(1f)
                    .rotation(Random.nextFloat()*PI.toFloat(),(-1f +Random.nextFloat() * 2)*10f)
                    .rndHue(0.1f)
                    .velocity(point((-1f + Random.nextFloat() * 2f)*100f,-400f * Random.nextFloat()))
            )
        }
        settleTerrain()
    }

    fun settleTerrain() {
        val totalPoints = WorldWidth
        var currentPoints = 0
        (0 until terrain.width).forEach { x ->
            var y = 0
            while (terrain.getRGB(x,y) != GROUND && y < terrain.height - 1){
                y++
            }
            val highGround = y
            var sky = 0
            (highGround until terrain.height).forEach { y ->
                if (terrain.getRGB(x,y) == SKY){
                    sky++
                    gfx.color = Color(GROUND)
                    gfx.fillRect(x,y,1,1)
                }
            }
            (highGround until (highGround + sky)).forEach { y ->
                gfx.color = Color(SKY)
                gfx.fillRect(x,y,1,1)
            }
            Loader.text = "Settling Terrain: ${((currentPoints.toFloat()/totalPoints)*100).toInt()}%"
            currentPoints++
        }
    }

    fun generateTerrain(){
        val totalPoints = WorldHeight * WorldWidth
        var currentPoints = 0
        val myNoise = Noise1d(1000)
        val s0 = 50f + Random.nextFloat()*200f
        val s1 = 50f + Random.nextFloat()*200f
        val s2 = 50f + Random.nextFloat()*200f
        val s3 = 50f + Random.nextFloat()*200f
        val peaks = 600f
        val heightOffset =  - Random.nextFloat()*200
        gfx.color = Color(SKY)
        gfx.fillRect(0,0, terrain.width, terrain.height)
        gfx.color = Color(GROUND)
        (0 until terrain.height).forEach{y ->
            (0 until terrain.width).forEach { x->
                val n0 = myNoise.noise(x/s0 +   0)
                val n1 = myNoise.noise(x/s1 + 100)
                val n2 = myNoise.noise(y/s2 + 200)
                val n3 = myNoise.noise(y/s3 + 300)
                val v1 = y + n0*n1*n2*n3*peaks
                val v2 = terrain.height/2 - heightOffset
                if (v1 > v2)gfx.fillRect(x,y,1,1)
                currentPoints++
            }
            Loader.text = "Generating Terrain: ${((currentPoints/totalPoints.toFloat())*100).toInt()}% "
        }
    }
}