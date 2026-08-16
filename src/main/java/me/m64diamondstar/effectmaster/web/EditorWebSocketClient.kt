package me.m64diamondstar.effectmaster.web

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.m64diamondstar.effectmaster.EffectMaster
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionStage

class EditorWebSocketClient(
    private val sessionCode: String,
    private val onConnect: () -> Unit,
    private val onMessage: (JsonObject) -> Unit,
    private val onClose: () -> Unit
) : WebSocket.Listener {

    private var webSocket: WebSocket? = null
    private var closed = false  // ← prevents double onClose calls
    private val messageBuffer = StringBuilder()

    fun connect() {
        // Get the relay server url from the environment variable.
        // Will change later to be more configurable to allow different types of relay servers.
        val relayUrl = System.getenv("relay-url")
        val uri = URI.create("$relayUrl?code=$sessionCode&role=plugin")

        HttpClient.newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(uri, this)
            .thenAccept { ws ->
                webSocket = ws
                EffectMaster.plugin().logger.info(
                    "[Editor] Connected to relay with session code: $sessionCode"
                )
                onConnect()
            }
            .exceptionally { err ->
                EffectMaster.plugin().logger.warning(
                    "[Editor] Failed to connect to relay: ${err.message}. Used URL: $uri"
                )
                fireClose()
                null
            }
    }

    fun send(obj: JsonObject) {
        webSocket?.sendText(obj.toString(), true)
    }

    fun close(silent: Boolean = false) {
        if (closed) return
        closed = true
        webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "Session ended")
        webSocket = null
        if (!silent) onClose()
    }

    fun isOpen() = webSocket != null && !webSocket!!.isOutputClosed

    private fun fireClose() {
        if (!closed) {
            closed = true
            onClose()
        }
    }

    override fun onText(
        ws: WebSocket, data: CharSequence, last: Boolean
    ): CompletionStage<*>? {
        messageBuffer.append(data)
        if (last) {
            val text = messageBuffer.toString()
            messageBuffer.clear()
            runCatching {
                val json = JsonParser.parseString(text).asJsonObject
                onMessage(json)
            }.onFailure {
                EffectMaster.plugin().logger.warning("[Editor] Bad message from relay: $text")
            }
        }
        if (!closed) ws.request(1)
        return null
    }

    override fun onClose(
        ws: WebSocket, statusCode: Int, reason: String
    ): CompletionStage<*>? {
        EffectMaster.plugin().logger.info("[Editor] Session closed ($statusCode: $reason)")
        webSocket = null
        fireClose()
        return null
    }

    override fun onError(ws: WebSocket, error: Throwable) {
        EffectMaster.plugin().logger.warning("[Editor] WebSocket error: ${error.message}")
        webSocket = null
        fireClose()
    }
}