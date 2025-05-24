package by.vpolkhovsky.chatx.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import by.vpolkhovsky.chatx.core.Event
import by.vpolkhovsky.chatx.core.NotificationController
import by.vpolkhovsky.chatx.data.UserProfileService
import by.vpolkhovsky.chatx.domain.ProfileData
import by.vpolkhovsky.chatx.ui.chat.ChatSelectorUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginScreenViewModel(
    val userProfileService: UserProfileService
) : ViewModel() {

    private val _uiState: MutableStateFlow<LoginScreenUiState> =
        MutableStateFlow(RetrievingDataLoadingScreenUiState)

    val uiState: StateFlow<LoginScreenUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                launch {
                    try {
                        val sessions = userProfileService.fetchActiveSessions()
                        withContext(Dispatchers.Main) {
                            _uiState.update {
                                ReadyLoginScreenUiState(sessions)
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            _uiState.update {
                                ReadyLoginScreenUiState(listOf())
                            }
                        }
                        NotificationController.notify("LoginScreenViewModel Init", e.message.toString())
                    }
                }
            }
        }
    }

    fun tryLogin(username: String, password: String, onLoginToProfileId: (Int) -> Unit) {
        val before = _uiState.value

        viewModelScope.launch {
            _uiState.update {
                RetrievingDataLoadingScreenUiState
            }
            withContext(Dispatchers.IO) {
                try {
                    userProfileService.tryLogin(username, password)?.let {
                        withContext(Dispatchers.Main) {
                            onLoginToProfileId(it.id)
                        }
                        _uiState.update {
                            ReadyLoginScreenUiState(
                                userProfileService.fetchActiveSessions()
                            )
                        }
                    } ?: resetState(before)
                } catch (e: Exception) {
                    resetState(before)
                    NotificationController.notify("Exception", e.message.toString())
                }
            }
        }
    }

    suspend fun resetState(uiState: LoginScreenUiState) {
        withContext(Dispatchers.Main) {
            _uiState.update {
                uiState
            }
        }
    }
}


sealed interface LoginScreenUiState

object RetrievingDataLoadingScreenUiState : LoginScreenUiState

data class ReadyLoginScreenUiState(
    val loadedProfiles: List<ProfileData>
) : LoginScreenUiState