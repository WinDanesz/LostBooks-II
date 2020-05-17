//package toast.lostBooks.command;
//
//import java.io.File;
//import java.util.regex.Pattern;
//
//import net.minecraft.command.CommandBase;
//import net.minecraft.command.CommandException;
//import net.minecraft.command.ICommandSender;
//import net.minecraft.entity.item.EntityItem;
//import net.minecraft.entity.player.EntityPlayerMP;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.util.text.TextComponentString;
//import toast.lostBooks.LostBooks;
//import toast.lostBooks.book.BookCollection;
//import toast.lostBooks.book.BookStats;
//import toast.lostBooks.book.IBook;
//import toast.lostBooks.helper.BookHelper;
//import toast.lostBooks.helper.FileHelper;
//import toast.lostBooks.helper.PropertyHelper;
//
//public class CommandTest extends CommandBase {
//    // Control code pattern to strip book titles of formatting. Will not strip \u00a7k.
//    private static final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FL-OR]");
//    // Dummy collection to load books with.
//    private static final BookCollection dummyCollection = new BookCollection();
//
//	//////////////// new methods
//
//	/// The command name.
//	@Override
//	public String getName() {
//		return "lbspawn";
//	}
//
//	/**
//	 * Returns the help string.
//	 */
//	@Override
//	public String getUsage(ICommandSender sender) {
//		return "/lbspawn <filepath> <player> - generates and spawns the book from the given file path.";
//	}
//
//    /**
//     * Return the required permission level for this command.
//     */
//    @Override
//    public int getRequiredPermissionLevel() {
//        return 2;
//    }
////    public void processCommand(ICommandSender sender, String[] args) {
//
//    /// Executes the command.
//    @Override
//    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//        if (args.length < 1) {
//            sender.sendMessage(new TextComponentString("Invalid number of arguments."));
//            sender.sendMessage(new TextComponentString(this.getUsage(sender)));
//        }
//        else {
//            EntityPlayerMP player = args.length > 1 ? CommandBase.getPlayer(sender, args[1]) : CommandBase.getCommandSenderAsPlayer(sender);
//
//            IBook book = FileHelper.loadBook(args[0].startsWith("adLib/") ? "adlib" : "", CommandTest.dummyCollection, LostBooks.CONFIG_DIRECTORY, new File(_LostBooks.CONFIG_DIRECTORY, "/LostBooks/" + args[0] + ".book"));
//            if (book == null) {
//                sender.sendMessage(new TextComponentString("Invalid book file path. (" + args[0] + ")"));
//                sender.sendMessage(new TextComponentString("Book path should resemble \"unique/collectionName/bookName\"."));
//            }
//            else {
//                ItemStack bookStack = new ItemStack(Items.WRITTEN_BOOK);
//                BookStats story = book.getBookStats(player);
//                if (story != null) {
//                    story.writeTo(bookStack);
//                    if (bookStack.hasTagCompound()) {
//                        if (args[0].startsWith("unique/") && PropertyHelper.getBoolean(PropertyHelper.GENERAL, "markUnique")) {
//                            BookHelper.addItemText(bookStack, "\u00a79Unique");
//                        }
//                        String title = CommandTest.patternControlCode.matcher(BookHelper.getTitle(bookStack)).replaceAll("");
//                        BookHelper.removeBookId(bookStack);
//                        EntityItem entityBook = player.entityDropItem(bookStack, 0.0F);
//                        entityBook.setPickupDelay(0);
//                        entityBook.setOwner(player.getName());
//                        sender.sendMessage(new TextComponentString("Giving " + player.getName() + " a copy of \u00a7o" + title + "\u00a7r."));
//                    }
//                }
//            }
//        }
//    }
//}
