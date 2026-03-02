package com.offlinenotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.offlinenotes.ui.OfflineNotesApp
import com.offlinenotes.ui.theme.OfflineNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OfflineNotesTheme {
                OfflineNotesApp()
            }
        }
    }
}
