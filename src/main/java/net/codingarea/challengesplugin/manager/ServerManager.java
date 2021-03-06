package net.codingarea.challengesplugin.manager;

import net.codingarea.challengesplugin.Challenges;
import net.codingarea.challengesplugin.manager.events.ChallengeEndCause;
import net.codingarea.challengesplugin.manager.lang.Translation;
import net.codingarea.challengesplugin.manager.players.stats.StatsAttribute;
import net.codingarea.challengesplugin.timer.ChallengeTimer;
import net.codingarea.challengesplugin.utils.Utils;
import net.codingarea.challengesplugin.utils.animation.AnimationSound;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author anweisen & Dominik
 * Challenges developed on 06-06-2020
 * https://github.com/anweisen
 * https://github.com/KxmischesDomi
 */

public class ServerManager {

	private static ServerManager instance;

	private final Challenges plugin;

	private final boolean clearInventoryOnEnd;
	private boolean won;

	public ServerManager(Challenges plugin) {
		instance = this;
		this.plugin = plugin;
		clearInventoryOnEnd = plugin.getConfig().getBoolean("drop-inventory-on-end");
	}

	public static void simulateChallengeEnd(Player player, ChallengeEndCause cause) {
		instance.handleChallengeEnd(player, cause, null);
	}

	public void handleChallengeEnd(Player player, ChallengeEndCause cause, PlayerDeathEvent deathEvent) {

		if (!Challenges.timerIsStarted()) return;
		if (cause == ChallengeEndCause.KILL_ALL) return;

		if (deathEvent != null) {
			deathEvent.setKeepInventory(true);
			deathEvent.getDrops().clear();
		}

		List<Player> winners = new ArrayList<>();
		if (cause == ChallengeEndCause.PLAYER_CHALLENGE_GOAL_REACHED || cause == ChallengeEndCause.LAST_MAN_STANDING) {
			winners.add(player);
		} else {
			if (plugin.getChallengeManager().getGoalManager().getCurrentGoal() != null) {
				List<Player> goalWinner = plugin.getChallengeManager().getGoalManager().getCurrentGoal().getWinners();
				if (goalWinner == null) {
					goalWinner = new ArrayList<>(Bukkit.getOnlinePlayers());
				}
				winners.addAll(goalWinner);
			}
		}

		if (!won) {
			won = true;
			for (Player currentWinner : winners) {
				plugin.getStatsManager().getPlayerStats(currentWinner).add(StatsAttribute.WON, 1);
			}
			plugin.getStatsManager().storeAll();
		}

		String players = Utils.getPlayerListAsString(winners);

		String message = "";
		if (cause == ChallengeEndCause.PLAYER_CHALLENGE_GOAL_REACHED || cause == ChallengeEndCause.LAST_MAN_STANDING) {
			message = Translation.CHALLENGE_END_GOAL_REACHED.get();
		} else if (cause == ChallengeEndCause.TIMER_END) {
			message = Translation.CHALLENGE_END_TIMER_END.get();
		} else if (cause == ChallengeEndCause.PLAYER_CHALLENGE_FAIL || cause == ChallengeEndCause.PLAYER_DEATH) {
			message = Translation.CHALLENGE_END_GOAL_FAILED.get();
		}
		message = message.replace("%time%", ChallengeTimer.getTimeDisplay(plugin.getChallengeTimer().getSeconds())).replace("%winner%", players).replace("%seed%", Bukkit.getWorlds().get(0).getSeed() + "");

		plugin.getChallengeTimer().stopTimer(null, false);

		for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
			dropInventory(currentPlayer.getInventory(), currentPlayer.getLocation());
			currentPlayer.setGameMode(GameMode.SPECTATOR);
			currentPlayer.updateInventory();
		}


		playEndEffect();

		String finalMessage = message;
		Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.broadcastMessage(finalMessage), 2);

	}

	private void dropInventory(PlayerInventory inventory, Location location) {

		if (!clearInventoryOnEnd) return;
		if (location == null || location.getWorld() == null) return;

		List<ItemStack> items = new ArrayList<>(Arrays.asList(inventory.getContents()));
		inventory.clear();

		for (ItemStack currentItemStack : items) {
			try {
				if (currentItemStack == null) continue;
				if (currentItemStack.getType() == Material.AIR) continue;
				location.getWorld().dropItemNaturally(location, currentItemStack);
			} catch (IllegalArgumentException ignored) { }
		}

	}

	private void playEndEffect() {

		// This try catch is needed because of a bug in the spigot-1.16 where you cannot spawn a firework and you get an IllegalArgumentException
		// In the paper-1.16 the bug doesn't exists, so use it lol
		try {
			for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
				currentPlayer.getWorld().spawnEntity(currentPlayer.getLocation(), EntityType.FIREWORK);
			}
		} catch (IllegalArgumentException ignored) { };

		new AnimationSound(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1F, 1F).broadcast();

	}

}
