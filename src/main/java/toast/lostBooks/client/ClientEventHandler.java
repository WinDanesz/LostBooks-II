//package toast.lostBooks.client;
//
//import net.minecraft.client.gui.GuiScreenBook;
//import net.minecraftforge.client.event.GuiOpenEvent;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.eventhandler.EventPriority;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.relauncher.Side;
//import net.minecraftforge.fml.relauncher.SideOnly;
//import toast.lostBooks.Properties;
//
//@SideOnly(Side.CLIENT)
//public class ClientEventHandler {
//
//    public ClientEventHandler() {
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    /**
//     * Called by GuiScreen.?.
//     * GuiScreen gui = the gui being opened.
//     *
//     * @param event The event being triggered.
//     */
//    @SubscribeEvent(priority = EventPriority.LOW)
//    public void onGuiOpened(GuiOpenEvent event) {
//        if (event.getGui() != null && event.getGui().getClass() == GuiScreenBook.class) {
//            boolean bookmark = Properties.getBoolean(Properties.UTILS, "openToPreviousPage");
//            boolean pauseGame = Properties.getBoolean(Properties.UTILS, "pauseWhileReading");
//            if (bookmark || !pauseGame) {
//                event.setGui(new GuiScreenBookUtil((GuiScreenBook) event.getGui(), bookmark, pauseGame));
//            }
//        }
//    }
//}
