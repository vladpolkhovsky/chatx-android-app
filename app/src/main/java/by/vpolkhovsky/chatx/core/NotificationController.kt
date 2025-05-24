package by.vpolkhovsky.chatx.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object NotificationController {

    private val queue: ArrayDeque<Event> = ArrayDeque(64)

    fun notify(type: String, message: String) {
        queue.addLast(Event(type, message))
    }

    val notificationFlow: Flow<String> = flow {
        while (true) {
            while(queue.isNotEmpty()) {
                val event = queue.removeFirst()
                emit("${event.type}: ${event.message}")
            }
            delay(500)
        }
    }
}

data class Event(
    val type: String,
    val message: String
)