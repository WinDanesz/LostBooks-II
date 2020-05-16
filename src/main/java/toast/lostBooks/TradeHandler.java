//package toast.lostBooks;
//
//import java.util.Random;
//
//import net.minecraft.entity.passive.EntityVillager;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.village.MerchantRecipe;
//import net.minecraft.village.MerchantRecipeList;
//import cpw.mods.fml.common.registry.VillagerRegistry;
//
//public class TradeHandler implements VillagerRegistry.IVillageTradeHandler {
//    public TradeHandler() {
//        /// 1 = Librarian
//        VillagerRegistry.instance().registerVillageTradeHandler(1, this);
//    }
//
//    /// Called when a villager is spawned to modify its trades.
//    @Override
//    public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
//        if (Properties.getBoolean(Properties.TRADING, "tradeChance", villager.getRNG())) {
//            final int[] bookCost = new int[2];
//            bookCost[0] = Math.max(1, Math.min(64, Properties.getInt(Properties.TRADING, "minBookCost")));
//            bookCost[1] = Math.max(bookCost[0], Math.min(64, Properties.getInt(Properties.TRADING, "maxBookCost")));
//            ItemStack book = Library.nextTradeBook(villager);
//            if (book != null) {
//                recipeList.add(new MerchantRecipe(new ItemStack(Items.emerald, bookCost[0] == bookCost[1] ? bookCost[0] : villager.getRNG().nextInt(bookCost[1] - bookCost[0]) + bookCost[0]), book));
//            }
//        }
//    }
//}