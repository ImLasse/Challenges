package net.codingarea.challengesplugin.challenges.goal;

import net.codingarea.challengesplugin.Challenges;
import net.codingarea.challengesplugin.challengetypes.CollectGoal;
import net.codingarea.challengesplugin.challengetypes.extra.ITimerStatusExecutor;
import net.codingarea.challengesplugin.manager.lang.ItemTranslation;
import net.codingarea.challengesplugin.manager.lang.Translation;
import net.codingarea.challengesplugin.manager.menu.MenuType;
import net.codingarea.challengesplugin.utils.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author anweisen & Dominik
 * Challenges developed on 06-01-2020
 * https://github.com/anweisen
 * https://github.com/KxmischesDomi
 */

public class CollectItemsGoal extends CollectGoal<Material> implements Listener, ITimerStatusExecutor {

	public CollectItemsGoal() {
		super(MenuType.GOALS);
	}

	@Override
	public @NotNull ItemStack getItem() {
		return new ItemBuilder(Material.STICK, ItemTranslation.COLLECT_ITEMS).build();
	}

	@Override
	public void onTimerStart() {
		if (!isCurrentGoal) return;
		points = new ConcurrentHashMap<>();
		updateScoreboard();
	}

	@EventHandler
	public void onPlayerItemCollect(PlayerPickupItemEvent event) {

		if (!isCurrentGoal || !Challenges.timerIsStarted()) return;
		handleNewPoint(event.getPlayer(), event.getItem().getItemStack().getType(), event.getItem().getItemStack().getType().name(), Translation.COLLECT_ITEMS_ITEM_REGISTERED);

	}

	@EventHandler
	public void onPlayerCraftItem(InventoryClickEvent event) {

		if (!isCurrentGoal || !Challenges.timerIsStarted()) return;
		if (!(event.getWhoClicked() instanceof Player)) return;
		if (event.getCurrentItem() == null) return;
		if (event.getClickedInventory() == null) return;
		Player player = (Player) event.getWhoClicked();
		if (!event.getClickedInventory().equals(player.getInventory()) && event.getClickedInventory().getType() == InventoryType.CHEST) return;

		handleNewPoint(player, event.getCurrentItem().getType(), event.getCurrentItem().getType().name(), Translation.COLLECT_ITEMS_ITEM_REGISTERED);

	}

}
