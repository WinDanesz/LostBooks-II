package toast.lostBooks.event;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import toast.lostBooks.Blackouts;
import toast.lostBooks.Library;
import toast.lostBooks.LostBooks;
import toast.lostBooks.TickHandler;
import toast.lostBooks.config.PropertyHelper;
import toast.lostBooks.helper.BookHelper;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventHandler {

    public EventHandler(){}

    /**
     * Called by EntityLiving.onDeath().
     * EntityLivingBase entityLiving = the entity dropping the items.
     * DamageSource source = the source of the lethal damage.
     * ArrayList<EntityItem> drops = the items being dropped.
     * int lootingLevel = the attacker's looting level.
     * boolean recentlyHit = if the entity was recently hit by another player.
     * int specialDropValue = recentlyHit ? entityLiving.getRNG().nextInt(200) - lootingLevel : 0.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntityLiving()== null || event.getEntityLiving().world.isRemote)
            return;
        if (LostBooks.debug || event.isRecentlyHit() && PropertyHelper.getBoolean(PropertyHelper.GENERAL, "dropRate", event.getEntityLiving().getRNG())) {
            ItemStack book = Library.nextBook(event.getEntityLiving());
            if (book != null) {
                EntityItem drop = new EntityItem(event.getEntityLiving().world, event.getEntityLiving().posX, event.getEntityLiving().posY, event.getEntityLiving().posZ, book);
                drop.setPickupDelay(10);
				event.getDrops().add(drop);
            }
        }
    }

    /**
     * Called by <unknown>.
     * EntityPlayer entityPlayer = the player picking up the item.
     * EntityItem item = the item being picked up.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityItemPickup(EntityItemPickupEvent event) {
        if (event.getEntityPlayer() == null || event.getEntityPlayer().world.isRemote)
            return;
        ItemStack book = event.getItem().getItem();
        String id = BookHelper.getBookId(book);
        if (id == null || id == "") {
            // Do nothing.
        }
        else if (BookHelper.isBookFound(event.getEntityPlayer(), id)) {
            if (PropertyHelper.getBoolean(PropertyHelper.GENERAL, "hardUniqueBlackouts")) {
                event.setCanceled(true);
                return;
            }
        }
        else if (Blackouts.DROP_BLACKOUTS) {
            BookHelper.markBookAsFound(event.getEntityPlayer(), id);
            TickHandler.unloadBlackouts(event.getEntityPlayer().world);
        }
        if (!event.getItem().world.isRemote && BookHelper.removeBookId(book)) {
            event.getItem().setItem(book);
//            event.item.setEntityItemStack(book);
        }
    }

    /**
     * Called by EntityItem.onUpdate().
     * EntityItem entityItem = the expiring item.
     * int extraLife = the time added to the item's life if this event is cancelled.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemExpire(ItemExpireEvent event) {
        if (PropertyHelper.getBoolean(PropertyHelper.GENERAL, "lostBookCaptureRate") && !event.isCanceled()) {
			ItemStack book = event.getEntityItem().getItem();
            if (book.getItem().getUnlocalizedNameInefficiently(book).equals("item.writtenBook") && !BookHelper.hasBookId(book)) {
                Library.LOST_BOOKS.capture(book);
                if (!event.getEntityItem().world.isRemote) {
                    event.getEntityItem().setItem(book);
                }
            }
        }
    }

    /**
     * Called by MinecraftServer.unloadAllWorlds()?.
     * World world = the world being unloaded.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnload(WorldEvent.Unload event) {
        if (event.getWorld() != null && !event.getWorld().isRemote) {
            TickHandler.unloadBlackouts(event.getWorld());
        }
    }
}