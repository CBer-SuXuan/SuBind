package me.suxuan.bind.listeners;

import me.suxuan.bind.SuBind;
import me.suxuan.bind.config.ConfigManager;
import me.suxuan.bind.gui.BindMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * 监听绑定菜单的点击事件
 */
public class InventoryListener implements Listener {

	private final SuBind plugin;

	public InventoryListener(SuBind plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// 如果点击的不是绑定菜单，直接返回
		if (!(event.getInventory().getHolder() instanceof BindMenu menu)) return;

		Player player = (Player) event.getWhoClicked();
		int slot = event.getRawSlot();
		int inputSlot = menu.getInputSlot();
		int confirmSlot = plugin.getConfig().getInt("gui.slots.confirm", 22);

		// 如果点击的是背景装饰格
		if (slot < event.getInventory().getSize() && slot != inputSlot && slot != confirmSlot) {
			event.setCancelled(true);
			return;
		}

		// 点击确认按钮
		if (slot == confirmSlot) {
			event.setCancelled(true);
			handleBindAction(player, menu);
			return;
		}

		// 当玩家放入或拿出物品时，延迟一刻更新确认按钮
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			menu.updateConfirmButton(event.getInventory().getItem(inputSlot), player);
		}, 1L);
	}

	private void handleBindAction(Player player, BindMenu menu) {
		ItemStack item = menu.getInventory().getItem(menu.getInputSlot());
		if (item == null || item.getType().isAir()) return;

		if (plugin.getBindManager().isBound(item)) {
			// 解绑分支
			UUID ownerUuid = plugin.getBindManager().getOwnerUUID(item);
			if (!player.getUniqueId().equals(ownerUuid)) {
				plugin.getLanguage().send(player, "bind.not-your-item");
				return;
			}

			// 获取解绑费用
			ConfigManager.BindCost unbindCost = plugin.getConfigManager().getUnbindCost(item);

			// 检查并扣除
			if (!checkAndConsumCost(player, unbindCost)) return;

			// 执行解绑
			plugin.getBindManager().unbindItem(item);
			plugin.getLanguage().send(player, "bind.unbind-success");
			menu.updateConfirmButton(item, player);
		} else {
			// 绑定分支
			ConfigManager.BindCost bindCost = plugin.getConfigManager().getCost(item);
			if (!checkAndConsumCost(player, bindCost)) return;

			// 执行绑定
			plugin.getBindManager().bindItem(item, player);
			plugin.getLanguage().send(player, "bind.success");
			menu.updateConfirmButton(item, player);
		}
		player.closeInventory();
	}

	/**
	 * 检查并扣除玩家的绑定费用
	 *
	 * @param player 玩家
	 * @param cost   绑定费用
	 * @return 是否成功扣除
	 */
	private boolean checkAndConsumCost(Player player, ConfigManager.BindCost cost) {
		// 检查金币
		if (cost.money() > 0 && !plugin.getEconomy().has(player, cost.money())) {
			plugin.getLanguage().send(player, "bind.insufficient-funds", "{price}", cost.money());
			return false;
		}
		// 检查道具
		ItemStack requirement = cost.requiredItem();
		if (requirement != null) {
			if (!hasEnoughItems(player, requirement)) {
				plugin.getLanguage().send(player, "bind.insufficient-items");
				return false;
			}
		}
		// 进行扣除
		if (cost.money() > 0) plugin.getEconomy().withdrawPlayer(player, cost.money());
		if (requirement != null) removePreciseItems(player, requirement);
		return true;
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		// 如果关闭的不是绑定菜单，直接返回
		if (event.getInventory().getHolder() instanceof BindMenu menu) {
			// 退还槽位里的物品给玩家
			ItemStack item = event.getInventory().getItem(menu.getInputSlot());
			if (item != null && item.getType() != Material.AIR) {
				event.getPlayer().getInventory().addItem(item).values().forEach(remaining -> {
					event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), remaining);
				});
			}
		}
	}

	/**
	 * 精确检查玩家背包是否拥有足够数量且 CMD 匹配的物品
	 */
	private boolean hasEnoughItems(Player player, ItemStack requirement) {
		int count = 0;
		int targetCmd = requirement.hasItemMeta() && requirement.getItemMeta().hasCustomModelData()
				? requirement.getItemMeta().getCustomModelData() : -1;

		for (ItemStack item : player.getInventory().getContents()) {

			if (item == null || item.getType() != requirement.getType()) continue;

			// 检查 CMD 是否匹配
			int currentCmd = item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
					? item.getItemMeta().getCustomModelData() : -1;

			if (currentCmd == targetCmd) {
				count += item.getAmount();
			}
		}
		return count >= requirement.getAmount();
	}

	/**
	 * 从玩家背包中精确扣除指定 CMD 和数量的物品
	 */
	private void removePreciseItems(Player player, ItemStack requirement) {
		int toRemove = requirement.getAmount();
		int targetCmd = requirement.hasItemMeta() && requirement.getItemMeta().hasCustomModelData()
				? requirement.getItemMeta().getCustomModelData() : -1;

		ItemStack[] contents = player.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			if (toRemove <= 0) break;

			ItemStack item = contents[i];
			if (item == null || item.getType() != requirement.getType()) continue;

			int currentCmd = item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
					? item.getItemMeta().getCustomModelData() : -1;

			if (currentCmd == targetCmd) {
				int amount = item.getAmount();
				if (amount <= toRemove) {
					toRemove -= amount;
					contents[i] = null; // 全部扣除
				} else {
					item.setAmount(amount - toRemove);
					toRemove = 0; // 扣完了
				}
			}
		}
		player.getInventory().setContents(contents);
	}
}