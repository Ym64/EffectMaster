package me.m64diamondstar.effectmaster.commands.utils

import me.m64diamondstar.effectmaster.commands.subcommands.*

object SubCommandRegistry {

    /**
     * Register all SubCommands on start.
     */
    fun loadSubCommands(){
        SubCommandManager.registerSubCommand(CreateSubCommand(), DeleteSubCommand(), PlaySubCommand(), PlayAtSubCommand(),
            PlayCategorySubCommand(), LocationSubCommand(), WikiSubCommand(), EditSubCommand(), EditorSubCommand(), RenameSubCommand(),
            PrivatePlaySubCommand(), CancelSubCommand(), EnterSubCommand(), ReloadSubCommand(), StopSubCommand(), WandSubCommand(),
            VersionSubCommand(), ExportSubCommand(), ImportSubCommand(), LoopSubCommand(), DebugSubCommand(), LockSubCommand(),
            UnlockSubCommand(), WebeditorSubCommand()
        )
    }

}