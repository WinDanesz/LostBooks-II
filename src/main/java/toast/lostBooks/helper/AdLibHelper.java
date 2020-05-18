package toast.lostBooks.helper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import toast.lostBooks.LostBooks;
import toast.lostBooks.book.BookStats;
import toast.lostBooks.book.Library;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;

public abstract class AdLibHelper {
    /// The word codes that are determined by words files.
    public static final HashSet<String> CUSTOM_WORD_CODES = new HashSet<String>(Arrays.asList("adjective", "adverb", "exclaimation", "noun", // auto-generated
            "noun.plural", // auto-generated
            "noun.bodypart", "noun.bodypart.plural", "noun.name", "noun.living", "noun.living.plural", "noun.object", "noun.object.plural", "noun.place", "noun.place.plural", "noun.pronoun", "noun.pronoun.possessive", "verb", "verb.past", "verb.present"));
    /// The word codes that are determined by circumstance.
    public static final HashSet<String> SPECIAL_WORD_CODES = new HashSet<String>(Arrays.asList("random.number", "random.digit", "random.letter", "random.color", "random.color.readable", "random.format", "random.format.readable", "entity", "entity.random", "entity.nearest", "entity.nearest.other", "player.random", "player.nearest", "player.nearest.other", "creature.random", "creature.nearest", "creature.nearest.other", "monster.random", "monster.nearest", "monster.nearest.other", "animal.random", "animal.nearest", "animal.nearest.other", "world", "world.dimension"));
    /// All word codes. Used to organize words.
    public static final HashSet<String> WORD_CODES = new HashSet<String>(Arrays.asList("random", "verb.infinitive"));
    /// All custom words in the mod, organized by word code.
    public static final HashMap<String, String[]> WORDS = new HashMap<String, String[]>(AdLibHelper.CUSTOM_WORD_CODES.size() + 1, 1F);
    static {
        AdLibHelper.WORD_CODES.addAll(AdLibHelper.CUSTOM_WORD_CODES);
        AdLibHelper.WORD_CODES.addAll(AdLibHelper.SPECIAL_WORD_CODES);
        FileHelper.loadWords(AdLibHelper.WORDS);
    }

    /// Called to initialize static members before they are needed.
    public static final void init() {
        // Currently does nothing.
    }

    /// Maintains compatibility with legacy word codes.
    public static String checkCode(String wordCode) {
        if (wordCode.equals("verb.infinitive"))
            return "verb";
        return wordCode;
    }

    /// Returns a random word for the given word code.
    public static String nextWord(EntityLivingBase entity, String wordCode) {
        boolean cap = false;
        if (wordCode.startsWith("c.")) {
            cap = true;
            wordCode = wordCode.substring(2);
        }
        wordCode = AdLibHelper.checkCode(wordCode);
        if (AdLibHelper.CUSTOM_WORD_CODES.contains(wordCode)) {
            String[] wordArray = AdLibHelper.WORDS.get(wordCode);
            if (wordArray.length > 0)
                return AdLibHelper.cap(cap, wordArray[LostBooks.random.nextInt(wordArray.length)]);
        }
        else if (AdLibHelper.SPECIAL_WORD_CODES.contains(wordCode) && entity != null) {
            if (wordCode.equals("entity"))
                return AdLibHelper.cap(cap, entity.getCommandSenderEntity().getName());
            String[] code = wordCode.split("\\.");
            if (code[0].equals("random")) {
                if (code[1].equals("number"))
                    return Integer.toString(LostBooks.random.nextInt(9) + 1);
                else if (code[1].equals("digit"))
                    return Integer.toString(LostBooks.random.nextInt(10));
                else if (code[1].equals("letter"))
                    return Integer.toString(LostBooks.random.nextInt(26) + 10, 36);
                else if (code[1].equals("color")) {
                    if (code.length > 2) {
                        int color = LostBooks.random.nextInt(12);
                        return "\u00a7" + Integer.toString(color < 10 ? color : color + 2, 16);
                    }
                    return "\u00a7" + Integer.toString(LostBooks.random.nextInt(16), 16);
                }
                else if (code[1].equals("format")) {
                    if (code.length > 2)
                        return "\u00a7" + Integer.toString(LostBooks.random.nextInt(4) + 21, 36);
                    return "\u00a7" + Integer.toString(LostBooks.random.nextInt(5) + 20, 36);
                }
            }
            else if (code[0].equals("world")) {
                if (code.length > 1 && code[1].equals("dimension"))
                    return AdLibHelper.cap(cap, entity.world.provider.getDimensionType().getName()); // TODO might be wrong?
                return AdLibHelper.cap(cap, entity.world.getWorldInfo().getWorldName());
            }
            else {
                try {
                    ArrayList<Entity> entityList;
                    if (code[0].equals("player")) {
                        entityList = (ArrayList<Entity>) ((ArrayList) entity.world.playerEntities).clone();
                    }
                    else {
                        entityList = (ArrayList<Entity>) ((ArrayList) entity.world.loadedEntityList).clone();
                        for (Iterator<Entity> itr = entityList.iterator(); itr.hasNext();)
                            if (!AdLibHelper.isAppropriate(code[0], itr.next())) {
                                itr.remove();
                            }
                    }
                    entityList.remove(entity);
                    if (!entityList.isEmpty()) {
                        Entity target = null;
                        if (code[1].equals("random")) {
                            target = entityList.get(LostBooks.random.nextInt(entityList.size()));
                            if (target != null)
                                return AdLibHelper.cap(cap, target.getCommandSenderEntity().getName());
                        }
                        else if (code[1].equals("nearest")) {
                            Entity other = null;
                            double closest = Double.POSITIVE_INFINITY;
                            double nextClosest = Double.POSITIVE_INFINITY;
                            double distance;
                            for (Entity testEntity : entityList) {
                                distance = entity.getDistanceSq(testEntity);
                                if (distance < closest) {
                                    nextClosest = closest;
                                    closest = distance;
                                    other = target;
                                    target = testEntity;
                                }
                                else if (distance < nextClosest) {
                                    nextClosest = distance;
                                    other = testEntity;
                                }
                            }
                            if (code.length > 2 && code[2].equals("other")) {
                                if (other != null)
                                    return AdLibHelper.cap(cap, other.getCommandSenderEntity().getName());
                            }
                            else if (target != null)
                                return AdLibHelper.cap(cap, target.getCommandSenderEntity().getName());
                        }
                    }
                }
                catch (Exception ex) {
                    // Do nothing.
                }
                return AdLibHelper.nextWord(entity, (cap ? "c." : "") + "noun.name");
            }
        }
        return AdLibHelper.cap(cap, AdLibHelper.mash());
    }

    /// Returns a random word for the given word code or variable.
    public static String nextWord(EntityLivingBase entity, String wordCode, HashMap<String, String> variables) {
        boolean cap = false;
        if (wordCode.startsWith("c.")) {
            cap = true;
            wordCode = wordCode.substring(2);
        }
        if (AdLibHelper.WORD_CODES.contains(wordCode))
            return AdLibHelper.nextWord(entity, (cap ? "c." : "") + wordCode);
        else if (variables.containsKey(wordCode)) {
            String word = variables.get(wordCode);
            return AdLibHelper.cap(cap, word);
        }
        return AdLibHelper.cap(cap, AdLibHelper.mash());
    }

    /// Capitalizes the string if true. Otherwise, does nothing.
    private static String cap(boolean cap, String value) {
        return cap ? TextHelper.cap(value) : value;
    }

    /// Returns true if the entity should be considered.
    public static boolean isAppropriate(String code, Entity entity) {
        if (code.equals("entity"))
            return true;
        else if (code.equals("creature"))
            return entity instanceof IAnimals;
        else if (code.equals("monster"))
            return entity instanceof IMob;
        else if (code.equals("animal"))
            return ! (entity instanceof IMob) && entity instanceof IAnimals;
        return false;
    }

    /// Returns a new randomly generated word.
    public static String mash() {
        String[] parts = { "cree", "per", "skel", "e", "ton", "zom", "bie", "spi", "der", "bla", "per", "the", "na", "ra", "a", "i", "o", "u", "do", "pa", "queb", "pin", "goo", "ball", "tall", "fu", "crab", "poo", "han", "so", "lo", "star", "me", "yo", "boo", "we", "jam", "ka", "tha", "che", "cha", "vu", "jack", "ed", "va", "ny", "she", "he", "ro", "bri", "ne", "fa", "ther", "to", "ast", "zap", "pick", "ax", "sho", "vel", "swo", "rd", "bow", "ar", "row", "arm", "or", "pump", "kin" };
        String word = "";
        for (int syl = 2 + (LostBooks.random.nextInt(3)); syl-- > 0; ) {
            word += parts[LostBooks.random.nextInt(parts.length)];
        }
        return word;
    }

    /// Generates an ad libbed book stats object based on the given file.
    public static BookStats generate(EntityLivingBase entity, File bookFile) {
        try {
            FileInputStream in = new FileInputStream(bookFile);
            HashMap<String, String> variables = new HashMap<String, String>();

            String[] story = new String[2];
            story[0] = "" + AdLibHelper.setVariablesAndGetTitle(entity, in, variables);
            story[1] = "";
            int dat;
            while ( (dat = in.read()) >= 0) {
                if (dat == Library.N) {
                    break;
                }
                if (dat == Library.R || dat == Library.F) {
                    continue;
                }
                if (dat == '@') {
                    boolean breakThrough = false;
                    String sub = "@";
                    while ( (dat = in.read()) >= 0) {
                        if (dat == Library.N) {
                            breakThrough = true;
                            break;
                        }
                        if (dat == Library.R || dat == Library.F) {
                            continue;
                        }
                        if (dat == ';') {
                            sub = AdLibHelper.nextWord(entity, sub.substring(1), variables);
                            break;
                        }
                        sub += FileHelper.toAllowedString((char) dat);
                        if (Character.isWhitespace(dat)) {
                            break;
                        }
                    }
                    story[1] += sub;
                    if (breakThrough) {
                        break;
                    }
                    continue;
                }
                story[1] += FileHelper.toAllowedString((char) dat);
            }

            ArrayList<String> text = new ArrayList<String>(50);
            String color = "";
            String formats = "";
            String page = "";
            while (true) {
                boolean ff = false;
                boolean s = false;
                String nextColor = color;
                String nextFormats = formats;
                String word = "";
                while ( (dat = in.read()) >= 0) {
                    if (dat == Library.F) {
                        ff = true;
                        break;
                    }
                    if (dat == Library.R) {
                        continue;
                    }

                    if (dat == '@') {
                        boolean doubleBreak = false;
                        String sub = "@";
                        while ( (dat = in.read()) >= 0) {
                            if (dat == Library.F) {
                                ff = true;
                                doubleBreak = true;
                                break;
                            }
                            if (dat == Library.R) {
                                continue;
                            }
                            if (dat == ';') {
                                sub = AdLibHelper.nextWord(entity, sub.substring(1), variables);
                                break;
                            }
                            sub += FileHelper.toAllowedString((char) dat);
                            if (Character.isWhitespace(dat)) {
                                doubleBreak = true;
                                break;
                            }
                        }

                        if (sub.startsWith("\u00a7")) {
                            char subDat = sub.charAt(1);
                            try {
                                if (subDat == '0') {
                                    nextColor = "";
                                }
                                else {
                                    Integer.parseInt(FileHelper.toAllowedString(subDat), 0x10);
                                    nextColor = "\u00a7" + FileHelper.toAllowedString(subDat);
                                }
                                nextFormats = "";
                            }
                            catch (Exception ex) {
                                subDat = Character.toLowerCase(subDat);
                                if (subDat == 'r') {
                                    nextFormats = "";
                                }
                                else if (subDat == 'k' || subDat == 'l' || subDat == 'm' || subDat == 'n' || subDat == 'o') {
                                    nextFormats += "\u00a7" + FileHelper.toAllowedString(subDat);
                                }
                                else {
                                    nextColor = "\u00a7f";
                                    nextFormats = "";
                                }
                            }
                        }

                        word += sub;
                        if (doubleBreak) {
                            break;
                        }
                        continue;
                    }

                    if (s) {
                        s = false;
                        if (dat != Library.N) {
                            try {
                                if (dat == '0') {
                                    nextColor = "";
                                }
                                else {
                                    Integer.parseInt(FileHelper.toAllowedString((char) dat), 0x10);
                                    nextColor = "\u00a7" + FileHelper.toAllowedString((char) dat);
                                }
                                nextFormats = "";
                            }
                            catch (Exception ex) {
                                char tmp = Character.toLowerCase((char) dat);
                                if (tmp == 'r') {
                                    nextFormats = "";
                                }
                                else if (tmp == 'k' || tmp == 'l' || tmp == 'm' || tmp == 'n' || tmp == 'o') {
                                    nextFormats += "\u00a7" + FileHelper.toAllowedString(tmp);
                                }
                                else {
                                    nextColor = "\u00a7f";
                                    nextFormats = "";
                                }
                            }
                        }
                    }
                    else if (dat == Library.S) {
                        s = true;
                    }

                    word += FileHelper.toAllowedString((char) dat);
                    if (Character.isWhitespace(dat)) {
                        break;
                    }
                }
                if (TextHelper.fitsOnPage(page + word)) {
                    page += word;
                }
                else {
                    text.add(page);
                    page = color + formats + word;
                }
                if (dat <= 0) {
                    text.add(page);
                    break;
                }
                if (ff) {
                    text.add(page);
                    color = "";
                    formats = "";
                    page = "";
                }
                else {
                    color = nextColor;
                    formats = nextFormats;
                }
            }
            text.trimToSize();
            in.close();
            AdLibHelper.finish(story, text);
            return new BookStats(bookFile.getName(), story[0], story[1], text.toArray(new String[0]));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new BookStats(bookFile.getName(), null, null, (String[]) null);
    }

    /// Handles variables and returns the title.
    private static String setVariablesAndGetTitle(EntityLivingBase entity, FileInputStream in, HashMap<String, String> variables) {
        try {
            String title = "";
            boolean canBeVariable = true;
            int dat;
            while ( (dat = in.read()) >= 0) {
                if (dat == Library.N) {
                    break;
                }
                if (dat == Library.R || dat == Library.F) {
                    continue;
                }
                if (dat == '@') {
                    boolean breakThrough = false;
                    String sub = "@";
                    while ( (dat = in.read()) >= 0) {
                        if (dat == Library.N) {
                            breakThrough = true;
                            break;
                        }
                        if (dat == Library.R || dat == Library.F) {
                            continue;
                        }
                        if (dat == ';') {
                            canBeVariable = false;
                            sub = AdLibHelper.nextWord(entity, sub.substring(1), variables);
                            break;
                        }
                        sub += FileHelper.toAllowedString((char) dat);
                        if (Character.isWhitespace(dat)) {
                            break;
                        }
                    }
                    title += sub;
                    if (breakThrough) {
                        break;
                    }
                    continue;
                }
                title += FileHelper.toAllowedString((char) dat);
            }
            if (canBeVariable && AdLibHelper.setVariable(entity, variables, title))
                return AdLibHelper.setVariablesAndGetTitle(entity, in, variables);
            return title;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return "Untitled";
    }

    /// Returns true if a variable is successfully parsed from the string.
    private static boolean setVariable(EntityLivingBase entity, HashMap<String, String> variables, String line) {
        String variable = "";
        String wordCode = "";
        char dat;
        byte state = 0;
        int length = line.length();
        for (int i = 0; i < length; i++) {
            dat = line.charAt(i);
            if (state == 0) {
                if (dat == ' ') {
                    if (variables.containsKey(variable)) {
                        break;
                    }
                    state = 1;
                }
                else {
                    variable += FileHelper.toAllowedString(dat);
                }
            }
            else if (state == 3) {
                if (dat == ';') {
                    if (i != length - 1) {
                        break;
                    }
                    if (!AdLibHelper.WORD_CODES.contains(wordCode.startsWith("c.") ? wordCode.substring(2) : wordCode)) {
                        break;
                    }
                    variables.put(variable, AdLibHelper.nextWord(entity, wordCode, variables));
                    return true;
                }
                wordCode += FileHelper.toAllowedString(dat);
            }
            else if (state == 1) {
                if (dat != '=') {
                    break;
                }
                state = 2;
            }
            else if (state == 2) {
                if (dat != ' ') {
                    break;
                }
                state = 3;
            }
        }
        return false;
    }

    /// Goes back over the whole story to work on code that requires a finished story, such as a(n).
    private static void finish(String[] story, ArrayList<String> text) {
        for (int i = story.length; i-- > 0;) {
            int index = story[i].length();
            String part;
            while ( (index = story[i].lastIndexOf("#a;", index - 1)) >= 0) {
                part = story[i].substring(index + 3);
                story[i] = story[i].substring(0, index) + (AdLibHelper.isNextVowel(part) == 1 ? "an" : "a") + part;
            }
        }
        ListIterator<String> iterator = text.listIterator(text.size());
        while (iterator.hasPrevious()) {
            String page = iterator.previous();
            int index = page.length();
            String part;
            boolean update = false;
            while ( (index = page.lastIndexOf("#a;", index - 1)) >= 0) {
                part = page.substring(index + 3);
                byte vowel = AdLibHelper.isNextVowel(part);
                if (vowel < 0 && iterator.nextIndex() + 1 < text.size()) {
                    ListIterator<String> subIterator = text.listIterator(iterator.nextIndex() + 1);
                    while (vowel < 0 && subIterator.hasNext()) {
                        vowel = AdLibHelper.isNextVowel(subIterator.next());
                    }
                }
                page = page.substring(0, index) + (vowel == 1 ? "an" : "a") + part;
                update = true;
            }
            if (update) {
                iterator.set(page);
            }
        }
    }

    /// Returns 1 if the first letter is a vowel 0 if it is not, and -1 if there are no letters.
    private static byte isNextVowel(String string) {
        int length = string.length();
        char dat;
        for (int i = 0; i < length; i++) {
            dat = string.charAt(i);
            if (Character.isLetterOrDigit(dat)) {
                dat = Character.toUpperCase(dat);
                return (byte) (dat == 'E' || dat == 'A' || dat == 'I' || dat == 'O' || dat == 'U' ? 1 : 0);
            }
        }
        return (byte) -1;
    }
}