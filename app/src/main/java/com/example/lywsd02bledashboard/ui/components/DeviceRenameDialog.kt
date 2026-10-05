package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.theme.InkMuted
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.TealPrimary

/**
 * 기기 별칭(Alias) 수정 다이얼로그
 */
@Composable
fun DeviceRenameDialog(
    currentAlias: String?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var aliasText by remember { mutableStateOf(currentAlias ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "기기 별칭 수정",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "이 기기를 알아보기 쉬운 친숙한 이름(예: 거실, 안방, 사무실)으로 변경하세요.",
                    fontSize = 13.sp,
                    color = InkMuted
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = aliasText,
                    onValueChange = { aliasText = it },
                    label = { 
                        Text(
                            text = "기기 별칭",
                            color = InkPrimary,
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = InkPrimary,
                        unfocusedTextColor = InkPrimary,
                        focusedLabelColor = TealPrimary,
                        unfocusedLabelColor = InkPrimary,
                        focusedBorderColor = TealPrimary,
                        unfocusedBorderColor = com.example.lywsd02bledashboard.theme.BorderLineStrong
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(aliasText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "저장", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소", color = InkPrimary, fontWeight = FontWeight.Bold)
            }
        }
    )
}
