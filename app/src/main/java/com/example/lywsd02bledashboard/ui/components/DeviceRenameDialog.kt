package com.example.lywsd02bledashboard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lywsd02bledashboard.R
import com.example.lywsd02bledashboard.theme.BorderLineStrong
import com.example.lywsd02bledashboard.theme.InkPrimary
import com.example.lywsd02bledashboard.theme.InkSecondary
import com.example.lywsd02bledashboard.theme.SurfaceWhite
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
        containerColor = SurfaceWhite,
        titleContentColor = InkPrimary,
        textContentColor = InkPrimary,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = stringResource(R.string.rename_dialog_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.rename_dialog_desc),
                    fontSize = 13.sp,
                    color = InkSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = aliasText,
                    onValueChange = { aliasText = it },
                    label = { 
                        Text(
                            text = stringResource(R.string.rename_dialog_label),
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
                        unfocusedBorderColor = BorderLineStrong
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
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.rename_dialog_btn_save), 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BorderLineStrong),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = InkPrimary),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.rename_dialog_btn_cancel), 
                    color = InkPrimary, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 13.sp
                )
            }
        }
    )
}
