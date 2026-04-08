package me.suxuan.bind;

import me.suxuan.bind.config.ConfigManager;
import me.suxuan.bind.config.Language;
import me.suxuan.bind.listeners.InventoryListener;
import me.suxuan.bind.listeners.ItemSecurityListener;
import me.suxuan.bind.listeners.MailNotifyListener;
import me.suxuan.bind.manager.BindManager;
import me.suxuan.bind.manager.ReturnManager;
import me.suxuan.bind.manager.mail.InternalMailImpl;
import me.suxuan.bind.manager.mail.MailProvider;
import me.suxuan.bind.manager.mail.SweetMailImpl;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * SuBind - 插件主类
 */
public final class SuBind extends JavaPlugin {

	private static SuBind instance;

	private BindManager bindManager;
	private Language language;

	private ConfigManager configManager;
	private ReturnManager returnManager;

	private FileConfiguration guiConfig;
	private File guiFile;

	private Economy economy;

	private MailProvider mailProvider;

	/**
	 * 插件启动时调用，初始化所有核心组件
	 */
	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		bindManager = new BindManager(this);
		language = new Language(this);
		configManager = new ConfigManager(this);
		returnManager = new ReturnManager(this);

		createGuiConfig();

		Bukkit.getPluginManager().registerEvents(new ItemSecurityListener(this, bindManager), this);
		Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
		Bukkit.getPluginManager().registerEvents(new MailNotifyListener(this), this);

		getCommand("subind").setExecutor(new BindCommand(this));

		if (!setupEconomy()) {
			getLogger().severe("SuBind插件无法启用，因为没有找到Vault依赖!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		setupMail();
	}

	/**
	 * 创建或加载 GUI 配置文件
	 * 如果文件不存在则从插件资源中复制默认配置
	 */
	public void createGuiConfig() {
		guiFile = new File(getDataFolder(), "gui.yml");
		if (!guiFile.exists()) {
			saveResource("gui.yml", false);
		}
		guiConfig = YamlConfiguration.loadConfiguration(guiFile);
	}

	/**
	 * 初始化 Vault 经济系统服务
	 *
	 * @return 初始化成功返回 true，否则返回 false
	 */
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		economy = rsp.getProvider();
		return true;
	}

	/**
	 * 设置邮件提供程序
	 */
	private void setupMail() {
		if (getServer().getPluginManager().isPluginEnabled("SweetMail")) {
			this.mailProvider = new SweetMailImpl(this);
			getLogger().info("检测到 SweetMail，已启用外部邮件支持。");
		} else {
			this.mailProvider = new InternalMailImpl(this);
			getLogger().info("未检测到 SweetMail，使用插件自带邮件系统。");
		}
	}

	/**
	 * 重载插件所有配置和数据
	 * 包括主配置、GUI 配置、语言文件和配置管理器
	 */
	public void reloadPlugin() {
		reloadConfig();
		guiConfig = YamlConfiguration.loadConfiguration(guiFile);
		language.reload();
		configManager.load();

		getLogger().info("SuBind插件重载成功！");
	}

	/**
	 * 获取语言管理器
	 *
	 * @return 语言管理器实例
	 */
	public Language getLanguage() {
		return language;
	}

	/**
	 * 获取绑定管理器
	 *
	 * @return 绑定管理器实例
	 */
	public BindManager getBindManager() {
		return bindManager;
	}

	/**
	 * 获取配置管理器
	 *
	 * @return 配置管理器实例
	 */
	public ConfigManager getConfigManager() {
		return configManager;
	}

	/**
	 * 获取 GUI 配置
	 *
	 * @return GUI 配置对象
	 */
	public FileConfiguration getGuiConfig() {
		return guiConfig;
	}

	/**
	 * 获取经济系统接口
	 *
	 * @return Vault 经济系统实例
	 */
	public Economy getEconomy() {
		return economy;
	}

	/**
	 * 获取邮件提供程序
	 *
	 * @return 邮件提供程序实例
	 */
	public MailProvider getMailProvider() {
		return mailProvider;
	}

	/**
	 * 获取返还管理器
	 *
	 * @return 返还管理器实例
	 */
	public ReturnManager getReturnManager() {
		return returnManager;
	}

	/**
	 * 获取插件实例
	 *
	 * @return 插件实例
	 */
	public static SuBind getInstance() {
		return instance;
	}
}