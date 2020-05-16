//package toast.lostBooks;
//
//import net.minecraft.command.CommandBase;
//import net.minecraft.command.ICommandSender;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.util.ChatComponentText;
//
//public class CommandBlackouts extends CommandBase {
//    /// The command name.
//    @Override
//    public String getCommandName() {
//        return "lbreset";
//    }
//
//    /**
//     * Return the required permission level for this command.
//     */
//    @Override
//    public int getRequiredPermissionLevel() {
//        return 2;
//    }
//
//    /// Returns the help string.
//    @Override
//    public String getCommandUsage(ICommandSender sender) {
//        return "/lbreset <player> - clears the target player's unique book blackouts.";
//    }
//
//    /// Executes the command.
//    @Override
//    public void processCommand(ICommandSender sender, String[] args) {
//        EntityPlayerMP player = args.length > 0 ? CommandBase.getPlayer(sender, args[0]) : CommandBase.getCommandSenderAsPlayer(sender);
//
//        FileHelper.clearBookData(player.getCommandSenderName());
//        player.addChatMessage(new ChatComponentText("Your unique book blackouts have been reset."));
//        if (player != sender) {
//            sender.addChatMessage(new ChatComponentText("You have reset " + player.getCommandSenderName() + "\'s unique book blackouts."));
//        }
//    }
//}
