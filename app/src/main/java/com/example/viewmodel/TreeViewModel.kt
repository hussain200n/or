package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import com.example.model.Branch
import com.example.model.Leaf
import com.example.model.TreeProject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.util.UUID

class TreeViewModel : ViewModel() {

    // ============ Core project state ============
    private val _project = MutableStateFlow(TreeProject())
    val project: StateFlow<TreeProject> = _project.asStateFlow()

    private val _selectedId = MutableStateFlow<String?>(null)
    val selectedId: StateFlow<String?> = _selectedId.asStateFlow()

    private val _showControlMenu = MutableStateFlow(false)
    val showControlMenu: StateFlow<Boolean> = _showControlMenu.asStateFlow()

    // isEditing mirrors "an element is currently selected", used to gate the properties panel
    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    // ============ Camera (pan / zoom) ============
    private val _pan = MutableStateFlow(Offset.Zero)
    val pan: StateFlow<Offset> = _pan.asStateFlow()

    private val _zoom = MutableStateFlow(1f)
    val zoom: StateFlow<Float> = _zoom.asStateFlow()

    private val _viewportSize = MutableStateFlow(Size.Zero)
    private var cameraInitialized = false

    // ============ Undo / Redo history ============
    private val undoStack = ArrayDeque<TreeProject>()
    private val redoStack = ArrayDeque<TreeProject>()
    private val maxHistory = 50

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private fun pushHistory() {
        undoStack.addLast(_project.value)
        if (undoStack.size > maxHistory) undoStack.removeFirst()
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(_project.value)
        _project.value = previous
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = true
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(_project.value)
        _project.value = next
        _canRedo.value = redoStack.isNotEmpty()
        _canUndo.value = true
    }

    // ============ Selection ============
    fun selectElement(id: String?) {
        _selectedId.value = id
        _isEditing.value = id != null
    }

    fun setShowControlMenu(show: Boolean) {
        _showControlMenu.value = show
    }

    private fun isLocked(id: String): Boolean {
        val proj = _project.value
        return when {
            id == "trunk" -> proj.trunk.isLocked
            id.startsWith("b") -> proj.branches.find { it.id == id }?.isLocked ?: false
            id.startsWith("l") -> proj.leaves.find { it.id == id }?.isLocked ?: false
            else -> false
        }
    }

    // ============ Camera controls ============
    fun setViewportSize(size: Size) {
        _viewportSize.value = size
        if (!cameraInitialized && size.width > 0f && size.height > 0f) {
            cameraInitialized = true
            resetView()
        }
    }

    fun updatePan(delta: Offset) {
        _pan.update { it + delta }
    }

    /** Multiplicative zoom, used for pinch gestures (factor around 1.0) */
    fun updateZoom(factor: Float) {
        _zoom.update { (it * factor).coerceIn(0.25f, 4f) }
    }

    fun resetView() {
        val size = _viewportSize.value
        _pan.value = Offset(size.width / 2f, if (size.height > 0f) size.height * 0.12f else 80f)
        _zoom.value = 1f
    }

    fun centerOnElement(id: String) {
        val proj = _project.value
        val size = _viewportSize.value
        val target: Offset = when {
            id == "trunk" -> Offset(proj.trunk.positionX, proj.trunk.positionY + proj.trunk.height / 2f)
            id.startsWith("b") -> proj.branches.find { it.id == id }?.let { Offset(it.p0X, it.p0Y) }
            id.startsWith("l") -> proj.leaves.find { it.id == id }?.let { Offset(it.positionX, it.positionY) }
            else -> null
        } ?: return

        val z = _zoom.value
        _pan.value = Offset(
            size.width / 2f - target.x * z,
            size.height / 2f - target.y * z
        )
    }

    // ============ Trunk ============
    fun updateTrunkProperties(
        width: Float,
        height: Float,
        fontFamily: String,
        fontSize: Float,
        barkTexture: Float
    ) {
        if (_project.value.trunk.isLocked) return
        pushHistory()
        _project.update { proj ->
            proj.copy(
                trunk = proj.trunk.copy(
                    width = width,
                    height = height,
                    fontFamily = fontFamily,
                    fontSize = fontSize,
                    barkTextureDensity = barkTexture
                )
            )
        }
    }

    fun updateTrunkNames(names: List<String>) {
        if (_project.value.trunk.isLocked) return
        pushHistory()
        _project.update { proj ->
            proj.copy(trunk = proj.trunk.copy(names = names.take(10)))
        }
    }

    // ============ Branch ============
    fun updateBranchProperties(
        id: String,
        width: Float,
        circleSize: Float,
        slant: Float,
        fontFamily: String,
        fontSize: Float,
        rotation: Float
    ) {
        if (isLocked(id)) return
        pushHistory()
        _project.update { proj ->
            proj.copy(
                branches = proj.branches.map { b ->
                    if (b.id == id) {
                        b.copy(
                            width = width,
                            circleSize = circleSize,
                            slantFactor = slant,
                            fontFamily = fontFamily,
                            fontSize = fontSize,
                            rotation = rotation
                        )
                    } else b
                }
            )
        }
    }

    // ============ Leaf ============
    fun updateLeafProperties(
        id: String,
        scale: Float,
        angle: Float,
        fontFamily: String,
        fontSize: Float,
        sharpness: Float,
        veinIntensity: Float,
        colorHex: String
    ) {
        if (isLocked(id)) return
        pushHistory()
        _project.update { proj ->
            proj.copy(
                leaves = proj.leaves.map { l ->
                    if (l.id == id) {
                        l.copy(
                            scale = scale,
                            angle = angle,
                            fontFamily = fontFamily,
                            fontSize = fontSize,
                            tipSharpness = sharpness,
                            veinIntensity = veinIntensity,
                            gradientColorHex = colorHex
                        )
                    } else l
                }
            )
        }
    }

    fun updateLeafPosition(id: String, position: Offset) {
        if (isLocked(id)) return
        pushHistory()
        _project.update { proj ->
            proj.copy(
                leaves = proj.leaves.map { l ->
                    if (l.id == id) l.copy(positionX = position.x, positionY = position.y) else l
                }
            )
        }
    }

    /** Live drag/pinch/rotate updates from a leaf's transformable gesture (screen-space deltas). */
    fun updateLeafTransform(leafId: String, scaleDelta: Float, rotDelta: Float, offsetDelta: Offset) {
        if (isLocked(leafId)) return
        val z = _zoom.value.coerceAtLeast(0.01f)
        _project.update { proj ->
            proj.copy(
                leaves = proj.leaves.map { leaf ->
                    if (leaf.id == leafId) {
                        leaf.copy(
                            positionX = leaf.positionX + offsetDelta.x / z,
                            positionY = leaf.positionY + offsetDelta.y / z,
                            angle = leaf.angle + rotDelta,
                            scale = (leaf.scale * scaleDelta).coerceIn(0.3f, 3f)
                        )
                    } else leaf
                }
            )
        }
    }

    /** Called once when a transform gesture on a leaf begins/ends, to snapshot history correctly. */
    fun commitLeafTransform() {
        pushHistory()
    }

    // ============ Generic element operations ============
    fun editNameSelected(newName: String) {
        val selId = _selectedId.value ?: return
        if (isLocked(selId) || selId == "trunk") return
        pushHistory()
        _project.update { proj ->
            when {
                selId.startsWith("b") -> proj.copy(
                    branches = proj.branches.map { if (it.id == selId) it.copy(name = newName) else it }
                )
                selId.startsWith("l") -> proj.copy(
                    leaves = proj.leaves.map { if (it.id == selId) it.copy(name = newName) else it }
                )
                else -> proj
            }
        }
    }

    fun toggleLockSelected() {
        val selId = _selectedId.value ?: return
        pushHistory()
        _project.update { proj ->
            when {
                selId == "trunk" -> proj.copy(trunk = proj.trunk.copy(isLocked = !proj.trunk.isLocked))
                selId.startsWith("b") -> proj.copy(
                    branches = proj.branches.map { if (it.id == selId) it.copy(isLocked = !it.isLocked) else it }
                )
                selId.startsWith("l") -> proj.copy(
                    leaves = proj.leaves.map { if (it.id == selId) it.copy(isLocked = !it.isLocked) else it }
                )
                else -> proj
            }
        }
    }

    fun deleteSelected() {
        val selId = _selectedId.value ?: return
        if (isLocked(selId) || selId == "trunk") return
        pushHistory()
        _project.update { proj ->
            proj.copy(
                branches = proj.branches.filter { it.id != selId },
                leaves = proj.leaves.filter { it.id != selId && it.parentId != selId }
            )
        }
        _selectedId.value = null
        _isEditing.value = false
        _showControlMenu.value = false
    }

    fun duplicateSelected() {
        val selId = _selectedId.value ?: return
        pushHistory()
        _project.update { proj ->
            when {
                selId.startsWith("b") -> {
                    val src = proj.branches.find { it.id == selId } ?: return@update proj
                    val newId = "b_" + UUID.randomUUID().toString().take(6)
                    val dx = 60f
                    val dup = src.copy(
                        id = newId,
                        name = "${src.name} (نسخة)",
                        p0X = src.p0X + dx, p1X = src.p1X + dx,
                        p2X = src.p2X + dx, p3X = src.p3X + dx,
                        isLocked = false
                    )
                    proj.copy(branches = proj.branches + dup)
                }
                selId.startsWith("l") -> {
                    val src = proj.leaves.find { it.id == selId } ?: return@update proj
                    val newId = "l_" + UUID.randomUUID().toString().take(6)
                    val dup = src.copy(
                        id = newId,
                        name = "${src.name} (نسخة)",
                        positionX = src.positionX + 40f,
                        positionY = src.positionY + 40f,
                        isLocked = false
                    )
                    proj.copy(leaves = proj.leaves + dup)
                }
                else -> proj
            }
        }
        _showControlMenu.value = false
    }

    fun addLeafToSelected() {
        val selId = _selectedId.value ?: return
        pushHistory()
        _project.update { proj ->
            val parentPos: Offset = when {
                selId == "trunk" -> Offset(proj.trunk.positionX, proj.trunk.positionY)
                selId.startsWith("b") -> proj.branches.find { it.id == selId }
                    ?.let { Offset(it.p3X, it.p3Y) } ?: Offset.Zero
                else -> Offset.Zero
            }
            val siblingCount = proj.leaves.count { it.parentId == selId }
            val newId = "l_" + UUID.randomUUID().toString().take(6)
            val newLeaf = Leaf(
                id = newId,
                parentId = selId,
                name = "ابن جديد",
                positionX = parentPos.x + 90f + (siblingCount * 30f),
                positionY = parentPos.y - 60f - (siblingCount * 25f)
            )
            proj.copy(leaves = proj.leaves + newLeaf)
        }
        _showControlMenu.value = false
    }

    fun addBranchToSelected() {
        pushHistory()
        _project.update { proj ->
            val t = proj.trunk
            val siblingCount = proj.branches.size
            val startX = t.positionX + (if (siblingCount % 2 == 0) 1 else -1) * (40f + siblingCount * 10f)
            val startY = t.positionY + t.height * 0.35f - siblingCount * 15f
            val endX = startX + (if (siblingCount % 2 == 0) 260f else -260f)
            val endY = startY - 220f

            val newId = "b_" + UUID.randomUUID().toString().take(6)
            val newBranch = Branch(
                id = newId,
                parentId = "trunk",
                name = "فرع جديد",
                p0X = startX, p0Y = startY,
                p1X = startX + (endX - startX) * 0.3f, p1Y = startY - 60f,
                p2X = startX + (endX - startX) * 0.7f, p2Y = endY + 40f,
                p3X = endX, p3Y = endY
            )
            proj.copy(branches = proj.branches + newBranch)
        }
        _showControlMenu.value = false
    }

    fun updateTitleAndDesigner(title: String, designer: String) {
        pushHistory()
        _project.update { proj ->
            proj.copy(
                title = title.ifBlank { proj.title },
                designer = designer.ifBlank { proj.designer }
            )
        }
    }

    /** Hit-tests the trunk and branches at a given model-space point. Leaves are hit-tested by their own composables. */
    fun hitTestTrunkOrBranch(modelPoint: Offset): String? {
        val proj = _project.value
        val t = proj.trunk
        val left = t.positionX - t.width / 2f - 60f
        val right = t.positionX + t.width / 2f + 60f
        val top = t.positionY - 10f
        val bottom = t.positionY + t.height + 10f
        if (modelPoint.x in left..right && modelPoint.y in top..bottom) {
            return "trunk"
        }

        var closestId: String? = null
        var closestDist = Float.MAX_VALUE
        for (b in proj.branches) {
            val p0 = Offset(b.p0X, b.p0Y)
            val p1 = Offset(b.p1X, b.p1Y)
            val p2 = Offset(b.p2X, b.p2Y)
            val p3 = Offset(b.p3X, b.p3Y)
            val steps = 24
            for (i in 0..steps) {
                val tt = i.toFloat() / steps
                val mt = 1f - tt
                val x = mt * mt * mt * p0.x + 3 * mt * mt * tt * p1.x + 3 * mt * tt * tt * p2.x + tt * tt * tt * p3.x
                val y = mt * mt * mt * p0.y + 3 * mt * mt * tt * p1.y + 3 * mt * tt * tt * p2.y + tt * tt * tt * p3.y
                val dist = kotlin.math.hypot((modelPoint.x - x).toDouble(), (modelPoint.y - y).toDouble()).toFloat()
                if (dist < closestDist) {
                    closestDist = dist
                    closestId = b.id
                }
            }
            // Also count the medallion node as part of the hit area
            val nodeDist = kotlin.math.hypot((modelPoint.x - p0.x).toDouble(), (modelPoint.y - p0.y).toDouble()).toFloat()
            if (nodeDist < closestDist) {
                closestDist = nodeDist
                closestId = b.id
            }
        }

        val threshold = 50f
        return if (closestId != null && closestDist <= threshold) closestId else null
    }
}
