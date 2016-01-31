package toast.lostBooks;

import java.util.ArrayList;
import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;

public abstract class BookHelper {
    /// Returns true if the book has a book id.
    public static boolean hasBookId(ItemStack book) {
        return book.stackTagCompound != null && book.stackTagCompound.hasKey("LB|ID");
    }

    /// Gets the book's id, returns null if no id is found.
    public static String getBookId(ItemStack book) {
        if (!BookHelper.hasBookId(book))
            return null;
        return book.stackTagCompound.getString("LB|ID");
    }

    /// Removes the book's id, if any. Returns true if the book is changed.
    public static boolean removeBookId(ItemStack book) {
        if (BookHelper.hasBookId(book)) {
            book.stackTagCompound.removeTag("LB|ID");
            return true;
        }
        return false;
    }

    /// Sets the book's id, title, and author.
    public static void setTitleAndAuthor(ItemStack book, String id, String title, String author) {
        if (book.stackTagCompound == null) {
            book.setTagCompound(new NBTTagCompound());
        }
        book.stackTagCompound.setString("LB|ID", id == null ? "" : id);
        book.stackTagCompound.setString("title", title);
        if (author != "") {
            book.stackTagCompound.setString("author", author);
        }
    }

    public static void setTitleAndAuthor(ItemStack book, String title, String author) {
        if (book.stackTagCompound == null) {
            book.setTagCompound(new NBTTagCompound());
        }
        book.stackTagCompound.setString("title", title);
        if (author != "") {
            book.stackTagCompound.setString("author", author);
        }
    }

    public static void setTitle(ItemStack book, String title) {
        if (book.stackTagCompound == null) {
            book.setTagCompound(new NBTTagCompound());
        }
        book.stackTagCompound.setString("title", title);
    }

    public static void setAuthor(ItemStack book, String author) {
        if (author == "")
            return;
        if (book.stackTagCompound == null) {
            book.setTagCompound(new NBTTagCompound());
        }
        book.stackTagCompound.setString("author", author);
    }

    /// Returns the book's title.
    public static String getTitle(ItemStack book) {
        if (book.stackTagCompound == null)
            return "Untitled Book";
        return book.stackTagCompound.getString("title");
    }

    /// Sets the book's content.
    public static void setPages(ItemStack book, String... pages) {
        if (book.stackTagCompound == null) {
            book.setTagCompound(new NBTTagCompound());
        }
        book.stackTagCompound.setTag("pages", new NBTTagList());
        NBTTagList pagesTag = book.stackTagCompound.getTagList("pages", 8);
        for (String page : pages) {
            pagesTag.appendTag(new NBTTagString(page));
        }
    }

    /// Writes the given text to the book's tool tip.
    public static void addItemText(ItemStack book, String... text) {
        if (book.stackTagCompound == null) {
            book.setTagCompound(new NBTTagCompound());
        }
        if (!book.stackTagCompound.hasKey("display")) {
            book.stackTagCompound.setTag("display", new NBTTagCompound());
        }
        NBTTagCompound tag = book.stackTagCompound.getCompoundTag("display");
        if (!tag.hasKey("Lore")) {
            tag.setTag("Lore", new NBTTagList());
        }
        for (String line : text) {
            tag.getTagList("Lore", 8).appendTag(new NBTTagString(line));
        }
    }

    /// Returns the blackouts for the given world.
    public static Blackouts getBlackouts(World world) {
        return TickHandler.getOrCreateBlackouts(world);
    }

    /// Returns the list of books the player has found.
    public static HashSet<String> getBookData(EntityPlayer player) {
        if (player == null)
            return new HashSet<String>(0);
        return FileHelper.loadBookData(player.getCommandSenderName());
    }

    /// Returns true if the book has already been found by the player.
    public static boolean isBookFound(EntityPlayer player, String id) {
        return BookHelper.getBookData(player).contains(id);
    }

    /// Saves the book id to the player to denote that the book has been picked up once.
    public static void markBookAsFound(EntityPlayer player, String id) {
        FileHelper.addBookData(player.getCommandSenderName(), id);
    }

    /// Returns true if a book with the given properties can be dropped by the entity.
    public static boolean canBookDrop(EntityLivingBase entity, ArrayList<Class> whitelist, ArrayList<Class> blacklist, HashSet<Integer> biomes) {
        // Check for valid biome
        if (biomes != null && !biomes.contains(Integer.valueOf(entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).biomeID)))
            return false;
        // Check for valid mob
        Class entityClass = entity.getClass();
        if (blacklist != null) {
            for (Class listedClass : blacklist)
                if (listedClass.equals(entityClass))
                    return false;
        }
        if (whitelist == null)
            return IMob.class.isAssignableFrom(entityClass) || INpc.class.isAssignableFrom(entityClass) || IMerchant.class.isAssignableFrom(entityClass);
        for (Class listedClass : whitelist)
            if (listedClass.isAssignableFrom(entityClass))
                return true;
        return false;
    }

    /// Gets the book's last recorded current page.
    public static int getCurrentPage(ItemStack book) {
        if (book.stackTagCompound == null)
            return 0;
        return book.stackTagCompound.getInteger("LB|CP");
    }

    /// Sets the book's current page.
    public static void setCurrentPage(ItemStack book, int currPage) {
        if (book.stackTagCompound == null) {
            book.stackTagCompound = new NBTTagCompound();
        }
        if (currPage <= 0) {
            book.stackTagCompound.removeTag("LB|CP");
        }
        else {
            book.stackTagCompound.setInteger("LB|CP", currPage);
        }
    }

}