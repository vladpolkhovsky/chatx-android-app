package by.vpolkhovsky.chatx.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.vpolkhovsky.chatx.R
import by.vpolkhovsky.chatx.core.ChatXAppViewModelProvider
import by.vpolkhovsky.chatx.domain.ProfileData
import by.vpolkhovsky.chatx.ui.theme.ChatXTheme

const val MAX_TEXT_FIELD_LENGTH = 25
val usernameRegex = "^\\S+$".toRegex()
val passwordRegex = "^[\\w\\S]+$".toRegex()

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    loginScreenViewModel: LoginScreenViewModel = viewModel(factory = ChatXAppViewModelProvider.Factory),
    onLoginToProfileId: (profileId: Int) -> Unit = { }
) {
    val uiState by loginScreenViewModel.uiState.collectAsState()

    LoginLayout(
        modifier = modifier,
        uiState = uiState,
        onLoginAction = { username, password ->
            loginScreenViewModel.tryLogin(username, password, onLoginToProfileId)
        }
    )
}

@Composable
fun LoginLayout(
    modifier: Modifier = Modifier,
    uiState: LoginScreenUiState,
    onLoginAction: (username: String, password: String) -> Unit = { username, password -> },
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp),
            modifier = modifier
                .padding(16.dp)
                .heightIn(250.dp, 480.dp)
                .widthIn(170.dp, 370.dp)
        ) {
            if (uiState is RetrievingDataLoadingScreenUiState) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        trackColor = MaterialTheme.colorScheme.onSurface,
                    )
                }
                return@ElevatedCard
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .weight(weight = 1f, fill = false),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    fontFamily = MaterialTheme.typography.displayLarge.fontFamily,
                    fontSize = MaterialTheme.typography.displayLarge.fontSize,
                )

                var usernameValue by remember { mutableStateOf("") }
                var passwordValue by remember { mutableStateOf("") }
                var showPassword by remember { mutableStateOf(false) }

                var isUsernameValueIncorrect by remember { mutableStateOf(false) }
                var isPasswordValueIncorrect by remember { mutableStateOf(false) }

                val (passwordFocusRequester) = FocusRequester.createRefs()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = usernameValue,
                            label = { Text(stringResource(R.string.login_login_field)) },
                            isError = isUsernameValueIncorrect,
                            onValueChange = {
                                if (it.length > MAX_TEXT_FIELD_LENGTH) {
                                    return@OutlinedTextField
                                }
                                usernameValue = it
                                isUsernameValueIncorrect =
                                    !isCorrectString(it, 5, usernameRegex)
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = {
                                passwordFocusRequester.requestFocus()
                            }),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (isUsernameValueIncorrect) {
                            Text(
                                text = stringResource(R.string.login_login_hint),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append(stringResource(R.string.login_example))
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(stringResource(R.string.login_login_example))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = passwordValue,
                            label = { Text(stringResource(R.string.login_password_field)) },
                            isError = isPasswordValueIncorrect,
                            onValueChange = {
                                if (it.length > MAX_TEXT_FIELD_LENGTH) {
                                    return@OutlinedTextField
                                }
                                passwordValue = it
                                isPasswordValueIncorrect =
                                    !isCorrectString(it, 8, passwordRegex)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        painter = if (showPassword) painterResource(R.drawable.visibility_off_24px) else painterResource(
                                            R.drawable.visibility_24px
                                        ),
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            keyboardActions = KeyboardActions(onNext = {
                                this.defaultKeyboardAction(imeAction = ImeAction.Done)
                            }),
                            modifier = Modifier.fillMaxWidth()
                                .focusRequester(passwordFocusRequester)
                        )
                        if (isPasswordValueIncorrect) {
                            Text(
                                text = stringResource(R.string.login_password_hint),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = buildAnnotatedString {
                                    append(stringResource(R.string.login_example))
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append(stringResource(R.string.login_password_example))
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        onLoginAction(usernameValue, passwordValue)
                    },
                    enabled = !(isUsernameValueIncorrect || isPasswordValueIncorrect),
                    modifier = Modifier.padding(6.dp),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(stringResource(R.string.login_button_text))
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun LoginScreenPreview() {
    ChatXTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            LoginLayout(
                modifier = Modifier.padding(),
                uiState = RetrievingDataLoadingScreenUiState
            )
        }
    }
}

@PreviewLightDark
@Composable
fun LoginScreenWithDataPreview() {
    ChatXTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            LoginLayout(
                modifier = Modifier.padding(),
                uiState = ReadyLoginScreenUiState(listOf(
                    ProfileData(1, "123-123-123"),
                    ProfileData(2, "222-222-222")
                ))
            )
        }
    }
}


@PreviewScreenSizes
@PreviewLightDark
@Composable
fun LoginScreenWithPreviewSizes() {
    ChatXTheme {
        Surface {
            LoginLayout(
                modifier = Modifier.padding(),
                uiState = RetrievingDataLoadingScreenUiState
            )
        }
    }
}



@PreviewScreenSizes
@PreviewLightDark
@Composable
fun LoginScreenWithPreviewSizesWithData() {
    ChatXTheme {
        Surface {
            LoginLayout(
                modifier = Modifier.padding(),
                uiState = ReadyLoginScreenUiState(listOf(
                    ProfileData(1, "123-123-123"),
                    ProfileData(2, "222-222-222")
                ))
            )
        }
    }
}

fun isCorrectString(
    value: String,
    minLength: Int,
    valueRegex: Regex,
    allowSpaces: Boolean = false
): Boolean {
    val valueWithoutSpaces =
        value.let { if (!allowSpaces) it.replace("\\s+".toRegex(), "") else it }
    if (valueWithoutSpaces != value) {
        return false
    }
    if (value.length < minLength) {
        return false
    }
    return valueWithoutSpaces.matches(valueRegex)
}
