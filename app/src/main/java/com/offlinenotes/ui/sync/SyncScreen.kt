package com.offlinenotes.ui.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SyncScreen(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sync (Opcional)")
        Text("OfflineNotes nao sincroniza arquivos por conta propria.")
        Text("Use o app oficial do Nextcloud Android para sincronizar a pasta externa.")
        Text("Passos:")
        Text("1. Escolha uma pasta local no OfflineNotes.")
        Text("2. No Nextcloud Android, sincronize essa mesma pasta.")
        Text("3. Continue editando normalmente offline no OfflineNotes.")
    }
}
