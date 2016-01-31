package toast.lostBooks;

import java.io.File;
import java.util.Random;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.config.Configuration;
import toast.lostBooks.client.ClientEventHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLModDisabledEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = _LostBooks.MODID, name = "Lost Books", version = _LostBooks.VERSION)
public class _LostBooks {
    /* TO DO *\
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

    // This mod's id.
    public static final String MODID = "LostBooks";
    // This mod's version.
    public static final String VERSION = "1.2.2";

    // If true, this mod starts up in debug mode.
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

    // The book item proxy.
    public static Item randomBook;

    // Called before initialization. Loads the properties/configurations.
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        _LostBooks.debugConsole("Loading in debug mode!");

        _LostBooks.CONFIG_DIRECTORY = event.getModConfigurationDirectory();
        _LostBooks.CONFIG = new Configuration(event.getSuggestedConfigurationFile());
        Properties.init(_LostBooks.CONFIG);

        _LostBooks.CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("LB|CP");
        _LostBooks.CHANNEL.registerMessage(MessageCurrPage.Handler.class, MessageCurrPage.class, 0, Side.SERVER);

        _LostBooks.randomBook = new ItemRandomBook().setUnlocalizedName("randomBook").setCreativeTab(CreativeTabs.tabMisc).setTextureName("book_written").setMaxStackSize(16);
        GameRegistry.registerItem(_LostBooks.randomBook, _LostBooks.randomBook.getUnlocalizedName().substring(5));
    }

    // Called during initialization. Registers entities, mob spawns, and renderers.
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new EventHandler();
        if (event.getSide() == Side.CLIENT) {
            new ClientEventHandler();
        }
        new TickHandler();
        new TradeHandler();
        if (Properties.getBoolean(Properties.GENERAL, "addChestLoot")) {
            ItemStack book = new ItemStack(_LostBooks.randomBook, 1, 0);
            WeightedRandomChestContent tmp = new WeightedRandomChestContent(book, 1, 1, 2);
            ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(tmp);
            ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(tmp);
            ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(tmp);
            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(tmp);
            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).addItem(new WeightedRandomChestContent(book, 1, 5, 4));
            ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(tmp);
            ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(tmp);
        }
    }

    // Called after initialization. Used to check for dependencies.
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        _LostBooks.console("Loading books...");
        _LostBooks.console("Loaded " + Library.UNIQUE_BOOK_COUNT + " unique books, " + Library.COMMON_BOOKS.size() + " common books, and " + Library.AD_LIB_BOOKS.size() + " adlib books!");
        _LostBooks.console("Loading words...");
        AdLibHelper.init();
        //BinHelper.genCharWidths();
        //BinHelper.genGlyphWidths();
    }

    // Called when this mod is disabled.
    @Mod.EventHandler
    public void disable(FMLModDisabledEvent event) {
        // Currently seems to not be called.
        _LostBooks.DISABLED = true;
        _LostBooks.debugConsole("DISABLED!");
    }

    // Use if you need to handle something before the server has even been created.
    @Mod.EventHandler
    public void serverStarting(FMLServerAboutToStartEvent event) {
        FileHelper.init(event.getServer());

        ServerCommandManager commandManager = (ServerCommandManager) event.getServer().getCommandManager();
        commandManager.registerCommand(new CommandTest());
        commandManager.registerCommand(new CommandBlackouts());
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
        if (_LostBooks.debug) {
            System.out.println("[LostBooks] (debug) " + message);
        }
    }

    // Throws a runtime exception with a message and this mod's name tag.
    public static void exception(String message) {
        throw new RuntimeException("[LostBooks] " + message);
    }

    // Throws a runtime exception with a message and this mod's name tag if debugging is enabled.
    public static void debugException(String message) {
        if (_LostBooks.debug)
            throw new RuntimeException("[LostBooks] " + message);
    }
}