package toast.lostBooks.book;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import toast.lostBooks.LostBooks;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class BookProperties {
    //TODO: check entity classes and fix the logic below
    /**
     * Valid vanilla entity IDs:
     * EntityLiving.class, "Mob"
     * boss.EntityDragon.class, "EnderDragon"
     * boss.EntityWither.class, "WitherBoss"
     * monster.EntityMob.class, "Monster"
     * monster.EntityCreeper.class, "Creeper"
     * monster.EntitySkeleton.class, "Skeleton"
     * monster.EntitySpider.class, "Spider"
     * monster.EntityGiantZombie.class, "Giant"
     * monster.EntityZombie.class, "Zombie"
     * monster.EntitySlime.class, "Slime"
     * monster.EntityGhast.class, "Ghast"
     * monster.EntityPigZombie.class, "PigZombie"
     * monster.EntityEnderman.class, "Enderman"
     * monster.EntityCaveSpider.class, "CaveSpider"
     * monster.EntitySilverfish.class, "Silverfish"
     * monster.EntityBlaze.class, "Blaze"
     * monster.EntityMagmaCube.class, "LavaSlime"
     * monster.EntityWitch.class, "Witch"
     * monster.EntitySnowman.class, "SnowMan"
     * monster.EntityIronGolem.class, "VillagerGolem"
     * passive.EntityBat.class, "Bat"
     * passive.EntityPig.class, "Pig"
     * passive.EntitySheep.class, "Sheep"
     * passive.EntityCow.class, "Cow"
     * passive.EntityChicken.class, "Chicken"
     * passive.EntitySquid.class, "Squid"
     * passive.EntityWolf.class, "Wolf"
     * passive.EntityMooshroom.class, "MushroomCow"
     * passive.EntityOcelot.class, "Ozelot"
     * passive.EntityVillager.class, "Villager"
     *
     * (not technically IDs)
     * <next three classes> "Default"
     * IMerchant.class, "Merchant"
     * INpc.class, "Npc"
     * monster.IMob.class, "Hostile"
     * player.EntityPlayer.class, "Player"
     **/
    /// A set of used properties, to prevent flooding.
    public static final HashSet<String> USED_KEYS = new HashSet<String>(Arrays.asList("weight", "whitelist", "blacklist", "can_buy", "in_order", "biomes"));
    /// Mapping of special entity ID values to their classes.
    public static final HashMap<String, Class> SPECIAL_IDS = new HashMap<String, Class>();
    static {
        BookProperties.SPECIAL_IDS.put("Merchant", IMerchant.class);
        BookProperties.SPECIAL_IDS.put("Npc", INpc.class);
        BookProperties.SPECIAL_IDS.put("Hostile", IMob.class);
        BookProperties.SPECIAL_IDS.put("Player", EntityPlayer.class);
    }

    /// The absolute path to this properties file.
    public final String fileName;
    /// The mapping of all properties in this file.
    private final HashMap<String, String> properties = new HashMap<String, String>();

    private BookProperties(File file) {
        this.fileName = file.getAbsolutePath();
        try {
            FileInputStream in = new FileInputStream(file);
            String line = "";
            int dat;
            while ( (dat = in.read()) >= 0) {
                if (dat == Library.R) {
                    continue;
                }
                if (dat == Library.N) {
                    this.parseLine(line);
                    line = "";
                }
                else {
                    line += Character.toString((char) dat);
                }
            }
            this.parseLine(line);
            in.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /// Reads the line and attempts to add it to the property table.
    private void parseLine(String line) {
        String[] split = line.split("=", 2);
        if (split.length == 2 && BookProperties.USED_KEYS.contains(split[0])) {
            this.properties.put(split[0], split[1]);
        }
    }

    /// Returns the value loaded for the key.
    public String getString(String key) {
        return this.getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        String value = this.properties.get(key.toLowerCase());
        if (value == null)
            return defaultValue;
        return value;
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = this.getString(key, null);
        if (value != null) {
            if (value.equalsIgnoreCase("true"))
                return true;
            if (value.equalsIgnoreCase("false"))
                return false;
        }
        return defaultValue;
    }

    public int getInt(String key) {
        return this.getInt(key, -1);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(this.getString(key, ""));
        }
        catch (Exception ex) {
            // Do nothing.
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return this.getInt(key, -1);
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(this.getString(key, ""));
        }
        catch (Exception ex) {
            // Do nothing.
        }
        return defaultValue;
    }

    /// Returns the number of properties in this file.
    public int size() {
        return this.properties.size();
    }

    /// Returns a new integer hash set (usually for the "biomes" key).
    public HashSet<Integer> getIdSet(String key, HashSet<Integer> defaultValue) {
        String idList = this.getString(key);
        if (idList != null)
            return BookProperties.getIdSet(this.fileName, idList);
        return defaultValue;
    }

    public static HashSet<Integer> getIdSet(String location, String idList) {
        if (idList.equals(""))
            return null;
        String[] idArray = idList.split(",");
        HashSet<Integer> ids = new HashSet<Integer>();
        for (String entry : idArray) {
            try {
                ids.add((Integer.parseInt(entry)));
            }
            catch (Exception ex) {
                LostBooks.console("[WARNING] \"" + entry + "\" is an invalid biome id. Location: " + location);
            }
        }
        if (ids.size() > 0)
            return ids;
        return null;
    }

    /// Returns a new entityliving class array list (usually for the "whitelist" or "blacklist" key).
    public ArrayList<Class> getEntityList(String key, ArrayList<Class> defaultValue) {
        String classList = this.getString(key);
        if (classList != null)
            return BookProperties.getEntityList(this.fileName, classList);
        return defaultValue;
    }

    public static ArrayList<Class> getEntityList(String location, String classList) {
        if (classList.equals(""))
            return null;
        String[] classArray = classList.split(",");
        ArrayList<Class> classes = new ArrayList<Class>();
        for (String entry : classArray) {
            /// The default value
            if (entry.equals("Default")) {
                if (!classes.contains(IMerchant.class)) {
                    classes.add(IMerchant.class);
                }
                if (!classes.contains(INpc.class)) {
                    classes.add(INpc.class);
                }
                if (!classes.contains(IMob.class)) {
                    classes.add(IMob.class);
                }
            }
            /// Check if it's a special case
            else if (BookProperties.SPECIAL_IDS.containsKey(entry)) {
                Class entityClass = BookProperties.SPECIAL_IDS.get(entry);
                if (!classes.contains(entityClass)) {
                    classes.add(entityClass);
                }
            }
            /// Check if it is an entity id
            else if (EntityList.getEntityNameList().contains(entry)) {
                try {
                    Class entityClass = EntityList.getClassFromName(entry);
                    if (!classes.contains(entityClass)) {
                        classes.add(entityClass);
                    }
                }
                catch (Exception ex) {
                    LostBooks.console("[ERROR] \"" + entry + "\" is not registered properly (EntityList). Location: " + location);
                }
            }
            /// Try to read it as an absolute path
            else {
                try {
                    Class entityClass = Class.forName(entry);
                    if (!classes.contains(entityClass)) {
                        classes.add(entityClass);
                    }
                }
                catch (Exception ex) {
                    LostBooks.console("[WARNING] \"" + entry + "\" is invalid. Location: " + location);
                }
            }
        }
        if (classes.size() > 0)
            return classes;
        return null;
    }

    /// Returns the BookUtils bitcode as determined by this book properties object.
    public byte getBookUtilsCode() {
        return this.getBookUtilsCode((byte) 0);
    }

    public byte getBookUtilsCode(byte defaultValue) {
        byte bitcode = 0;
        if (!this.getBoolean("edit", (1 & defaultValue) == 0)) {
            bitcode |= 1;
        }
        if (!this.getBoolean("copy", (2 & defaultValue) == 0)) {
            bitcode |= 2;
        }
        if (!this.getBoolean("copy_edit", (4 & defaultValue) == 0)) {
            bitcode |= 4;
        }
        if (!this.getBoolean("copy_copy", (8 & defaultValue) == 0)) {
            bitcode |= 8;
        }
        return bitcode;
    }

    /// Loads the book properties for the given file.
    public static BookProperties loadBookProperties(File folder, File file) {
        try {
            String fileName = file.getName();
            if (fileName.endsWith(".book")) {
                fileName = fileName.substring(0, fileName.length() - 5);
            }
            File propFile = new File(folder, fileName + ".txt");
            if (propFile.exists()) {
                BookProperties props = new BookProperties(propFile);
                if (props.size() > 0)
                    return props;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}