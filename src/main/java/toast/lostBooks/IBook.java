package toast.lostBooks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public interface IBook {
    /// Returns the relative file name for this book, including the file extension, if there is one.
    String getName();

    /// Returns the id for this book, if there is one.
    String getId();

    /// Returns book stats to be written to a book, if possible.
    BookStats getBookStats(EntityLivingBase entity);

    /// Returns book stats to be written to a book, if possible.
    BookStats getBookStatsSpawn(EntityPlayer player);

    /// Returns book stats to be written to a book.
    BookStats getBookStatsTrade(EntityLivingBase entity);

    /// Returns true if any books can be dropped by the entity.
    boolean isValid(EntityLivingBase entity);

    /// Returns true if any books can be spawned for the player (including looted dusty books).
    boolean isValidSpawn(EntityPlayer player);

    /// Returns true if any books can be sold by the entity.
    boolean isValidTrade(EntityLivingBase entity);

    /// Returns the weight of this object.
    int getWeight();

    /// Returns the number of stories included in this book.
    int size();
}