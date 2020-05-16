//package toast.lostBooks;
//
//import java.io.File;
//import java.util.regex.Pattern;
//
//import net.minecraft.command.CommandBase;
//import net.minecraft.command.ICommandSender;
//import net.minecraft.entity.item.EntityItem;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.ChatComponentText;
//
//public class CommandTest extends CommandBase {
//    // Control code pattern to strip book titles of formatting. Will not strip \u00a7k.
//    private static final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FL-OR]");
//    // Dummy collection to load books with.
//    private static final BookCollection dummyCollection = new BookCollection();
//
//    /// The command name.
//    @Override
//    public String getCommandName() {
//        return "lbspawn";
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
//        return "/lbspawn <filepath> <player> - generates and spawns the book from the given file path.";
//    }
//
//    /// Executes the command.
//    @Override
//    public void processCommand(ICommandSender sender, String[] args) {
//        if (args.length < 1) {
//            sender.addChatMessage(new ChatComponentText("Invalid number of arguments."));
//            sender.addChatMessage(new ChatComponentText(this.getCommandUsage(sender)));
//        }
//        else {
//            EntityPlayerMP player = args.length > 1 ? CommandBase.getPlayer(sender, args[1]) : CommandBase.getCommandSenderAsPlayer(sender);
//
//            IBook book = FileHelper.loadBook(args[0].startsWith("adLib/") ? "adlib" : "", CommandTest.dummyCollection, _LostBooks.CONFIG_DIRECTORY, new File(_LostBooks.CONFIG_DIRECTORY, "/LostBooks/" + args[0] + ".book"));
//            if (book == null) {
//                sender.addChatMessage(new ChatComponentText("Invalid book file path. (" + args[0] + ")"));
//                sender.addChatMessage(new ChatComponentText("Book path should resemble \"unique/collectionName/bookName\"."));
//            }
//            else {
//                ItemStack bookStack = new ItemStack(Items.written_book);
//                BookStats story = book.getBookStats(player);
//                if (story != null) {
//                    story.writeTo(bookStack);
//                    if (bookStack.stackTagCompound != null) {
//                        if (args[0].startsWith("unique/") && Properties.getBoolean(Properties.GENERAL, "markUnique")) {
//                            BookHelper.addItemText(bookStack, "\u00a79Unique");
//                        }
//                        String title = CommandTest.patternControlCode.matcher(BookHelper.getTitle(bookStack)).replaceAll("");
//                        BookHelper.removeBookId(bookStack);
//                        EntityItem entityBook = player.entityDropItem(bookStack, 0.0F);
//                        entityBook.delayBeforeCanPickup = 0;
//                        entityBook.func_145797_a(player.getCommandSenderName());
//                        sender.addChatMessage(new ChatComponentText("Giving " + player.getCommandSenderName() + " a copy of \u00a7o" + title + "\u00a7r."));
//                    }
//                }
//            }
//        }
//    }
//}
