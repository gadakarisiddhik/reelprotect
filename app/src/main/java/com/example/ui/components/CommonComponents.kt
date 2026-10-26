package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProtectionStatus
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.StatusAllowedSlate
import com.example.ui.theme.StatusErrorRed
import com.example.ui.theme.StatusProtectedGreen
import com.example.ui.theme.StatusSetupAmber
import com.example.ui.theme.VioletPrimary

@Composable
fun ProtectionStatusBadge(
    status: ProtectionStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label, icon) = when (status) {
        ProtectionStatus.ACTIVE_PROTECTION -> Quadruple(
            StatusProtectedGreen.copy(alpha = 0.15f),
            StatusProtectedGreen,
            "Protected",
            Icons.Default.CheckCircle
        )
        ProtectionStatus.SETUP_REQUIRED -> Quadruple(
            StatusSetupAmber.copy(alpha = 0.15f),
            StatusSetupAmber,
            "Setup Required",
            Icons.Default.Warning
        )
        ProtectionStatus.ALLOWED -> Quadruple(
            StatusAllowedSlate.copy(alpha = 0.15f),
            StatusAllowedSlate,
            "Allowed",
            Icons.Default.Info
        )
        ProtectionStatus.LIMITED_SUPPORT -> Quadruple(
            StatusSetupAmber.copy(alpha = 0.15f),
            StatusSetupAmber,
            "Limited Support",
            Icons.Default.Info
        )
        ProtectionStatus.NOT_INSTALLED -> Quadruple(
            StatusAllowedSlate.copy(alpha = 0.1f),
            StatusAllowedSlate,
            "Not Installed",
            Icons.Default.Info
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AppBrandBadge(
    appName: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, brandChar) = when (appName.lowercase()) {
        "youtube" -> Color(0xFFEF4444) to "YT"
        "instagram" -> Color(0xFFE1306C) to "IG"
        "facebook" -> Color(0xFF1877F2) to "FB"
        "tiktok" -> Color(0xFF000000) to "TK"
        "snapchat" -> Color(0xFFFACC15) to "SC"
        else -> VioletPrimary to appName.take(2).uppercase()
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .background(bgColor, CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = brandChar,
            color = if (appName.lowercase() == "snapchat") Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ActionConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = VioletPrimary)
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
