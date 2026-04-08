package me.suxuan.bind.manager.mail;

import me.suxuan.bind.SuBind;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.sweetmail.IMail;
import top.mrxiaom.sweetmail.attachments.AttachmentItem;

import java.util.List;
import java.util.UUID;

public class SweetMailImpl implements MailProvider {

	private final SuBind plugin;

	public SweetMailImpl(SuBind plugin) {
		this.plugin = plugin;
	}

	@Override
	public void sendMail(UUID uuid, String title, List<String> content, ItemStack item) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		IMail.api()
				.createSystemMail(plugin.getConfig().getString("mail.from"))
				.setIcon(plugin.getConfig().getString("mail.icon"))
				.setTitle(plugin.getConfig().getString("mail.title"))
				.setContent(plugin.getConfig().getStringList("mail.content").stream().map(line -> line.replace("{player}", offlinePlayer.getName())).toList())
				.addAttachments(
						AttachmentItem.build(item)
				)
				.setReceiverFromPlayer(offlinePlayer)
				.send();
	}

}
