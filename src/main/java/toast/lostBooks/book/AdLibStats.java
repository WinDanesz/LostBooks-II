package toast.lostBooks.book;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import toast.lostBooks.BookProperties;
import toast.lostBooks.helper.AdLibHelper;
import toast.lostBooks.helper.BookHelper;

public class AdLibStats implements IBook {
    /// The file for this ad lib.
    public final File bookFile;

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

    public AdLibStats(BookCollection parentCollection, File file, BookProperties properties) {
        this.bookFile = file;

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

    /// Returns the relative file name for this book, including the file extension, if there is one.
    @Override
    public String getName() {
        return this.bookFile.getName();
    }

    /// Returns the id for this book, if there is one.
    @Override
    public String getId() {
        return null;
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStats(EntityLivingBase entity) {
        return AdLibHelper.generate(entity, this.bookFile);
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStatsSpawn(EntityPlayer player) {
        return this.getBookStats(player);
    }

    /// Returns book stats to be written to a book, if possible.
    @Override
    public BookStats getBookStatsTrade(EntityLivingBase entity) {
        return this.getBookStats(entity);
    }

    /// Returns true if any books can be dropped by the entity.
    @Override
    public boolean isValid(EntityLivingBase entity) {
        return this.getWeight() > 0 && BookHelper.canBookDrop(entity, this.whitelist, this.blacklist, this.biomes);
    }

    /// Returns true if any books can be spawned for the player (including looted dusty books).
    @Override
    public boolean isValidSpawn(EntityPlayer player) {
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