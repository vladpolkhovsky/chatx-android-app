package by.vpolkhovsky.chatx.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.vpolkhovsky.chatx.core.ChatXAppViewModelProvider
import by.vpolkhovsky.chatx.domain.ChatsPreviewData
import by.vpolkhovsky.chatx.domain.FileAttachmentsData
import by.vpolkhovsky.chatx.domain.LastMessagePreviewData
import by.vpolkhovsky.chatx.domain.abbreviateIfGreaterThan
import by.vpolkhovsky.chatx.ui.theme.ChatXTheme
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSelectorScreen(
    modifier: Modifier = Modifier,
    onBackToLogin: () -> Unit = {},
    onChangeSession: (profileId: Int) -> Unit = {},
    onChatSelected: (profile: Int, chat: Int) -> Unit = { a, b -> },
    chatSelectorViewModel: ChatSelectorViewModel = viewModel(factory = ChatXAppViewModelProvider.Factory)
) {
    val uiState by chatSelectorViewModel.uiState.collectAsState()

    if (uiState is LoadingChatSelectorUiState) {
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

        return
    }

    val readyUiState = uiState as ReadyChatSelectorUiState

    ChatSelectorLayout(
        modifier = modifier,
        chatUiState = readyUiState,
        onChatSelected = onChatSelected,
        backToLogin = onBackToLogin,
        onLogout = {
            chatSelectorViewModel.logout(onBackToLogin)
        },
        onCreateChat = { chatName ->
            chatSelectorViewModel.createNewChat(chatName)
        },
        onJoinChat = { code ->
            chatSelectorViewModel.joinByCode(code)
        },
        onSelectOtherSession = { profileId ->
            onChangeSession(profileId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSelectorLayout(
    modifier: Modifier = Modifier,
    chatUiState: ReadyChatSelectorUiState,
    onChatSelected: (profile: Int, chat: Int) -> Unit = { a, b -> },
    backToLogin: () -> Unit = {},
    onCreateChat: (chatName: String) -> Unit = { chatName -> },
    onJoinChat: (code: String) -> Unit = { code -> },
    onSelectOtherSession: (profileId: Int) -> Unit = { profileId -> },
    onLogout: () -> Unit = { }
) {

    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showChatCreationDialog by remember { mutableStateOf(false) }
    var showChatJoinDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerContent = {
            SideChatsMenu(
                chatUiState = chatUiState,
                logout = onLogout,
                showCreateChatAlert = {
                    showChatCreationDialog = true
                },
                showJoinChatAlert = {
                    showChatJoinDialog = true
                },
                backToLogin = backToLogin,
                selectOtherSession = onSelectOtherSession,
                modifier = modifier
            )
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = chatUiState.getCurrentProfile().username + " chats",
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) {
                                    drawerState.open()
                                } else {
                                    drawerState.close()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            ChatSelectorChatMenu(
                onChatSelected = onChatSelected,
                chatUiState = chatUiState,
                modifier = modifier
                    .padding(innerPadding)
            )
        }

        if (showChatCreationDialog) {
            ChatCreationDialog(
                onCreateClick = { chatName ->
                    onCreateChat(chatName)
                    showChatCreationDialog = false
                },
                onDismissClick = {
                    showChatCreationDialog = false
                }
            )
        }

        if (showChatJoinDialog) {
            JoinChatDialog(
                onJoinClick = { code ->
                    onJoinChat(code)
                    showChatJoinDialog = false
                },
                onDismissClick = {
                    showChatJoinDialog = false
                }
            )
        }
    }
}

@Composable
fun ChatSelectorChatMenu(
    onChatSelected: (profile: Int, chat: Int) -> Unit = { a, b -> },
    chatUiState: ReadyChatSelectorUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(6.dp),
    ) {
        itemsIndexed(chatUiState.chats) { index, chat ->
            ChatItem(
                currentProfileId = chatUiState.currentProfileId,
                onChatSelected = onChatSelected,
                chat = chat
            )
        }
    }
}

@Composable
fun ChatItem(
    currentProfileId: Int,
    chat: ChatsPreviewData,
    onChatSelected: (profile: Int, chat: Int) -> Unit = { a, b -> },
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.extraSmall,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = modifier.clickable(
            onClick = {
                onChatSelected(currentProfileId, chat.id)
            }
        )
    ) {
        Column(
            modifier = modifier
                .padding(8.dp, 4.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = chat.chatName,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.tertiary,
                fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
            )
            if (chat.lastMessage != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChatLastMessage(
                        lastMessage = chat.lastMessage,
                        modifier = modifier.weight(1f)
                    )
                    if (chat.newMessageCount > 0) {
                        ChatNewMessageIndicator(chat.newMessageCount.abbreviateIfGreaterThan(99));
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Row(
                    modifier = modifier.weight(1f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Participants",
                        modifier = modifier.size(MaterialTheme.typography.bodySmall.fontSize.value.dp)
                    )
                    Text(
                        text = chat.participantCount.abbreviateIfGreaterThan(99),
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.secondary,
                        overflow = TextOverflow.Visible,
                        textAlign = TextAlign.End,
                        fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                    )
                }
                Spacer(modifier = modifier.weight(1f))
                val lastMessageDate = chat.lastMessage?.date ?: chat.chatCreationDate
                Text(
                    text = lastMessageDate.toDateString(),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    overflow = TextOverflow.Visible,
                    textAlign = TextAlign.End,
                    fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily
                )
            }

        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun ChatNewMessageIndicator(newMessageCount: String) {
    Box(
        modifier = Modifier
            .size(32.dp, 22.dp)
            .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = newMessageCount,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
            fontSize = MaterialTheme.typography.labelMedium.fontSize,
            fontFamily = MaterialTheme.typography.labelMedium.fontFamily
        )
    }
}

@Composable
fun FileAttachmentItem(
    file: FileAttachmentsData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Download,
            contentDescription = "Download " + file.filename,
            modifier.size(16.dp)
        )
        Text(
            text = file.filename,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            fontFamily = MaterialTheme.typography.labelSmall.fontFamily
        )
    }
    Spacer(Modifier.width(8.dp))
}

@Composable
fun ChatLastMessage(
    lastMessage: LastMessagePreviewData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = lastMessage.profile.username,
            maxLines = 1,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = MaterialTheme.typography.labelSmall.fontSize,
            fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            fontFamily = MaterialTheme.typography.labelSmall.fontFamily
        )
        if (lastMessage.text != null) {
            Text(
                text = lastMessage.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = MaterialTheme.typography.labelSmall.fontSize,
                color = MaterialTheme.colorScheme.secondary,
                fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                fontFamily = MaterialTheme.typography.labelSmall.fontFamily
            )
        }
        if (lastMessage.files.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${lastMessage.files.size} files added",
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    lineHeight = MaterialTheme.typography.labelSmall.fontSize,
                    color = MaterialTheme.colorScheme.secondary,
                    fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                )
            }
        }
    }
}

@Composable
fun SideChatsMenu(
    chatUiState: ReadyChatSelectorUiState,
    logout: () -> Unit = {},
    backToLogin: () -> Unit = {},
    modifier: Modifier = Modifier,
    selectOtherSession: (Int) -> Unit = {},
    showCreateChatAlert: () -> Unit = {},
    showJoinChatAlert: () -> Unit = {}
) {
    ModalDrawerSheet {
        Column {
            Text(
                text = "Menu",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleLarge
            )
            HorizontalDivider()
            Text(
                "Active sessions",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            for (profile in chatUiState.loadedProfiles) {
                val isSelected = chatUiState.currentProfileId == profile.id
                NavigationDrawerItem(
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = profile.username
                            )
                            Spacer(modifier.width(8.dp))
                            Text(
                                text = profile.username,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                    },
                    selected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            selectOtherSession(profile.id)
                        }
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "Chats",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            NavigationDrawerItem(
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create chat"
                        )
                        Spacer(modifier.width(8.dp))
                        Text(
                            text = "Create chat",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                onClick = showCreateChatAlert,
                selected = false
            )
            NavigationDrawerItem(
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.DoorFront,
                            contentDescription = "Join chat"
                        )
                        Spacer(modifier.width(8.dp))
                        Text(
                            text = "Join chat",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                onClick = showJoinChatAlert,
                selected = false
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "Session management",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            NavigationDrawerItem(
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Login,
                            contentDescription = "Login new"
                        )
                        Spacer(modifier.width(8.dp))
                        Text(
                            text = "Login to new session",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                onClick = backToLogin,
                selected = false
            )
            NavigationDrawerItem(
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Logout"
                        )
                        Spacer(modifier.width(8.dp))
                        Text(
                            text = "Logout",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                onClick = logout,
                selected = false
            )
        }
    }
}

@Composable
fun ChatCreationDialog(
    onCreateClick: (String) -> Unit = {},
    onDismissClick: () -> Unit = {}
) {
    var chatName by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        icon = {
            Icon(Icons.Outlined.Create, contentDescription = "Create chat")
        },
        title = {
            Text(text = "New chat name", color = MaterialTheme.colorScheme.onPrimaryContainer)
        },
        text = {
            OutlinedTextField(
                value = chatName,
                onValueChange = {
                    chatName = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    defaultKeyboardAction(imeAction = ImeAction.Done)
                    onCreateClick(chatName)
                })
            )
        },
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(
                onClick = {
                    onCreateClick(chatName)
                },
                enabled = chatName.isNotEmpty(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun JoinChatDialog(
    onJoinClick: (String) -> Unit = {},
    onDismissClick: () -> Unit = {}
) {
    var code by remember { mutableStateOf("") }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        icon = {
            Icon(Icons.Outlined.Search, contentDescription = "Join to chat")
        },
        title = {
            Text(text = "Chat code", color = MaterialTheme.colorScheme.onPrimaryContainer)
        },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = {
                    code = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    defaultKeyboardAction(imeAction = ImeAction.Done)
                    onJoinClick(code)
                })
            )
        },
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(
                onClick = {
                    onJoinClick(code)
                },
                enabled = code.isNotEmpty(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("Join")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Text("Cancel")
            }
        }
    )
}


@PreviewLightDark
@Composable
fun ChatItemPreview() {
    ChatXTheme {
        ChatItem(1, chat = ChatsPreviewData(1, "123 123", 12, 200, null, LocalDateTime.now()))
    }
}

@PreviewLightDark
@Composable
fun ChatCreationDialogPreview() {
    ChatXTheme {
        ChatCreationDialog()
    }
}

@PreviewLightDark
@Composable
fun JoinChatDialogPreview() {
    ChatXTheme {
        JoinChatDialog()
    }
}

@PreviewLightDark
@Composable
fun SideChatsMenuPreview() {
    ChatXTheme {
        SideChatsMenu(chatUiState = ReadyChatSelectorUiState.Default)
    }
}

@PreviewLightDark
@Composable
fun ChatSelectorChatMenuPreview() {
    ChatXTheme {
        ChatSelectorChatMenu(chatUiState = ReadyChatSelectorUiState.Default)
    }
}

@PreviewLightDark
@Composable
fun ChatsScreenPreview() {
    ChatXTheme {
        ChatSelectorLayout(
            chatUiState = ReadyChatSelectorUiState.Default
        )
    }
}