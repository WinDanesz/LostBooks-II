package toast.lostBooks;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public abstract class RandomHelper {
    /// Randomly chooses a valid book from what is given.
    public static IBook choose(EntityLivingBase entity, IBook... items) {
        int length = items.length;
        boolean[] valid = new boolean[length];
        for (int i = 0; i < length; i++) {
            valid[i] = items[i].isValid(entity);
        }
        int totalWeight = 0;
        for (int i = 0; i < length; i++)
            if (valid[i]) {
                totalWeight += items[i].getWeight();
            }
        if (totalWeight > 0) {
            totalWeight = _LostBooks.random.nextInt(totalWeight);
            for (int i = 0; i < length; i++)
                if (valid[i] && (totalWeight -= items[i].getWeight()) < 0)
                    return items[i];
        }
        return null;
    }

    /// Randomly chooses a valid spawn book from what is given.
    public static IBook chooseSpawn(EntityPlayer player, IBook... items) {
        int length = items.length;
        boolean[] valid = new boolean[length];
        for (int i = 0; i < length; i++) {
            valid[i] = items[i].isValidSpawn(player);
        }
        int totalWeight = 0;
        for (int i = 0; i < length; i++)
            if (valid[i]) {
                totalWeight += items[i].getWeight();
            }
        if (totalWeight > 0) {
            totalWeight = _LostBooks.random.nextInt(totalWeight);
            for (int i = 0; i < length; i++)
                if (valid[i] && (totalWeight -= items[i].getWeight()) < 0)
                    return items[i];
        }
        return null;
    }

    /// Randomly chooses a valid trade book from what is given.
    public static IBook chooseTrade(EntityLivingBase entity, IBook... items) {
        int length = items.length;
        boolean[] valid = new boolean[length];
        for (int i = 0; i < length; i++) {
            valid[i] = items[i].isValidTrade(entity);
        }
        int totalWeight = 0;
        for (int i = 0; i < length; i++)
            if (valid[i]) {
                totalWeight += items[i].getWeight();
            }
        if (totalWeight > 0) {
            totalWeight = _LostBooks.random.nextInt(totalWeight);
            for (int i = 0; i < length; i++)
                if (valid[i] && (totalWeight -= items[i].getWeight()) < 0)
                    return items[i];
        }
        return null;
    }
}