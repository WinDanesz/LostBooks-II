package toast.lostBooks;

import java.util.HashMap;

import net.minecraft.world.World;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class TickHandler {
    /// Cache of blacked out book IDs for each world. Only updated when a unique book is dropped, and only if sufficient time has passed or a unique book has been picked up.
    private static final HashMap<World, Blackouts> blackoutCache = new HashMap<World, Blackouts>();
    /// Counter to periodically clear the blackout cache.
    private static byte updateTicks = Byte.MIN_VALUE;

    /// Gets or creates a new blackout object and caches it.
    public static Blackouts getOrCreateBlackouts(World world) {
        Blackouts blackouts = TickHandler.blackoutCache.get(world);
        if (blackouts == null) {
            blackouts = new Blackouts(world);
            TickHandler.blackoutCache.put(world, blackouts);
        }
        return blackouts;
    }

    /// Clears the blackouts from the cache.
    public static void unloadBlackouts() {
        TickHandler.blackoutCache.clear();
    }

    public static void unloadBlackouts(World world) {
        TickHandler.blackoutCache.remove(world);
    }

    public TickHandler() {
        FMLCommonHandler.instance().bus().register(this);
    }

    /**
     * Called when the config is changed.
     * String modID = the id of the mod.
     * boolean isWorldRunning = true if the world is running.
     * boolean requiresMcRestart = true if any changed items are marked as requiring a world restart.
     * String configID = a string identifier for the config.
     * 
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID == _LostBooks.MODID) {
            // TODO update properties?
        }
    }

    /**
     * Called each tick.
     * TickEvent.Type type = the type of tick.
     * Side side = the side this tick is on.
     * TickEvent.Phase phase = the phase of this tick (START, END).
     * 
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (++TickHandler.updateTicks == Byte.MAX_VALUE) {
                TickHandler.updateTicks = Byte.MIN_VALUE;
                TickHandler.unloadBlackouts();
            }
        }
    }
}
