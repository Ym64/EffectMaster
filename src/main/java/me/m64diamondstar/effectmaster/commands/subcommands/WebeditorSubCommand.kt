package me.m64diamondstar.effectmaster.commands.subcommands

import me.m64diamondstar.effectmaster.commands.utils.SubCommand
import me.m64diamondstar.effectmaster.ktx.emComponent
import me.m64diamondstar.effectmaster.web.EditorSessionManager
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WebeditorSubCommand: SubCommand {
    override fun getName(): String {
        return "webeditor"
    }

    override fun execute(sender: CommandSender, args: Array<String>) {
        if (args.size == 1) {
            if (sender !is Player) {
                sender.sendMessage(emComponent("<prefix><error>You can only use this command as a player."))
                return
            }
            // All feedback happens inside the EditorSessionManager
            EditorSessionManager.startSession(sender)
        }
    }

    override fun getTabCompleters(
        sender: CommandSender,
        args: Array<String>
    ): ArrayList<String> {
        return ArrayList()
    }
}