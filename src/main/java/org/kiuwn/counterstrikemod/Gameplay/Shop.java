package org.kiuwn.counterstrikemod.Gameplay;

import net.minecraft.world.item.Item;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Shop {
    private final ArrayList<ShopItem> items = new ArrayList<>();

    public Shop() {}

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public ArrayList<ShopItem> getItems() {
        return items;
    }

    public ShopItem getItem(String path) {
        for (ShopItem item : items) {
            if (item.path().equals(path)) {
                return item;
            }
        }
        return null;
    }

    public ArrayList<ShopItem> getItemsWithPath(String path) {
        ArrayList<ShopItem> shopItems = new ArrayList<>();

        for (ShopItem item : items) {
            if (item.path().startsWith(path)) {
                shopItems.add(item);
            }
        }

        return shopItems;
    }
}
