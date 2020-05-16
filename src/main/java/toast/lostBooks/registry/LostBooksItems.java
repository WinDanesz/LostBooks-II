package toast.lostBooks.registry;

import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import toast.lostBooks.LostBooks;
import toast.lostBooks.item.ItemRandomBook;

import javax.annotation.Nonnull;

@GameRegistry.ObjectHolder(LostBooks.MODID)
@Mod.EventBusSubscriber
public class LostBooksItems {
	private LostBooksItems() {} // no instances!

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder() { return null; }

	public static final Item random_book = placeholder();

	private static void registerItem(IForgeRegistry<Item> registry, String name, Item item) {
		item.setRegistryName(LostBooks.MODID, name);
		item.setTranslationKey(item.getRegistryName().toString());
		registry.register(item);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Item> event) {

		IForgeRegistry<Item> registry = event.getRegistry();

		registerItem(registry, "random_book", new ItemRandomBook());
	}
}
