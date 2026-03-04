package com.offlinenotes.viewmodel

import androidx.lifecycle.ViewModel
import com.offlinenotes.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HelpLink(
    val title: String,
    val subtitle: String,
    val url: String
)

data class HelpUiState(
    val versionLabel: String,
    val buildDateLabel: String,
    val authorLabel: String,
    val links: List<HelpLink>
)

class HelpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        HelpUiState(
            versionLabel = "Versao: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            buildDateLabel = "Build: ${BuildConfig.BUILD_DATE}",
            authorLabel = "Autor: bashln",
            links = listOf(
                HelpLink(
                    title = "Repositorio oficial",
                    subtitle = "Codigo-fonte e releases no GitHub",
                    url = "https://github.com/bashln/offline-notes"
                ),
                HelpLink(
                    title = "Releases",
                    subtitle = "Baixe a ultima versao estavel",
                    url = "https://github.com/bashln/offline-notes/releases"
                ),
                HelpLink(
                    title = "Autor",
                    subtitle = "Perfil do mantenedor",
                    url = "https://github.com/bashln"
                )
            )
        )
    )

    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()
}
