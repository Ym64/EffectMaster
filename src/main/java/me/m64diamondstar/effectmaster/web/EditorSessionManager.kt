package me.m64diamondstar.effectmaster.web

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.m64diamondstar.effectmaster.EffectMaster
import me.m64diamondstar.effectmaster.ktx.emComponent
import me.m64diamondstar.effectmaster.shows.utils.ShowUtils
import org.bukkit.entity.Player
import java.util.*

object EditorSessionManager {

    private val sessions = HashMap<UUID, EditorWebSocketClient>()
    private val editorConnected = HashSet<UUID>()
    private val browserDisconnected = HashSet<UUID>()

    private fun hasEditorConnected(player: Player) = player.uniqueId in editorConnected
    private fun markEditorConnected(player: Player) { editorConnected.add(player.uniqueId) }

    fun startSession(player: Player): String {
        closeSession(player, silent = true)

        val code = generateCode()
        val url  = "http://localhost:5173/editor?code=$code"

        player.sendMessage(emComponent("<short_prefix><background>Creating editor session..."))

        val client = EditorWebSocketClient(
            sessionCode = code,
            onConnect = {
                player.sendMessage(emComponent(
                    "<short_prefix><default>Session ready! Open the editor <u><click:open_url:'$url'>here</u>."
                ))
            },
            onMessage = { json ->
                if (!hasEditorConnected(player)) {
                    markEditorConnected(player)
                    player.sendMessage(emComponent("<short_prefix><success>Browser connected to editor session."))
                }
                handleMessage(json, player)
            },
            onClose = {
                // Only show "session closed" if the browser didn't already
                // disconnect cleanly, otherwise EDITOR_DISCONNECTED already
                // told the player what happened
                if (!browserDisconnected.remove(player.uniqueId)) {
                    player.sendMessage(emComponent("<short_prefix><gray>Editor session closed."))
                }
                sessions.remove(player.uniqueId)
                editorConnected.remove(player.uniqueId)
            }
        )

        sessions[player.uniqueId] = client
        client.connect()
        return code
    }

    fun hasSession(player: Player) = sessions[player.uniqueId]?.isOpen() == true

    fun closeSession(player: Player, silent: Boolean = false) {
        sessions.remove(player.uniqueId)?.close(silent)
        editorConnected.remove(player.uniqueId)
        browserDisconnected.remove(player.uniqueId)
    }

    private fun handleMessage(json: JsonObject, player: Player) {
        val client = sessions[player.uniqueId] ?: return

        when (json.get("action")?.asString ?: json.get("type")?.asString) {
            "GET_SHOWS"          -> handleGetShows(client)
            "STOP_SHOW"          -> handleStopShow(json, client)
            "EDITOR_DISCONNECTED" -> {
                browserDisconnected.add(player.uniqueId) // ← mark before showing message
                editorConnected.remove(player.uniqueId)
                player.sendMessage(emComponent("<short_prefix><error>The browser disconnected from the editor."))
                client.close()
            }
            else -> client.send(error("Unknown action"))
        }
    }

    private fun handleGetShows(client: EditorWebSocketClient) {
        val array = JsonArray()

        ShowUtils.getAllShows().forEach { (category, name) ->
            val obj = JsonObject()
            obj.addProperty("category", category.name)
            obj.addProperty("name", name.name)
            array.add(obj)
        }
        val response = JsonObject()
        response.addProperty("type", "SHOW_LIST")
        response.add("shows", array)
        client.send(response)
    }

    private fun handleStopShow(json: JsonObject, client: EditorWebSocketClient) {
        val category = json.get("category")?.asString ?: return client.send(error("Missing category"))
        val name     = json.get("name")?.asString     ?: return client.send(error("Missing name"))

        EffectMaster.plugin().logger.info("Stopping show $category/$name for editor session")
        ShowUtils.getRunningShows(category, name).forEach {
            EffectMaster.plugin().logger.info("Stopping show $category/$name for editor session")
            it.cancel()
        }

        val response = JsonObject()
        response.addProperty("type", "SUCCESS")
        response.addProperty("message", "Show stopped.")
        client.send(response)
    }

    // ── Helpers ───────────────────────────────────────────────────

    private fun error(message: String) = JsonObject().apply {
        addProperty("type", "ERROR")
        addProperty("message", message)
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no confusable chars
        return (1..12).map { chars.random() }.joinToString("")
    }
}