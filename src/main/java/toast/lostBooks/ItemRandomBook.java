package toast.lostBooks;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemRandomBook extends Item {
    /**
     * Allows items to add custom lines of information to the mouseover description.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean detailedInfo) {
        list.add("\u00a77Right-click to dust off");
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed.
     */
    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            ItemStack newBook = Library.nextSpawnBook(player);
            if (newBook == null)
                return itemStack;

            String id = BookHelper.getBookId(newBook);
            if (id == null || id == "") {
                // Do nothing.
            }
            else if (Blackouts.DROP_BLACKOUTS) {
                BookHelper.markBookAsFound(player, id);
                TickHandler.unloadBlackouts(world);
            }
            BookHelper.removeBookId(newBook);
            if (!player.capabilities.isCreativeMode) {
                itemStack.stackSize--;
            }
            if (itemStack.stackSize <= 0)
                return newBook;
            EntityItem entityBook = player.entityDropItem(newBook, 0.0F);
            entityBook.delayBeforeCanPickup = 0;
            entityBook.func_145797_a(player.getCommandSenderName());
        }
        return itemStack;
    }
}
