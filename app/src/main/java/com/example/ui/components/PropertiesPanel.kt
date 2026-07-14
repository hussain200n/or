package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TreeProject
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium

@Composable
fun PropertiesPanel(
    selectedId: String?,
    project: TreeProject,
    onClose: () -> Unit,
    onUpdateTrunk: (width: Float, height: Float, fontFamily: String, fontSize: Float, barkTexture: Float) -> Unit,
    onUpdateTrunkNames: (List<String>) -> Unit,
    onUpdateBranch: (id: String, width: Float, circleSize: Float, slant: Float, fontFamily: String, fontSize: Float, rotation: Float) -> Unit,
    onUpdateLeaf: (id: String, scale: Float, angle: Float, fontFamily: String, fontSize: Float, sharpness: Float, veinIntensity: Float, colorHex: String) -> Unit,
    onUpdateLeafPosition: (id: String, x: Float, y: Float) -> Unit,
    onRenameElement: (String) -> Unit,
    onToggleLock: () -> Unit,
    onFocusCamera: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = selectedId != null,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            .widthIn(max = 550.dp)
    ) {
        val selId = selectedId
        if (selId != null) {
            val isTrunk = selId == "trunk"
            val isBranch = selId.startsWith("b")
            val isLeaf = selId.startsWith("l")

            val elementTitle = when {
                isTrunk -> "الجذع الملكي الأثري (الأصل)"
                isBranch -> "غصن النسب الممتد"
                isLeaf -> "ورقة الفرع المباركة"
                else -> ""
            }

            val isLocked = when {
                isTrunk -> project.trunk.isLocked
                isBranch -> project.branches.find { it.id == selId }?.isLocked ?: false
                isLeaf -> project.leaves.find { it.id == selId }?.isLocked ?: false
                else -> false
            }

            var isCollapsed by remember(selId) { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(12.dp))
                    .border(1.2.dp, GoldPrimary, RoundedCornerShape(12.dp))
                    .testTag("properties_panel"),
                colors = CardDefaults.cardColors(containerColor = CharcoalInk.copy(alpha = 0.96f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(GoldAccent, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = elementTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                            if (isLocked) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(مقفل)",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF5252)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { onFocusCamera(selId) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "تركيز الكاميرا",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = onToggleLock,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "قفل العنصر",
                                    tint = if (isLocked) Color(0xFFFF5252) else GoldAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            IconButton(
                                onClick = { isCollapsed = !isCollapsed },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCollapsed) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isCollapsed) "توسيع الخيارات" else "تصغير الخيارات",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق اللوحة",
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (!isCollapsed) {
                        Spacer(modifier = Modifier.height(6.dp))

                        if (isTrunk) {
                            TrunkNamesEditor(
                                currentNames = project.trunk.names,
                                onNamesChanged = onUpdateTrunkNames,
                                enabled = !isLocked
                            )
                        } else {
                            val currentName = if (isBranch) {
                                project.branches.find { it.id == selId }?.name ?: ""
                            } else {
                                project.leaves.find { it.id == selId }?.name ?: ""
                            }
                            var nameInput by remember(selId) { mutableStateOf(currentName) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = nameInput,
                                    onValueChange = {
                                        nameInput = it
                                        if (it.isNotBlank()) onRenameElement(it.trim())
                                    },
                                    enabled = !isLocked,
                                    label = { Text("الاسم المعروض", color = ParchmentMedium) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("element_name_input"),
                                    maxLines = 1,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedLabelColor = GoldAccent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                                .border(0.5.dp, GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "التعديل الفني والهندسي التراثي:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (isTrunk) {
                                val trunk = project.trunk
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        LuxurySlider(
                                            label = "عرض/سمك الجذع (بكسل):",
                                            value = trunk.width,
                                            enabled = !isLocked,
                                            onValueChange = {
                                                onUpdateTrunk(it, trunk.height, trunk.fontFamily, trunk.fontSize, trunk.barkTextureDensity)
                                            },
                                            valueRange = 100f..400f
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LuxurySlider(
                                            label = "طول الجذع الكلي (بكسل):",
                                            value = trunk.height,
                                            enabled = !isLocked,
                                            onValueChange = {
                                                onUpdateTrunk(trunk.width, it, trunk.fontFamily, trunk.fontSize, trunk.barkTextureDensity)
                                            },
                                            valueRange = 250f..850f
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        LuxurySlider(
                                            label = "كثافة ملمس اللحاء (الخشب):",
                                            value = trunk.barkTextureDensity,
                                            enabled = !isLocked,
                                            onValueChange = {
                                                onUpdateTrunk(trunk.width, trunk.height, trunk.fontFamily, trunk.fontSize, it)
                                            },
                                            valueRange = 0.2f..3.0f
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LuxurySlider(
                                            label = "حجم خط الجذع:",
                                            value = trunk.fontSize,
                                            enabled = !isLocked,
                                            onValueChange = {
                                                onUpdateTrunk(trunk.width, trunk.height, trunk.fontFamily, it, trunk.barkTextureDensity)
                                            },
                                            valueRange = 18f..50f
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LuxuryFontSelector(
                                    selectedFont = trunk.fontFamily,
                                    enabled = !isLocked,
                                    onFontSelected = {
                                        onUpdateTrunk(trunk.width, trunk.height, it, trunk.fontSize, trunk.barkTextureDensity)
                                    }
                                )
                            } else if (isBranch) {
                                val branch = project.branches.find { it.id == selId }
                                if (branch != null) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            LuxurySlider(
                                                label = "سمك الغصن (الخشب):",
                                                value = branch.width,
                                                enabled = !isLocked,
                                                onValueChange = {
                                                    onUpdateBranch(branch.id, it, branch.circleSize, branch.slantFactor, branch.fontFamily, branch.fontSize, branch.rotation)
                                                },
                                                valueRange = 8f..40f
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LuxurySlider(
                                                label = "انحناء الغصن (Cubic bias):",
                                                value = branch.slantFactor,
                                                enabled = !isLocked,
                                                onValueChange = {
                                                    onUpdateBranch(branch.id, branch.width, branch.circleSize, it, branch.fontFamily, branch.fontSize, branch.rotation)
                                                },
                                                valueRange = -2.0f..3.0f
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            LuxurySlider(
                                                label = "حجم دائرة الغصن الاسمية:",
                                                value = branch.circleSize,
                                                enabled = !isLocked,
                                                onValueChange = {
                                                    onUpdateBranch(branch.id, branch.width, it, branch.slantFactor, branch.fontFamily, branch.fontSize, branch.rotation)
                                                },
                                                valueRange = 25f..80f
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LuxurySlider(
                                                label = "دوران الغصن الإجمالي:",
                                                value = branch.rotation,
                                                enabled = !isLocked,
                                                onValueChange = {
                                                    onUpdateBranch(branch.id, branch.width, branch.circleSize, branch.slantFactor, branch.fontFamily, branch.fontSize, it)
                                                },
                                                valueRange = -180f..180f
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            LuxuryFontSelector(
                                                selectedFont = branch.fontFamily,
                                                enabled = !isLocked,
                                                onFontSelected = {
                                                    onUpdateBranch(branch.id, branch.width, branch.circleSize, branch.slantFactor, it, branch.fontSize, branch.rotation)
                                                }
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            LuxurySlider(
                                                label = "حجم خط دائرة الغصن:",
                                                value = branch.fontSize,
                                                enabled = !isLocked,
                                                onValueChange = {
                                                    onUpdateBranch(branch.id, branch.width, branch.circleSize, branch.slantFactor, branch.fontFamily, it, branch.rotation)
                                                },
                                                valueRange = 12f..36f
                                            )
                                        }
                                    }
                                }
                            } else if (isLeaf) {
                                val leaf = project.leaves.find { it.id == selId }
                                if (leaf != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 280.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = "الموقع الإحداثي الدقيق للورقة (بكسل):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent,
                                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("المحور الأفقي X:", fontSize = 10.sp, color = ParchmentMedium)
                                                    Text("${leaf.positionX.toInt()}", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                }
                                                Slider(
                                                    value = leaf.positionX,
                                                    onValueChange = { onUpdateLeafPosition(leaf.id, it, leaf.positionY) },
                                                    enabled = !isLocked,
                                                    valueRange = -900f..900f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = GoldPrimary,
                                                        activeTrackColor = GoldPrimary,
                                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                                    ),
                                                    modifier = Modifier.height(24.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    TextButton(
                                                        onClick = { onUpdateLeafPosition(leaf.id, leaf.positionX - 10f, leaf.positionY) },
                                                        enabled = !isLocked,
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.weight(1f).height(24.dp)
                                                    ) {
                                                        Text("-10", fontSize = 10.sp, color = GoldAccent)
                                                    }
                                                    TextButton(
                                                        onClick = { onUpdateLeafPosition(leaf.id, leaf.positionX + 10f, leaf.positionY) },
                                                        enabled = !isLocked,
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.weight(1f).height(24.dp)
                                                    ) {
                                                        Text("+10", fontSize = 10.sp, color = GoldAccent)
                                                    }
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("المحور الرأسي Y:", fontSize = 10.sp, color = ParchmentMedium)
                                                    Text("${leaf.positionY.toInt()}", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                                                }
                                                Slider(
                                                    value = leaf.positionY,
                                                    onValueChange = { onUpdateLeafPosition(leaf.id, leaf.positionX, it) },
                                                    enabled = !isLocked,
                                                    valueRange = -900f..900f,
                                                    colors = SliderDefaults.colors(
                                                        thumbColor = GoldPrimary,
                                                        activeTrackColor = GoldPrimary,
                                                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                                    ),
                                                    modifier = Modifier.height(24.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    TextButton(
                                                        onClick = { onUpdateLeafPosition(leaf.id, leaf.positionX, leaf.positionY - 10f) },
                                                        enabled = !isLocked,
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.weight(1f).height(24.dp)
                                                    ) {
                                                        Text("-10", fontSize = 10.sp, color = GoldAccent)
                                                    }
                                                    TextButton(
                                                        onClick = { onUpdateLeafPosition(leaf.id, leaf.positionX, leaf.positionY + 10f) },
                                                        enabled = !isLocked,
                                                        contentPadding = PaddingValues(0.dp),
                                                        modifier = Modifier.weight(1f).height(24.dp)
                                                    ) {
                                                        Text("+10", fontSize = 10.sp, color = GoldAccent)
                                                    }
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

                                        Text(
                                            text = "مقياس الورقة الفني (Scale):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                        LuxurySlider(
                                            label = "مقياس حجم الورقة:",
                                            value = leaf.scale,
                                            enabled = !isLocked,
                                            onValueChange = {
                                                onUpdateLeaf(leaf.id, it, leaf.angle, leaf.fontFamily, leaf.fontSize, leaf.tipSharpness, leaf.veinIntensity, leaf.gradientColorHex)
                                            },
                                            valueRange = 0.4f..2.5f
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val scales = listOf(
                                                0.5f to "صغير",
                                                1.0f to "طبيعي",
                                                1.5f to "كبير",
                                                2.0f to "عملاق"
                                            )
                                            scales.forEach { (scVal, scLabel) ->
                                                val isSel = (leaf.scale - scVal) in -0.05f..0.05f
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(
                                                            if (isSel) GoldPrimary else Color.White.copy(alpha = 0.05f),
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .border(0.5.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                        .clickable(enabled = !isLocked) {
                                                            onUpdateLeaf(leaf.id, scVal, leaf.angle, leaf.fontFamily, leaf.fontSize, leaf.tipSharpness, leaf.veinIntensity, leaf.gradientColorHex)
                                                        }
                                                        .padding(vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(scLabel, fontSize = 10.sp, color = if (isSel) CharcoalInk else Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

                                        Text(
                                            text = "زاوية دوران الورقة:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                        LuxurySlider(
                                            label = "درجة دوران محور النمو (0 - 360°):",
                                            value = leaf.angle.coerceIn(0f, 360f),
                                            enabled = !isLocked,
                                            onValueChange = {
                                                onUpdateLeaf(leaf.id, leaf.scale, it, leaf.fontFamily, leaf.fontSize, leaf.tipSharpness, leaf.veinIntensity, leaf.gradientColorHex)
                                            },
                                            valueRange = 0f..360f
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            val angles = listOf(
                                                0f to "٠°",
                                                90f to "٩٠°",
                                                180f to "١٨٠°",
                                                270f to "٢٧٠°",
                                                360f to "٣٦٠°"
                                            )
                                            angles.forEach { (angVal, angLabel) ->
                                                val isSel = (leaf.angle - angVal) in -0.5f..0.5f
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(
                                                            if (isSel) GoldPrimary else Color.White.copy(alpha = 0.05f),
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .border(0.5.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                                        .clickable(enabled = !isLocked) {
                                                            onUpdateLeaf(leaf.id, leaf.scale, angVal, leaf.fontFamily, leaf.fontSize, leaf.tipSharpness, leaf.veinIntensity, leaf.gradientColorHex)
                                                        }
                                                        .padding(vertical = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(angLabel, fontSize = 10.sp, color = if (isSel) CharcoalInk else Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))

                                        Text(
                                            text = "تفاصيل شكل الورقة والخط واللون:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldAccent,
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        )
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                LuxurySlider(
                                                    label = "حدة رأس الورقة (Tip):",
                                                    value = leaf.tipSharpness,
                                                    enabled = !isLocked,
                                                    onValueChange = {
                                                        onUpdateLeaf(leaf.id, leaf.scale, leaf.angle, leaf.fontFamily, leaf.fontSize, it, leaf.veinIntensity, leaf.gradientColorHex)
                                                    },
                                                    valueRange = 0.5f..2.5f
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LuxuryColorPicker(
                                                    selectedColorHex = leaf.gradientColorHex,
                                                    enabled = !isLocked,
                                                    onColorSelected = {
                                                        onUpdateLeaf(leaf.id, leaf.scale, leaf.angle, leaf.fontFamily, leaf.fontSize, leaf.tipSharpness, leaf.veinIntensity, it)
                                                    }
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                LuxurySlider(
                                                    label = "كثافة وبروز العروق:",
                                                    value = leaf.veinIntensity,
                                                    enabled = !isLocked,
                                                    onValueChange = {
                                                        onUpdateLeaf(leaf.id, leaf.scale, leaf.angle, leaf.fontFamily, leaf.fontSize, leaf.tipSharpness, it, leaf.gradientColorHex)
                                                    },
                                                    valueRange = 0.2f..3.0f
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LuxurySlider(
                                                    label = "حجم خط الورقة:",
                                                    value = leaf.fontSize,
                                                    enabled = !isLocked,
                                                    onValueChange = {
                                                        onUpdateLeaf(leaf.id, leaf.scale, leaf.angle, leaf.fontFamily, it, leaf.tipSharpness, leaf.veinIntensity, leaf.gradientColorHex)
                                                    },
                                                    valueRange = 10f..35f
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        LuxuryFontSelector(
                                            selectedFont = leaf.fontFamily,
                                            enabled = !isLocked,
                                            onFontSelected = {
                                                onUpdateLeaf(leaf.id, leaf.scale, leaf.angle, it, leaf.fontSize, leaf.tipSharpness, leaf.veinIntensity, leaf.gradientColorHex)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LuxurySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, color = ParchmentMedium, fontWeight = FontWeight.Medium)
            Text(text = "%.1f".format(value), fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = GoldPrimary,
                activeTrackColor = GoldPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(28.dp)
        )
    }
}

@Composable
fun LuxuryFontSelector(
    selectedFont: String,
    onFontSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val fonts = listOf(
        "Default" to "الخط الافتراضي (الرسمي)",
        "Serif" to "خط كلاسيكي أثري (النسخ)",
        "SansSerif" to "خط معاصر (الرقعة)",
        "Monospace" to "خط كوفي هندسي (ديواني)"
    )
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(text = "طراز الخط التراثي العربي:", fontSize = 11.sp, color = ParchmentMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fonts.find { it.first == selectedFont }?.second ?: "الافتراضي",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "قائمة الخطوط",
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CharcoalInk)
            ) {
                fonts.forEach { (key, arabicName) ->
                    DropdownMenuItem(
                        text = { Text(arabicName, color = Color.White, fontSize = 12.sp) },
                        onClick = {
                            onFontSelected(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LuxuryColorPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val presetColors = listOf(
        "#2E7D32" to "أخضر نضِر",
        "#1B5E20" to "أخضر داكن",
        "#4CAF50" to "زيتوني زاهٍ",
        "#E65100" to "خريفي ذهبي",
        "#8D6E63" to "بني أثري"
    )

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(vertical = 4.dp)) {
        Text(text = "لون الورقة والتدريج:", fontSize = 11.sp, color = ParchmentMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(Color(android.graphics.Color.parseColor(selectedColorHex)), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = presetColors.find { it.first == selectedColorHex }?.second ?: "لون مخصص",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "قائمة الألوان",
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CharcoalInk)
            ) {
                presetColors.forEach { (hex, name) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(name, color = Color.White, fontSize = 12.sp)
                            }
                        },
                        onClick = {
                            onColorSelected(hex)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TrunkNamesEditor(
    currentNames: List<String>,
    onNamesChanged: (List<String>) -> Unit,
    enabled: Boolean = true
) {
    var newNameInput by remember { mutableStateOf("") }
    val namesList = remember(currentNames) { mutableStateListOf<String>().apply { addAll(currentNames) } }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "إدارة أسماء أصل الشجرة (الجذع):",
            fontSize = 11.sp,
            color = ParchmentMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newNameInput,
                onValueChange = { newNameInput = it },
                enabled = enabled,
                label = { Text("أدخل اسم السلف", color = ParchmentMedium) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                maxLines = 1,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                    focusedLabelColor = GoldAccent
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (enabled && newNameInput.isNotBlank() && namesList.size < 10) {
                        namesList.add(newNameInput.trim())
                        onNamesChanged(namesList.toList())
                        newNameInput = ""
                    }
                },
                enabled = enabled,
                modifier = Modifier
                    .background(GoldPrimary, CircleShape)
                    .size(42.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة سلف", tint = CharcoalInk)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(namesList) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${namesList.size - index}. $item",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = {
                                namesList.removeAt(index)
                                onNamesChanged(namesList.toList())
                            },
                            enabled = enabled,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
