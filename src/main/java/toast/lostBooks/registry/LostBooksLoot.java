package toast.lostBooks.registry;

import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import toast.lostBooks.config.ConfigPropertyHelper;

/**
 * Class responsible for handling loot injection.
 *
 * @author Win Danesz
 * @since LostBooks 2.0
 */

@Mod.EventBusSubscriber
public class LostBooksLoot {
	private static final boolean ADD_CHEST_LOOT = ConfigPropertyHelper.getBoolean(ConfigPropertyHelper.GENERAL, "addChestLoot");

	private LostBooksLoot() {} // no instances!

	@SubscribeEvent
	public static void onLootTableLoadEvent(LootTableLoadEvent event) {
		if (ADD_CHEST_LOOT) {
			if (event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT)) {
				final LootPool pool2 = event.getTable().getPool("pool2");
				if (pool2 != null) {
					pool2.addEntry(new LootEntryItem(LostBooksItems.random_book, 5, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
			if (event.getName().equals(LootTableList.CHESTS_DESERT_PYRAMID)) {
				final LootPool pool1 = event.getTable().getPool("pool1");
				if (pool1 != null) {
					pool1.addEntry(new LootEntryItem(LostBooksItems.random_book, 10, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
			if (event.getName().equals(LootTableList.CHESTS_JUNGLE_TEMPLE)) {
				final LootPool main = event.getTable().getPool("main");
				if (main != null) {
					main.addEntry(new LootEntryItem(LostBooksItems.random_book, 3, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
			if (event.getName().equals(LootTableList.CHESTS_STRONGHOLD_CORRIDOR)) {
				final LootPool main = event.getTable().getPool("main");
				if (main != null) {
					main.addEntry(new LootEntryItem(LostBooksItems.random_book, 5, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
			if (event.getName().equals(LootTableList.CHESTS_STRONGHOLD_LIBRARY)) {
				final LootPool main = event.getTable().getPool("main");
				if (main != null) {
					main.addEntry(new LootEntryItem(LostBooksItems.random_book, 20, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
			if (event.getName().equals(LootTableList.CHESTS_STRONGHOLD_CROSSING)) {
				final LootPool main = event.getTable().getPool("main");
				if (main != null) {
					main.addEntry(new LootEntryItem(LostBooksItems.random_book, 5, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
			if (event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON)) {
				final LootPool pool2 = event.getTable().getPool("pool2");
				if (pool2 != null) {
					pool2.addEntry(new LootEntryItem(LostBooksItems.random_book, 10, 0, new LootFunction[0], new LootCondition[0], "lostbooks_random_book"));
				}
			}
		}
	}
}
