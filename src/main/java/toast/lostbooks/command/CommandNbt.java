package toast.lostbooks.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandNbt extends CommandBase {
    /// The command name.
    @Override
    public String getName() {
        return "lbnbt";
    }

    /**
     * Return the required permission level for this command.
     */
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /// Returns the help string.
    @Override
    public String getUsage(ICommandSender sender) {
        return "/lbnbt - prints the NBT tag of the item in your main hand.";
    }

    /// Executes the command.
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack stack = player.getHeldItemMainhand();

        if (stack.isEmpty()) {
            sender.sendMessage(new TextComponentString("You are not holding any item in your main hand."));
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || tag.getKeySet().isEmpty()) {
            sender.sendMessage(new TextComponentString("Held item has no NBT tag."));
            return;
        }

        sender.sendMessage(new TextComponentString(tag.toString()));
    }
}


