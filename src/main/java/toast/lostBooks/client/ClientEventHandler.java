package toast.lostBooks.client;

import net.minecraft.client.gui.GuiScreenBook;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import toast.lostBooks.Properties;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    public ClientEventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Called by GuiScreen.?.
     * GuiScreen gui = the gui being opened.
     * 
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onGuiOpened(GuiOpenEvent event) {
        if (event.gui != null && event.gui.getClass() == GuiScreenBook.class) {
            boolean bookmark = Properties.getBoolean(Properties.UTILS, "openToPreviousPage");
            boolean pauseGame = Properties.getBoolean(Properties.UTILS, "pauseWhileReading");
            if (bookmark || !pauseGame) {
                event.gui = new GuiScreenBookUtil((GuiScreenBook) event.gui, bookmark, pauseGame);
            }
        }
    }
}
