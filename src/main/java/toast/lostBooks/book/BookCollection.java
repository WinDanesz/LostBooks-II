package toast.lostBooks.book;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import toast.lostBooks.config.PropertyHelper;
import toast.lostBooks.helper.FileHelper;
import toast.lostBooks.helper.RandomHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class BookCollection implements IBook {
    /// The file name for this book.
    public final String fileName;
    /// Used by unique books to be marked as found. Otherwise, this is null.
    public final String bookId;
    /// Used by the major book categories to return the config weight. Otherwise, this is null.
    public final String categoryId;
    /// List of registered books in this collection.
    public final IBook[] books;

    /// The weight of this item.
    public final int weight;
    /// List of basic entity classes this can drop from.
    public final ArrayList<Class> whitelist;
    /// List of exact entity classes this can NOT drop from.
    public final ArrayList<Class> blacklist;
    /// Whether this book can be sold by librarian villagers.
    public final boolean canBuy;
    /// Whether the books in this collection drop in order.
    public final boolean inOrder;
    /// Set of biome ids this book can drop in. If null, this is ignored.
    public final HashSet<Integer> biomes;

    /// This is used only for dummy collections. Never use for anything important.
    public BookCollection() {
        this.fileName = "dummy";
        this.bookId = null;
        this.categoryId = "dummy";

        this.weight = -1;
        this.whitelist = null;
        this.blacklist = null;
        this.canBuy = true;
        this.inOrder = false;
        this.biomes = null;
        this.books = new IBook[0];
    }

    /// This is called to initialize each major category (common, unique, etc.).
    public BookCollection(String category, File folder) {
        this.fileName = folder.getName();
        this.bookId = category == "unique" ? "" : null;
        this.categoryId = category;

        this.weight = -1;
        this.whitelist = null;
        this.blacklist = null;
        this.canBuy = true;
        this.inOrder = category == "unique";
        this.biomes = null;
        this.books = FileHelper.loadCollection(category, this, folder);
    }

    /// This should only be called by loadCollection() in FileHelper.
    public BookCollection(String category, BookCollection parentCollection, File folder, BookProperties properties) {
        this.fileName = folder.getName();
        this.bookId = category == "unique" ? parentCollection.getId() + this.fileName + "/" : null;
        this.categoryId = null;

        ArrayList<Class> whitelistDefault = parentCollection.whitelist;
        ArrayList<Class> blacklistDefault = parentCollection.blacklist;
        boolean canBuyDefault = parentCollection.canBuy;
        boolean inOrderDefault = parentCollection.inOrder;
        HashSet<Integer> biomesDefault = parentCollection.biomes;
        if (properties != null) {
            this.weight = Math.max(0, properties.getInt("weight", 100));
            this.whitelist = properties.getEntityList("whitelist", whitelistDefault);
            this.blacklist = properties.getEntityList("blacklist", blacklistDefault);
            this.canBuy = properties.getBoolean("can_buy", canBuyDefault);
            this.inOrder = category == "unique" && properties.getBoolean("in_order", inOrderDefault);
            this.biomes = properties.getIdSet("biomes", biomesDefault);
        }
        else {
            this.weight = 100;
            this.whitelist = whitelistDefault;
            this.blacklist = blacklistDefault;
            this.canBuy = canBuyDefault;
            this.inOrder = category == "unique" && inOrderDefault;
            this.biomes = biomesDefault;
        }

        this.books = FileHelper.loadCollection(category, this, folder);
    }

    /// Returns the relative file name for this book, including the file extension, if there is one.
    @Override
    public String getName() {
        return this.fileName;
    }

    /// Returns the id for this book, if there is one.
    @Override
    public String getId() {
        return this.bookId;
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStats(EntityLivingBase entity) {
        if (this.inOrder) {
            // TO DO: order code
        }
        IBook book = RandomHelper.choose(entity, this.books);
        return book == null ? null : book.getBookStats(entity);
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStatsSpawn(EntityPlayer player) {
        if (this.inOrder) {
            // TO DO: order code
        }
        IBook book = RandomHelper.chooseSpawn(player, this.books);
        return book == null ? null : book.getBookStatsSpawn(player);
    }

    /// Returns book stats to be written to a book.
    @Override
    public BookStats getBookStatsTrade(EntityLivingBase entity) {
        IBook book = RandomHelper.chooseTrade(entity, this.books);
        return book == null ? null : book.getBookStatsTrade(entity);
    }

    /// Returns true if any books can be dropped by the entity.
    @Override
    public boolean isValid(EntityLivingBase entity) {
        for (IBook book : this.books)
            if (book != null && book.isValid(entity))
                return true;
        return false;
    }

    /// Returns true if any books can be spawned for the player (including looted dusty books).
    @Override
    public boolean isValidSpawn(EntityPlayer player) {
        for (IBook book : this.books)
            if (book != null && book.isValidSpawn(player))
                return true;
        return false;
    }

    /// Returns true if any books can be sold by the entity.
    @Override
    public boolean isValidTrade(EntityLivingBase entity) {
        for (IBook book : this.books)
            if (book != null && book.isValidTrade(entity))
                return true;
        return false;
    }

    /// Returns the weight of this object.
    @Override
    public int getWeight() {
        if (this.categoryId == null)
            return this.weight;
        return PropertyHelper.getInt(PropertyHelper.GENERAL, this.categoryId + "BookWeight");
    }

    /// Returns the number of stories included in this book.
    @Override
    public int size() {
        int size = 0;
        for (IBook book : this.books)
            if (book != null) {
                size += book.size();
            }
        return size;
    }
}