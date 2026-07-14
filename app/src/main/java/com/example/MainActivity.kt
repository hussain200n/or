package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.canvas.FamilyTreeCanvas
import com.example.viewmodel.TreeViewModel
import com.example.ui.components.DesignToolbar
import com.example.ui.components.PropertiesPanel
import com.example.ui.components.LuxuryExportDialog
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ParchmentLight
import com.example.ui.theme.ParchmentMedium

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        TreeStudioDashboard(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TreeStudioDashboard(
    modifier: Modifier = Modifier,
    viewModel: TreeViewModel = viewModel()
) {
    val context = LocalContext.current
    val project by viewModel.project.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()
    val showControlMenu by viewModel.showControlMenu.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showTrunkNameManager by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSuccessExportDialog by remember { mutableStateOf(false) }
    var exportedFilePath by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize()) {

        // 1. Infinite Painting Canvas (Underlay)
        FamilyTreeCanvas(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Sleek Floating Design Toolbar
        DesignToolbar(
            onAddTrunkClick = { showTrunkNameManager = true },
            onAddBranchClick = { viewModel.addBranchToSelected() },
            onAddLeafClick = {
                if (selectedId != null) {
                    viewModel.addLeafToSelected()
                } else {
                    Toast.makeText(context, "الرجاء تحديد فرع أو جذع أولاً لإضافة ورقة إليه", Toast.LENGTH_SHORT).show()
                }
            },
            onExportClick = { showExportDialog = true },
            onResetView = { viewModel.resetView() },
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            canUndo = canUndo,
            canRedo = canRedo,
            selectedId = selectedId,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 3. Zoom Controllers (Floating Corner Widget)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
        ) {
            IconButton(
                onClick = { viewModel.updateZoom(1.15f) },
                modifier = Modifier
                    .shadow(6.dp, CircleShape)
                    .background(CharcoalInk, CircleShape)
                    .size(48.dp)
                    .border(1.5.dp, GoldAccent, CircleShape)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = GoldAccent)
            }
            Spacer(modifier = Modifier.height(12.dp))
            IconButton(
                onClick = { viewModel.updateZoom(0.85f) },
                modifier = Modifier
                    .shadow(6.dp, CircleShape)
                    .background(CharcoalInk, CircleShape)
                    .size(48.dp)
                    .border(1.5.dp, GoldAccent, CircleShape)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = GoldAccent)
            }
        }

        // 4. Granular Heritage Properties Panel
        PropertiesPanel(
            selectedId = if (isEditing) selectedId else null,
            project = project,
            onClose = { viewModel.selectElement(null) },
            onUpdateTrunk = { width, height, fontFamily, fontSize, barkTexture ->
                viewModel.updateTrunkProperties(width, height, fontFamily, fontSize, barkTexture)
            },
            onUpdateTrunkNames = { names ->
                viewModel.updateTrunkNames(names)
            },
            onUpdateBranch = { id, width, circleSize, slant, fontFamily, fontSize, rotation ->
                viewModel.updateBranchProperties(id, width, circleSize, slant, fontFamily, fontSize, rotation)
            },
            onUpdateLeaf = { id, scale, angle, fontFamily, fontSize, sharpness, veinIntensity, colorHex ->
                viewModel.updateLeafProperties(id, scale, angle, fontFamily, fontSize, sharpness, veinIntensity, colorHex)
            },
            onUpdateLeafPosition = { id, x, y ->
                viewModel.updateLeafPosition(id, androidx.compose.ui.geometry.Offset(x, y))
            },
            onRenameElement = { newName ->
                viewModel.editNameSelected(newName)
            },
            onToggleLock = { viewModel.toggleLockSelected() },
            onFocusCamera = { id ->
                viewModel.centerOnElement(id)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // 5. Floating Long-Press Context Menu dialog (Arabic controls)
        if (showControlMenu && selectedId != null) {
            val selId = selectedId!!
            val isTrunk = selId == "trunk"
            val isBranch = selId.startsWith("b")
            val isLeaf = selId.startsWith("l")

            Dialog(onDismissRequest = { viewModel.setShowControlMenu(false) }) {
                Card(
                    modifier = Modifier
                        .width(300.dp)
                        .border(2.dp, GoldPrimary, RoundedCornerShape(16.dp))
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CharcoalInk)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "خيارات التحكم الفاخرة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(
                            color = GoldPrimary.copy(alpha = 0.3f),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        DialogMenuItem(
                            icon = Icons.Default.Edit,
                            label = if (isTrunk) "إدارة أسماء الجذع" else "تعديل الاسم",
                            tint = GoldAccent,
                            onClick = {
                                viewModel.setShowControlMenu(false)
                                if (isTrunk) showTrunkNameManager = true else showRenameDialog = true
                            }
                        )

                        val isLocked = when {
                            isTrunk -> project.trunk.isLocked
                            isBranch -> project.branches.find { it.id == selId }?.isLocked ?: false
                            isLeaf -> project.leaves.find { it.id == selId }?.isLocked ?: false
                            else -> false
                        }
                        DialogMenuItem(
                            icon = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            label = if (isLocked) "إلغاء قفل العنصر" else "قفل العنصر لمنع التعديل",
                            tint = if (isLocked) Color(0xFFFF5252) else GoldAccent,
                            onClick = { viewModel.toggleLockSelected() }
                        )

                        if ((isTrunk || isBranch) && !isLocked) {
                            DialogMenuItem(
                                icon = Icons.Default.AddCircle,
                                label = "إضافة ابن جديد (ورقة)",
                                tint = ForestGreen,
                                onClick = { viewModel.addLeafToSelected() }
                            )
                        }

                        if (isTrunk && !isLocked) {
                            DialogMenuItem(
                                icon = Icons.Default.Add,
                                label = "إضافة فرع جديد (غصن)",
                                tint = GoldAccent,
                                onClick = { viewModel.addBranchToSelected() }
                            )
                        }

                        if ((isBranch || isLeaf) && !isLocked) {
                            DialogMenuItem(
                                icon = Icons.Default.ContentCopy,
                                label = "نسخ وتكرار هذا العنصر",
                                tint = Color.White,
                                onClick = { viewModel.duplicateSelected() }
                            )
                        }

                        if ((isBranch || isLeaf) && !isLocked) {
                            DialogMenuItem(
                                icon = Icons.Default.Delete,
                                label = "حذف العنصر نهائياً",
                                tint = Color(0xFFFF5252),
                                onClick = { viewModel.deleteSelected() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.setShowControlMenu(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إغلاق القائمة", color = CharcoalInk, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. Trunk Names Manager Modal
        if (showTrunkNameManager) {
            val namesList = remember { mutableStateListOf<String>().apply { addAll(project.trunk.names) } }
            var newNameInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showTrunkNameManager = false },
                title = {
                    Text(
                        "إدارة أسماء أصل الشجرة (الجذع)",
                        fontWeight = FontWeight.Bold,
                        color = CharcoalInk,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "الحد الأقصى للأسماء هو 10 أسماء. تظهر الأسماء بالترتيب التصاعدي: الاسم الأول في الأسفل (الجد الأكبر)، والاسم الأخير في الأعلى (الأحدث).",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newNameInput,
                                onValueChange = { newNameInput = it },
                                label = { Text("أدخل اسم السلف") },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = Color.LightGray
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (newNameInput.isNotBlank()) {
                                        if (namesList.size >= 10) {
                                            Toast.makeText(context, "الحد الأقصى للأسماء في الجذع هو 10 أسماء فقط", Toast.LENGTH_SHORT).show()
                                        } else {
                                            namesList.add(newNameInput.trim())
                                            newNameInput = ""
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .background(GoldPrimary, CircleShape)
                                    .size(48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "إضافة سلف", tint = CharcoalInk)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        ) {
                            itemsIndexed(namesList) { index, item ->
                                val visualIndex = namesList.size - index
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .background(ParchmentLight, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .background(GoldPrimary, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = visualIndex.toString(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = CharcoalInk
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(text = item, color = CharcoalInk, fontWeight = FontWeight.SemiBold)
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { namesList.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateTrunkNames(namesList)
                            showTrunkNameManager = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("حفظ التغييرات", color = CharcoalInk, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showTrunkNameManager = false }
                    ) {
                        Text("إلغاء", color = CharcoalInk)
                    }
                }
            )
        }

        // 7. Rename Branch / Leaf Modal Dialog
        if (showRenameDialog && selectedId != null) {
            val selId = selectedId!!
            val initialVal = if (selId.startsWith("b")) {
                project.branches.find { it.id == selId }?.name ?: ""
            } else {
                project.leaves.find { it.id == selId }?.name ?: ""
            }
            var renameInput by remember { mutableStateOf(initialVal) }

            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                title = {
                    Text(
                        text = "تعديل الاسم",
                        fontWeight = FontWeight.Bold,
                        color = CharcoalInk,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            label = { Text("الاسم الجديد") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (renameInput.isNotBlank()) {
                                viewModel.editNameSelected(renameInput.trim())
                            }
                            showRenameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("تعديل", color = CharcoalInk, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showRenameDialog = false }
                    ) {
                        Text("إلغاء", color = CharcoalInk)
                    }
                }
            )
        }

        // 8. Luxury Studio Export Settings Modal Dialog
        if (showExportDialog) {
            LuxuryExportDialog(
                project = project,
                onDismissRequest = { showExportDialog = false },
                onTitleAndDesignerUpdated = { title, designer ->
                    viewModel.updateTitleAndDesigner(title, designer)
                },
                onExportSuccess = { path ->
                    exportedFilePath = path
                    showExportDialog = false
                    showSuccessExportDialog = true
                }
            )
        }

        // 9. Success Export Message Dialog
        if (showSuccessExportDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessExportDialog = false },
                title = {
                    Text(
                        "تم التصدير بنجاح! 🎉",
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "تم تصنيع وتوليد الملف الفني الفاخر لشجرة العائلة وحفظه بنجاح.",
                            fontSize = 13.sp,
                            color = CharcoalInk,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ParchmentMedium, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "مسار حفظ الملف:",
                                fontSize = 11.sp,
                                color = CharcoalInk.copy(alpha = 0.6f)
                            )
                            Text(
                                text = exportedFilePath,
                                fontSize = 12.sp,
                                color = CharcoalInk,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(top = 4.dp),
                                style = androidx.compose.ui.text.TextStyle(textDirection = TextDirection.Ltr)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("مسار ملف الشجرة", exportedFilePath)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ مسار الملف إلى الحافظة!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ مسار الملف", color = CharcoalInk)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showSuccessExportDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("ممتاز", color = CharcoalInk, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun DialogMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
