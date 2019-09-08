package net.granseal.scorchedcoders

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Point2D


//Extensions
fun Graphics2D.drawNamePlate(name: String = "Default", x: Int = 0, y: Int = 0,
                             foreground: Color = Color.GREEN,
                             background: Color = Color.BLACK,
                             outline: Color = Color.WHITE,
                             padding: Int = 4,
                             healthBar: Float = 1f)
{
    val xOffset = getFontMetrics().stringWidth(name)/2
    val yOffset = getFontMetrics().height
    color = outline
    fillRoundRect(
        -xOffset - padding + x, -yOffset * 2 - padding + y,
        xOffset + xOffset + padding * 2, yOffset + padding * 2,
        8,8)
    color = background
    fillRoundRect(
        -xOffset - padding + 1+x, -yOffset * 2 - padding + 1+y,
        xOffset + xOffset + padding * 2 - 2, yOffset + padding * 2 - 2,
        8,8)
    color = Color.RED
    fillRoundRect(
        -xOffset - padding + 1+x, -yOffset * 2 - padding + 1+y,
        ((xOffset + xOffset + padding * 2 - 2)*(1-healthBar)).toInt(), yOffset + padding * 2 - 2,
        8,8)
    color = foreground
    drawString(name,-xOffset+x,-yOffset - padding + y)
}

fun Graphics2D.drawLine(start: Point2D, end: Point2D){
    drawLine(start.x.toInt(),start.y.toInt(),end.x.toInt(),end.y.toInt())
}