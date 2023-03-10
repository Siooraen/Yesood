package me.siooraen.yesood.function

import com.mojang.brigadier.suggestion.Suggestions
import me.siooraen.yesood.Yesood
import org.bukkit.Bukkit
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerKickEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.CraftingInventory
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.library.reflex.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XSound
import taboolib.module.nms.PacketReceiveEvent
import taboolib.module.nms.PacketSendEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * @author sky
 * @since 2019-11-20 21:49
 */
object FunctionPacket {

    private val bite = ConcurrentHashMap<String, Int>()
    private val entityPackets = arrayOf("PacketPlayOutEntityVelocity", "PacketPlayOutEntityMetadata", "PacketPlayOutEntityStatus", "PacketPlayOutEntityEffect")

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        bite.remove(e.player.name)
    }

    @SubscribeEvent
    fun e(e: PlayerKickEvent) {
        bite.remove(e.player.name)
    }

    @SubscribeEvent
    fun e(e: PacketSendEvent) {
        if (e.packet.name == "PacketPlayOutChat" && e.packet.read<String>("a").toString().contains("chat.type.advancement")) {
            e.isCancelled = true
        }
        if (e.packet.name == "PacketPlayOutTabComplete" && !e.player.isOp) {
            if (e.packet.read<Suggestions>("b")!!.list.any { Bukkit.getPlayerExact(it.text) == null }) {
                return
            }
            e.isCancelled = true
        }
        if (e.packet.name == "PacketPlayOutWorldParticles" && e.packet.read<Any>("j")!!.invokeMethod<String>("a")!! == "minecraft:damage_indicator") {
            e.isCancelled = true
        }
        if (e.packet.name in entityPackets && e.packet.read<Int>("a") == bite[e.player.name]) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PacketReceiveEvent) {
        if (e.packet.name == "PacketPlayInAutoRecipe" || e.packet.name == "PacketPlayInRecipeDisplayed") {
            if (!Yesood.allowCraftDisplay) {
                e.isCancelled = true
            }
        }
        if (e.packet.name == "PacketPlayInUseItem" || e.packet.name == "PacketPlayInUseEntity" || e.packet.name == "PacketPlayInArmAnimation") {
            if (e.player.openInventory.topInventory !is CraftingInventory) {
                e.isCancelled = true
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerFishEvent) {
        if (e.state == PlayerFishEvent.State.REEL_IN) {
            // ????????????
            val hook = e.invokeMethod<Entity>("getHook")!!
            submit(delay = 20) {
                XSound.ENTITY_FISHING_BOBBER_SPLASH.play(hook.location, 0f, 0f)
            }
            submit(delay = 40) {
                XSound.ENTITY_FISHING_BOBBER_SPLASH.play(hook.location, 0f, 0f)
            }
        }
        if (e.state == PlayerFishEvent.State.BITE) {
            bite[e.player.name] = e.invokeMethod<Entity>("getHook")!!.entityId
            submit(delay = 40) {
                bite.remove(e.player.name)
            }
        }
    }
}