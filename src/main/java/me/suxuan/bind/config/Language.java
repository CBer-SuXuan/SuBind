package me.suxuan.bind.config;

import me.suxuan.bind.SuBind;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理语言相关内容
 */
public class Language {

	private final SuBind plugin;
	private FileConfiguration langConfig;
	private final Map<String, Component> messageCache = new HashMap<>();
	private final Map<String, String> rawMessageCache = new HashMap<>();
	private final MiniMessage mm = MiniMessage.miniMessage();

	public Language(SuBind plugin) {
		this.plugin = plugin;
		reload();
	}

	/**
	 * 加载或重载语言文件
	 */
	public void reload() {
		File langFile = new File(plugin.getDataFolder(), "lang.yml");
		if (!langFile.exists()) {
			plugin.saveResource("lang.yml", false);
		}

		langConfig = YamlConfiguration.loadConfiguration(langFile);
		messageCache.clear();

		for (String key : langConfig.getKeys(true)) {
			if (langConfig.isString(key)) {
				String message = langConfig.getString(key);
				if (message == null) continue;
				rawMessageCache.put(key, message);
				messageCache.put(key, mm.deserialize(message));
			}
		}
	}

	/**
	 * 获取原始消息
	 */
	public String getRawMessage(String path) {
		return rawMessageCache.getOrDefault(path, "<red>找不到键<yellow>" + path + "<red>对应的语言配置");
	}

	/**
	 * 获取指定消息
	 */
	public Component getMessage(String path) {
		return messageCache.getOrDefault(path, mm.deserialize("<red>找不到键<yellow>" + path + "<red>对应的语言配置"));
	}

	/**
	 * 向发送者发送消息
	 */
	public void send(CommandSender sender, String path, Object... placeholders) {
		Component message = getMessage(path);

		// 替换占位符
		for (int i = 0; i < placeholders.length; i += 2) {
			if (i + 1 < placeholders.length) {
				String placeholder = String.valueOf(placeholders[i]);
				Object value = placeholders[i + 1];

				Component replacement;

				if (placeholder.equalsIgnoreCase("{item}") && value instanceof ItemStack item) {
					// 获取展示名（如果没改名则用译名）
					Component itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
							? item.getItemMeta().displayName()
							: Component.translatable(item.getType().translationKey());

					if (itemName == null) continue;
					replacement = itemName.hoverEvent(item.asHoverEvent());
				} else {
					replacement = Component.text(String.valueOf(value));
				}

				message = message.replaceText(TextReplacementConfig.builder()
						.matchLiteral(placeholder)
						.replacement(replacement)
						.build());
			}
		}

		if (message != Component.empty()) {
			sender.sendMessage(message);
		}
	}

	@Override
	public String toString() {
		return "Language{" +
				"messageCache=" + messageCache +
				'}';
	}
}