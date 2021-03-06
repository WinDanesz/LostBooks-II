package toast.lostBooks.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import toast.lostBooks.Blackouts;
import toast.lostBooks.TickHandler;
import toast.lostBooks.book.Library;
import toast.lostBooks.helper.BookHelper;

import javax.annotation.Nullable;
import java.util.List;

public class ItemRandomBook extends Item {

	public ItemRandomBook() {
		setCreativeTab(CreativeTabs.MISC);
		setMaxStackSize(16);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		tooltip.add("\u00a77Right-click to dust off"); //TODO move to lang keys
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		player.getCooldownTracker().setCooldown(this, 20);
		ItemStack itemstack = player.getHeldItem(hand);
		if (!world.isRemote) {
			ItemStack newBook = Library.nextSpawnBook(player);
			if (newBook == null)
				return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemstack);

			String id = BookHelper.getBookId(newBook);
			if (id == null || id == "") {
				// Do nothing.
			} else if (Blackouts.DROP_BLACKOUTS) {
				BookHelper.markBookAsFound(player, id);
				TickHandler.unloadBlackouts(world);
			}
			BookHelper.removeBookId(newBook);
			if (!player.capabilities.isCreativeMode) {
				itemstack.shrink(1);
			}
			if (itemstack.getCount() <= 0)
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, newBook);
			EntityItem entityBook = player.entityDropItem(newBook, 0.0F);
			entityBook.setPickupDelay(0);
			//			entityBook.func_145797_a(player.getCommandSenderEntity().getName()); // TODO: what was this?
		}
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
	}
}


