package toast.lostBooks.helper;

import toast.lostBooks.LostBooks;

import java.io.File;
import java.io.FileInputStream;

public class TextHelper {
    /// All allowed characters.
    public static final String allowedCharacters = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000";
    /// Array of width of all the characters in default.png.
    public static byte[] charWidth = new byte[256];
    /// Array of the start/end column (in upper/lower nibble) for every glyph in the /font directory.
    public static byte[] glyphWidth = new byte[65536];
    static {
        try {
            FileInputStream in;
            in = new FileInputStream(new File(LostBooks.CONFIG_DIRECTORY, "/LostBooks/charWidths.bin"));
            in.read(TextHelper.charWidth);
            in.close();
            in = new FileInputStream(new File(LostBooks.CONFIG_DIRECTORY, "/LostBooks/glyphWidths.bin"));
            in.read(TextHelper.glyphWidth);
            in.close();
        }
        catch (Exception ex) {
            LostBooks.console("Error reading font widths! @" + ex.getClass().getName());
        }
    }

    /// Returns true if the String will fit on a page.
    public static boolean fitsOnPage(String string) {
        return TextHelper.splitStringWidth(string, 118) <= 118;
    }

    /// Returns the width of the wordwrapped String.
    public static int splitStringWidth(String string, int width) {
        return 9 * TextHelper.arrayFormattedStringToWidth(string, width).length;
    }

    /// Breaks a string into a list of pieces that will fit a specified width.
    public static String[] arrayFormattedStringToWidth(String string, int width) {
        return TextHelper.wrapFormattedStringToWidth(string, width).split("\n");
    }

    /// Inserts newline and formatting into a string to wrap it within the specified width.
    public static String wrapFormattedStringToWidth(String string, int width) {
        int length = TextHelper.sizeStringToWidth(string, width);
        if (string.length() <= length)
            return string;
        String part = string.substring(0, length);
        char c = string.charAt(length);
        boolean space = c == 32 || c == 10;
        String remaining = TextHelper.getFormatFromString(part) + string.substring(length + (space ? 1 : 0));
        return part + "\n" + TextHelper.wrapFormattedStringToWidth(remaining, width);
    }

    /// Determines how many characters from the string will fit into the specified width.
    public static int sizeStringToWidth(String string, int widthMax) {
        int length = string.length();
        int width = 0;
        int tried = 0;
        int fit = -1;
        for (boolean incr = false; tried < length; tried++) {
            char c = string.charAt(tried);
            switch (c) {
                case 10:
                    tried--;
                    break;
                case 167:
                    if (tried < length - 1) {
                        tried++;
                        char format = string.charAt(tried);
                        if (format != 108 && format != 76) {
                            if (format == 114 || format == 82) {
                                incr = false;
                            }
                        }
                        else {
                            incr = true;
                        }
                    }
                    break;
                case 32:
                    fit = tried;
                    //$FALL-THROUGH$
                default:
                    width += TextHelper.getCharWidth(c);
                    if (incr) {
                        width++;
                    }
            }
            if (c == 10) {
                tried++;
                fit = tried;
                break;
            }
            if (width > widthMax) {
                break;
            }
        }
        return tried != length && fit != -1 && fit < tried ? fit : tried;
    }

    /// Returns the width of the Character.
    public static int getCharWidth(char c) {
        if (c == 167)
            return -1;
        if (c == 32)
            return 4;
        int allowedIndex = TextHelper.allowedCharacters.indexOf(c);
        if (allowedIndex >= 0)
            return TextHelper.charWidth[allowedIndex];
        if (TextHelper.glyphWidth[c] != 0) {
            int hiBits = TextHelper.glyphWidth[c] >>> 4;
            int loBits = TextHelper.glyphWidth[c] & 15;
            if (loBits > 7) {
                loBits = 15;
                hiBits = 0;
            }
            loBits++;
            return (loBits - hiBits) / 2 + 1;
        }
        return 0;
    }

    /// Digests a string for nonprinting formatting characters then returns a string containing only that formatting.
    public static String getFormatFromString(String string) {
        String format = "";
        int index = -1;
        int length = string.length();
        while ( (index = string.indexOf(167, index + 1)) != -1) {
            if (index < length - 1) {
                char c = string.charAt(index + 1);
                if (TextHelper.isFormatColor(c)) {
                    format = "\u00a7" + c;
                }
                else if (TextHelper.isFormatSpecial(c)) {
                    format = format + "\u00a7" + c;
                }
            }
        }
        return format;
    }

    /// Checks if the char code is a hexadecimal character, used to set color.
    public static boolean isFormatColor(char c) {
        return c >= 48 && c <= 57 || c >= 97 && c <= 102 || c >= 65 && c <= 70;
    }

    /// Checks if the char code is O-K...lLrRk-o... used to set special formatting.
    public static boolean isFormatSpecial(char c) {
        return c >= 107 && c <= 111 || c >= 75 && c <= 79 || c == 114 || c == 82;
    }

    // Makes the first letter upper case.
    public static String cap(String string) {
        char[] chars = string.toCharArray();
        if (chars.length <= 0)
            return "";
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    // Makes the first letter lower case.
    public static String decap(String string) {
        char[] chars = string.toCharArray();
        if (chars.length <= 0)
            return "";
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}