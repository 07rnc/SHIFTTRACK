package com.example.shifttrack.websocket

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class WebSocketEvent {
    data class AttendanceUpdate(val data: String) : WebSocketEvent()
    data class LeaveUpdate(val data: String) : WebSocketEvent()
    data class ShiftUpdate(val data: String) : WebSocketEvent()
    data class ConnectionChanged(val isConnected: Boolean) : WebSocketEvent()
}

class WebSocketManager {
    private var socket: Socket? = null
    private val _events = MutableSharedFlow<WebSocketEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<WebSocketEvent> = _events.asSharedFlow()

    fun connect(serverUrl: String, authToken: String?) {
        try {
            if (socket?.connected() == true) return

            val options = IO.Options().apply {
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 2000
                timeout = 10000
                if (!authToken.isNullOrBlank()) {
                    auth = mapOf("token" to authToken)
                }
            }

            socket = IO.socket(serverUrl, options).apply {
                on(Socket.EVENT_CONNECT) {
                    Log.d("WebSocketManager", "Connected to live WebSocket")
                    _events.tryEmit(WebSocketEvent.ConnectionChanged(true))
                }
                on(Socket.EVENT_DISCONNECT) {
                    Log.d("WebSocketManager", "Disconnected from WebSocket")
                    _events.tryEmit(WebSocketEvent.ConnectionChanged(false))
                }
                on("attendance:update") { args ->
                    val data = args.getOrNull(0)?.toString() ?: ""
                    _events.tryEmit(WebSocketEvent.AttendanceUpdate(data))
                }
                on("leave:update") { args ->
                    val data = args.getOrNull(0)?.toString() ?: ""
                    _events.tryEmit(WebSocketEvent.LeaveUpdate(data))
                }
                on("shift:update") { args ->
                    val data = args.getOrNull(0)?.toString() ?: ""
                    _events.tryEmit(WebSocketEvent.ShiftUpdate(data))
                }
                connect()
            }
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Failed to connect WebSocket: ${e.message}")
        }
    }

    fun disconnect() {
        try {
            socket?.disconnect()
            socket?.off()
            socket = null
            _events.tryEmit(WebSocketEvent.ConnectionChanged(false))
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Error disconnecting: ${e.message}")
        }
    }
}
