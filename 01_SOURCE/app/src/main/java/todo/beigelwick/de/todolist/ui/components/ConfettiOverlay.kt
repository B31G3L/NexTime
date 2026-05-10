package todo.beigelwick.de.todolist.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val CONFETTI_COLORS = listOf(
    Color(0xFFFF7043), Color(0xFFEF5350), Color(0xFFEC407A),
    Color(0xFFAB47BC), Color(0xFF5C6BC0), Color(0xFF42A5F5),
    Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFFFFA726),
    Color(0xFFFFFFFF), Color(0xFFFFD700)
)

private enum class ConfettiShape { RECT, CIRCLE, TRIANGLE }

private data class ConfettiParticle(
    val x: Float, val y: Float,
    val velocityX: Float, val velocityY: Float,
    val rotation: Float, val rotationSpeed: Float,
    val color: Color, val shape: ConfettiShape,
    val size: Float, val alpha: Float
)

@Composable
fun ConfettiOverlay(active: Boolean, modifier: Modifier = Modifier) {
    if (!active) return

    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var tick      by remember { mutableStateOf(0) }

    LaunchedEffect(active) {
        if (active) particles = List(120) { createParticle() }
    }

    LaunchedEffect(active) {
        if (!active) return@LaunchedEffect
        while (true) {
            delay(16L)
            tick++
            particles = particles
                .map { p ->
                    p.copy(
                        x         = p.x + p.velocityX,
                        y         = p.y + p.velocityY,
                        velocityY = p.velocityY + 0.15f,
                        rotation  = p.rotation + p.rotationSpeed,
                        alpha     = if (p.y > 1200f) (p.alpha - 0.03f).coerceAtLeast(0f) else p.alpha
                    )
                }
                .filter { it.alpha > 0f && it.y < 2000f }
            if (tick < 30 && particles.size < 80) {
                particles = particles + List(5) { createParticle() }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { drawParticle(it) }
    }
}

private fun DrawScope.drawParticle(p: ConfettiParticle) {
    val center = Offset(p.x, p.y)
    rotate(degrees = p.rotation, pivot = center) {
        when (p.shape) {
            ConfettiShape.RECT -> drawRect(
                color   = p.color.copy(alpha = p.alpha),
                topLeft = Offset(p.x - p.size / 2, p.y - p.size / 4),
                size    = Size(p.size, p.size / 2)
            )
            ConfettiShape.CIRCLE -> drawCircle(
                color  = p.color.copy(alpha = p.alpha),
                radius = p.size / 2,
                center = center
            )
            ConfettiShape.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(p.x, p.y - p.size / 2)
                    lineTo(p.x + p.size / 2, p.y + p.size / 2)
                    lineTo(p.x - p.size / 2, p.y + p.size / 2)
                    close()
                }
                drawPath(path = path, color = p.color.copy(alpha = p.alpha))
            }
        }
    }
}

private fun createParticle(): ConfettiParticle {
    val angle = Random.nextFloat() * 360f
    val speed = Random.nextFloat() * 12f + 4f
    return ConfettiParticle(
        x             = Random.nextFloat() * 1080f,
        y             = Random.nextFloat() * -200f - 50f,
        velocityX     = cos(Math.toRadians(angle.toDouble())).toFloat() * speed * 0.4f,
        velocityY     = sin(Math.toRadians(angle.toDouble())).toFloat() * speed * 0.3f + 2f,
        rotation      = Random.nextFloat() * 360f,
        rotationSpeed = Random.nextFloat() * 8f - 4f,
        color         = CONFETTI_COLORS.random(),
        shape         = ConfettiShape.values().random(),
        size          = Random.nextFloat() * 16f + 8f,
        alpha         = Random.nextFloat() * 0.4f + 0.6f
    )
}