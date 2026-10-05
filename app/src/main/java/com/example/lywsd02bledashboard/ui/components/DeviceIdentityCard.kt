package com.example.lywsd02bledashboard.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.DeepTeal
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.SurfaceSoft
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * 연결된 기기 식별 카드: 별칭/이름 표시, 별칭 변경 다이얼로그 호출, MAC 주소 복사 기능
 */
@Composable
fun DeviceIdentityCard(
    deviceId: String?,
    deviceName: String?,
    deviceAlias: String?,
    isConnected: Boolean,
    onRenameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isConnected) SurfaceSoft else Color(0xFFF0F0F0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = if (isConnected) TealPrimary else InkMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        val displayTitle = when {
                            !deviceAlias.isNullOrBlank() -> deviceAlias
                            !deviceName.isNullOrBlank() -> deviceName
                            else -> "연결된 기기 없음"
                        }
                        Text(
                            text = displayTitle,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )

                        val idText = if (deviceId != null) {
                            val shortId = deviceId.replace(":", "").takeLast(6).uppercase()
                            "ID · $shortId ($deviceId)"
                        } else {
                            "기기 연결 대기 중"
                        }
                        Text(
                            text = idText,
                            fontSize = 12.sp,
                            color = InkMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // 별칭 변경 버튼
                OutlinedButton(
                    onClick = onRenameClick,
                    enabled = isConnected,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "별칭 변경",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "이름 변경", fontSize = 12.sp)
                }
            }

            // MAC 주소 복사 행
            if (deviceId != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceSoft)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MAC 주소: $deviceId",
                        fontSize = 11.sp,
                        color = InkMuted,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MAC Address", deviceId)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "MAC 주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "복사",
                            modifier = Modifier.size(14.dp),
                            tint = TealPrimary
                        )
                    }
                }
            }
        }
    }
}
