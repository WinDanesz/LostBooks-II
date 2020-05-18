package toast.lostBooks.book;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import toast.lostBooks.LostBooks;

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
			book.writeToNBT(bookTag); // book.readFromNBT(this.bookTag);
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