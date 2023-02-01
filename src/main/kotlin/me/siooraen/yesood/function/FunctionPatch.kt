package me.siooraen.yesood.function

import me.siooraen.yesood.Yesood
import me.siooraen.yesood.Yesood.bypass
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.*
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.random
import taboolib.platform.util.isNotAir

/**
 * @author sky
 * @since 2021/3/11 10:46 上午
 */
object FunctionPatch {

    /**
     * 禁止合成物品
     */
    @SubscribeEvent
    fun e(e: CraftItemEvent) {
        if (!Yesood.allowCraft) {
            e.isCancelled = true
        }
    }

    /**
     * 禁止合成
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun e(e: InventoryClickEvent) {
        if (!Yesood.allowCraft) {
            e.isCancelled = e.clickedInventory?.type == InventoryType.CRAFTING
        }
    }

    /**
     * 禁止合成
     */
    @SubscribeEvent(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun e(e: InventoryDragEvent) {
        if (!Yesood.allowCraft) {
            e.isCancelled = e.inventory.type == InventoryType.CRAFTING && e.rawSlots.any { it < 5 }
        }
    }

    /**
     * 禁止方块交互
     */
    @SubscribeEvent
    fun e(e: PlayerInteractEvent) {
        if ((e.action == Action.RIGHT_CLICK_BLOCK || e.action == Action.LEFT_CLICK_BLOCK) && !e.player.bypass()) {
            val type = e.clickedBlock!!.type.name
            if (Yesood.blockInteract.any { if (it.endsWith("?")) it.substring(0, it.length - 1) in type else it == type }) {
                e.isCancelled = true
            }
        }
    }

    /**
     * 禁止鱼竿移动公民
     * 禁止创造模式射出的弓箭在世界停留
     */
    @SubscribeEvent
    fun e(e: ProjectileHitEvent) {
        if (e.entity is FishHook && (e.hitEntity is ArmorStand || e.hitEntity?.hasMetadata("NPC") == true)) {
            e.entity.remove()
        }
        if (e.entity is Arrow && (e.entity as Arrow).pickupStatus != AbstractArrow.PickupStatus.ALLOWED) {
            e.entity.remove()
        }
    }

    /**
     * 重做荆棘伤害
     */
    @SubscribeEvent(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun e(e: EntityDamageByEntityEvent) {
        if (e.cause == EntityDamageEvent.DamageCause.THORNS && e.damager is LivingEntity && Yesood.thornOverride) {
            e.damage = 1.0
            getArmor(e.damager as LivingEntity)
                .filter { it.isNotAir() }
                .forEach { item ->
                    val level = item!!.getEnchantmentLevel(Enchantment.THORNS)
                    if (level <= 5) {
                        if (Math.random() <= level * 0.2) {
                            e.damage += random(1, 4)
                        }
                    } else {
                        e.damage += (level - 5)
                    }
                }
        }
    }

    private fun getArmor(entity: LivingEntity): Array<ItemStack?> {
        val items = arrayOfNulls<ItemStack>(6)
        items[0] = entity.equipment?.helmet
        items[1] = entity.equipment?.chestplate
        items[2] = entity.equipment?.leggings
        items[3] = entity.equipment?.boots
        return items
    }
}