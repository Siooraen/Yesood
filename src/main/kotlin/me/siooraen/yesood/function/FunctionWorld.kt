package me.siooraen.yesood.function

import me.siooraen.yesood.Yesood
import me.siooraen.yesood.Yesood.bypass
import org.bukkit.Material
import org.bukkit.entity.Hanging
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.player.*
import org.bukkit.event.raid.RaidTriggerEvent
import org.bukkit.util.Vector
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import taboolib.common.util.Location
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.util.setLocation
import taboolib.platform.util.attacker
import taboolib.platform.util.toBukkitLocation
import taboolib.platform.util.toProxyLocation

/**
 * @author sky
 * @since 2019-11-20 21:21
 */
@Suppress("SpellCheckingInspection")
object FunctionWorld {

    init {
        command("setserverspawn", permission = "admin") {
            execute<Player> { sender, _, _ ->
                val loc = sender.location.clone()
                Yesood.data.setLocation("spawn", loc.toProxyLocation())
                sender.sendMessage("服务器出生点已被重设在${loc.x},${loc.y},${loc.z}(${loc.yaw},${loc.pitch})")
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        if (Yesood.data.contains("spawn")) {
            submit(delay = 20) {
                e.player.teleport(Yesood.data.getLocation("spawn")!!.toBukkitLocation())
            }
        }
    }

    @SubscribeEvent
    fun e(e: PlayerRespawnEvent) {
        if (Yesood.data.contains("spawn")) {
            e.respawnLocation = Yesood.data.getLocation("spawn")!!.toBukkitLocation()
        }
    }

    @SubscribeEvent
    fun e(e: EntityBreedEvent) {
        if ("BREED" in Yesood.blockFeatures) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: LeavesDecayEvent) {
        if ("LEAVES_DECAY" in Yesood.blockFeatures) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityChangeBlockEvent) {
        if ("ENTITY_CHANGE_BLOCK" in Yesood.blockFeatures && e.entity is LivingEntity) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if ("FARMLAND_PHYSICAL" in Yesood.blockFeatures && e.action == Action.PHYSICAL && e.clickedBlock!!.type == Material.FARMLAND) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityInteractEvent) {
        if ("FARMLAND_PHYSICAL" in Yesood.blockFeatures && e.block.type == Material.FARMLAND) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: HangingBreakEvent) {
        if ("HANGING_BREAK" in Yesood.blockFeatures && e.cause != HangingBreakEvent.RemoveCause.ENTITY) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: HangingBreakByEntityEvent) {
        if ("HANGING_BREAK" in Yesood.blockFeatures && e.remover?.bypass(true) == false) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerInteractEntityEvent) {
        if ("HANGING_BREAK" in Yesood.blockFeatures && !e.player.bypass(true) && e.rightClicked is Hanging) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: EntityDamageByEntityEvent) {
        val player = e.attacker ?: return
        if ("HANGING_BREAK" in Yesood.blockFeatures && !player.bypass(true) && e.entity is Hanging) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerTeleportEvent) {
        e.isCancelled = e.cause.name in Yesood.blockTeleport
    }

    @SubscribeEvent
    fun e(e: EntityExplodeEvent) {
        if ("ENTITY_EXPLODE" in Yesood.blockFeatures) {
            e.blockList().clear()
        }
    }

    @SubscribeEvent
    fun e(e: BlockExplodeEvent) {
        if ("BLOCK_EXPLODE" in Yesood.blockFeatures) {
            e.blockList().clear()
        }
    }

    @SubscribeEvent
    fun e(e: RaidTriggerEvent) {
        if ("RAID" in Yesood.blockFeatures) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: BlockSpreadEvent) {
        if ("SPREAD" in Yesood.blockFeatures) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: BlockGrowEvent) {
        if ("GROW" in Yesood.blockFeatures) {
            e.isCancelled = true
        }
    }

    @SubscribeEvent
    fun e(e: PlayerMoveEvent) {
        val to = e.to!!
        if (e.from.x != to.x || e.from.y != to.y || e.from.z != to.z) {
            if (to.y < 10 && Yesood.voidProtect) {
                e.isCancelled = true
                // 返回大厅
                submit {
                    e.player.velocity = Vector(0, 0, 0)
                    e.player.teleport(e.player.world.spawnLocation)
                }
            }
        }
    }

    private fun ConfigurationSection.getLocation(path: String): Location? {
        getConfigurationSection(path)?.let { section ->
            return Location(
                section.getString("world"),
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                section.getDouble("yaw").toFloat(),
                section.getDouble("pitch").toFloat()
            )
        } ?: return null
    }
}