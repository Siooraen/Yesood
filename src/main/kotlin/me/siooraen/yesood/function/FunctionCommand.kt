package me.siooraen.yesood.function

import me.siooraen.yesood.Yesood
import org.bukkit.command.PluginCommand
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerCommandSendEvent
import org.spigotmc.SpigotConfig
import taboolib.common.LifeCycle
import taboolib.common.io.taboolibId
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.event.SubscribeEvent
import taboolib.platform.BukkitCommand

/**
 * @author sky
 * @since 2019-11-20 21:48
 */
object FunctionCommand {

    @Awake(LifeCycle.ACTIVE)
    fun e() {
        if (Yesood.conf.getBoolean("command-block")) {
            PlatformFactory.getAPI<BukkitCommand>().commandMap.commands.forEach { command ->
                if (Yesood.conf.getStringList("block-command-path").any { name -> command.javaClass.name.startsWith(name) }) {
                    if (command !is PluginCommand || !command.javaClass.name.startsWith("io.izzel.$taboolibId")) {
                        command.permission = "*"
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerCommandSendEvent) {
        if (!e.player.isOp && Yesood.conf.getBoolean("command-block")) {
            e.commands.removeAll(Yesood.conf.getStringList("block-command-name").toSet())
            e.commands.removeAll(Yesood.conf.getStringList("block-command-send").toSet())
            e.commands.removeIf { it.contains(":") }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerCommandPreprocessEvent) {
        if (e.player.isOp || !Yesood.conf.getBoolean("command-block")) {
            return
        }
        val v = e.message.split(" ")[0].lowercase().substring(1)
        if (v.contains(":") || v in Yesood.conf.getStringList("block-command-name")) {
            e.isCancelled = true
            e.player.sendMessage(SpigotConfig.unknownCommandMessage)
        }
    }
}