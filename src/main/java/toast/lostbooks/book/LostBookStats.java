package toast.lostbooks.book;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import toast.lostbooks.LostBooks;

import java.io.File;
import java.io.FileInputStream;

public class LostBookStats extends BookStats {
    /// The book's file.
    public final File bookFile;
    /// The NBT tag the book is saved as.
    public NBTTagCompound bookTag;

    public LostBookStats(File file) {
        super(file.getName(), null, null, (String[]) null);
        this.bookFile = file;
        try {
            if (file.exists()) {
                this.bookTag = CompressedStreamTools.readCompressed(new FileInputStream(file));
            }
        }
        catch (Exception ex) {
            LostBooks.console("Failed to load lost book!");
            ex.printStackTrace();
        }
    }

    /// Writes these BookStats to the book and returns it.
    @Override
    public ItemStack writeTo(ItemStack book) {
        if (this.bookTag != null) {
            // Clean any JSON formatting from old captured books
            NBTTagCompound cleanedTag = this.bookTag.copy();
            if (cleanedTag.hasKey("tag") && cleanedTag.getCompoundTag("tag").hasKey("pages")) {
                NBTTagList pages = cleanedTag.getCompoundTag("tag").getTagList("pages", 8);
                for (int i = 0; i < pages.tagCount(); i++) {
                    String pageText = pages.getStringTagAt(i);
                    // Check if this is JSON formatted text and clean it
                    if (pageText.startsWith("{\"text\":\"") && pageText.endsWith("\"}")) {
                        // Extract the text content and unescape it
                        String cleanText = pageText.substring(9, pageText.length() - 2);
                        cleanText = cleanText.replace("\\n", "\n")
                                           .replace("\\\"", "\"")
                                           .replace("\\\\", "\\");
                        pages.set(i, new NBTTagString(cleanText));
                    }
                }
            }
            book.writeToNBT(cleanedTag);
        }
        this.bookFile.delete();
        return book;
    }

    /// Returns true if any books can be dropped by the entity.
    @Override
    public boolean isValid(EntityLivingBase entity) {
        return true;
    }

    /// Returns true if any books can be sold by the entity.
    @Override
    public boolean isValidTrade(EntityLivingBase entity) {
        return true;
    }
}