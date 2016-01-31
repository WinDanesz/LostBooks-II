package toast.lostBooks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler {
    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

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
        if (event.entityLiving == null || event.entityLiving.worldObj.isRemote)
            return;
        if (_LostBooks.debug || event.recentlyHit && Properties.getBoolean(Properties.GENERAL, "dropRate", event.entityLiving.getRNG())) {
            ItemStack book = Library.nextBook(event.entityLiving);
            if (book != null) {
                EntityItem drop = new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, book);
                drop.delayBeforeCanPickup = 10;
                event.drops.add(drop);
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
        if (event.entityPlayer == null || event.entityPlayer.worldObj.isRemote)
            return;
        ItemStack book = event.item.getEntityItem();
        String id = BookHelper.getBookId(book);
        if (id == null || id == "") {
            // Do nothing.
        }
        else if (BookHelper.isBookFound(event.entityPlayer, id)) {
            if (Properties.getBoolean(Properties.GENERAL, "hardUniqueBlackouts")) {
                event.setCanceled(true);
                return;
            }
        }
        else if (Blackouts.DROP_BLACKOUTS) {
            BookHelper.markBookAsFound(event.entityPlayer, id);
            TickHandler.unloadBlackouts(event.entityPlayer.worldObj);
        }
        if (!event.item.worldObj.isRemote && BookHelper.removeBookId(book)) {
            event.item.setEntityItemStack(book);
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
        if (Properties.getBoolean(Properties.GENERAL, "lostBookCaptureRate") && !event.isCanceled()) {
            ItemStack book = event.entityItem.getEntityItem();
            if (book.getItem().getUnlocalizedName(book).equals("item.writtenBook") && !BookHelper.hasBookId(book)) {
                Library.LOST_BOOKS.capture(book);
                if (!event.entityItem.worldObj.isRemote) {
                    event.entityItem.setEntityItemStack(book);
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
        if (event.world != null && !event.world.isRemote) {
            TickHandler.unloadBlackouts(event.world);
        }
    }
}