package me.suxuan.bind.config;

import me.suxuan.bind.SuBind;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * 负责解析配置并提供物品绑定消耗的计算逻辑
 */
public class ConfigManager {

	private final SuBind plugin;
	private final Map<String, BindCost> customCosts = new HashMap<>();
	private final Map<String, BindCost> unbindCustomCosts = new HashMap<>();
	private BindCost defaultCost;
	private BindCost unbindDefaultCost;

	public ConfigManager(SuBind plugin) {
		this.plugin = plugin;
		load();
	}

	public void load() {
		// 初始化绑定消耗配置
		customCosts.clear();
		ConfigurationSection section = plugin.getConfig().getConfigurationSection("bind-settings.custom-costs");

		if (section != null) {
			for (String key : section.getKeys(false)) {
				customCosts.put(key.toUpperCase(), parseCost(section.getConfigurationSection(key)));
			}
		}
		defaultCost = parseCost(plugin.getConfig().getConfigurationSection("bind-settings.default-cost"));

		// 初始化解绑消耗配置
		unbindCustomCosts.clear();
		ConfigurationSection unbindSection = plugin.getConfig().getConfigurationSection("unbind-settings.custom-costs");

		if (unbindSection != null) {
			for (String key : unbindSection.getKeys(false)) {
				unbindCustomCosts.put(key.toUpperCase(), parseCost(unbindSection.getConfigurationSection(key)));
			}
		}
		unbindDefaultCost = parseCost(plugin.getConfig().getConfigurationSection("unbind-settings.default-cost"));
	}

	/**
	 * 解析绑定消耗配置
	 */
	private BindCost parseCost(ConfigurationSection section) {
		if (section == null) return new BindCost(0, null, "");

		double money = section.getDouble("money", 0);
		ItemStack itemStack = null;

		if (section.contains("item")) {
			Material mat = Material.matchMaterial(section.getString("item.material", "AIR"));
			if (mat != null && mat != Material.AIR) {
				itemStack = new ItemStack(mat, section.getInt("item.amount", 1));
				int cmd = section.getInt("item.custom-model-data", 0);
				if (cmd > 0) {
					ItemMeta meta = itemStack.getItemMeta();
					meta.setCustomModelData(cmd);
					itemStack.setItemMeta(meta);
				}
			}
		}
		return new BindCost(money, itemStack, section.getString("item.name", ""));
	}

	/**
	 * 根据物品获取对应的绑定消耗
	 */
	public BindCost getCost(ItemStack item) {
		if (item == null || item.getType().isAir()) return defaultCost;

		String materialName = item.getType().name();
		int cmd = item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
				? item.getItemMeta().getCustomModelData() : 0;

		// 尝试匹配 MATERIAL:CMD
		BindCost cost = customCosts.get(materialName + ":" + cmd);
		if (cost != null) return cost;

		// 尝试匹配 MATERIAL:0
		cost = customCosts.get(materialName + ":0");
		if (cost != null) return cost;

		// 返回默认
		return defaultCost;
	}

	/**
	 * 获取解绑物品的消耗
	 */
	public BindCost getUnbindCost(ItemStack item) {
		if (item == null || item.getType().isAir()) return unbindDefaultCost;

		String materialName = item.getType().name();
		int cmd = item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
				? item.getItemMeta().getCustomModelData() : 0;

		// 优先匹配精确 CMD
		BindCost cost = unbindCustomCosts.get(materialName + ":" + cmd);
		if (cost != null) return cost;

		// 匹配材质
		cost = unbindCustomCosts.get(materialName + ":0");
		if (cost != null) return cost;

		return unbindDefaultCost;
	}

	/**
	 * 存储绑定消耗数据的记录类
	 */
	public record BindCost(double money, ItemStack requiredItem, String name) {
	}
}