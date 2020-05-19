package toast.lostBooks.book;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import toast.lostBooks.trade.VillagerBookTrades;

public class CommonProxy {

	public void registerVillagerTrades() {
		VillagerRegistry.VillagerCareer LIBRARIAN = ForgeRegistries.VILLAGER_PROFESSIONS.getValue(new ResourceLocation("minecraft:librarian")).getCareer(0);
		LIBRARIAN.addTrade(1, new VillagerBookTrades());
	}
}
