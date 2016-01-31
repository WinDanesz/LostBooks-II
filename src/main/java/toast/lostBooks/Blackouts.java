package toast.lostBooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class Blackouts {
    // Properties stored for easy access.
    public static final boolean DROP_BLACKOUTS = Properties.getBoolean(Properties.GENERAL, "uniqueBlackouts");
    public static final boolean ADAPTIVE_DROPS = Properties.getBoolean(Properties.GENERAL, "adaptiveDrops");

    // Set of all book ids that have been blacked out.
    private final HashSet<String> blackouts;

    public Blackouts(World world) {
        if (Blackouts.DROP_BLACKOUTS) {
            this.blackouts = Library.getUniqueIds();
            HashSet<String> foundIds;
            Iterator<String> iterator;
            for (EntityPlayer player : (ArrayList<EntityPlayer>) world.playerEntities) {
                foundIds = BookHelper.getBookData(player);
                for (iterator = this.blackouts.iterator(); iterator.hasNext();)
                    if (!foundIds.contains(iterator.next())) {
                        iterator.remove();
                    }
                if (this.blackouts.isEmpty()) {
                    break;
                }
            }
        }
        else {
            this.blackouts = new HashSet<String>(0);
        }
    }

    // Returns true if the book id is blacked out.
    public boolean isBlackedOut(String id) {
        return Blackouts.DROP_BLACKOUTS && this.blackouts.contains(id);
    }
}