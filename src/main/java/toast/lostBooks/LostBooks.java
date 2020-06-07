package toast.lostBooks;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLModDisabledEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import toast.lostBooks.book.CommonProxy;
import toast.lostBooks.book.Library;
import toast.lostBooks.command.CommandBlackouts;
import toast.lostBooks.config.PropertyHelper;
import toast.lostBooks.helper.AdLibHelper;
import toast.lostBooks.helper.ConfigFileHelper;
import toast.lostBooks.helper.FileHelper;

import java.io.File;
import java.util.Random;

@Mod(modid = LostBooks.MODID, name = LostBooks.NAME, version = LostBooks.VERSION, acceptedMinecraftVersions = LostBooks.MC_VERSION, updateJSON = LostBooks.UPDATE_JSON)

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
	public static final String VERSION = "2.0.1";
	public static final String MC_VERSION = "[1.12.2]";
	public static final String UPDATE_JSON = "https://raw.githubusercontent.com/WinDanesz/LostBooks-II/1.12.2/.forge/update.json";

	// If true, this mod starts up in debug mode, printing extra information to the console.
	public static final boolean debug = false;
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
	@SidedProxy(clientSide = "toast.lostBooks.client.ClientProxy", serverSide = "toast.lostBooks.book.CommonProxy")
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
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		ConfigFileHelper.init();
		proxy.registerVillagerTrades();
		new TickHandler();
	}

	// Called after initialization. Used to check for dependencies.
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		LostBooks.console("Loading books...");
		LostBooks.console("Loaded " + Library.UNIQUE_BOOK_COUNT + " unique books, " + Library.COMMON_BOOKS.size() + " common books, and " + Library.AD_LIB_BOOKS.size() + " adlib books!");
		AdLibHelper.init();
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
	public void serverStarting(FMLServerStartingEvent event) {
		FileHelper.init(event.getServer());

		event.registerServerCommand(new CommandBlackouts());
		//		event.registerCommand(new CommandTest()); TODO
	}

	// Prints the message to the console with this mod's name tag. //test
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