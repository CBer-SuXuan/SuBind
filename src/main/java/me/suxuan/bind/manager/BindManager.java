package me.suxuan.bind.manager;

import me.suxuan.bind.SuBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * BindManager: 处理物品 NBT 读写与绑定状态
 */
public class BindManager {

	private final SuBind plugin;
	private final NamespacedKey ownerKey;

	public BindManager(SuBind plugin) {
		this.plugin = plugin;
		this.ownerKey = new NamespacedKey(plugin, "item_owner_uuid");
	}

	/**
	 * 将物品绑定给指定玩家
	 *
	 * @param item   目标物品
	 * @param player 拥有者
	 */
	public void bindItem(ItemStack item, Player player) {
		if (item == null || item.getType().isAir()) return;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return;

		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());

		List<Component> lore = meta.lore();
		if (lore == null) lore = new ArrayList<>();
		lore.add(MiniMessage.miniMessage()
				.deserialize(plugin.getConfig().getString("bind-lore", "绑定物品").replace("{player}", player.getName()))
				.decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);
		item.setItemMeta(meta);
	}

	/**
	 * 检查物品是否已被绑定
	 */
	public boolean isBound(ItemStack item) {
		if (item == null || !item.hasItemMeta()) return false;
		return item.getItemMeta().getPersistentDataContainer().has(ownerKey, PersistentDataType.STRING);
	}

	/**
	 * 获取物品的所有者 UUID
	 */
	public UUID getOwnerUUID(ItemStack item) {
		if (!isBound(item)) return null;
		String uuidStr = item.getItemMeta().getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
		return uuidStr != null ? UUID.fromString(uuidStr) : null;
	}

	/**
	 * 解除绑定
	 */
	public void unbindItem(ItemStack item) {
		if (!isBound(item)) return;
		ItemMeta meta = item.getItemMeta();
		PersistentDataContainer pdc = meta.getPersistentDataContainer();
		pdc.remove(ownerKey);

		List<Component> lore = meta.lore();
		if (lore != null)
			lore.removeLast();
		meta.lore(lore);

		item.setItemMeta(meta);
	}
}