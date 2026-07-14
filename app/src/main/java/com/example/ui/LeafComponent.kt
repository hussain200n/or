package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.model.Leaf
import com.example.ui.canvas.LeafPainter
import com.example.viewmodel.TreeViewModel
import kotlin.math.roundToInt

private val LEAF_BOX_SIZE = 200.dp

@Composable
fun LeafComponent(
    leaf: Leaf,
    viewModel: TreeViewModel,
    isSelected: Boolean,
    pan: Offset,
    zoom: Float
) {
    val density = LocalDensity.current
    val halfBoxPx = with(density) { (LEAF_BOX_SIZE / 2).toPx() }

    // Screen-space top-left position for the leaf's bounding box, derived from
    // model-space position transformed by the current camera pan/zoom.
    val screenX = leaf.positionX * zoom + pan.x - halfBoxPx
    val screenY = leaf.positionY * zoom + pan.y - halfBoxPx

    val transformState = rememberTransformableState { zoomChange, offsetChange, rotationChange ->
        if (!leaf.isLocked) {
            viewModel.updateLeafTransform(
                leafId = leaf.id,
                scaleDelta = zoomChange,
                rotDelta = rotationChange,
                offsetDelta = offsetChange
            )
        }
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(screenX.roundToInt(), screenY.roundToInt()) }
            .size(LEAF_BOX_SIZE)
            .graphicsLayer(
                scaleX = zoom,
                scaleY = zoom
            )
            .pointerInput(leaf.id) {
                detectTapGestures(
                    onTap = {
                        viewModel.selectElement(leaf.id)
                        viewModel.setShowControlMenu(false)
                    },
                    onLongPress = {
                        viewModel.selectElement(leaf.id)
                        viewModel.setShowControlMenu(true)
                    }
                )
            }
            .then(
                if (!leaf.isLocked) Modifier.transformable(state = transformState) else Modifier
            )
    ) {
        Canvas(modifier = Modifier.size(LEAF_BOX_SIZE)) {
            // Draw relative to the box's own center so the painter's local (0,0)
            // matches the leaf's absolute position handled by the offset above.
            translate(size.width / 2f, size.height / 2f) {
                LeafPainter.drawLeaf(
                    drawScope = this,
                    leaf = leaf,
                    isSelected = isSelected
                )
            }
        }
    }
}

private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.translate(
    left: Float,
    top: Float,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    androidx.compose.ui.graphics.drawscope.withTransform({ translate(left, top) }, block)
}
