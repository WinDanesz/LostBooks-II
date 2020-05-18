package toast.lostBooks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import toast.lostBooks.book.BookStats;
import toast.lostBooks.book.IBook;
import toast.lostBooks.config.PropertyHelper;
import toast.lostBooks.helper.BookHelper;
import toast.lostBooks.helper.FileHelper;
import toast.lostBooks.helper.RandomHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class LostBookCollection implements IBook {
    /// Form Feed [FF]. Char value is 12.
    public static final String F = "\f";

    /// The folder containing the lost books.
    public static File BOOK_DIRECTORY;

    public LostBookCollection(String category) {
        // Do nothing.
    }

    /// Called when a book is "lost" to save it. It is assumed the book is appropriate, for the most part.
    @SuppressWarnings("resource")
    public void capture(ItemStack book) {
        if (book == null || book.hasTagCompound())
            return;
        try {
            String fileName = "";
            for (char letter : book.getTagCompound().getString("title").toCharArray())
                if (Character.isLetterOrDigit(letter)) {
                    fileName += Character.toString(letter);
                }
            File save = new File(LostBookCollection.BOOK_DIRECTORY, fileName + ".dat");
            if (save.exists()) {
                int attempt = 0;
                for (; attempt < 100; attempt++)
                    if (! (save = new File(LostBookCollection.BOOK_DIRECTORY, fileName + attempt + ".dat")).exists()) {
                        break;
                    }
                if (attempt > 99)
                    return;
                fileName += attempt;
            }
            CompressedStreamTools.writeCompressed(book.writeToNBT(new NBTTagCompound()), new FileOutputStream(save));
            book.setCount(0);
        }
        catch (Exception ex) {
            LostBooks.console("Failed to capture lost book!");
            ex.printStackTrace();
        }
    }

    /// Loads all lost books.
    private IBook[] loadBooks() {
        try {
            File[] bookFiles = LostBookCollection.BOOK_DIRECTORY.listFiles(new FileHelper.ExtensionFilter(".dat"));
            ArrayList<IBook> books = new ArrayList<IBook>(bookFiles.length);
            for (File file : bookFiles) {
                try {
                    new FileInputStream(file).close();
                    books.add(new LostBookStats(file));
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return books.toArray(new IBook[0]);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new IBook[0];
    }

    /// Returns the relative file name for this book, including the file extension, if there is one.
    @Override
    public String getName() {
        return LostBookCollection.BOOK_DIRECTORY.getName();
    }

    /// Returns the id for this book, if there is one.
    @Override
    public String getId() {
        return null;
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStats(EntityLivingBase entity) {
        IBook book = RandomHelper.choose(entity, this.loadBooks());
        return book == null ? null : book.getBookStats(entity);
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStatsSpawn(EntityPlayer player) {
        IBook book = RandomHelper.chooseSpawn(player, this.loadBooks());
        return book == null ? null : book.getBookStatsSpawn(player);
    }

    /// Returns book stats to be written to a book.
    @Override
    public BookStats getBookStatsTrade(EntityLivingBase entity) {
        IBook book = RandomHelper.chooseTrade(entity, this.loadBooks());
        return book == null ? null : book.getBookStatsTrade(entity);
    }

    /// Returns true if any books can be dropped by the entity.
    @Override
    public boolean isValid(EntityLivingBase entity) {
        return this.getWeight() > 0 && this.size() > 0 && BookHelper.canBookDrop(entity, BookProperties.getEntityList("LostBooks.cfg", PropertyHelper.getString(PropertyHelper.LOST_BOOKS, "whitelist")), BookProperties.getEntityList("LostBooks.cfg", PropertyHelper.getString(PropertyHelper.LOST_BOOKS, "blacklist")), BookProperties.getIdSet("LostBooks.cfg", PropertyHelper.getString(PropertyHelper.LOST_BOOKS, "biomes")));
    }

    /// Returns true if any books can be spawned for the player (including looted dusty books).
    @Override
    public boolean isValidSpawn(EntityPlayer player) {
        return this.getWeight() > 0 && this.size() > 0;
    }

    /// Returns true if any books can be sold by the entity.
    @Override
    public boolean isValidTrade(EntityLivingBase entity) {
        return this.getWeight() > 0 && this.size() > 0;
    }

    /// Returns the weight of this object.
    @Override
    public int getWeight() {
        return PropertyHelper.getInt(PropertyHelper.GENERAL, "lostBookWeight");
    }

    /// Returns the number of stories included in this book.
    @Override
    public int size() {
        try {
            //nullpointer error
            return LostBookCollection.BOOK_DIRECTORY.listFiles(new FileHelper.ExtensionFilter(".dat")).length;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }
}