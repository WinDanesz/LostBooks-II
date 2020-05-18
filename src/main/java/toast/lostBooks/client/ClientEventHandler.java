package toast.lostBooks.client;

import net.minecraft.client.gui.GuiScreenBook;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import toast.lostBooks.LostBooks;
import toast.lostBooks.config.PropertyHelper;
import toast.lostBooks.gui.GuiScreenBookUtil;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = LostBooks.MODID)
public class ClientEventHandler {

	//    public static final ClientEventHandler INSTANCE = new ClientEventHandler();

	public ClientEventHandler() {
		//        MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Called by GuiScreen.?.
	 * GuiScreen gui = the gui being opened.
	 *
	 * @param event The event being triggered.
	 */
	@SubscribeEvent()
	public static void onGuiOpened(GuiOpenEvent event) {
		System.out.println("called ");
		if (event.getGui() != null && event.getGui().getClass() == GuiScreenBook.class) {
			boolean bookmark = PropertyHelper.getBoolean(PropertyHelper.UTILS, "openToPreviousPage");
			boolean pauseGame = PropertyHelper.getBoolean(PropertyHelper.UTILS, "pauseWhileReading");
			if (bookmark || !pauseGame) {
				event.setGui(new GuiScreenBookUtil((GuiScreenBook) event.getGui(), bookmark, pauseGame));
			}
		}
	}
}
