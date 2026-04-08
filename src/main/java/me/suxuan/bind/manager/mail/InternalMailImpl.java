package me.suxuan.bind.manager.mail;

import me.suxuan.bind.SuBind;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 内置邮件系统实现类
 * 负责将无法归还的物品持久化存储到 mails.yml
 */
public class InternalMailImpl implements MailProvider {

	private final SuBind plugin;
	private final File mailFile;
	private final FileConfiguration mailConfig;

	public InternalMailImpl(SuBind plugin) {
		this.plugin = plugin;
		this.mailFile = new File(plugin.getDataFolder(), "mails.yml");
		this.mailConfig = YamlConfiguration.loadConfiguration(mailFile);
	}

	@Override
	public void sendMail(UUID uuid, String title, List<String> content, ItemStack item) {
		// 在 YAML 中以 UUID 为键存储
		String path = uuid.toString();

		// 获取该玩家现有的邮件列表（附件列表）
		List<ItemStack> items = (List<ItemStack>) mailConfig.getList(path + ".items", new ArrayList<>());
		items.add(item);

		// 更新配置
		mailConfig.set(path + ".items", items);
		// 这里可以扩展存储标题和内容，但内置系统核心是退还物品
		// mailConfig.set(path + ".last_reason", title);

		saveConfig();
	}

	/**
	 * 获取玩家待领取的邮件物品列表
	 */
	public List<ItemStack> getPendingItems(UUID uuid) {
		return (List<ItemStack>) mailConfig.getList(uuid.toString() + ".items", new ArrayList<>());
	}

	/**
	 * 玩家领取后清除数据
	 */
	public void clearMail(UUID uuid) {
		mailConfig.set(uuid.toString(), null);
		saveConfig();
	}

	private void saveConfig() {
		try {
			mailConfig.save(mailFile);
		} catch (IOException e) {
			plugin.getLogger().severe("无法保存 mails.yml 邮件数据！");
			e.printStackTrace();
		}
	}
}