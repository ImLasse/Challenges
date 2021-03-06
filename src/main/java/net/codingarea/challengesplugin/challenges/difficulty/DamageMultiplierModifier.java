package net.codingarea.challengesplugin.challenges.difficulty;

import net.codingarea.challengesplugin.Challenges;
import net.codingarea.challengesplugin.challengetypes.Modifier;
import net.codingarea.challengesplugin.manager.events.ChallengeEditEvent;
import net.codingarea.challengesplugin.manager.lang.ItemTranslation;
import net.codingarea.challengesplugin.utils.items.ItemBuilder;
import net.codingarea.challengesplugin.manager.menu.MenuType;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author anweisen & Dominik
 * Challenges developed on 06-03-2020
 * https://github.com/anweisen
 * https://github.com/KxmischesDomi
 */
public class DamageMultiplierModifier extends Modifier implements Listener {

    public DamageMultiplierModifier() {
        super(MenuType.DIFFICULTY, 5);
    }

    @Override
    public @NotNull ItemStack getItem() {
        return new ItemBuilder(Material.STONE_SWORD, ItemTranslation.DAMAGE_MULTIPLIER).hideAttributes().build();
    }

    @Override
    public void onMenuClick(ChallengeEditEvent event) { }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!Challenges.timerIsStarted()) return;
        if (event.getCause() == DamageCause.VOID) return;

        event.setDamage(event.getDamage() * value);

    }

}
