package me.suxuan.bind.listeners;

import me.suxuan.bind.SuBind;
import me.suxuan.bind.manager.BindManager;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 处理物品丢弃、防损毁及生物捡拾逻辑
 */
public class ItemSecurityListener implements Listener {

	private final SuBind plugin;
	private final BindManager bindManager;

	// 用于记录玩家最后一次尝试丢弃物品的时间戳
	private final Map<UUID, Long> dropConfirmMap = new HashMap<>();

	public ItemSecurityListener(SuBind plugin, BindManager bindManager) {
		this.plugin = plugin;
		this.bindManager = bindManager;
	}

	/**
	 * 3秒内连按两次Q才能丢弃
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDrop(PlayerDropItemEvent event) {
		ItemStack item = event.getItemDrop().getItemStack();

		if (!bindManager.isBound(item)) return;

		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		long currentTime = System.currentTimeMillis();

		// 第二次按Q，检查是否在配置的时间间隔内按了两次
		if (dropConfirmMap.containsKey(uuid)) {
			long lastClick = dropConfirmMap.get(uuid);
			if (currentTime - lastClick <= plugin.getConfig().getInt("drop-interval", 3) * 1000L) {
				dropConfirmMap.remove(uuid);
				plugin.getLanguage().send(player, "security.drop-confirmed");
				return;
			}
		}

		// 第一次按Q，拦截事件并记录时间
		event.setCancelled(true);
		dropConfirmMap.put(uuid, currentTime);
		plugin.getLanguage().send(player, "security.drop-first-attempt",
				"{time}", plugin.getConfig().getInt("drop-interval", 3));
	}

	/**
	 * 岩浆、雷击、仙人掌、TNT 炸不掉
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onItemDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Item itemEntity)) return;

		ItemStack item = itemEntity.getItemStack();
		if (bindManager.isBound(item)) {
			event.setCancelled(true);
		}
	}

	/**
	 * 防止自然消失
	 */
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if (bindManager.isBound(event.getEntity().getItemStack())) {
			event.setCancelled(true);
		}
	}

	/**
	 * 禁止僵尸、狐狸等非玩家实体拾取绑定物品
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityPickup(EntityPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();

		if (bindManager.isBound(item)) {
			if (!(event.getEntity() instanceof Player)) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 * 处理物品拾取逻辑
	 * 如果拾取者不是物品拥有者，则开始计时归还
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerPickup(EntityPickupItemEvent event) {

		if (!(event.getEntity() instanceof Player player)) return;

		ItemStack item = event.getItem().getItemStack();

		// 如果物品没绑定，不需要处理归还逻辑
		if (!bindManager.isBound(item)) return;

		UUID ownerUuid = bindManager.getOwnerUUID(item);
		UUID pickerUuid = player.getUniqueId();

		// 如果拾取者就是原主人，清除掉之前的拾取标记
		if (pickerUuid.equals(ownerUuid)) {
			plugin.getReturnManager().clearPickupTags(item);
			return;
		}

		// 如果拾取者不是原主人，标记拾取信息
		plugin.getReturnManager().markAsPickedUp(item, player);
	}

	/**
	 * 在指定世界自动绑定拾取的物品
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onAutoBindPickup(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;

		// 检查当前世界是否在白名单中
		String worldName = player.getWorld().getName();
		List<String> enabledWorlds = plugin.getConfig().getStringList("auto-bind-settings.enabled-worlds");
		if (!enabledWorlds.contains(worldName)) return;

		ItemStack item = event.getItem().getItemStack();

		// 检查物品是否合法
		if (item.getType().isAir()) return;

		// 检查是否已经绑定过
		if (plugin.getBindManager().isBound(item)) {
			if (plugin.getConfig().getBoolean("auto-bind-settings.ignore-already-bound", true)) {
				return;
			}
		}

		// 执行强制绑定
		plugin.getBindManager().bindItem(item, player);

		// 提示信息（如果开启）
		if (plugin.getConfig().getBoolean("auto-bind-settings.send-message", true)) {
			plugin.getLanguage().send(player, "bind.auto-bound-world", "{item}", item);
		}
	}
}