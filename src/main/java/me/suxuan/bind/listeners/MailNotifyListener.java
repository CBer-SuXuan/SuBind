package me.suxuan.bind.listeners;

import me.suxuan.bind.SuBind;
import me.suxuan.bind.manager.mail.InternalMailImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MailNotifyListener implements Listener {

	private final SuBind plugin;

	public MailNotifyListener(SuBind plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (plugin.getMailProvider() instanceof InternalMailImpl internal) {
			if (!internal.getPendingItems(player.getUniqueId()).isEmpty()) {
				plugin.getLanguage().send(player, "mail.notify");
			}
		}
	}

}
