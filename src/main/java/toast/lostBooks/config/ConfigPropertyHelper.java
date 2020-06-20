package toast.lostBooks.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import toast.lostBooks.LostBooks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * This helper class automatically creates, stores, and retrieves properties.
 * Supported data types:
 * String, boolean, int, double
 * <p>
 * Any property can be retrieved as an Object or String.
 * Any non-String property can also be retrieved as any other non-String property.
 * Retrieving a number as a boolean will produce a randomized output depending on the value.
 */
public abstract class ConfigPropertyHelper {
	/// Array of book type names.
	public static final String[] BOOK_CATEGORIES = {"unique", "common", "adlib", "lost"};
	/// The localization code for the mod.
	public static final String LOCAL = "lostbooks";
	/// Common category names.
	public static final String GENERAL = "general";
	public static final String LOST_BOOKS = "lostBookProperties";
	public static final String TRADING = "trading";
	public static final String UTILS = "utilities";
	/// Mapping of all properties in the mod to their values.
	private static final HashMap<String, Object> map = new HashMap();

	/// Initializes these properties.
	public static void init(Configuration config) {
		config.load();
		ArrayList<String> order = new ArrayList<String>();

		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "generateDefaultBookPackAtStart", true, "Determines whether the default book pack should be unpacked to the config folder at start or not. Its enough to start the game once then disable this, if you want to alter the default pack, or you want to get rid of it entirely. Keeping this setting True will re-generate the book pack every time the game starts!");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "dropRate", 0.03, "The chance (from 0 to 1) for a book to be dropped from a mob. Default is 0.03 (3% chance).");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "addChestLoot", true, "If this is true, dusty books will generate as chest loot. Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "markUnique", true, "If this is true, unique books will have an additional line under the author in their tooltip that says \"Unique\". (So people know which books they can not get again.) Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "uniqueBlackouts", true, "If this is true, unique books will not drop if every player on the server has picked one up. Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "hardUniqueBlackouts", true, "If this is true, unique books can not be picked up by any player more than once. This only applies to mob drops. Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "adaptiveDrops", true, "If this is true, non-unique books will drop more often to fill the place of blacked-out unique books. Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, "lostBookCaptureRate", 1.0, "The chance (from 0 to 1) for books left on the floor to be saved to the list of \"lost\" books to be dropped later. Each save is deleted once its book has been dropped. Default is 1.0 (100% chance).");
		for (String category : ConfigPropertyHelper.BOOK_CATEGORIES) {
			ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.GENERAL, category + "BookWeight", category == "unique" ? 2 : 1, "The weighted chance that a dropped book will be " + (category == "adlib" ? "an " + category : "a " + category) + " book. Default is " + (category == "unique" ? 2 : 1) + ".");
		}
		//config.setCategoryPropertyOrder(Properties.GENERAL, order);
		order.clear();

		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.LOST_BOOKS, "whitelist", "", "Comma separated list of entities that lost books will drop from. (e.g., Skeleton,Player,Zombie) Default is empty.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.LOST_BOOKS, "blacklist", "", "Comma separated list of entities that lost books will NOT drop from. Default is empty.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.LOST_BOOKS, "biomes", "", "Comma separated list of biome ids that lost books will drop in. If left empty, lost books will drop in all biomes. Default is empty.");
		//config.setCategoryPropertyOrder(Properties.LOST_BOOKS, order);
		order.clear();

		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.TRADING, "tradeChance", 0.8, "The chance (from 0 to 1) for a generated book to be considered as a possible trade option. (Not the direct chance for a librarian to be selling a book.) Default is 0.8.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.TRADING, "minBookCost", 2, "The minimum emerald cost (from 1 to 64) a villager will charge for a book. Default is 2.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.TRADING, "maxBookCost", 4, "The maximum emerald cost (from 1 to 64) a villager will charge for a book. Default is 4.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.TRADING, "sellLost", true, "If this is true, librarians will be able to find and sell lost books. Note that unless you \"lose\" a lot of books and/or have a really low lost book drop rate, you probably won't see any in trades anyway. Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.TRADING, "sellUnique", false, "If this is true, librarians will be able to sell unique books. Trades ignore all unique blackout restrictions! Default is false.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.TRADING, "sellDustyBook", true, "If this is true, librarians will be able to sell dusty books. Default is true.");
		//config.setCategoryPropertyOrder(Properties.TRADING, order);
		order.clear();

		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.UTILS, "openToPreviousPage", true, "(Client-only) If true, books will reopen to the last page they were open to. Default is true.");
		ConfigPropertyHelper.add(config, order, ConfigPropertyHelper.UTILS, "pauseWhileReading", false, "(Client-only) If true, having a book open will pause the game in singleplayer, as it does in vanilla. Default is false.");
		//config.setCategoryPropertyOrder(Properties.UTILS, order);
		order.clear();

		config.addCustomCategoryComment(ConfigPropertyHelper.GENERAL, "General and/or miscellaneous options.");
		config.addCustomCategoryComment(ConfigPropertyHelper.TRADING, "Options for trades with librarian villagers.");
		config.addCustomCategoryComment(ConfigPropertyHelper.LOST_BOOKS, "Equivalent to a book properties file. Applies to all \"lost\" books.");
		config.addCustomCategoryComment(ConfigPropertyHelper.UTILS, "Various options relating to books and the book gui.");
		config.save();
	}

	/// Gets the mod's random number generator.
	public static Random random() {
		return LostBooks.random;
	}

	/// Passes to the mod.
	public static void debugException(String message) {
		LostBooks.debugException(message);
	}

	/// Loads the property as the specified value.
	public static void add(Configuration config, ArrayList<String> order, String category, String field, String defaultValue, String comment) {
		Property prop = config.get(category, field, defaultValue, comment);
		prop.setLanguageKey("toast." + ConfigPropertyHelper.LOCAL + ".configgui." + category + "." + field);
		order.add(prop.getName());
		ConfigPropertyHelper.map.put(category + "@" + field, prop.getString());
	}

	public static void add(Configuration config, ArrayList<String> order, String category, String field, int defaultValue, String comment) {
		Property prop = config.get(category, field, defaultValue, comment);
		prop.setLanguageKey("toast." + ConfigPropertyHelper.LOCAL + ".configgui." + category + "." + field);
		order.add(prop.getName());
		ConfigPropertyHelper.map.put(category + "@" + field, Integer.valueOf(prop.getInt(defaultValue)));
	}

	public static void add(Configuration config, ArrayList<String> order, String category, String field, boolean defaultValue, String comment) {
		Property prop = config.get(category, field, defaultValue, comment);
		prop.setLanguageKey("toast." + ConfigPropertyHelper.LOCAL + ".configgui." + category + "." + field);
		order.add(prop.getName());
		ConfigPropertyHelper.map.put(category + "@" + field, Boolean.valueOf(prop.getBoolean(defaultValue)));
	}

	public static void add(Configuration config, ArrayList<String> order, String category, String field, double defaultValue, String comment) {
		Property prop = config.get(category, field, defaultValue, comment);
		prop.setLanguageKey("toast." + ConfigPropertyHelper.LOCAL + ".configgui." + category + "." + field);
		order.add(prop.getName());
		ConfigPropertyHelper.map.put(category + "@" + field, Double.valueOf(prop.getDouble(defaultValue)));
	}

	/// Gets the Object property.
	public static Object getProperty(String category, String field) {
		return ConfigPropertyHelper.map.get(category + "@" + field);
	}

	/// Gets the value of the property (instead of an Object representing it).
	public static String getString(String category, String field) {
		return ConfigPropertyHelper.getProperty(category, field).toString();
	}

	public static boolean getBoolean(String category, String field) {
		return ConfigPropertyHelper.getBoolean(category, field, ConfigPropertyHelper.random());
	}

	public static boolean getBoolean(String category, String field, Random random) {
		Object property = ConfigPropertyHelper.getProperty(category, field);
		if (property instanceof Boolean)
			return ((Boolean) property).booleanValue();
		if (property instanceof Integer)
			return random.nextInt(((Number) property).intValue()) == 0;
		if (property instanceof Double)
			return random.nextDouble() < ((Number) property).doubleValue();
		ConfigPropertyHelper.debugException("Tried to get boolean for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
		return false;
	}

	public static int getInt(String category, String field) {
		Object property = ConfigPropertyHelper.getProperty(category, field);
		if (property instanceof Number)
			return ((Number) property).intValue();
		if (property instanceof Boolean)
			return ((Boolean) property).booleanValue() ? 1 : 0;
		ConfigPropertyHelper.debugException("Tried to get int for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
		return 0;
	}

	public static double getDouble(String category, String field) {
		Object property = ConfigPropertyHelper.getProperty(category, field);
		if (property instanceof Number)
			return ((Number) property).doubleValue();
		if (property instanceof Boolean)
			return ((Boolean) property).booleanValue() ? 1.0 : 0.0;
		ConfigPropertyHelper.debugException("Tried to get double for invalid property! @" + property == null ? "(null)" : property.getClass().getName());
		return 0.0;
	}
}