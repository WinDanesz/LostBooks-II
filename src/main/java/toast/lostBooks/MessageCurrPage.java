package toast.lostBooks;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage; // import cpw.mods.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler; // import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext; //import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import toast.lostBooks.helper.BookHelper;

public class MessageCurrPage implements IMessage {
    // The page the book is open to.
    public int currPage;

    public MessageCurrPage() {
    }

    public MessageCurrPage(int currPage) {
        this.currPage = currPage;
    }

    /*
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#fromBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        this.currPage = buf.readInt();
    }

    /*
     * @see cpw.mods.fml.common.network.simpleimpl.IMessage#toBytes(io.netty.buffer.ByteBuf)
     */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.currPage);
    }

    public static class Handler implements IMessageHandler<MessageCurrPage, IMessage> {

        /*
         * @see cpw.mods.fml.common.network.simpleimpl.IMessageHandler#onMessage(cpw.mods.fml.common.network.simpleimpl.IMessage, cpw.mods.fml.common.network.simpleimpl.MessageContext)
         */
        @Override
        public IMessage onMessage(MessageCurrPage message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            ItemStack book = player.inventory.getCurrentItem();
            if (book != null && (book.getItem() == Items.WRITTEN_BOOK || book.getItem() == Items.WRITABLE_BOOK)) {
                BookHelper.setCurrentPage(book, message.currPage);
            }
            return null;
        }

    }
}
