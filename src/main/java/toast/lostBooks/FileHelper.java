package toast.lostBooks;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.server.MinecraftServer;

//import net.minecraft.util.ChatAllowedCharacters;

public abstract class FileHelper {
    /// The current world folder.
    public static File WORLD_DIRECTORY;

    /// Initializes server files. Called before the server starts up.
    public static void init(MinecraftServer server) {
        try {
            FileHelper.WORLD_DIRECTORY = new File(server.getFile("saves/" + server.getFolderName()), "LostBooks");
            LostBookCollection.BOOK_DIRECTORY = new File(FileHelper.WORLD_DIRECTORY, "lost");
            LostBookCollection.BOOK_DIRECTORY.mkdirs();
        }
        catch (Exception ex) {
            _LostBooks.console("Failed to initialize unique book data storage! You may feel a strong disturbance in the force...");
            ex.printStackTrace();
            FileHelper.WORLD_DIRECTORY = null;
        }
    }

    /// Loads all stories in the given folder. Indirectly called recursively on each inner folder.
    public static IBook[] loadCollection(String category, BookCollection parentCollection, File folder) {
        try {
            folder.mkdirs();
            File[] bookFolders = folder.listFiles(new FolderFilter());
            ArrayList<IBook> bookList = FileHelper.loadBooks(category, parentCollection, folder);
            int length = bookFolders.length;
            if (bookList.size() + length == 0)
                return new IBook[0];
            BookCollection collection;
            for (int id = 0; id < length; id++) {
                collection = new BookCollection(category, parentCollection, bookFolders[id], BookProperties.loadBookProperties(folder, bookFolders[id]));
                if (collection.books.length == 1) {
                    bookList.add(collection.books[0]);
                }
                else if (collection.books.length > 1) {
                    bookList.add(collection);
                }
            }
            bookList.trimToSize();
            return FileHelper.sortBooks(parentCollection, bookList);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new IBook[0];
    }

    /// Returns the collection list as a final array and sorts, if needed.
    private static IBook[] sortBooks(BookCollection parentCollection, ArrayList<IBook> bookList) {
        if (parentCollection != null && parentCollection.inOrder) {
            IBook[] books = FileHelper.sortToArray(bookList);
            if (books != null)
                return books;
        }
        return bookList.toArray(new IBook[0]);
    }

    /// Returns a sorted array or null if the sort fails.
    private static IBook[] sortToArray(ArrayList<IBook> bookList) {
        int length = bookList.size();
        IBook[] books = new IBook[length];
        int index;
        String name;
        for (IBook book : bookList) {
            name = book.getName();
            if (name.endsWith(".book")) {
                name = name.substring(0, name.length() - 5);
            }
            try {
                index = Integer.parseInt(name) - 1;
                if (books[index] != null)
                    return null;
                books[index] = book;
            }
            catch (Exception ex) {
                return null;
            }
        }
        return books;
    }

    /// Loads the books in the given folder.
    private static ArrayList<IBook> loadBooks(String category, BookCollection parentCollection, File folder) {
        if (category == "adlib")
            return FileHelper.loadAdLibs(parentCollection, folder);
        try {
            File[] bookFiles = folder.listFiles(new ExtensionFilter(".book"));
            int length = bookFiles.length;
            if (length == 0)
                return new ArrayList<IBook>(0);
            ArrayList<IBook> bookList = new ArrayList<IBook>(length);
            IBook book;
            for (File bookFile : bookFiles) {
                book = FileHelper.loadBook(category, parentCollection, folder, bookFile);
                if (book != null) {
                    bookList.add(book);
                }
            }
            bookList.trimToSize();
            return bookList;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<IBook>(0);
    }

    /// Loads the book in the given folder.
    public static IBook loadBook(String category, BookCollection parentCollection, File folder, File bookFile) {
        if (category == "adlib")
            return FileHelper.loadAdLib(parentCollection, folder, bookFile);
        IBook book = null;
        try {
            FileInputStream in = new FileInputStream(bookFile);

            String[] story = new String[2];
            story[0] = "";
            story[1] = "";
            int dat;
            for (int i = 0; i < 2; i++) {
                while ( (dat = in.read()) >= 0) {
                    if (dat == Library.N) {
                        break;
                    }
                    if (dat == Library.R || dat == Library.F) {
                        continue;
                    }
                    story[i] += FileHelper.toAllowedString((char) dat);
                }
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
            book = new BookStats(parentCollection, bookFile, BookProperties.loadBookProperties(folder, bookFile), category == "unique", story[0], story[1], text.toArray(new String[0]));
            in.close();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return book;
    }

    /// Loads the ad lib books from the given folder.
    private static ArrayList<IBook> loadAdLibs(BookCollection parentCollection, File folder) {
        try {
            File[] bookFiles = folder.listFiles(new ExtensionFilter(".book"));
            int length = bookFiles.length;
            if (length == 0)
                return new ArrayList<IBook>(0);
            ArrayList<IBook> bookList = new ArrayList<IBook>(length);
            IBook book;
            for (File bookFile : bookFiles) {
                book = FileHelper.loadAdLib(parentCollection, folder, bookFile);
                if (book != null) {
                    bookList.add(book);
                }
            }
            bookList.trimToSize();
            return bookList;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ArrayList<IBook>(0);
    }

    /// Loads the ad lib book in the given folder.
    public static IBook loadAdLib(BookCollection parentCollection, File folder, File bookFile) {
        IBook book = null;
        try {
            new FileInputStream(bookFile).close();
            book = new AdLibStats(parentCollection, bookFile, BookProperties.loadBookProperties(folder, bookFile));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return book;
    }

    /// Loads the words from the words folder.
    public static void loadWords(HashMap<String, String[]> words) {
        try {
            HashMap<String, HashSet<String>> wordSets = new HashMap<String, HashSet<String>>(AdLibHelper.CUSTOM_WORD_CODES.size() + 1, 1F);
            for (String wordCode : AdLibHelper.CUSTOM_WORD_CODES) {
                wordSets.put(wordCode, new HashSet<String>(255));
            }
            File folder = new File(_LostBooks.CONFIG_DIRECTORY, "/LostBooks/words");
            folder.mkdirs();
            File[] wordsFiles = folder.listFiles(new ExtensionFilter(".words"));
            for (File wordsFile : wordsFiles) {
                FileInputStream in = new FileInputStream(wordsFile);
                byte status = 0;
                String wordCode = "";
                String word = "";
                int dat;
                while ( (dat = in.read()) >= 0) {
                    if (dat == Library.R) {
                        continue;
                    }
                    if (dat == Library.N) {
                        if (status == 0) {
                            if (wordCode == "") {
                                continue;
                            }
                            wordCode = AdLibHelper.checkCode(wordCode);
                            if (!AdLibHelper.CUSTOM_WORD_CODES.contains(wordCode)) {
                                if (AdLibHelper.WORD_CODES.contains(wordCode)) {
                                    _LostBooks.console("Words can not be added to code: " + wordCode + " (" + wordsFile.getName() + ")");
                                }
                                else if (wordCode.charAt(0) != '#') {
                                    _LostBooks.console("Invalid word code: " + wordCode + " (" + wordsFile.getName() + ")");
                                }
                                wordCode = "";
                                continue;
                            }
                            status = 1;
                        }
                        else if (status == 1) {
                            if (word == "") {
                                wordCode = "";
                                status = 0;
                                continue;
                            }
                            wordSets.get(wordCode).add(word);
                            word = "";
                        }
                        continue;
                    }
                    if (status == 0) {
                        wordCode += FileHelper.toAllowedString((char) dat);
                    }
                    else if (status == 1) {
                        word += FileHelper.toAllowedString((char) dat);
                    }
                }
                if (status == 1 && word != "") {
                    wordSets.get(wordCode).add(word);
                }
                in.close();
            }
            /// Combined word lists. The first code in each array gets all following codes in the same array added to it.
            String[][] comboMap = { { "noun", "noun.bodypart", "noun.living", "noun.object", "noun.place" }, { "noun.plural", "noun.bodypart.plural", "noun.living.plural", "noun.object.plural", "noun.place.plural" } };
            HashSet<String> combo;
            for (String[] comboList : comboMap) {
                combo = wordSets.get(comboList[0]);
                for (int i = comboList.length; i-- > 1;) {
                    combo.addAll(wordSets.get(comboList[i]));
                }
            }

            int amount = 0;
            String[] wordList;
            for (String wordCode : AdLibHelper.CUSTOM_WORD_CODES) {
                wordList = new ArrayList<String>(wordSets.get(wordCode)).toArray(new String[0]);
                words.put(wordCode, wordList);
                amount += wordList.length;
            }
            _LostBooks.console("Loaded " + amount + " words!");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /// Returns the string version of the character that should be used in a book.
    public static String toAllowedString(char c) {
        //if (ChatAllowedCharacters.isAllowedCharacter(c))
        return Character.toString(c);
        //return "";
    }

    /// Returns a list of book ids that have been found by the player.
    public static HashSet<String> loadBookData(String username) {
        try {
            File playerFile = new File(FileHelper.WORLD_DIRECTORY, username + ".txt");
            if (playerFile.exists()) {
                try {
                    HashSet<String> bookData = new HashSet<String>(Library.UNIQUE_BOOK_COUNT);
                    FileInputStream in = new FileInputStream(playerFile);
                    int dat;
                    String value = "";
                    while ( (dat = in.read()) >= 0) {
                        if (dat == Library.N) {
                            if (value != "") {
                                bookData.add(value);
                            }
                            value = "";
                        }
                        else {
                            value += FileHelper.toAllowedString((char) dat);
                        }
                    }
                    if (value != "") {
                        bookData.add(value);
                    }
                    in.close();
                    return bookData;
                }
                catch (Exception ex) {
                    _LostBooks.console("Error reading player file! Destroying...");
                    if (!playerFile.delete()) {
                        _LostBooks.console("Failed to destroy player file!");
                    }
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception ex) {
            _LostBooks.console("Failed to load player book data! (" + username + ")");
            ex.printStackTrace();
        }
        return new HashSet<String>(0);
    }

    /// Adds the given book id to the list of book ids the player has found.
    public static void addBookData(String username, String id) {
        try {
            File playerFile = new File(FileHelper.WORLD_DIRECTORY, username + ".txt");
            playerFile.createNewFile();
            FileWriter out = new FileWriter(playerFile, true);
            out.write(id + Library.N);
            out.close();
        }
        catch (Exception ex) {
            _LostBooks.console("Failed to save player book data! (" + username + ")");
            ex.printStackTrace();
        }
    }

    /// Deletes player book data.
    public static void clearBookData() {
        try {
            File[] playerFiles = FileHelper.WORLD_DIRECTORY.listFiles(new ExtensionFilter(".txt"));
            for (File playerFile : playerFiles) {
                playerFile.delete();
            }
        }
        catch (Exception ex) {
            _LostBooks.console("Failed to clear player book data!");
            ex.printStackTrace();
        }
    }

    public static void clearBookData(String username) {
        try {
            File playerFile = new File(FileHelper.WORLD_DIRECTORY, username + ".txt");
            playerFile.delete();
        }
        catch (Exception ex) {
            _LostBooks.console("Failed to delete player book data! (" + username + ")");
            ex.printStackTrace();
        }
    }

    /// All the file filters used.
    public static class ExtensionFilter implements FilenameFilter {
        /// The file extension to accept.
        private final String extension;

        public ExtensionFilter(String ext) {
            this.extension = ext;
        }

        /// Returns true if the file should be accepted.
        @Override
        public boolean accept(File file, String name) {
            return name.endsWith(this.extension);
        }
    }

    public static class FolderFilter implements FileFilter {
        public FolderFilter() {
        }

        /// Returns true if the file should be accepted.
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }
}