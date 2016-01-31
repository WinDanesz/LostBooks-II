package toast.lostBooks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class BookStats implements IBook {
    /// The file name for this book.
    public final String fileName;
    /// Used by unique books to be marked as found. Otherwise, this is null.
    public final String bookId;
    /// The book's title and author.
    public final String title, author;
    /// The book's pages.
    public final String[] pages;

    /// The weight of this item.
    public final int weight;
    /// List of basic entity classes this can drop from.
    public final ArrayList<Class> whitelist;
    /// List of exact entity classes this can NOT drop from.
    public final ArrayList<Class> blacklist;
    /// Whether this book can be sold by librarian villagers.
    public final boolean canBuy;
    /// Set of biome ids this book can drop in. If null, this is ignored.
    public final HashSet<Integer> biomes;

    /// Used only for dummy instances. Never use for stored books.
    public BookStats(String name, String t, String a, String... text) {
        this.fileName = name;
        this.bookId = null;
        this.title = t == null ? "Untitled" : t;
        this.author = a == null ? "Anonymous" : a;
        this.pages = text == null ? new String[0] : text;

        this.weight = 100;
        this.whitelist = null;
        this.blacklist = null;
        this.canBuy = true;
        this.biomes = null;
    }

    /// This generates almost all book instances in this mod.
    public BookStats(BookCollection parentCollection, File file, BookProperties properties, boolean unique, String t, String a, String... text) {
        this.fileName = file.getName();
        this.bookId = unique ? parentCollection.getId() + this.fileName.substring(0, this.fileName.length() - 5) : null;
        this.title = t == null ? "Untitled" : t;
        this.author = a == null ? "Anonymous" : a;
        this.pages = text == null ? new String[0] : text;

        ArrayList<Class> whitelistDefault = parentCollection.whitelist;
        ArrayList<Class> blacklistDefault = parentCollection.blacklist;
        boolean canBuyDefault = parentCollection.canBuy;
        HashSet<Integer> biomesDefault = parentCollection.biomes;
        if (properties != null) {
            this.weight = Math.max(0, properties.getInt("weight", 100));
            this.whitelist = properties.getEntityList("whitelist", whitelistDefault);
            this.blacklist = properties.getEntityList("blacklist", blacklistDefault);
            this.canBuy = properties.getBoolean("can_buy", canBuyDefault);
            this.biomes = properties.getIdSet("biomes", biomesDefault);
        }
        else {
            this.weight = 100;
            this.whitelist = whitelistDefault;
            this.blacklist = blacklistDefault;
            this.canBuy = canBuyDefault;
            this.biomes = biomesDefault;
        }
    }

    /// Writes these BookStats to the book and returns it.
    public ItemStack writeTo(ItemStack book) {
        BookHelper.setTitleAndAuthor(book, this.bookId, this.title, this.author);
        BookHelper.setPages(book, this.pages);
        return book;
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
        if (this.bookId != null && Blackouts.DROP_BLACKOUTS && Blackouts.ADAPTIVE_DROPS && BookHelper.getBlackouts(entity.worldObj).isBlackedOut(this.bookId))
            return null;
        return this;
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStatsSpawn(EntityPlayer player) {
        if (this.bookId != null && Blackouts.DROP_BLACKOUTS && Blackouts.ADAPTIVE_DROPS && BookHelper.isBookFound(player, this.bookId))
            return null;
        return this;
    }

    /// Returns book stats to be written to a book.
    @Override
    public BookStats getBookStatsTrade(EntityLivingBase entity) {
        return this;
    }

    /// Returns true if any books can be dropped by the entity.
    @Override
    public boolean isValid(EntityLivingBase entity) {
        if (this.bookId != null && Blackouts.DROP_BLACKOUTS && !Blackouts.ADAPTIVE_DROPS && BookHelper.getBlackouts(entity.worldObj).isBlackedOut(this.bookId))
            return false;
        return this.getWeight() > 0 && BookHelper.canBookDrop(entity, this.whitelist, this.blacklist, this.biomes);
    }

    /// Returns true if any books can be spawned for the player (including looted dusty books).
    @Override
    public boolean isValidSpawn(EntityPlayer player) {
        if (this.bookId != null && Blackouts.DROP_BLACKOUTS && !Blackouts.ADAPTIVE_DROPS && BookHelper.isBookFound(player, this.bookId))
            return false;
        return this.getWeight() > 0;
    }

    /// Returns true if any books can be sold by the entity.
    @Override
    public boolean isValidTrade(EntityLivingBase entity) {
        return this.canBuy && this.getWeight() > 0;
    }

    /// Returns the weight of this object.
    @Override
    public int getWeight() {
        return this.weight;
    }

    /// Returns the number of stories included in this book.
    @Override
    public int size() {
        return 1;
    }
}