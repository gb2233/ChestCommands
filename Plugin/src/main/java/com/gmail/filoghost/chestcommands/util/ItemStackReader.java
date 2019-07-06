/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gmail.filoghost.chestcommands.util;

import com.gmail.filoghost.chestcommands.exception.FormatException;
import com.gmail.filoghost.chestcommands.serializer.EnchantmentSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemStackReader {

  private Material material = Material.STONE; // In the worst case (bad exception handling) we just get stone
  private int amount = 1;
  private List<ItemFlag> itemFlags = new ArrayList<>();
  private List<String> lore = new ArrayList<>();
  private String displayName = null;
  private HashMap<Enchantment, Integer> enchantments = new HashMap<>();
  private List<PotionEffect> effects = new ArrayList<>();
  private DyeColor baseColor = DyeColor.WHITE;
  private List<Pattern> patterns = new ArrayList<>();
  private Color color;
  private short dataValue = 0;
  private boolean explicitDataValue = false;

  /**
   * Reads item in the format "id:data, amount [,<itemMeta>]" id can be either the id of the
   * material or its name. for example wool:5, 3 is a valid input.
   */
  public ItemStackReader(String input, boolean parseAmount) throws FormatException {
    Validate.notNull(input, "input cannot be null");

    // READ DATA
    String[] split = input.split(",");
    if (split.length > 2) {
      String[] itemData = Arrays.copyOfRange(split, 2, split.length);
      for (String data : itemData) {
        data = data.trim();
        if (data.toLowerCase().startsWith(Nodes.NAME)) {
          parseDisplayName(data);
        }
        if (data.toLowerCase().startsWith(Nodes.LORE)) {
          parseLore(data);
        }
        if (data.toLowerCase().startsWith(Nodes.COLOR)) {
          parseColor(data);
        }
        if (data.toLowerCase().startsWith(Nodes.POTION)) {
          parsePotion(data);
        }
        if (data.toLowerCase().startsWith(Nodes.BASE_COLOR)) {
          parseBannerBaseColor(data);
        }
        if (data.toLowerCase().startsWith(Nodes.PATTERN)) {
          parseBannerPattern(data);
        }
        if (data.toLowerCase().startsWith(Nodes.ENCHANT)) {
          parseEnchant(data);
        }
        if (data.toLowerCase().startsWith(Nodes.FLAG)) {
          parseItemFlag(data);
        }
      }
    }

    // Remove spaces, they're not needed
    input = StringUtils.stripChars(input, " _-");

    if (parseAmount) {
      // Read the optional amount
      String[] splitAmount = input.split(",");

      if (splitAmount.length > 1) {

        if (!Utils.isValidInteger(splitAmount[1])) {
          throw new FormatException("invalid amount \"" + splitAmount[1] + "\"");
        }

        int amount = Integer.parseInt(splitAmount[1]);
        if (amount <= 0) {
          throw new FormatException("invalid amount \"" + splitAmount[1] + "\"");
        }
        this.amount = amount;

        // Only keep the first part as input
        input = splitAmount[0];
      }
    }

    // Read the optional data value
    String[] splitByColons = input.split(":");

    if (splitByColons.length > 1) {

      if (!Utils.isValidShort(splitByColons[1])) {
        throw new FormatException("invalid data value \"" + splitByColons[1] + "\"");
      }

      short dataValue = Short.parseShort(splitByColons[1]);
      if (dataValue < 0) {
        throw new FormatException("invalid data value \"" + splitByColons[1] + "\"");
      }

      this.explicitDataValue = true;
      this.dataValue = dataValue;

      // Only keep the first part as input
      input = splitByColons[0];
    }

    Material material = MaterialsRegistry.matchMaterial(input);

    if (material == null || MaterialsRegistry.isAir(material)) {
      throw new FormatException("invalid material \"" + input + "\"");
    }
    this.material = material;
  }

  // Item flags
  // <itemflag1> <itemflag2>
  private void parseItemFlag(String input) throws FormatException {
    String flags = input.substring(Nodes.FLAG.length()).trim();
    for (String flag : flags.split(" ")) {
      try {
        itemFlags.add(ItemFlag.valueOf(flag.toUpperCase()));
      } catch (Exception e) {
        throw new FormatException("invalid item flags \"" + input + "\"");
      }
    }
  }

  // Banner Pattern
  // <pattern>|<color>
  private void parseBannerPattern(String input) throws FormatException {
    patterns = ItemUtils.parseBannerPatternList(
        Arrays.asList(input.substring(Nodes.PATTERN.length()).trim().replace("|", ":").split(" ")));
  }

  // Base color
  // <color>
  private void parseBannerBaseColor(String input) throws FormatException {
    baseColor = ItemUtils.parseDyeColor(input.substring(Nodes.BASE_COLOR.length()).trim());
  }

  // Enchantment
  // <enchant>|<level>
  private void parseEnchant(String input) throws FormatException {
    String enchants = input.substring(Nodes.ENCHANT.length()).trim();
    for (String enchant : enchants.split(" ")) {
      String[] data = enchant.split("[|]");
      if (data.length != 2) {
        throw new FormatException("invalid enchant format \"" + input + "\"");
      }
      if (!Utils.isValidInteger(data[1])) {
        throw new FormatException("invalid integer \"" + input + "\"");
      }
      if (EnchantmentSerializer.matchEnchantment(data[0]) == null) {
        throw new FormatException("invalid enchant type \"" + input + "\"");
      }
      enchantments.put(EnchantmentSerializer.matchEnchantment(data[0]), Integer.parseInt(data[1]));
    }
  }

  // Potion
  // <effect>|<duration>|<power>
  private void parsePotion(String input) throws FormatException {
    String potions = input.substring(Nodes.POTION.length()).trim();
    for (String potion : potions.split(" ")) {
      String[] data = potion.split("[|]");
      if (data.length != 3) {
        throw new FormatException("invalid potion format \"" + input + "\"");
      }
      if (!(Utils.isValidInteger(data[1]) || Utils.isValidInteger(data[2]))) {
        throw new FormatException("invalid integer \"" + input + "\"");
      }
      if (PotionEffectType.getByName(data[0]) == null) {
        throw new FormatException("invalid effect type \"" + input + "\"");
      }
      effects.add(new PotionEffect(PotionEffectType.getByName(data[0]), Integer.parseInt(data[1]),
          Integer.parseInt(data[2])));
    }
  }

  // Color
  // <RED> <GREEN> <BLUE>
  private void parseColor(String input) throws FormatException {
    color = ItemUtils.parseColor(input.substring(Nodes.COLOR.length()).trim().replace(" ", ","));
  }

  // Lore
  // <Lore1> <Lore2>
  // Example: This_is_line_1 This_is_line_2
  private void parseLore(String input) {
    for (String line : input.substring(Nodes.LORE.length()).trim().split(" ")) {
      lore.add(ChatColor.translateAlternateColorCodes('&', line.replace("_", " ")));
    }
  }

  // Display Name
  // <display_name>
  // Example: This_Is_Example
  private void parseDisplayName(String input) {
    displayName = ChatColor.translateAlternateColorCodes('&',
        input.substring(Nodes.NAME.length()).trim().replace("_", " "));
  }

  public Material getMaterial() {
    return material;
  }

  public int getAmount() {
    return amount;
  }

  public short getDataValue() {
    return dataValue;
  }

  public boolean hasExplicitDataValue() {
    return explicitDataValue;
  }

  public ItemStack createStack() {
    ItemStack item = new ItemStack(material, amount, dataValue);
    ItemMeta itemMeta = item.getItemMeta();

    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(lore);
    enchantments.forEach((enchant, level) -> itemMeta.addEnchant(enchant, level, true));
    itemFlags.forEach(itemMeta::addItemFlags);
    if (itemMeta instanceof PotionMeta) {
      effects.forEach((effect) -> ((PotionMeta) itemMeta).addCustomEffect(effect, true));
    }
    if (itemMeta instanceof BannerMeta) {
      ((BannerMeta) itemMeta).setBaseColor(baseColor);
      ((BannerMeta) itemMeta).setPatterns(patterns);
    }
    if (itemMeta instanceof LeatherArmorMeta) {
      ((LeatherArmorMeta) itemMeta).setColor(color);
    }

    item.setItemMeta(itemMeta);
    return item;
  }

  public List<ItemFlag> getItemFlags() {
    return itemFlags;
  }

  public List<String> getLore() {
    return lore;
  }

  public String getDisplayName() {
    return displayName;
  }

  public HashMap<Enchantment, Integer> getEnchants() {
    return enchantments;
  }

  public List<PotionEffect> getEffects() {
    return effects;
  }

  private class Nodes {

    public static final String
        LORE = "lore:",
        NAME = "name:",
        PATTERN = "pattern:",
        BASE_COLOR = "base-color:",
        ENCHANT = "enchant:",
        POTION = "effect:",
        FLAG = "flag:",
        COLOR = "color:";
  }
}
