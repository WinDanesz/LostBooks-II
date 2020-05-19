package toast.lostBooks.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import toast.lostBooks.helper.FileHelper;

public class CommandBlackouts extends CommandBase {
	/// The command name.
    @Override
    public String getName() {
        return "lbreset";
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
        return "/lbreset <player> - clears the target player's unique book blackouts.";
    }

    /// Executes the command.
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerMP entityplayermp = args.length == 0 ? getCommandSenderAsPlayer(sender) : getPlayer(server, sender, args[0]);

        FileHelper.clearBookData(entityplayermp.getName());

		sender.sendMessage(new TextComponentTranslation("Your unique book blackouts have been reset."));
        if (entityplayermp != sender) {
			sender.sendMessage(new TextComponentTranslation("You have reset " + entityplayermp.getName() + "\'s unique book blackouts."));
        }
    }
}
