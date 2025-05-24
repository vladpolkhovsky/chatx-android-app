package by.vpolkhovsky.chatx.ui.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import by.vpolkhovsky.chatx.core.ChatXAppViewModelProvider
import by.vpolkhovsky.chatx.domain.FileAttachmentsData
import by.vpolkhovsky.chatx.domain.MessageData
import by.vpolkhovsky.chatx.ui.components.ChatXAppBar
import by.vpolkhovsky.chatx.ui.components.CodePopup
import by.vpolkhovsky.chatx.ui.components.FunctionalityNotAvailablePopup
import by.vpolkhovsky.chatx.ui.components.UserInput
import by.vpolkhovsky.chatx.ui.theme.ChatXTheme

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatScreenViewModel: ChatScreenViewModel = viewModel(factory = ChatXAppViewModelProvider.Factory),
    onBack: () -> Unit = {}
) {
    val uiState by chatScreenViewModel.uiState.collectAsState()

    if (uiState is ChatUiStateLoading) {
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

    val readyUiState = uiState as ChatUiStateReady

    ChatScreenLayout(
        onReachedEndScroll = { chatScreenViewModel.needToScrollDown = false },
        needToScrollDown = chatScreenViewModel.needToScrollDown,
        onReplyChatSelected = {
            chatScreenViewModel.selectReplyMessage(it)
        },
        onReplyChatCleared = {
            chatScreenViewModel.clearReplyMessage()
        },
        sendMessage = chatScreenViewModel::sendMessage,
        replyToMessage = chatScreenViewModel.replyToMessage,
        modifier = modifier,
        uiState = readyUiState,
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenLayout(
    onReplyChatSelected: (Int) -> Unit = {},
    onReplyChatCleared: () -> Unit = {},
    sendMessage: (String) -> Unit = {},
    replyToMessage: Int? = null,
    modifier: Modifier = Modifier,
    uiState: ChatUiStateReady,
    needToScrollDown: Boolean = false,
    onReachedEndScroll: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(), topBar = {
            ChannelNameBar(
                channelName = uiState.chatName,
                channelMembers = uiState.chatMembers.size,
                code = uiState.code,
                modifier = modifier,
                onNavIconPressed = onBack
            )
        }) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ChatMessagesBox(
                onReplyChatCleared = onReplyChatCleared,
                onReplyChatSelected = onReplyChatSelected,
                replyToMessage = replyToMessage,
                messages = uiState.messages,
                currentProfileId = uiState.currentProfile.id,
                needToScrollDown = needToScrollDown,
                onReachedEndScroll = onReachedEndScroll,
                modifier = modifier.weight(1f)
            )
            UserInput(
                onMessageSent = sendMessage
            )
        }
    }
}

@Composable
fun ChatMessagesBox(
    onReplyChatSelected: (Int) -> Unit = {},
    onReplyChatCleared: () -> Unit = {},
    replyToMessage: Int? = null,
    currentProfileId: Int,
    messages: List<MessageData>,
    needToScrollDown: Boolean = false,
    onReachedEndScroll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.BottomCenter, modifier = modifier.fillMaxSize()) {
        val lazyListState = rememberLazyListState(messages.size)

        LazyColumn(modifier = modifier, state = lazyListState) {
            itemsIndexed(messages) { index, msg ->
                Spacer(modifier = Modifier.height(6.dp))
                MessageBox(
                    currentProfileId = currentProfileId,
                    messageData = msg,
                    onReplyChatSelected = onReplyChatSelected
                )
            }
        }

        if (replyToMessage != null) {
            ReplyToMessageComponent(
                message = messages.find { it.id == replyToMessage }!!,
                modifier = modifier,
                onReplyChatCleared = onReplyChatCleared
            )
        }

        if (needToScrollDown) {
            LaunchedEffect(needToScrollDown) {
                lazyListState.animateScrollToItem(messages.size)
                onReachedEndScroll()
            }
        }
    }
}

@Composable
fun ReplyToMessageComponent(
    onReplyChatCleared: () -> Unit = {},
    message: MessageData,
    modifier: Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier) {
        Card(
            modifier = modifier.padding(6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            shape = AbsoluteRoundedCornerShape(
                topLeft = 4.dp, topRight = 4.dp, bottomLeft = 4.dp, bottomRight = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(4.dp)
            ) {
                Text(text = buildAnnotatedString {
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontFamily = MaterialTheme.typography.labelSmall.fontFamily
                        )
                    ) {
                        append("Reply to: ")
                    }
                    withStyle(
                        SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontStyle = MaterialTheme.typography.labelMedium.fontStyle,
                            fontSize = MaterialTheme.typography.labelMedium.fontSize,
                            fontFamily = MaterialTheme.typography.labelMedium.fontFamily
                        )
                    ) {
                        append(message.from.username)
                    }
                })
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    message.text.toString(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.bodyMedium.fontSize,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontStyle = MaterialTheme.typography.bodyMedium.fontStyle,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Light,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
                )
            }
        }
        Icon(
            imageVector = Icons.Default.Close,
            tint = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier
                .align(alignment = Alignment.TopEnd)
                .clickable(onClick = onReplyChatCleared)
                .padding(4.dp),
            contentDescription = "delete reply"
        )
    }
}

@Composable
fun MessageBox(
    onReplyChatSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    currentProfileId: Int,
    messageData: MessageData,
    isReply: Boolean = false
) {
    val cardColor =
        if (messageData.from.id == currentProfileId) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh

    val textColor =
        if (messageData.from.id == currentProfileId) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSecondaryContainer

    val colPadding = if (isReply) 0.dp else 6.dp

    Column(
        modifier = modifier
            .combinedClickable(enabled = !isReply, onClick = {}, onLongClick = {
                onReplyChatSelected(messageData.id)
            })
            .fillMaxWidth()
            .padding(colPadding)
    ) {
        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(cardColor),
            elevation = CardDefaults.elevatedCardElevation(3.dp, 3.dp, 3.dp, 3.dp, 3.dp, 3.dp),
            shape = AbsoluteRoundedCornerShape(
                topLeft = 0.dp, topRight = 2.dp, bottomLeft = 2.dp, bottomRight = 0.dp
            )
        ) {
            val horizontalPadding = if (isReply) 3.dp else 5.dp
            val verticalPadding = if (isReply) 1.dp else 3.dp
            val topPaddingIfLastReply = if (isReply && messageData.replyTo == null) 6.dp else 0.dp

            Spacer(Modifier.height(3.dp))

            Column(
                modifier = modifier.padding(
                    start = horizontalPadding,
                    bottom = verticalPadding,
                    top = topPaddingIfLastReply
                )
            ) {
                messageData.replyTo?.let {
                    Row {
                        Spacer(
                            Modifier
                                .width(2.dp)
                                .border(1.dp, textColor)
                        )
                        MessageBox(
                            messageData = messageData.replyTo,
                            isReply = true,
                            currentProfileId = currentProfileId,
                            modifier = modifier.fillMaxSize()
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Text(
                    modifier = modifier.padding(end = 6.dp),
                    text = messageData.from.username,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = MaterialTheme.typography.labelLarge.fontSize,
                    color = textColor,
                    fontStyle = MaterialTheme.typography.labelLarge.fontStyle,
                    fontSize = MaterialTheme.typography.labelLarge.fontSize,
                    fontFamily = MaterialTheme.typography.labelLarge.fontFamily
                )

                Spacer(modifier = Modifier.height(2.dp))

                messageData.text?.let {
                    Text(
                        text = messageData.text,
                        modifier = modifier.padding(end = 6.dp),
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodyLarge.fontSize,
                        color = textColor,
                        fontStyle = MaterialTheme.typography.bodyLarge.fontStyle,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Light,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                    )
                }

                val scroll = rememberScrollState()

                Row(
                    modifier = Modifier
                        .horizontalScroll(scroll)
                        .padding(top = 3.dp, end = 6.dp)
                ) {
                    for (file in messageData.files) {
                        File(file = file, textColor = textColor)
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text = messageData.date.toDateString(),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .align(alignment = Alignment.End),
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = MaterialTheme.typography.titleSmall.fontSize,
                    color = textColor,
                    fontStyle = MaterialTheme.typography.titleSmall.fontStyle,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    fontWeight = FontWeight.Light,
                    fontFamily = MaterialTheme.typography.titleSmall.fontFamily
                )
            }
        }
    }
}

@Composable
fun File(
    file: FileAttachmentsData,
    textColor: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.extraSmall
            )
            .padding(2.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Download,
            contentDescription = file.filename,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = file.filename,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            lineHeight = MaterialTheme.typography.labelSmall.fontSize,
            color = textColor,
            fontStyle = MaterialTheme.typography.labelSmall.fontStyle,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            fontFamily = MaterialTheme.typography.labelSmall.fontFamily
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelNameBar(
    code: String? = null,
    channelName: String,
    channelMembers: Int,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    onNavIconPressed: () -> Unit = { }
) {
    var functionalityNotAvailablePopupShown by remember { mutableStateOf(false) }
    var showCodePopup by remember { mutableStateOf(false) }

    if (functionalityNotAvailablePopupShown) {
        FunctionalityNotAvailablePopup { functionalityNotAvailablePopupShown = false }
    }

    if (showCodePopup && code != null) {
        CodePopup(code) { showCodePopup = false }
    }

    ChatXAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        onNavIconPressed = onNavIconPressed,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = channelName, style = MaterialTheme.typography.titleMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val lineHeightSp: TextUnit = MaterialTheme.typography.bodySmall.fontSize
                    val lineHeightDp: Dp = with(LocalDensity.current) {
                        lineHeightSp.toDp() + 1.dp
                    }
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Members",
                        modifier = modifier.size(lineHeightDp)
                    )
                    Text(
                        text = channelMembers.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        actions = {
            Icon(
                imageVector = Icons.Outlined.Info,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable(onClick = {
                        functionalityNotAvailablePopupShown = code == null
                        showCodePopup = code != null
                    })
                    .padding(horizontal = 12.dp, vertical = 16.dp)
                    .height(24.dp),
                contentDescription = null
            )
        })
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun ChatScreenPreview() {
    ChatXTheme {
        ChatScreenLayout(
            uiState = ChatUiStateReady.Default
        )
    }
}