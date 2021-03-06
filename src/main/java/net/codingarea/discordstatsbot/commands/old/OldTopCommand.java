package net.codingarea.discordstatsbot.commands.old;

import net.codingarea.challengesplugin.manager.players.stats.PlayerStats;
import net.codingarea.challengesplugin.manager.players.stats.StatsAttribute;
import net.codingarea.challengesplugin.utils.commons.Log;
import net.codingarea.challengesplugin.utils.sql.MySQL;
import net.codingarea.discordstatsbot.DiscordBot;
import net.codingarea.discordstatsbot.commandmanager.events.CommandEvent;
import net.codingarea.discordstatsbot.commandmanager.commands.Command;
import net.dv8tion.jda.api.entities.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import static net.codingarea.challengesplugin.utils.ImageUtils.*;

/**
 * @author anweisen
 * Challenges developed on 07-14-2020
 * https://github.com/anweisen
 */

public class OldTopCommand extends Command {

	public static final int COOLDOWN_MILLIS = 10*1000;

	private final HashMap<User, Long> userOnCoolDown; // Saves the millis until the user is on cooldown
	private final BufferedImage background;

	public OldTopCommand(DiscordBot bot) {
		super("top", true, "best", "lb", "lead");
		userOnCoolDown = new HashMap<>();
		background = bot.getBackground();
	}

	@Override
	public void onCommand(CommandEvent event) {

		if (event.getArgs().length == 0) {
			event.queueReply("Benutze " + CommandEvent.syntax(event, "<category>"));
			return;
		}

		String categoryName = event.getArgsAsString();
		StatsAttribute category = StatsAttribute.byName(categoryName);

		if (category == null) {
			event.queueReply("Die Kategorie `" + categoryName + "` gibt es nicht. Mit " + CommandEvent.syntax(event, "categories", false) + " siehst du alle.");
			return;
		}

		Long until; // not using the primitive datatype to prevent a NullPointerException
		if ((until = userOnCoolDown.get(event.getUser())) != null) {
			if (until > System.currentTimeMillis()) {
				DecimalFormat format = new DecimalFormat("0.0");
				long millis = until - System.currentTimeMillis();
				event.queueReply("Du kannst diesen Command erst wieder in **" + format.format((double) millis / 1000) +  "s** nutzen");
				return;
			}
		}

		userOnCoolDown.put(event.getUser(), System.currentTimeMillis() + COOLDOWN_MILLIS);
		event.getChannel().sendTyping().queue();

		try {

			ResultSet result = MySQL.get("SELECT * FROM user");

			if (result == null || !result.next()) {
				event.queueReply("Keine Spieler gefunden");
				return;
			}

			TreeMap<Double, List<String>> stats = new TreeMap<>(Collections.reverseOrder());

			do {

				String playerName = result.getString("player");
				String currentStatsJSON = result.getString("stats");

				if (playerName == null || currentStatsJSON == null) continue;

				PlayerStats currentStats = PlayerStats.fromJSON(currentStatsJSON);
				double attribute = currentStats.get(category);

				if (attribute == 0) continue;

				List<String> playersWithStat = stats.getOrDefault(attribute, new ArrayList<>());
				playersWithStat.add(playerName);

				stats.put(attribute, playersWithStat);

			} while (result.next());

			try {

				BufferedImage image = getTopImage(stats, category);

				File folder = new File("./images");
				if (!folder.exists()) folder.mkdirs();
				File file = new File(folder + "/top-" + event.getUser().getId() + ".png");
				if (!file.exists()) file.createNewFile();

				ImageIO.write(image, "png", file);

				event.getChannel().sendFile(file, "top.png").queue(message -> {
					file.delete();
				}, exception -> {});

			} catch (IOException ex) {
				Log.severe("Could not create image files :: " + ex.getMessage());
				event.queueReply("Etwas ist mit dem Server schief gelaufen");
			}

		} catch (SQLException ex) {
			Log.severe("Could not fetch top players :: " + ex.getMessage());
			event.queueReply("Etwas ist mit der Datenbank schief gelaufen");
		}

	}

	private BufferedImage getTopImage(TreeMap<Double, List<String>> stats, StatsAttribute category) {

		int width = 2048;
		int height = 1824;

		Color backgroundColor = Color.decode("#2C2F33");
		Color frameColor = Color.decode("#ffffff");
		Color playerNameColor = Color.decode("#D0D3D5");
		Color textColor = Color.decode("#99999A");
		Color attributeColor = Color.decode("#c4c2c2");

		// Creating images
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D graphics = bufferedImage.createGraphics();

		// Setting background and image if available
		graphics.setColor(backgroundColor);
		graphics.fillRect(0, 0, width, height);
		if (background != null) graphics.drawImage(background, 0, 0, width, height, null);

		// Drawing title
		graphics.setFont(new Font("Arial", Font.BOLD, 100));
		graphics.setColor(playerNameColor);
		int titleY = 150;
		int titleWidth = addCenteredText(graphics, "Leaderboard", titleY, width);

		// Drawing subtitle
		int subTitleY = titleY * 2;
		int subTitleWidth = addCenteredText(graphics, category.getName(), subTitleY, width);

		// Drawing line under title
		graphics.setColor(frameColor);
		int lineXPosition = width / 2 - Math.max(titleWidth, subTitleWidth) / 2;
		graphics.fillRect(lineXPosition - 20, titleY + 40, Math.max(titleWidth, subTitleWidth) + 40, 10);

		int attributeTextSize = 80;
		int attributePaddingTop = 23;
		graphics.setFont(new Font("Arial", Font.BOLD, attributeTextSize));

		char splitter = '»';

		int first = subTitleY + 115 + 40;
		int place = 1;
		int players = 0;
		for (Entry<Double, List<String>> currentEntry : stats.entrySet()) {

			for (String currentPlayer : currentEntry.getValue()) {
				addAttribute(graphics, (place) + ". " + currentPlayer, category.format(currentEntry.getKey(), true), splitter, first + (attributeTextSize + attributePaddingTop) * players, width, textColor, attributeColor);
				players++;
				if (players > 13) break;
			}

			place++;

		}

		graphics.dispose();

		// Downscaling the image by 3 so that it can be send faster
		// Could have made the image smaller from the beginning but I don't care I'm too lazy
		BufferedImage scaled = new BufferedImage(width / 3, height / 3, BufferedImage.TYPE_INT_RGB);
		Graphics2D scaledGraphics = scaled.createGraphics();

		scaledGraphics.drawImage(bufferedImage, 0, 0, width / 3, height / 3, null);
		scaledGraphics.dispose();

		return scaled;

	}

}
