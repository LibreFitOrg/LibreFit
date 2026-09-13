/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2025-2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.ui.components.animations.morphShape

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.LoadingIndicatorDefaults.ContainerHeight
import androidx.compose.material3.LoadingIndicatorDefaults.ContainerWidth
import androidx.compose.material3.LoadingIndicatorDefaults.IndicatorSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.InfiniteAnimationPolicy
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEach
import androidx.graphics.shapes.Cubic
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnimatedMorphShapes(
    modifier: Modifier = Modifier,
    morphIntervalMillis: Long,
    globalRotationDurationMillis: Int,
    shapeSize: Dp,
    colors: List<Color>,
    roundedPolygons: List<RoundedPolygon>,
    onColorUpdate: ((Color) -> Unit)? = null,
) {
    val fullRotation = remember { 360f }
    val quarterRotation = remember { fullRotation / 4f }
    val defaultColor = MaterialTheme.colorScheme.primaryContainer
    var color by remember { mutableStateOf(colors.randomOrNull() ?: defaultColor) }

    val activeIndicatorScale = remember {
        IndicatorSize.value / min(ContainerWidth.value, ContainerHeight.value)
    }

    val morphSequence =
        remember(roundedPolygons) {
            morphSequence(polygons = roundedPolygons, circularSequence = true)
        }
    val shapesScaleFactor =
        remember(roundedPolygons) {
            // Calculate the shapes scale factor that will be applied to the morphed path as it's
            // scaled into the available size.
            // This overall scale factor ensures that the shapes are rendered without clipping and
            // at the correct ratio within the component by taking into account their occupied size
            // as they rotate, and taking into account the spec's ActiveIndicatorScale.
            calculateScaleFactor(roundedPolygons) * activeIndicatorScale
        }
    val morphProgress = remember { Animatable(0f) }
    var morphRotationTargetAngle by remember { mutableFloatStateOf(quarterRotation) }
    val globalRotation = remember { Animatable(0f) }
    var currentMorphIndex by remember(roundedPolygons) { mutableIntStateOf(0) }
    LaunchedEffect(roundedPolygons) {
        val morphAnimationBlock = {
            launch {
                // Note that we up the visibilityThreshold here to 0.1, which is x10 than the
                // default threshold, and ends the low-damping spring in a shorter time.
                val morphAnimationSpec =
                    spring(dampingRatio = 0.6f, stiffness = 200f, visibilityThreshold = 0.1f)
                while (true) {
                    // Async launch of a spring that will finish in less than MorphIntervalMillis.
                    // We then delay the entire while loop by X ms till the next morph starts.
                    val deferred = async {
                        val animationResult =
                            morphProgress.animateTo(
                                targetValue = 1f,
                                animationSpec = morphAnimationSpec,
                            )
                        if (animationResult.endReason == AnimationEndReason.Finished) {
                            currentMorphIndex = (currentMorphIndex + 1) % morphSequence.size
                            morphProgress.snapTo(0f)
                            morphRotationTargetAngle =
                                (morphRotationTargetAngle + quarterRotation) % fullRotation
                        }
                    }
                    color = colors.randomOrNull() ?: defaultColor
                    onColorUpdate?.invoke(color)
                    delay(morphIntervalMillis.milliseconds)
                    deferred.await()
                }
            }
        }

        val rotationAnimationBlock = {
            launch {
                globalRotation.animateTo(
                    targetValue = fullRotation,
                    animationSpec =
                        infiniteRepeatable(
                            tween(globalRotationDurationMillis, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart,
                        ),
                )
            }
        }

        // Possibly skip the infinite animation block when an InfiniteAnimationPolicy is
        // installed.
        when (val policy = coroutineContext[InfiniteAnimationPolicy]) {
            null -> {
                morphAnimationBlock()
                rotationAnimationBlock()
            }

            else ->
                policy.onInfiniteOperation {
                    morphAnimationBlock()
                    rotationAnimationBlock()
                }
        }
    }

    val path = remember { Path() }
    val scaleMatrix = remember { Matrix() }
    Box(
        modifier =
            modifier
                .progressSemantics()
                .size(shapeSize)
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier =
                Modifier
                    .aspectRatio(1f, matchHeightConstraintsFirst = true)
                    .drawWithContent {
                        val progress = morphProgress.value
                        // Rotate clockwise.
                        rotate(progress * 90 + morphRotationTargetAngle + globalRotation.value) {
                            drawPath(
                                path =
                                    processPath(
                                        path =
                                            morphSequence[currentMorphIndex].toPath(
                                                // Use the coerced progress value to eliminate any
                                                // bounciness from the morph. We scale the drawing
                                                // to simulate some bounciness instead.
                                                progress = progress,
                                                path = path,
                                                startAngle = 0,
                                            ),
                                        size = size,
                                        scaleFactor = shapesScaleFactor,
                                        scaleMatrix = scaleMatrix,
                                    ),
                                color = color,
                                style = Fill,
                            )
                        }
                    }
        )
    }
}

/**
 * Returns a list of [Morph]s that describe the sequence of shapes that the [LoadingIndicator]
 * should animate in a loop.
 *
 * This function will create a morph between each consecutive [RoundedPolygon] shaped in the
 * provided array, and will create a final morph from the last shape to the first shape.
 *
 * Note that each [RoundedPolygon] within the returns [Morph]s is normalized here.
 *
 * @param polygons a [List] of [RoundedPolygon]s to create [Morph]s for
 * @param circularSequence indicate if an additional [Morph] should be created from the last item to
 *   the first item of the list in order to create a circular sequence of morphs
 * @see RoundedPolygon.normalized
 */

private fun morphSequence(polygons: List<RoundedPolygon>, circularSequence: Boolean): List<Morph> {
    return buildList {
        for (i in polygons.indices) {
            if (i + 1 < polygons.size) {
                add(Morph(polygons[i].normalized(), polygons[i + 1].normalized()))
            } else if (circularSequence) {
                // Create a morph from the last shape to the first shape
                add(Morph(polygons[i].normalized(), polygons[0].normalized()))
            }
        }
    }
}

/**
 * Calculates a scale factor that will be used when scaling the provided [RoundedPolygon]s into a
 * specified sized container.
 *
 * Since the polygons may rotate, a simple [RoundedPolygon.calculateBounds] is not enough to
 * determine the size the polygon will occupy as it rotates. Using the simple bounds calculation may
 * result in a clipped shape.
 *
 * This function calculates and returns a scale factor by utilizing the
 * [RoundedPolygon.calculateMaxBounds] and comparing its result to the
 * [RoundedPolygon.calculateBounds]. The scale factor can later be used when calling [processPath].
 */
private fun calculateScaleFactor(indicatorPolygons: List<RoundedPolygon>): Float {
    var scaleFactor = 1f
    // Axis-aligned max bounding box for this object, where the rectangles left, top, right, and
    // bottom values will be stored in entries 0, 1, 2, and 3, in that order.
    val bounds = FloatArray(size = 4)
    val maxBounds = FloatArray(size = 4)
    indicatorPolygons.fastForEach { polygon ->
        polygon.calculateBounds(bounds)
        polygon.calculateMaxBounds(maxBounds)
        val scaleX = bounds.width() / maxBounds.width()
        val scaleY = bounds.height() / maxBounds.height()
        // We use max(scaleX, scaleY) to handle cases like a pill-shape that can throw off the
        // entire calculation.
        scaleFactor = min(scaleFactor, max(scaleX, scaleY))
    }
    return scaleFactor
}

/**
 * Process a given path to scale it and center it inside the given size.
 *
 * @param path a [Path] that was generated by a _normalized_ [Morph] or [RoundedPolygon]
 * @param size a [Size] that the provided [path] is going to be scaled and centered into
 * @param scaleFactor a scale factor that will be taken into account uniformly when the [path] is
 *   scaled (i.e. the scaleX would be the [size] width x the scale factor, and the scaleY would be
 *   the [size] height x the scale factor)
 * @param scaleMatrix a [Matrix] that would be used to apply the scaling. Note that any provided
 *   matrix will be reset in this function.
 */
private fun processPath(
    path: Path,
    size: Size,
    scaleFactor: Float,
    scaleMatrix: Matrix = Matrix(),
): Path {
    scaleMatrix.reset()

    scaleMatrix.apply { scale(x = size.width * scaleFactor, y = size.height * scaleFactor) }

    // Scale to the desired size.
    path.transform(scaleMatrix)

    // Translate the path to align its center with the available size center.
    path.translate(size.center - path.getBounds().center)
    return path
}

/**
 * Returns the width value from the [FloatArray] that was calculated by a
 * [RoundedPolygon.calculateBounds] or [[RoundedPolygon.calculateMaxBounds]].
 */
private fun FloatArray.width(): Float {
    // Expecting a FloatArray of [left, top, right, bottom]
    return this[2] - this[0]
}

/**
 * Returns the height value from the [FloatArray] that was calculated by a
 * [RoundedPolygon.calculateBounds] or [RoundedPolygon.calculateMaxBounds].
 */
private fun FloatArray.height(): Float {
    // Expecting a FloatArray of [left, top, right, bottom]
    return this[3] - this[1]
}

/**
 * Returns a [Path] for a [Morph].
 *
 * @param progress the [Morph]'s progress
 * @param path a [Path] to rewind and set with the new path data
 * @param startAngle an angle (in degrees) to rotate the [Path] to start drawing from
 * @param repeatPath whether or not to repeat the [Path] twice before closing it. This flag is
 *   useful when the caller would like to draw parts of the path while offsetting the start and stop
 *   positions (for example, when phasing and rotating a path to simulate a motion as a Star
 *   circular progress indicator advances).
 * @param closePath whether or not to close the created [Path]
 * @param rotationPivotX the rotation pivot on the X axis. By default it's set to 0, and that should
 *   align with [Morph] instances that were created for RoundedPolygons with zero centerX. In case
 *   the RoundedPolygon were normalized (i.e. moved to (0.5, 0.5)), or where created with a
 *   different centerX coordinated, this pivot point may need to be aligned to support a proper
 *   rotation.
 * @param rotationPivotY the rotation pivot on the Y axis. By default it's set to 0, and that should
 *   align with [Morph] instances that were created for RoundedPolygons with zero centerY. In case
 *   the RoundedPolygon were normalized (i.e. moves to (0.5, 0.5)), or where created with a
 *   different centerY coordinated, this pivot point may need to be aligned to support a proper
 *   rotation.
 */
internal fun Morph.toPath(
    progress: Float,
    path: Path = Path(),
    startAngle: Int = 270, // 12 O'clock
    repeatPath: Boolean = false,
    closePath: Boolean = true,
    rotationPivotX: Float = 0f,
    rotationPivotY: Float = 0f,
): Path {
    pathFromCubics(
        path = path,
        startAngle = startAngle,
        repeatPath = repeatPath,
        closePath = closePath,
        cubics = asCubics(progress),
        rotationPivotX = rotationPivotX,
        rotationPivotY = rotationPivotY,
    )
    return path
}

private fun pathFromCubics(
    path: Path,
    startAngle: Int,
    repeatPath: Boolean,
    closePath: Boolean,
    cubics: List<Cubic>,
    rotationPivotX: Float,
    rotationPivotY: Float,
) {
    var first = true
    var firstCubic: Cubic? = null
    path.rewind()
    cubics.fastForEach {
        if (first) {
            path.moveTo(it.anchor0X, it.anchor0Y)
            if (startAngle != 0) {
                firstCubic = it
            }
            first = false
        }
        path.cubicTo(
            it.control0X,
            it.control0Y,
            it.control1X,
            it.control1Y,
            it.anchor1X,
            it.anchor1Y,
        )
    }
    if (repeatPath) {
        var firstInRepeat = true
        cubics.fastForEach {
            if (firstInRepeat) {
                path.lineTo(it.anchor0X, it.anchor0Y)
                firstInRepeat = false
            }
            path.cubicTo(
                it.control0X,
                it.control0Y,
                it.control1X,
                it.control1Y,
                it.anchor1X,
                it.anchor1Y,
            )
        }
    }

    if (closePath) path.close()

    if (startAngle != 0 && firstCubic != null) {
        val angleToFirstCubic =
            radiansToDegrees(
                atan2(
                    y = cubics[0].anchor0Y - rotationPivotY,
                    x = cubics[0].anchor0X - rotationPivotX,
                )
            )
        // Rotate the Path to to start from the given angle.
        path.transform(Matrix().apply { rotateZ(-angleToFirstCubic + startAngle) })
    }
}

private fun radiansToDegrees(radians: Float): Float {
    return (radians * 180.0 / PI).toFloat()
}