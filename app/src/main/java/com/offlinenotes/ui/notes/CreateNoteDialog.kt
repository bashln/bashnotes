package com.offlinenotes.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.offlinenotes.domain.NoteKind

@Composable
fun CreateNoteDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, kind: NoteKind) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var kind by remember { mutableStateOf(NoteKind.MARKDOWN_NOTE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nova nota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("nota-YYYYMMDD") }
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = kind == NoteKind.MARKDOWN_NOTE,
                        onClick = { kind = NoteKind.MARKDOWN_NOTE },
                        label = { Text("Nota (.md)") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = kind == NoteKind.MARKDOWN_TASKS,
                        onClick = { kind = NoteKind.MARKDOWN_TASKS },
                        label = { Text("Lista (.md)") }
                    )
                }

                FilterChip(
                    selected = kind == NoteKind.ORG_NOTE,
                    onClick = { kind = NoteKind.ORG_NOTE },
                    label = { Text("Org (.org)") },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.trim(), kind) }) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
