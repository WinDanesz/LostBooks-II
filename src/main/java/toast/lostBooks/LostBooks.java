package toast.lostBooks;

import net.minecraft.command.ServerCommandManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import toast.lostBooks.client.ClientEventHandler;
import toast.lostBooks.helper.FileHelper;
import toast.lostBooks.helper.PropertyHelper;

import java.io.File;
import java.util.Random;
//import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
//import cpw.mods.fml.common.registry.GameRegistry;
//import cpw.mods.fml.relauncher.Side;
//import net.minecraft.util.WeightedRandomChestContent;
//import net.minecraftforge.common.ChestGenHooks;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.SidedProxy;
//import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
//import org.apache.logging.log4j.Logger;

@Mod(modid = LostBooks.MODID, name = "Lost Books", version = LostBooks.VERSION, acceptedMinecraftVersions = LostBooks.MC_VERSION)

public class LostBooks {
    /* TODO *\
     >> currentTasks
     * Nearest player does not get the holding player when a book is dusted
	 * Allow adding of word codes
     * Make stock stories
     * Page jumping
     >> tasks
     * Collection
             ? higher librarian drop chance
     * Properties
            > world-test drop requirements (dimension, time?, y-val?)
     * Series
            > drop in order (unique only?)
     >> goals
     * Add-ons!
            > Random questlines
            > Random spellbooks
     \* ** ** */

	public static final String NAME = "Lost Books II";
	public static final String MODID = "lostbooks";
	public static final String VERSION = "2.0.0";
	public static final String MC_VERSION = "[1.12.2]";

	// If true, this mod starts up in debug mode, printing extra information to the console.
	public static final boolean debug = true;
	// The mod's random number generator.
	public static final Random random = new Random();
	// The network channel for this mod.
	public static SimpleNetworkWrapper CHANNEL;
	// If true, this mod will cease to function.
	public static boolean DISABLED = false;
	// The directory of the config files.
	public static File CONFIG_DIRECTORY;

	// The actual configurations.
	public static Configuration CONFIG;

	// Location of the proxy code, used by Forge.
	@SidedProxy(clientSide = "toast.lostBooks.client.ClientProxy", serverSide = "toast.lostBooks.CommonProxy")
	public static CommonProxy proxy;

	// Called before initialization. Loads the properties/configurations.
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LostBooks.debugConsole("Loading in debug mode!");

		LostBooks.CONFIG_DIRECTORY = event.getModConfigurationDirectory();
		LostBooks.CONFIG = new Configuration(event.getSuggestedConfigurationFile());
		PropertyHelper.init(LostBooks.CONFIG);

		LostBooks.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("LB|CP");
		LostBooks.CHANNEL.registerMessage(MessageCurrPage.Handler.class, MessageCurrPage.class, 0, Side.SERVER);

//		LostBooks.randomBook = new ItemRandomBook().setUnlocalizedName("randomBook").setCreativeTab(CreativeTabs.tabMisc).setTextureName("book_written").setMaxStackSize(16);
//		GameRegistry.registerItem(LostBooks.randomBook, LostBooks.randomBook.getUnlocalizedName().substring(5));
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerVillagerTrades();


		// TODO: re-add the loot injection
//		if (event.getSide() == Side.CLIENT) {
//			new ClientEventHandler();
//		}
//		new TickHandler();
//		if (Properties.getBoolean(Properties.GENERAL, "addChestLoot")) {
//			ItemStack book = new ItemStack(LostBooks.randomBook, 1, 0);
//			WeightedRandomChestContent tmp = new WeightedRandomChestContent(book, 1, 1, 2);
//			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(tmp);
//			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(tmp);
//			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(tmp);
//			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(tmp);
//			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(book, 1, 5, 4));
//			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(tmp);
//			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(tmp);
//		}
	}

	// Called after initialization. Used to check for dependencies.
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		LostBooks.console("Loading books...");
		LostBooks.console("Loaded " + Library.UNIQUE_BOOK_COUNT + " unique books, " + Library.COMMON_BOOKS.size() + " common books, and " + Library.AD_LIB_BOOKS.size() + " adlib books!");
//		AdLibHelper.init();
		//BinHelper.genCharWidths();
		//BinHelper.genGlyphWidths();
	}

	// Called when this mod is disabled.
	@Mod.EventHandler
	public void disable(FMLModDisabledEvent event) {
		// Currently seems to not be called.
		LostBooks.DISABLED = true;
		LostBooks.debugConsole("DISABLED!");
	}

	// Use if you need to handle something before the server has even been created.
	@Mod.EventHandler
	public void serverStarting(FMLServerAboutToStartEvent event) {
		FileHelper.init(event.getServer());

		ServerCommandManager commandManager = (ServerCommandManager) event.getServer().getCommandManager();
//		commandManager.registerCommand(new CommandTest());
//		commandManager.registerCommand(new CommandBlackouts());
	}

	// Makes the first letter upper case.
	public static String cap(String string) {
		char[] chars = string.toCharArray();
		if (chars.length <= 0)
			return "";
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

	// Makes the first letter lower case.
	public static String decap(String string) {
		char[] chars = string.toCharArray();
		if (chars.length <= 0)
			return "";
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}

	// Prints the message to the console with this mod's name tag.
	public static void console(String message) {
		System.out.println("[LostBooks] " + message);
	}

	// Prints the message to the console with this mod's name tag if debugging is enabled.
	public static void debugConsole(String message) {
		if (LostBooks.debug) {
			System.out.println("[LostBooks] (debug) " + message);
		}
	}

	// Throws a runtime exception with a message and this mod's name tag.
	public static void exception(String message) {
		throw new RuntimeException("[LostBooks] " + message);
	}

	// Throws a runtime exception with a message and this mod's name tag if debugging is enabled.
	public static void debugException(String message) {
		if (LostBooks.debug)
			throw new RuntimeException("[LostBooks] " + message);
	}
}