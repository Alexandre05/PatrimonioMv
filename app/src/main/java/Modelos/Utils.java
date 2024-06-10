package Modelos;

import java.util.List;

public class Utils {
    public static boolean containsNullItems(List<Item> items) {
        for (Item item : items) {
            if (item == null) {
                return true;
            }
        }
        return false;
    }
}
