package org.kiuwn.counterstrikemod.Gameplay;

import net.minecraft.world.item.ItemStack;

public record ShopItem(String name, String path, int price, ItemStack item) {}
