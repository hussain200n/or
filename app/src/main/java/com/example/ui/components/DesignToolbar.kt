package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalInk
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.ParchmentMedium

@Composable
fun DesignToolbar(
    onAddTrunkClick: () -> Unit,
    onAddBranchClick: () -> Unit,
    onAddLeafClick: () -> Unit,
    onExportClick: () -> Unit,
    onResetView: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    selectedId: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(10.dp))
            .border(1.dp, GoldPrimary, RoundedCornerShape(10.dp))
            .testTag("design_toolbar"),
        colors = CardDefaults.cardColors(containerColor = CharcoalInk.copy(alpha = 0.95f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(GoldPrimary, CircleShape)
                        .size(6.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "مرسم الدخيل",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                    Text(
                        text = "تصميم مشجرات النسب",
                        fontSize = 8.sp,
                        color = ParchmentMedium.copy(alpha = 0.8f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = onAddTrunkClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toolbar_trunk_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "الجذع",
                        modifier = Modifier.size(12.dp),
                        tint = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "الجذع", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddBranchClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toolbar_branch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "الفرع",
                        modifier = Modifier.size(12.dp),
                        tint = GoldAccent
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "الفرع", fontSize = 10.sp, color = GoldAccent, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onAddLeafClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toolbar_leaf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "الورقة",
                        modifier = Modifier.size(12.dp),
                        tint = ForestGreen
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(text = "الورقة", fontSize = 10.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(28.dp)
                        .border(0.5.dp, GoldAccent.copy(alpha = if (canUndo) 0.3f else 0.1f), CircleShape)
                        .testTag("toolbar_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "تراجع",
                        tint = GoldAccent.copy(alpha = if (canUndo) 1f else 0.35f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(28.dp)
                        .border(0.5.dp, GoldAccent.copy(alpha = if (canRedo) 0.3f else 0.1f), CircleShape)
                        .testTag("toolbar_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "إعادة",
                        tint = GoldAccent.copy(alpha = if (canRedo) 1f else 0.35f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = onResetView,
                    modifier = Modifier
                        .size(28.dp)
                        .border(0.5.dp, GoldAccent.copy(alpha = 0.3f), CircleShape)
                        .testTag("toolbar_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "إعادة ضبط",
                        tint = GoldAccent,
                        modifier = Modifier.size(12.dp)
                    )
                }

                Button(
                    onClick = onExportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("toolbar_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "التصدير",
                        tint = CharcoalInk,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "التصدير",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalInk
                    )
                }
            }
        }
    }
}
