package net.codingarea.challengesplugin.challenges.challenges;

import net.codingarea.challengesplugin.challengetypes.AdvancedChallenge;
import net.codingarea.challengesplugin.manager.WorldManager;
import net.codingarea.challengesplugin.manager.events.ChallengeEditEvent;
import net.codingarea.challengesplugin.manager.lang.ItemTranslation;
import net.codingarea.challengesplugin.manager.menu.MenuType;
import net.codingarea.challengesplugin.utils.items.ItemBuilder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anweisen & Dominik
 * Challenges developed on 06-19-2020
 * https://github.com/anweisen
 * https://github.com/KxmischesDomi
 */
public class ChunkDeconstructChallenge extends AdvancedChallenge {

    public ChunkDeconstructChallenge() {
       super(MenuType.CHALLENGES, 60);
    }

    @Override
    public void onTimeActivation() {
        List<Chunk> chunks = new ArrayList<>();
        nextActionInSeconds = value;

        for (Player currentPlayer : Bukkit.getOnlinePlayers()) {
            if (currentPlayer.getGameMode() == GameMode.SPECTATOR) continue;
            if (WorldManager.isInExtraWorld(currentPlayer)) continue;
            Chunk chunk = currentPlayer.getLocation().getChunk();
            if (chunks.contains(chunk)) continue;
            chunks.add(chunk);
        }

        for (Chunk currentChuck : chunks) {

            List<Block> blocks = getAllBlockInChuckAtHeight(currentChuck, currentChuck.getWorld().getMaxHeight()-1);

            for (Block currentBlock : blocks) {
                Block highestBlock = getHighestBlock(currentBlock.getLocation());//bin gleich wieder da
                if (highestBlock == null) continue;
                highestBlock.setType(Material.AIR, true);
            }
        }
    }

    @Override
    public @NotNull ItemStack getItem() {
        return new ItemBuilder(Material.DIAMOND_PICKAXE, ItemTranslation.CHUNK_DECONSTRUCT).hideAttributes().build();
    }

    @Override
    public void onValueChange(ChallengeEditEvent event) {
        this.nextActionInSeconds = this.value;
    }

    @Override
    public void onEnable(ChallengeEditEvent event) {
        this.value = 10;
        this.nextActionInSeconds = 10;
    }

    @Override
    public void onDisable(ChallengeEditEvent event) { }

    private Block getHighestBlock(Location location) {

        location = location.clone();

        while (location.getBlock().isPassable() && location.getBlockY() > 0) {
            location.add(0, -1, 0);
        }

        if (location.getBlockY() == 0 || location.getBlock().getType() == Material.BEDROCK) {
            return null;
        }

        return location.getBlock();

    }

    private List<Block> getAllBlockInChuckAtHeight(Chunk chunk, int height) {

        List<Block> list = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                list.add(chunk.getBlock(i, height, j));
            }
        }

        return list;

    }

}