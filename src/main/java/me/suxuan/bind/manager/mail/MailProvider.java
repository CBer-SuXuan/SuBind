package me.suxuan.bind.manager.mail;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface MailProvider {
	/**
	 * 向玩家发送带有附件物品的邮件
	 *
	 * @param uuid    接收者UUID
	 * @param title   邮件标题
	 * @param content 邮件内容列表
	 * @param item    附件物品
	 */
	void sendMail(UUID uuid, String title, List<String> content, ItemStack item);
}