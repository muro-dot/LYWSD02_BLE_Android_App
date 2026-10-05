package com.example.lywsd02bledashboard.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.model.ConnectionState
import com.example.lywsd02bledashboard.theme.CyanAccent
import com.example.lywsd02bledashboard.theme.DeepTeal
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.StatusAmber
import com.example.lywsd02bledashboard.theme.StatusGreen
import com.example.lywsd02bledashboard.theme.StatusRed
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * 대시보드 상단 헤더: 앱 타이틀, 연결 상태 배지, 기기 검색 / 연결 해제 버튼
 */
@Composable
fun HeaderSection(
    connectionState: ConnectionState,
    onSearchClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepTeal,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 앱 타이틀 & 로고 아이콘
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(TealPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = "Bluetooth Logo",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "LYWSD02 BLE",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Xiaomi Mijia 센서 대시보드",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 상태 뱃지
                StatusBadge(connectionState = connectionState)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 주요 제어 버튼 (기기 검색 / 연결 해제)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (connectionState) {
                    ConnectionState.CONNECTED -> {
                        Button(
                            onClick = onDisconnectClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "연결 해제", fontWeight = FontWeight.Bold)
                        }
                    }
                    ConnectionState.CONNECTING -> {
                        Button(
                            onClick = onDisconnectClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StatusAmber,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "연결 취소", fontWeight = FontWeight.Bold)
                        }
                    }
                    ConnectionState.SCANNING -> {
                        Button(
                            onClick = onSearchClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothSearching,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "검색 중...", fontWeight = FontWeight.Bold)
                        }
                    }
                    ConnectionState.DISCONNECTED -> {
                        Button(
                            onClick = onSearchClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TealPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "기기 검색", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 연결 상태를 시각적으로 나타내는 인디케이터 배지
 */
@Composable
private fun StatusBadge(connectionState: ConnectionState) {
    val (bgColor, textColor, label, dotColor) = when (connectionState) {
        ConnectionState.CONNECTED -> Quadruple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "연결됨",
            StatusGreen
        )
        ConnectionState.CONNECTING -> Quadruple(
            Color(0xFFFFF8E1),
            Color(0xFFF57F17),
            "연결 중...",
            StatusAmber
        )
        ConnectionState.SCANNING -> Quadruple(
            Color(0xFFE0F7FA),
            Color(0xFF006064),
            "탐색 중",
            CyanAccent
        )
        ConnectionState.DISCONNECTED -> Quadruple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            "연결 안 됨",
            StatusRed
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
