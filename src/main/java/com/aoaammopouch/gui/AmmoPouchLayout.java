package com.aoaammopouch.gui;

import java.util.Arrays;
import java.util.List;


public class AmmoPouchLayout {

    // TODO: I'm kinda tempted to add an overlay with the remaining ammo count for each slot.
    //       That's a tad out-of-scope, but it would be very nice QoL.

    /** Description of inventory slots layouts, indexed on the total number of slots */
    private static final List<String> SLOT_DISTRIBUTION_PATTERNS = Arrays.asList(
        // X = SLOT, O = EMPTY SLOT, / = NEW ROW, _ = SIDE PADDING
        // All rows must have the same number of characters, and number of X is equal to line#
        // A generic algorithm could have been used, but some layouts are tricky due to numbers
        // that cannot divide nicely for some rows. Better to do some manual work than trying
        // to debug something endlessly until arriving to the same result.
        // We are not expected to have more than 36 slots, so this is good enough for now.
        "    X    ",
        "   XX    ",
        "   XXX   ",
        "   XX    /   XX    ",
        "  XXXXX  ",
        "   XXX   /   XXX   ",
        " XXXXXXX ",
        "  XXXX   /  XXXX   ",
        "   XXX   /   XXX   /   XXX   ",
        "  XXXXX  /  XXXXX  ",
        "  XXXXX  /  XXXXX  /  OOXOO  ",
        "  XXXX   /  XXXX   /  XXXX   ",
        "  XXXXX  /  XXXXX  /  OXXXO  ",
        " XXXXXX  / XXXXXX  / OOXXOO  ",
        "  XXXXX  /  XXXXX  /  XXXXX  ",
        "  XXXX   /  XXXX   /  XXXX   /  XXXX   ",
        " XXXXXXX / XXXXXXX / OOXXXOO ",
        " XXXXXX  / XXXXXX  / XXXXXX  ",
        " XXXXXXX / XXXXXXX / OXXXXXO ",
        "  XXXXX  /  XXXXX  /  XXXXX  /  XXXXX  ",
        " XXXXXXX / XXXXXXX / XXXXXXX ",
        "XXXXXXXX /XXXXXXXX /OXXXXXXO ",
        "XXXXXXXXX/XXXXXXXXX/OOXXXXXOO",
        "XXXXXXXX /XXXXXXXX /XXXXXXXX ",
        "XXXXXXXXX/XXXXXXXXX/OXXXXXXXO",
        " XXXXXXX / XXXXXXX / XXXXXXX / OXXXXXO ",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX",
        " XXXXXXX / XXXXXXX / XXXXXXX / XXXXXXX ",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX/OOXOOOXOO",
        "XXXXXXXX /XXXXXXXX /XXXXXXXX /OXXXXXXO ",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX/OXXOOOXXO",
        "XXXXXXXX /XXXXXXXX /XXXXXXXX /XXXXXXXX ",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX/XXXOOOXXX",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX/OXXXXXXXO",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX/XXXXOXXXX",
        "XXXXXXXXX/XXXXXXXXX/XXXXXXXXX/XXXXXXXXX"
    );

    /** The size of each slot in pixels */
    public static final int SLOT_SIZE = 18;
    /** The maximum number of rows in the ammo pouch */
    public static final int MAX_ROWS = 4;
    /** The maximum number of columns in the ammo pouch */
    public static final int MAX_COLUMNS = 9;

    /** Padding above the ammo pouch slots */
    public static final int TOP_PADDING = 18;
    /** Padding on the sides of the ammo pouch slots */
    public static final int SIDE_PADDING = 7;
    /** Padding between the ammo pouch slots and the player inventory */
    public static final int BOTTOM_PADDING = 20;
    /** Additional offset for the hotbar */
    public static final int HOTBAR_SPACING = 4;

    public static final int PLAYER_INVENTORY_OFFSET_Y = MAX_ROWS * SLOT_SIZE + TOP_PADDING + BOTTOM_PADDING;
    public static final int HOTBAR_OFFSET_Y = PLAYER_INVENTORY_OFFSET_Y + 3 * SLOT_SIZE + HOTBAR_SPACING;
    public static final int GUI_WIDTH = MAX_COLUMNS * SLOT_SIZE + 2 * SIDE_PADDING;
    public static final int GUI_HEIGHT = PLAYER_INVENTORY_OFFSET_Y + 4 * SLOT_SIZE + HOTBAR_SPACING;

    private final int[] slotX;
    private final int[] slotY;

    private AmmoPouchLayout(int[] slotX, int[] slotY) {
        this.slotX = slotX;
        this.slotY = slotY;
    }

    public static AmmoPouchLayout create(int slotCount) {
        int normalizedCount = Math.max(1, Math.min(36, slotCount));

        String pattern = SLOT_DISTRIBUTION_PATTERNS.get(normalizedCount - 1);
        String[] rowPatterns = pattern.split("/");

        int[] slotX = new int[normalizedCount];
        int[] slotY = new int[normalizedCount];
        int slotIndex = 0;

        // The Os ensure we maintain the correct spacing for empty slots
        int rowStartX = SIDE_PADDING + (MAX_COLUMNS - rowPatterns[0].trim().length()) * SLOT_SIZE / 2;
        int rowStartY = TOP_PADDING + (MAX_ROWS - rowPatterns.length) * SLOT_SIZE / 2;

        for (int row = 0; row < rowPatterns.length; row++) {
            String rowPattern = rowPatterns[row].trim();
            int rowLength = rowPattern.length();

            for (int col = 0; col < rowLength; col++) {
                char c = rowPattern.charAt(col);
                if (c == 'X') {
                    slotX[slotIndex] = rowStartX + col * SLOT_SIZE;
                    slotY[slotIndex] = rowStartY + row * SLOT_SIZE;
                    slotIndex++;
                }
            }
        }

        return new AmmoPouchLayout(slotX, slotY);
    }

    public int getSlotX(int slot) {
        return this.slotX[slot];
    }

    public int getSlotY(int slot) {
        return this.slotY[slot];
    }
}