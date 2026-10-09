package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.model.AppUpdateInfo
import com.example.lywsd02bledashboard.model.UpdateDownloadState
import com.example.lywsd02bledashboard.theme.BorderLine
import com.example.lywsd02bledashboard.theme.BorderLineStrong
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.InkSecondary
import com.example.lywsd02bledashboard.theme.StatusRed
import com.example.lywsd02bledashboard.theme.SurfaceSoft
import com.example.lywsd02bledashboard.theme.SurfaceWhite
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * GitHub 최신 릴리즈 발견 시 알림을 띄우고 원클릭으로 다운로드 및 설치할 수 있는 모달 다이얼로그
 */
@Composable
fun UpdateDialog(
    updateInfo: AppUpdateInfo,
    downloadState: UpdateDownloadState,
    onDismiss: () -> Unit,
    onStartDownload: () -> Unit,
    onInstall: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = {
            if (downloadState !is UpdateDownloadState.Downloading) {
                onDismiss()
            }
        },
        containerColor = SurfaceWhite,
        titleContentColor = InkPrimary,
        textContentColor = InkPrimary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = stringResource(R.string.update_dialog_title),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Text(
                        text = "v${updateInfo.currentVersion} ➔ v${updateInfo.latestVersion}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TealPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (updateInfo.releaseTitle.isNotBlank()) {
                    Text(
                        text = updateInfo.releaseTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 릴리즈 노트 영역
                if (updateInfo.releaseNotes.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceSoft)
                            .border(1.dp, BorderLine, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = updateInfo.releaseNotes.trim(),
                            fontSize = 12.sp,
                            color = InkSecondary,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 다운로드 및 설치 진행 상태 영역
                when (downloadState) {
                    is UpdateDownloadState.Idle -> {
                        Text(
                            text = stringResource(R.string.update_dialog_prompt),
                            fontSize = 13.sp,
                            color = InkSecondary
                        )
                    }
                    is UpdateDownloadState.Downloading -> {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.update_dialog_downloading),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = InkPrimary
                                )
                                Text(
                                    text = "${downloadState.progressPercent}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { downloadState.progressPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = TealPrimary,
                                trackColor = TealPrimary.copy(alpha = 0.2f),
                            )
                        }
                    }
                    is UpdateDownloadState.DownloadCompleted -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealPrimary.copy(alpha = 0.08f))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.update_dialog_completed),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = InkPrimary
                            )
                        }
                    }
                    is UpdateDownloadState.Error -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusRed.copy(alpha = 0.08f))
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StatusRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = downloadState.errorMessage,
                                fontSize = 12.sp,
                                color = StatusRed,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (downloadState) {
                is UpdateDownloadState.Idle, is UpdateDownloadState.Error -> {
                    Button(
                        onClick = onStartDownload,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_btn_update), 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                is UpdateDownloadState.Downloading -> {
                    // 다운로드 중
                }
                is UpdateDownloadState.DownloadCompleted -> {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TealPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.update_dialog_btn_install), 
                            fontSize = 13.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        dismissButton = {
            if (downloadState !is UpdateDownloadState.Downloading) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, BorderLineStrong),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    val dismissBtnText = if (downloadState is UpdateDownloadState.DownloadCompleted) {
                        stringResource(R.string.update_dialog_btn_later)
                    } else {
                        stringResource(R.string.update_dialog_btn_close)
                    }
                    Text(
                        text = dismissBtnText,
                        color = InkPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}
