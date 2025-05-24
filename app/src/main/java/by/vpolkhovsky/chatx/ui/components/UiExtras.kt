package by.vpolkhovsky.chatx.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import by.vpolkhovsky.chatx.ui.theme.ChatXTheme

@Composable
fun FunctionalityNotAvailablePopup(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Text(
                text = "Functionality not available.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CLOSE")
            }
        }
    )
}

@Composable
fun CodePopup(code: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Invite code",
                        fontWeight = FontWeight.Normal,
                        lineHeight = MaterialTheme.typography.displaySmall.fontSize,
                        fontStyle = MaterialTheme.typography.displaySmall.fontStyle,
                        fontSize = MaterialTheme.typography.displaySmall.fontSize,
                        fontFamily = MaterialTheme.typography.displaySmall.fontFamily
                    )
                    Spacer(Modifier.height(30.dp))
                    Text(
                        text = code,
                        fontWeight = FontWeight.Bold,
                        lineHeight = MaterialTheme.typography.displayMedium.fontSize,
                        fontStyle = MaterialTheme.typography.displayMedium.fontStyle,
                        fontSize = MaterialTheme.typography.displayMedium.fontSize,
                        fontFamily = MaterialTheme.typography.displayMedium.fontFamily
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "CLOSE")
            }
        }
    )
}

@PreviewLightDark
@Composable
fun FunctionalityNotAvailablePopupPreview() {
    ChatXTheme {
        FunctionalityNotAvailablePopup(
            onDismiss = {

            }
        )
    }
}

@PreviewLightDark
@Composable
fun CodePopupPreview() {
    ChatXTheme {
        CodePopup(
            code = "abcde123",
            onDismiss = {

            }
        )
    }
}
