package me.suxuan.bind.gui;

import me.suxuan.bind.SuBind;
import me.suxuan.bind.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 绑定操作的 GUI 界面
 */
public class BindMenu implements InventoryHolder {

	private final SuBind plugin;
	private final Inventory inventory;
	private final FileConfiguration guiConfig;
	private final MiniMessage mm = MiniMessage.miniMessage();

	public BindMenu(SuBind plugin) {
		this.plugin = plugin;
		this.guiConfig = plugin.getGuiConfig();

		String title = guiConfig.getString("bind-menu.title", "物品绑定");
		int rows = guiConfig.getInt("bind-menu.rows", 3) * 9;
		this.inventory = Bukkit.createInventory(this, rows, mm.deserialize(title));

		setupDecorations();
		updateConfirmButton(null, null);
		inventory.setItem(getInputSlot(), null);
	}

	/**
	 * 实时更新确认按钮的状态
	 */
	public void updateConfirmButton(ItemStack inputItem, HumanEntity viewer) {
		int confirmSlot = guiConfig.getInt("bind-menu.slots.confirm", 22);

		if (inputItem == null || inputItem.getType().isAir()) {
			inventory.setItem(confirmSlot, createGuiItem("bind-menu.buttons.no-item", null, false));
			return;
		}

		boolean isBound = plugin.getBindManager().isBound(inputItem);

		if (isBound) {
			UUID ownerUuid = plugin.getBindManager().getOwnerUUID(inputItem);
			if (viewer.getUniqueId().equals(ownerUuid)) {
				// 如果是本人，显示解绑按钮
				ConfigManager.BindCost unbindCost = plugin.getConfigManager().getUnbindCost(inputItem);
				inventory.setItem(confirmSlot, createGuiItem("bind-menu.buttons.confirm-unbind", unbindCost, true));
			} else {
				// 如果不是本人，显示禁止操作
				inventory.setItem(confirmSlot, createGuiItem("bind-menu.buttons.not-your-item", null, false));
			}
		} else {
			// 获取花费
			ConfigManager.BindCost cost = plugin.getConfigManager().getCost(inputItem);
			inventory.setItem(confirmSlot, createGuiItem("bind-menu.buttons.confirm", cost, true));
		}

	}

	private ItemStack createGuiItem(String path, ConfigManager.BindCost cost, boolean showCost) {
		ConfigurationSection section = guiConfig.getConfigurationSection(path);
		if (section == null) return new ItemStack(Material.STONE);

		Material mat = Material.matchMaterial(section.getString("material", "STONE"));
		ItemStack item = new ItemStack(mat != null ? mat : Material.STONE);
		ItemMeta meta = item.getItemMeta();

		// 设置名称
		String name = section.getString("name");
		if (name != null) meta.displayName(mm.deserialize(name).decoration(TextDecoration.ITALIC, false));

		// 设置 Lore
		List<String> loreStrings = section.getStringList("lore");
		if (!loreStrings.isEmpty()) {
			List<Component> lore = new ArrayList<>();
			if (showCost) {
				for (String line : loreStrings) {
					lore.add(mm.deserialize(line.replace("{cost}", String.valueOf(cost.money()))
									.replace("{item_name}", cost.requiredItem() != null ? cost.name() : plugin.getConfig().getString("placeholder.item_name"))
									.replace("{item_amount}", cost.requiredItem() != null ? String.valueOf(cost.requiredItem().getAmount())
											: plugin.getConfig().getString("placeholder.item_amount")))
							.decoration(TextDecoration.ITALIC, false));
				}
			} else {
				lore.addAll(loreStrings.stream()
						.map(s -> mm.deserialize(s).decoration(TextDecoration.ITALIC, false))
						.toList());
			}
			meta.lore(lore);
		}

		// 设置 CustomModelData
		int cmd = section.getInt("modeldata", 0);
		if (cmd > 0) meta.setCustomModelData(cmd);

		item.setItemMeta(meta);
		return item;
	}

	// 使用配置文件中的装饰
	private void setupDecorations() {
		// 填充背景
		ItemStack filler = createGuiItem("bind-menu.filler", null, false);
		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, filler);
		}

		// 加载静态装饰
		ConfigurationSection decorations = guiConfig.getConfigurationSection("bind-menu.decorations");
		if (decorations == null) return;
		for (String key : decorations.getKeys(false)) {
			int slot = decorations.getInt(key + ".slot");
			inventory.setItem(slot, createGuiItem("bind-menu.decorations." + key, null, false));
		}
	}

	public int getInputSlot() {
		return guiConfig.getInt("bind-menu.slots.input", 13);
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}
}