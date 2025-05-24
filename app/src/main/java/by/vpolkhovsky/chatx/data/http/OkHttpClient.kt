package by.vpolkhovsky.chatx.data.http

import by.vpolkhovsky.chatx.core.NotificationController
import com.launchdarkly.eventsource.EventHandler
import com.launchdarkly.eventsource.EventSource
import com.launchdarkly.eventsource.MessageEvent
import event.MessageDto
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import rest.ChatCodeDto
import rest.ChatDto
import rest.CreateChatRequest
import rest.LoginRequest
import rest.LoginResponse
import rest.NewMessageRequest
import rest.UserDto
import java.io.Closeable
import java.net.HttpURLConnection
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.function.Supplier

class OkHttpProvider(
    val domain: String
) {
    companion object {
        val jsonMediaType = "application/json".toMediaType()
    }

    fun getClient(token: String? = null): Client {
        return Client(domain, token)
    }

    class Client(
        val domain: String,
        val token: String?
    ) {
        fun login(username: String, password: String): Pair<UserDto, String>? {
            val token = notifyIfError {
                val loginRequest = LoginRequest(username, password)

                val loginRequestBody = Json.encodeToString(loginRequest)
                    .toRequestBody(jsonMediaType)

                val login = Request.Builder()
                    .url("$domain/user/login")
                    .post(loginRequestBody)
                    .build()

                val loginResponse = client().newCall(login).execute()

                checkResponse(loginResponse)

                Json.decodeFromString<LoginResponse>(loginResponse.body!!.string()).token
            }

            if (token == null) {
                return null
            }

            return notifyIfError {
                val iamRespBody = Request.Builder()
                    .url("$domain/user/iam")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val iamResponse = client().newCall(iamRespBody).execute()

                checkResponse(iamResponse)

                val userDto = Json.decodeFromString<UserDto>(iamResponse.body!!.string())

                Pair(userDto, token)
            }
        }

        fun isTokenAlive(token: String): Boolean {
            return this.notifyIfError {
                val iamRespBody = Request.Builder()
                    .url("$domain/user/iam")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val iamResponse = client().newCall(iamRespBody).execute()

                iamResponse.isSuccessful
            } ?: true
        }

        fun fetchUserChats(): List<ChatDto> {
            return notifyIfError {
                val fetchChatRequest = Request.Builder()
                    .url("$domain/chat/")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val fetchChatResponse = client().newCall(fetchChatRequest).execute()
                checkResponse(fetchChatResponse)

                Json.decodeFromString<List<ChatDto>>(fetchChatResponse.body!!.string())
            } ?: emptyList()
        }

        fun createChat(request: CreateChatRequest) {
            notifyIfError {
                val body = Json.encodeToString(request).toRequestBody(jsonMediaType)

                val createChatRequest = Request.Builder()
                    .url("$domain/chat/create")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val createChatResponse = client().newCall(createChatRequest).execute()
                checkResponse(createChatResponse)
            }
        }

        fun getChatCode(chatId: Int): String? {
            return notifyIfError {
                val request = Request.Builder()
                    .url("$domain/chat/$chatId/code")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val createChatResponse = client().newCall(request).execute()
                checkResponse(createChatResponse)

                Json.decodeFromString<ChatCodeDto>(createChatResponse.body!!.string()).code
            }
        }

        fun joinToChatByCode(code: String) {
            notifyIfError {
                val request = Request.Builder()
                    .url("$domain/chat/join/code/$code")
                    .header("Authorization", "Bearer $token")
                    .post("1".toRequestBody())
                    .build()

                val createChatResponse = client().newCall(request).execute()
                checkResponse(createChatResponse)
            }
        }

        fun loadAllChatMembers(chatId: Int): List<UserDto> {
            return notifyIfError {
                val listMembers = Request.Builder()
                    .url("$domain/chat/members/$chatId")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val resp = client().newCall(listMembers).execute()
                checkResponse(resp)


                Json.decodeFromString<List<UserDto>>(resp.body!!.string())
            } ?: emptyList()
        }

        fun collectSseNewChatInformation(): Flow<ChatDto> = flow {
            val queue: ArrayDeque<ChatDto> = ArrayDeque<ChatDto>(8)

            val sseClient = SseClient(
                callback = {
                    queue.addLast(Json.decodeFromString<ChatDto>(it))
                },
                errorCallback = {
                    NotificationController.notify("SSE Load chat error", it?.message.toString())
                }
            )

            SseHandler(sseClient, "$domain/chat/sse/new", token!!).use {
                while (true) {
                    while (queue.isNotEmpty()) {
                        emit(queue.removeFirst())
                    }
                    delay(500)
                }
            }
        }

        fun sendMessage(request: NewMessageRequest): Unit {
            notifyIfError {
                val body = Json.encodeToString(request).toRequestBody(jsonMediaType)

                val createChatRequest = Request.Builder()
                    .url("$domain/message/")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val createChatResponse = client().newCall(createChatRequest).execute()
                checkResponse(createChatResponse)
            }
        }

        fun loadAllMessages(chatId: Int): List<MessageDto> {
            return notifyIfError {
                val fetchMessages = Request.Builder()
                    .url("$domain/message/$chatId")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val fetchMessagesResponse = client().newCall(fetchMessages).execute()
                checkResponse(fetchMessagesResponse)

                Json.decodeFromString<List<MessageDto>>(fetchMessagesResponse.body!!.string())
            } ?: emptyList()
        }

        fun collectSseNewMessages(): Flow<MessageDto> = flow {
            val queue: ArrayDeque<MessageDto> = ArrayDeque<MessageDto>(8)

            val sseClient = SseClient(
                callback = {
                    queue.addLast(Json.decodeFromString<MessageDto>(it))
                },
                errorCallback = {
                    NotificationController.notify("SSE Load new messages error", it?.message.toString())
                }
            )

            SseHandler(sseClient, "$domain/message/sse/new", token!!).use {
                while (true) {
                    while (queue.isNotEmpty()) {
                        emit(queue.removeFirst())
                    }
                    delay(500)
                }
            }
        }

        private fun checkResponse(response: Response) {
            if (response.isSuccessful) {
                return;
            }
            if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                NotificationController.notify("Auth", "Token invalid")
                throw IllegalStateException("Token invalid")
            }
            NotificationController.notify("Auth", "Unknown error. code: $response.code")
            throw IllegalStateException("Unknown error. code: $response.code")
        }

        private fun <T> notifyIfError(supplier: Supplier<T>): T? {
            try {
                return supplier.get()
            } catch (e: Exception) {
                NotificationController.notify("Error during request", e.message.toString())
            }
            return null
        }


        companion object {
            private fun client(): OkHttpClient = OkHttpClient()
        }
    }
}

fun localDateTimeFromMillis(value: Long): LocalDateTime {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(value),
        ZoneId.systemDefault()
    )
}

class SseHandler(
    sseClient: SseClient,
    url: String,
    token: String
) : Closeable {

    private val eventSource = EventSource.Builder(sseClient, URI.create(url))
        .headers(Headers.headersOf("Authorization", "Bearer $token"))
        .connectTimeout(Duration.ofSeconds(10))
        .backoffResetThreshold(Duration.ofSeconds(5))
        .build()

    init {
        eventSource.start()
    }

    override fun close() {
        eventSource.close()
    }
}

class SseClient(
    val callback: (String) -> Unit,
    val errorCallback: (Throwable?) -> Unit
) : EventHandler, Closeable {

    override fun onOpen() {

    }

    override fun onClosed() {

    }

    override fun onMessage(
        event: String?,
        messageEvent: MessageEvent?
    ) {
        messageEvent?.data?.let { data ->
            callback(data)
        }
    }

    override fun onComment(comment: String?) {

    }

    override fun onError(t: Throwable?) {
        errorCallback(t)
    }

    override fun close() {

    }
}