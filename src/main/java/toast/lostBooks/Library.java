package toast.lostBooks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public abstract class Library {
    /// Line Feed [LF]. Normally typed with \n.
    public static final char N = 0x000a;
    /// Form Feed [FF]. Normally typed with \f.
    public static final char F = 0x000c;
    /// Carriage Return [CR]. Normally typed with \r.
    public static final char R = 0x000d;
    /// Section Symbol [S]. Normally typed with \u00a7.
    public static final char S = 0x00a7;

    /// Collection of all registered unique books.
    public static final IBook UNIQUE_BOOKS = new BookCollection("unique", new File(_LostBooks.CONFIG_DIRECTORY, "/LostBooks/unique"));
    /// Collection of all registered common books.
    public static final IBook COMMON_BOOKS = new BookCollection("common", new File(_LostBooks.CONFIG_DIRECTORY, "/LostBooks/common"));
    /// Collection of all registered ad lib books.
    public static final IBook AD_LIB_BOOKS = new BookCollection("adlib", new File(_LostBooks.CONFIG_DIRECTORY, "/LostBooks/adLib"));
    /// Collection representing lost books.
    public static final LostBookCollection LOST_BOOKS = new LostBookCollection("lost");
    /// Number of unique books, so it doesn't need to be calculated every time.
    public static final int UNIQUE_BOOK_COUNT = Library.UNIQUE_BOOKS.size();

    /// Returns the next book to be dropped.
    public static ItemStack nextBook(EntityLivingBase entity) {
        IBook category = RandomHelper.choose(entity, Library.UNIQUE_BOOKS, Library.COMMON_BOOKS, Library.AD_LIB_BOOKS, Library.LOST_BOOKS);
        if (category != null) {
            ItemStack book = new ItemStack(Items.written_book);
            BookStats story = category.getBookStats(entity);
            if (category == Library.UNIQUE_BOOKS) {
                if (story == null) {
                    category = RandomHelper.choose(entity, Library.COMMON_BOOKS, Library.AD_LIB_BOOKS, Library.LOST_BOOKS);
                    if (category != null) {
                        story = category.getBookStats(entity);
                    }
                }
                else if (Properties.getBoolean(Properties.GENERAL, "markUnique")) {
                    BookHelper.addItemText(book, "\u00a79Unique");
                }
            }
            if (story != null) {
                story.writeTo(book);
                if (book.stackTagCompound != null)
                    return book;
            }
        }
        return null;
    }

    /// Returns the next book to be spawned.
    public static ItemStack nextSpawnBook(EntityPlayer player) {
        IBook category = RandomHelper.chooseSpawn(player, Library.UNIQUE_BOOKS, Library.COMMON_BOOKS, Library.AD_LIB_BOOKS, Library.LOST_BOOKS);
        if (category != null) {
            ItemStack book = new ItemStack(Items.written_book);
            BookStats story = category.getBookStatsSpawn(player);
            if (category == Library.UNIQUE_BOOKS) {
                if (story == null) {
                    category = RandomHelper.chooseSpawn(player, Library.COMMON_BOOKS, Library.AD_LIB_BOOKS, Library.LOST_BOOKS);
                    if (category != null) {
                        story = category.getBookStatsSpawn(player);
                    }
                }
                else if (Properties.getBoolean(Properties.GENERAL, "markUnique")) {
                    BookHelper.addItemText(book, "\u00a79Unique");
                }
            }
            if (story != null) {
                story.writeTo(book);
                if (book.stackTagCompound != null)
                    return book;
            }
        }
        return null;
    }

    /// Returns the next book for a villager's trade inventory.
    public static ItemStack nextTradeBook(EntityLivingBase entity) {
        ArrayList<IBook> tradeableBooks = new ArrayList<IBook>(4);
        tradeableBooks.add(Library.COMMON_BOOKS);
        tradeableBooks.add(Library.AD_LIB_BOOKS);
        if (Properties.getBoolean(Properties.TRADING, "sellUnique")) {
            tradeableBooks.add(Library.UNIQUE_BOOKS);
        }
        if (Properties.getBoolean(Properties.TRADING, "sellLost")) {
            tradeableBooks.add(Library.LOST_BOOKS);
        }
        IBook category = RandomHelper.chooseTrade(entity, tradeableBooks.toArray(new IBook[0]));
        if (category != null) {
            BookStats story = category.getBookStatsTrade(entity);
            if (story != null) {
                ItemStack book = story.writeTo(new ItemStack(Items.written_book));
                BookHelper.removeBookId(book);
                return book;
            }
        }
        return null;
    }

    /// Returns a list of all unique book ids.
    public static HashSet<String> getUniqueIds() {
        HashSet<String> ids = new HashSet<String>(Library.UNIQUE_BOOK_COUNT);
        Library.addCollectionIds(ids, (BookCollection) Library.UNIQUE_BOOKS);
        return ids;
    }

    private static void addCollectionIds(HashSet<String> ids, BookCollection collection) {
        for (IBook book : collection.books) {
            if (book instanceof BookCollection) {
                Library.addCollectionIds(ids, (BookCollection) book);
            }
            else {
                ids.add(book.getId());
            }
        }
    }
}