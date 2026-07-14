package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import com.example.ui.LeafComponent
import com.example.viewmodel.TreeViewModel

@Composable
fun FamilyTreeCanvas(
    viewModel: TreeViewModel,
    modifier: Modifier = Modifier
) {
    val project by viewModel.project.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()
    val pan by viewModel.pan.collectAsState()
    val zoom by viewModel.zoom.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF7))
            .onSizeChanged { size ->
                viewModel.setViewportSize(Size(size.width.toFloat(), size.height.toFloat()))
            }
    ) {
        // 1. Draw trunk + branches on a single shared canvas, in model space.
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { screenOffset ->
                            val modelPoint = Offset(
                                (screenOffset.x - pan.x) / zoom,
                                (screenOffset.y - pan.y) / zoom
                            )
                            val id = viewModel.hitTestTrunkOrBranch(modelPoint)
                            viewModel.selectElement(id)
                            viewModel.setShowControlMenu(false)
                        },
                        onLongPress = { screenOffset ->
                            val modelPoint = Offset(
                                (screenOffset.x - pan.x) / zoom,
                                (screenOffset.y - pan.y) / zoom
                            )
                            val id = viewModel.hitTestTrunkOrBranch(modelPoint)
                            if (id != null) {
                                viewModel.selectElement(id)
                                viewModel.setShowControlMenu(true)
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, panChange, zoomChange, _ ->
                        viewModel.updatePan(panChange)
                        viewModel.updateZoom(zoomChange)
                    }
                }
        ) {
            withTransform({
                translate(pan.x, pan.y)
                scale(zoom, zoom, pivot = Offset.Zero)
            }) {
                TrunkPainter.drawTrunk(
                    drawScope = this,
                    trunk = project.trunk,
                    isSelected = selectedId == "trunk"
                )

                for (branch in project.branches) {
                    BranchPainter.drawBranch(
                        drawScope = this,
                        branch = branch,
                        isSelected = selectedId == branch.id
                    )
                }
            }
        }

        // 2. Leaves as independent, interactive composables layered above the canvas.
        project.leaves.forEach { leaf ->
            key(leaf.id) {
                LeafComponent(
                    leaf = leaf,
                    viewModel = viewModel,
                    isSelected = selectedId == leaf.id,
                    pan = pan,
                    zoom = zoom
                )
            }
        }
    }
}
