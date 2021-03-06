package net.codingarea.challengesplugin.challengetypes;

import net.codingarea.challengesplugin.manager.events.ChallengeEditEvent;
import net.codingarea.challengesplugin.manager.menu.MenuClickHandler;
import net.codingarea.challengesplugin.manager.menu.MenuType;
import net.codingarea.challengesplugin.utils.animation.AnimationSound;
import org.bukkit.inventory.ItemStack;

/**
 * @author anweisen & Dominik
 * Challenges developed on 06-15-2020
 * https://github.com/anweisen
 * https://github.com/KxmischesDomi
 */

public abstract class AdvancedChallenge extends Challenge {

	protected int value = 1;
	protected int maxValue = 2;
	protected int minValue = 1;

	protected int countUp = 1;

	protected AnimationSound sound = AnimationSound.STANDARD_SOUND;

	public AdvancedChallenge(MenuType menu) {
		super(menu);
	}

	public AdvancedChallenge(MenuType menu, int maxValue) {
		super(menu);
		this.maxValue = maxValue;
	}

	public AdvancedChallenge(MenuType menu, int maxValue, int minValue) {
		this(menu, maxValue);
		this.minValue = minValue;
	}

	public AdvancedChallenge(MenuType menu, int maxValue, int minValue, int countUp) {
		this(menu, maxValue, minValue);
		this.countUp = countUp;
	}

	public abstract void onValueChange(ChallengeEditEvent event);

	@Override
	public void handleClick(ChallengeEditEvent event) {

		if (MenuClickHandler.shouldChangeGoalValue(event.getClickResult())) {

			if (!enabled) {
				enabled = true;
				if (activationSound != null) activationSound.play(event.getPlayer());
				onEnable(event);
				return;
			}

			if (event.wasRightClick()) {
				if ((value - countUp) >= minValue) {
					value -= countUp;
				} else {
					value = maxValue;
				}
			} else {
				if (!((value + countUp) > maxValue)) {
					value += countUp;
				} else {
					value = minValue;
				}
			}

			if (sound != null) sound.play(event.getPlayer());
			onValueChange(event);

		} else {

			if (enabled) {
				enabled = false;
				if (deactivationSound != null) deactivationSound.play(event.getPlayer());
				value = minValue;
				onDisable(event);
			} else {
				enabled = true;
				if (activationSound != null) activationSound.play(event.getPlayer());
				onEnable(event);
			}

		}

	}

	@Override
	public ItemStack getItemToShow() {
		ItemStack item = getItem();
		item.setAmount(enabled ? value : 1);
		return item;
	}

}
