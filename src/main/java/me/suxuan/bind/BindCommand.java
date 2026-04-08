package me.suxuan.bind;

import me.suxuan.bind.gui.BindMenu;
import me.suxuan.bind.manager.mail.InternalMailImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件主命令处理器
 */
public class BindCommand implements CommandExecutor, TabExecutor {

	private final SuBind plugin;

	public BindCommand(SuBind plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

		if (args.length == 0 || args[0].equalsIgnoreCase("bind")) {  // 检查权限并为玩家打开绑定 GUI 界面
			if (!(sender instanceof Player player)) {
				plugin.getLanguage().send(sender, "system.only-player");
				return true;
			}
			if (!player.hasPermission("rebind.use")) {
				plugin.getLanguage().send(player, "system.no-permission");
				return true;
			}
			player.openInventory(new BindMenu(plugin).getInventory());
			return true;
		}

		if (args[0].equalsIgnoreCase("reload")) {  // 重载插件配置、语言文件及内部缓存
			if (!sender.hasPermission("rebind.admin")) {
				plugin.getLanguage().send(sender, "system.no-permission");
				return true;
			}
			plugin.reloadPlugin();
			plugin.getLanguage().send(sender, "system.reload-success");
			return true;
		}

		if (args[0].equalsIgnoreCase("name")) {  // 调试工具：显示玩家手上物品的 MiniMessage 格式名称或译名
			if (!(sender instanceof Player player)) {
				plugin.getLanguage().send(sender, "system.only-player");
				return true;
			}
			if (!player.hasPermission("rebind.name")) {
				plugin.getLanguage().send(player, "system.no-permission");
				return true;
			}
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.getType().isAir()) {
				player.sendMessage(MiniMessage.miniMessage().deserialize("<red>手持一个物品查看显示名称！"));
				return true;
			}
			// 检查物品是否有显示名称
			if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName())
				player.sendMessage(MiniMessage.miniMessage().deserialize("<green>物品显示名称为：</green><white>").append(Component.translatable(item.translationKey())));
			else
				player.sendMessage(MiniMessage.miniMessage().deserialize("<green>物品显示名称为：</green><white>").append(item.getItemMeta().displayName()));
			return true;
		}

		if (args[0].equalsIgnoreCase("help")) {  // 显示帮助信息
			if (!sender.hasPermission("rebind.help")) {
				plugin.getLanguage().send(sender, "system.no-permission");
				return true;
			}
			sendHelp(sender);
			return true;
		}

		if (args[0].equalsIgnoreCase("claim")) {  // 领取绑定物品

			if (!(sender instanceof Player player)) {
				plugin.getLanguage().send(sender, "system.only-player");
				return true;
			}
			if (!sender.hasPermission("rebind.claim")) {
				plugin.getLanguage().send(sender, "system.no-permission");
				return true;
			}

			// 检查是否有内置邮件
			if (plugin.getMailProvider() instanceof InternalMailImpl internal) {
				List<ItemStack> items = internal.getPendingItems(player.getUniqueId());

				if (items.isEmpty()) {
					plugin.getLanguage().send(player, "mail.no-items");
					return true;
				}

				// 发放物品
				for (ItemStack item : items) {
					player.getInventory().addItem(item).values().forEach(remain ->
							player.getWorld().dropItemNaturally(player.getLocation(), remain));
				}

				internal.clearMail(player.getUniqueId());
				plugin.getLanguage().send(player, "mail.claim-success");
			} else {
				sendHelp(player);
			}
			return true;
		}

		return true;
	}

	private void sendHelp(CommandSender sender) {
		MiniMessage mm = MiniMessage.miniMessage();
		sender.sendMessage(mm.deserialize("<gold><bold>SuBind 帮助</bold></gold>"));
		if (sender.hasPermission("rebind.use")) {
			sender.sendMessage(mm.deserialize("<white>/bind [bind]</white><gray>- 打开绑定界面</gray>"));
		}
		if (sender.hasPermission("rebind.help")) {
			sender.sendMessage(mm.deserialize("<white>/bind help </white><gray>- 显示帮助信息</gray>"));
		}
		if (sender.hasPermission("rebind.claim")) {
			sender.sendMessage(mm.deserialize("<white>/bind claim </white><gray>- 领取绑定物品</gray>"));
		}
		if (sender.hasPermission("rebind.admin")) {
			sender.sendMessage(mm.deserialize("<white>/bind reload </white><gray>- 重载配置文件</gray>"));
		}
		if (sender.hasPermission("rebind.name")) {
			sender.sendMessage(mm.deserialize("<white>/bind name </white><gray>- 查看物品显示名称</gray>"));
		}
	}

	// TAB 补全
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("rebind.admin"))
				return Arrays.asList("bind", "reload", "name", "help", "claim").stream()
						.filter(s -> s.startsWith(args[0].toLowerCase()))
						.collect(Collectors.toList());
			else
				return Arrays.asList("bind", "help", "claim").stream()
						.filter(s -> s.startsWith(args[0].toLowerCase()))
						.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}
}