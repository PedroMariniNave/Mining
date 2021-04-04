package com.zpedroo.mining.utils.builder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.zpedroo.mining.managers.FileManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemBuilder {

    private ItemStack item;
    private static Method metaSetProfileMethod;
    private static Field metaProfileField;

    public static ItemStack build(FileManager file, String where) {
        return build(file, where, null, null);
    }

    public static ItemStack build(FileManager file, String where, String[] placeholders, String[] replacers) {
        ItemBuilder builder;

        String type = StringUtils.replace(file.get().getString(where + ".type"), " ", "").toUpperCase();
        int amount = file.get().contains(where + ".amount") ? file.get().getInt(where + ".amount") : 1;

        if (StringUtils.contains(type, ":")) {
            String[] typeSplit = type.split(":");
            short durability = Short.parseShort(typeSplit[1]);
            builder = new ItemBuilder(Material.getMaterial(typeSplit[0]), amount, durability);
        } else {
            builder = new ItemBuilder(Material.getMaterial(type), amount);
        }

        if (file.get().contains(where + ".owner")) {
            String owner = StringUtils.replaceEach(file.get().getString(where + ".owner"), placeholders, replacers);

            if (owner.length() <= 17) {
                builder.setSkullOwner(owner);
            } else {
                SkullMeta meta = (SkullMeta) builder.build().getItemMeta();
                mutateItemMeta(meta, owner);
                builder.build().setItemMeta(meta);
            }
        }

        if (file.get().contains(where + ".name")) {
            String name = ChatColor.translateAlternateColorCodes('&', file.get().getString(where + ".name"));

            if (placeholders != null && placeholders.length > 0 && placeholders.length == replacers.length) {
                name = StringUtils.replaceEach(name, placeholders, replacers);
            }

            builder.withName(name);
        }

        if (file.get().contains(where + ".lore")) {
            builder.withLore(file.get().getStringList(where + ".lore"), placeholders, replacers);
        }

        if (file.get().contains(where + ".attributes") && file.get().getString(where + ".attributes").equalsIgnoreCase("false")) {
            builder.hideAttributes();
        }

        if (file.get().contains(where + ".glow") && file.get().getString(where + ".glow").equalsIgnoreCase("true")) {
            builder.setGlow();
        } else {
            if (file.get().contains(where + ".enchants")) {
                for (String str : file.get().getStringList(where + ".enchants")) {
                    String enchantment = StringUtils.replace(str, " ", "");

                    if (StringUtils.contains(enchantment, ",")) {
                        String[] enchantmentSplit = enchantment.split(",");
                        int level = Integer.parseInt(StringUtils.replaceEach(enchantmentSplit[1], placeholders, replacers));
                        builder.withEnchantment(Enchantment.getByName(enchantmentSplit[0]), level);
                    } else {
                        builder.withEnchantment(Enchantment.getByName(enchantment));
                    }
                }
            }
        }

        return builder.build();
    }

    public ItemBuilder(Material material, int amount) {
        Validate.notNull(material);
        if (material.toString().equalsIgnoreCase("SKULL_ITEM")) {
            item = new ItemStack(material, amount, (short) 3);
            return;
        }

        item = new ItemStack(material, amount);
    }

    public ItemBuilder(Material material, int amount, short durability) {
        Validate.notNull(material);
        if (material.toString().equalsIgnoreCase("SKULL_ITEM") && (durability != 3)) {
            item = new ItemStack(material, amount, (short) 3);
            return;
        }

        item = new ItemStack(material, amount, durability);
    }

    public ItemBuilder withName(String name) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder withLore(List<String> lore, String[] placeholders, String[] replacers) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;
        if (lore == null || lore.size() <= 0) return this;

        List<String> toAdd = new ArrayList<>(lore.size());
        boolean replace = placeholders != null && placeholders.length > 0 && placeholders.length == replacers.length;

        for (String str : lore) {
            toAdd.add(ChatColor.translateAlternateColorCodes('&', replace ? StringUtils.replaceEach(str, placeholders, replacers) : str));
        }

        meta.setLore(toAdd);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder withEnchantment(Enchantment enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addEnchant(enchant, level, true);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder withEnchantment(Enchantment enchant) {
        return withEnchantment(enchant, 1);
    }

    public ItemBuilder setGlow() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addEnchant(Enchantment.OXYGEN, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder hideAttributes() {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return this;

        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkullOwner(String owner) {
        if (!item.getType().equals(Material.SKULL_ITEM)) return this;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return this;

        meta.setOwner(owner);
        item.setItemMeta(meta);
        return this;
    }

    public ItemStack build() {
        return item;
    }

    private static void mutateItemMeta(SkullMeta meta, String b64) {
        try {
            if (metaSetProfileMethod == null) {
                metaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
                metaSetProfileMethod.setAccessible(true);
            }
            metaSetProfileMethod.invoke(meta, makeProfile(b64));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            try {
                if (metaProfileField == null) {
                    metaProfileField = meta.getClass().getDeclaredField("profile");
                    metaProfileField.setAccessible(true);
                }
                metaProfileField.set(meta, makeProfile(b64));

            } catch (NoSuchFieldException | IllegalAccessException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    private static GameProfile makeProfile(String b64) {
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        GameProfile profile = new GameProfile(id, "aaaaa");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }
}