package net.codingarea.challengesplugin.commands;

import net.codingarea.challengesplugin.Challenges;
import net.codingarea.challengesplugin.manager.lang.Prefix;
import net.codingarea.challengesplugin.manager.lang.Translation;
import net.codingarea.challengesplugin.utils.animation.AnimationSound;
import net.codingarea.challengesplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author anweisen & Dominik
 * Challenges developed on 06-07-2020
 * https://github.com/anweisen
 * https://github.com/KxmischesDomi
 */

public class VillageCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

		if (!(sender instanceof Player)) return true;

		final Player player = (Player) sender;

		player.setNoDamageTicks(10);

		Bukkit.getScheduler().runTaskLater(Challenges.getInstance(), () -> {

			Location village = player.getLocation().getWorld().locateNearestStructure(player.getLocation(), StructureType.VILLAGE, 3000, true);
			if (village == null) {
				player.sendMessage(Prefix.CHALLENGES + Translation.VILLAGE_TELEPORT_ERROR.get());
				return;
			}

			village = Utils.getHighestBlock(village);

			player.teleport(village);
			player.sendMessage(Prefix.CHALLENGES + Translation.VILLAGE_TELEPORT.get());
			AnimationSound.TELEPORT_SOUND.playDelayed(Challenges.getInstance(), player, 1);

		}, 1);

		return true;
	}

}
