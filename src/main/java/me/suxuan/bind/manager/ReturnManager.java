package me.suxuan.bind.manager;

import me.suxuan.bind.SuBind;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

/**
 * 负责处理非拥有者拾取物品后的逻辑及自动返还
 */
public class ReturnManager extends BukkitRunnable {

	private final SuBind plugin;
	private final NamespacedKey pickupTimeKey;
	private final NamespacedKey pickerUuidKey;

	public ReturnManager(SuBind plugin) {
		this.plugin = plugin;
		this.pickupTimeKey = new NamespacedKey(plugin, "pickup_timestamp");
		this.pickerUuidKey = new NamespacedKey(plugin, "picker_uuid");
		// 每分钟检查一次全服在线玩家背包
		this.runTaskTimerAsynchronously(plugin, 1200L, 1200L);
	}

	/**
	 * 当玩家拾取物品时调用
	 */
	public void markAsPickedUp(ItemStack item, Player picker) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		if (!meta.getPersistentDataContainer().has(pickupTimeKey))
			meta.getPersistentDataContainer().set(pickupTimeKey, PersistentDataType.LONG, System.currentTimeMillis());
		meta.getPersistentDataContainer().set(pickerUuidKey, PersistentDataType.STRING, picker.getUniqueId().toString());
		item.setItemMeta(meta);
		plugin.getLanguage().send(picker, "return.pickup-bind", "{item}", item,
				"{delay}", plugin.getConfig().getLong("return-settings.delay", 30),
				"{player}", Bukkit.getOfflinePlayer(plugin.getBindManager().getOwnerUUID(item)).getName()
		);
	}

	@Override
	public void run() {
		long delayMillis = plugin.getConfig().getLong("return-settings.delay", 30) * 60 * 1000;
		long now = System.currentTimeMillis();

		for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
			ItemStack[] contents = onlinePlayer.getInventory().getContents();
			for (int i = 0; i < contents.length; i++) {
				ItemStack item = contents[i];
				if (item == null || item.getType() == Material.AIR) continue;

				if (shouldReturn(item, now, delayMillis)) {
					processReturn(onlinePlayer, i, item);
				}
			}
		}
	}

	/**
	 * 检查物品是否应该自动返还
	 */
	private boolean shouldReturn(ItemStack item, long now, long delayMillis) {
		if (!plugin.getBindManager().isBound(item)) return false;

		ItemMeta meta = item.getItemMeta();
		if (!meta.getPersistentDataContainer().has(pickupTimeKey, PersistentDataType.LONG)) return false;

		long pickupTime = meta.getPersistentDataContainer().getOrDefault(pickupTimeKey, PersistentDataType.LONG, 0L);
		return (now - pickupTime) >= delayMillis;
	}

	/**
	 * 处理物品自动返还
	 */
	private void processReturn(Player holder, int slot, ItemStack item) {
		UUID ownerUuid = plugin.getBindManager().getOwnerUUID(item);
		if (ownerUuid == null) return;

		// 清除拾取标记
		ItemMeta meta = item.getItemMeta();
		meta.getPersistentDataContainer().remove(pickupTimeKey);
		meta.getPersistentDataContainer().remove(pickerUuidKey);
		item.setItemMeta(meta);

		// 从当前持有者背包移除
		holder.getInventory().setItem(slot, null);

		Player owner = Bukkit.getPlayer(ownerUuid);
		if (owner != null && owner.isOnline()) {
			// 给原主人
			owner.getInventory().addItem(item).values().forEach(remaining -> {
				owner.getWorld().dropItemNaturally(owner.getLocation(), remaining);
			});
			plugin.getLanguage().send(owner, "return.received-auto", "{item}", item, "{player}", owner.getName());
			plugin.getLanguage().send(holder, "return.removed-auto", "{item}", item, "{player}", owner.getName());
		} else {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerUuid);
			if (offlinePlayer.getName() == null) return;
			plugin.getMailProvider().sendMail(
					ownerUuid,
					plugin.getLanguage().getRawMessage("mail.title"),
					List.of(plugin.getLanguage().getRawMessage("mail.content")),
					item
			);
			plugin.getLanguage().send(holder, "return.removed-auto", "{item}", item, "{player}", offlinePlayer.getName());
		}
	}

	/**
	 * 清除拾取标记（当原主人捡回物品时调用）
	 */
	public void clearPickupTags(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		// 移除 PDC 中的拾取数据
		meta.getPersistentDataContainer().remove(pickupTimeKey);
		meta.getPersistentDataContainer().remove(pickerUuidKey);
		item.setItemMeta(meta);
	}

	public NamespacedKey getPickupTimeKey() {
		return pickupTimeKey;
	}

	public NamespacedKey getPickerUuidKey() {
		return pickerUuidKey;
	}
}