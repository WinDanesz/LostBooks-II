package toast.lostBooks.trade;

import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import toast.lostBooks.book.Library;
import toast.lostBooks.config.ConfigPropertyHelper;
import toast.lostBooks.registry.LostBooksItems;

import java.util.Random;

public class VillagerBookTrades implements EntityVillager.ITradeList {

	@Override
	public void addMerchantRecipe(IMerchant merchant, MerchantRecipeList recipeList, Random random) {
		// a random book:
		final int[] bookCost = new int[2];
		bookCost[0] = Math.max(1, Math.min(64, ConfigPropertyHelper.getInt(ConfigPropertyHelper.TRADING, "minBookCost")));
		bookCost[1] = Math.max(bookCost[0], Math.min(64, ConfigPropertyHelper.getInt(ConfigPropertyHelper.TRADING, "maxBookCost")));
		ItemStack book = Library.nextTradeBook(merchant.getCustomer());
		if (book != null) {
			recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, bookCost[0] == bookCost[1] ? bookCost[0] : random.nextInt(bookCost[1] - bookCost[0]) + bookCost[0]), book));
		}

		// dusty book:
		if (ConfigPropertyHelper.getBoolean(ConfigPropertyHelper.TRADING, "sellDustyBook")) {
			ItemStack itemstack = new ItemStack(LostBooksItems.random_book);
			int j = 7 + random.nextInt(5);

			if (j > 64) {
				j = 64;
			}
			recipeList.add(new MerchantRecipe(new ItemStack(Items.EMERALD, j), itemstack));

		}

	}
}