package toast.lostBooks.client;

import net.minecraft.client.gui.GuiScreenBook;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import toast.lostBooks.config.ConfigPropertyHelper;
import toast.lostBooks.gui.GuiScreenBookUtil;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {

	public ClientEventHandler() {}

	/**
	 * Called by GuiScreen.?.
	 * GuiScreen gui = the gui being opened.
	 *
	 * @param event The event being triggered.
	 */
	@SubscribeEvent()
	public static void onGuiOpened(GuiOpenEvent event) {
		if (event.getGui() != null && event.getGui().getClass() == GuiScreenBook.class) {
			boolean bookmark = ConfigPropertyHelper.getBoolean(ConfigPropertyHelper.UTILS, "openToPreviousPage");
			boolean pauseGame = ConfigPropertyHelper.getBoolean(ConfigPropertyHelper.UTILS, "pauseWhileReading");
			if (bookmark || !pauseGame) {
				event.setGui(new GuiScreenBookUtil((GuiScreenBook) event.getGui(), bookmark, pauseGame));
			}
		}
	}
}
