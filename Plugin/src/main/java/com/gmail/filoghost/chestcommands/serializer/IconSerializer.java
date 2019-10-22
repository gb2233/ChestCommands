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
package com.gmail.filoghost.chestcommands.serializer;

import com.gmail.filoghost.chestcommands.api.Icon;
import com.gmail.filoghost.chestcommands.config.AsciiPlaceholders;
import com.gmail.filoghost.chestcommands.exception.FormatException;
import com.gmail.filoghost.chestcommands.internal.CommandsClickHandler;
import com.gmail.filoghost.chestcommands.internal.RequiredItem;
import com.gmail.filoghost.chestcommands.internal.icon.ExtendedIcon;
import com.gmail.filoghost.chestcommands.internal.icon.IconCommand;
import com.gmail.filoghost.chestcommands.util.ErrorLogger;
import com.gmail.filoghost.chestcommands.util.FormatUtils;
import com.gmail.filoghost.chestcommands.util.ItemStackReader;
import com.gmail.filoghost.chestcommands.util.ItemUtils;
import com.gmail.filoghost.chestcommands.util.Utils;
import com.gmail.filoghost.chestcommands.util.Validate;
import com.gmail.filoghost.chestcommands.util.nbt.parser.MojangsonParseException;
import com.gmail.filoghost.chestcommands.util.nbt.parser.MojangsonParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;

public class IconSerializer {

  public static Icon loadIconFromSection(ConfigurationSection section, String iconName,
      String menuFileName, ErrorLogger errorLogger) {
    Validate.notNull(section, "ConfigurationSection cannot be null");

    // The icon is valid even without a Material
    ExtendedIcon icon = new ExtendedIcon();

    if (section.isSet(Nodes.ID)) {
      try {
        ItemStackReader itemReader = new ItemStackReader(section.getString(Nodes.ID), true);
        icon.setMaterial(itemReader.getMaterial());
        icon.setDataValue(itemReader.getDataValue());
        icon.setAmount(itemReader.getAmount());
      } catch (FormatException e) {
        errorLogger.addError(
            "The icon \"" + iconName + "\" in the menu \"" + menuFileName + "\" has an invalid ID: "
                + e.getMessage());
      }
    }

    if (section.isSet(Nodes.AMOUNT)) {
      icon.setAmount(section.getInt(Nodes.AMOUNT));
    }

    if (section.isSet(Nodes.DURABILITY)) {
      icon.setDataValue((short) section.getInt(Nodes.DURABILITY));
    } else if (section.isSet(Nodes.DATA_VALUE)) { // Alias
      icon.setDataValue((short) section.getInt(Nodes.DATA_VALUE));
    }

    if (section.isSet(Nodes.NBT_DATA)) {
      String nbtData = section.getString(Nodes.NBT_DATA);
      try {
        // Check that NBT has valid syntax before applying it to the icon
        MojangsonParser.parse(nbtData);
        icon.setNBTData(nbtData);
      } catch (MojangsonParseException e) {
        errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
            + "\" has an invalid NBT-DATA: " + e.getMessage());
      }
    }

    icon.setName(AsciiPlaceholders
        .placeholdersToSymbols(FormatUtils.colorizeName(section.getString(Nodes.NAME))));
    icon.setLore(AsciiPlaceholders
        .placeholdersToSymbols(FormatUtils.colorizeLore(section.getStringList(Nodes.LORE))));

    if (section.isSet(Nodes.ENCHANT)) {
      icon.setEnchantments(EnchantmentSerializer
          .loadEnchantments(section.getString(Nodes.ENCHANT), iconName, menuFileName, errorLogger));
    }

    if (section.isSet(Nodes.COLOR)) {
      try {
        icon.setColor(ItemUtils.parseColor(section.getString(Nodes.COLOR)));
      } catch (FormatException e) {
        errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
            + "\" has an invalid COLOR: " + e.getMessage());
      }
    }

    icon.setSkullOwner(section.getString(Nodes.SKULL_OWNER));

    if (section.isSet(Nodes.BANNER_COLOR)) {
      try {
        icon.setBannerColor(ItemUtils.parseDyeColor(section.getString(Nodes.BANNER_COLOR)));
      } catch (FormatException e) {
        errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
            + "\" has an invalid BASE-COLOUR: " + e.getMessage());
      }
    }

    if (section.isSet(Nodes.BANNER_PATTERNS)) {
      try {
        icon.setBannerPatterns(
            ItemUtils.parseBannerPatternList(section.getStringList(Nodes.BANNER_PATTERNS)));
      } catch (FormatException e) {
        errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
            + "\" has an invalid PATTERN-LIST: " + e.getMessage());
      }
    }

    List<FireworkEffect> fireworkEffects = new ArrayList<>();
    if (section.isSet(Nodes.FIREWORK)) {
      try {
        if (section.isList(Nodes.FIREWORK)) {
          for (String firework : section.getStringList(Nodes.FIREWORK)) {
            fireworkEffects.add(ItemUtils.parseFireworkEffect(firework));
          }
        } else {
          for (String firework : section.getString(Nodes.FIREWORK).split(" ")) {
            fireworkEffects.add(ItemUtils.parseFireworkEffect(firework));
          }
        }
        icon.setFireworkEffects(fireworkEffects);
      } catch (FormatException e) {
        errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
            + "\" has an invalid FIREWORK: " + e.getMessage());
      }
    }

    icon.setPermission(section.getString(Nodes.PERMISSION));
    icon.setPermissionMessage(FormatUtils.addColors(section.getString(Nodes.PERMISSION_MESSAGE)));
    icon.setViewPermission(section.getString(Nodes.VIEW_PERMISSION));

    boolean closeOnClick = !section.getBoolean(Nodes.KEEP_OPEN);
    icon.setCloseOnClick(closeOnClick);

    if (section.isConfigurationSection(Nodes.COMMAND)) {
      // LEFT COMMAND
      if (section.isSet(Nodes.COMMAND_LEFT)) {

        List<IconCommand> commands;

        if (section.isList(Nodes.COMMAND_LEFT)) {
          commands = Utils.newArrayList();

          for (String commandString : section.getStringList(Nodes.COMMAND_LEFT)) {
            if (commandString.isEmpty()) {
              continue;
            }
            commands.add(CommandSerializer.matchCommand(commandString));
          }

        } else {
          commands = CommandSerializer.readCommands(section.getString(Nodes.COMMAND_LEFT));
        }

        icon.setClickLeftHandler(new CommandsClickHandler(commands, closeOnClick));
      }
      // RIGHT COMMAND
      if (section.isSet(Nodes.COMMAND_RIGHT)) {

        List<IconCommand> commands;

        if (section.isList(Nodes.COMMAND_RIGHT)) {
          commands = Utils.newArrayList();

          for (String commandString : section.getStringList(Nodes.COMMAND_RIGHT)) {
            if (commandString.isEmpty()) {
              continue;
            }
            commands.add(CommandSerializer.matchCommand(commandString));
          }

        } else {
          commands = CommandSerializer.readCommands(section.getString(Nodes.COMMAND_RIGHT));
        }

        icon.setClickRightHandler(new CommandsClickHandler(commands, closeOnClick));
      }
      // MIDDLE COMMAND
      if (section.isSet(Nodes.COMMAND_MIDDLE)) {

        List<IconCommand> commands;

        if (section.isList(Nodes.COMMAND_MIDDLE)) {
          commands = Utils.newArrayList();

          for (String commandString : section.getStringList(Nodes.COMMAND_MIDDLE)) {
            if (commandString.isEmpty()) {
              continue;
            }
            commands.add(CommandSerializer.matchCommand(commandString));
          }

        } else {
          commands = CommandSerializer.readCommands(section.getString(Nodes.COMMAND_MIDDLE));
        }

        icon.setClickMiddleHandler(new CommandsClickHandler(commands, closeOnClick));
      }
    } else if (section.isSet(Nodes.COMMAND)) {

      List<IconCommand> commands;

      if (section.isList(Nodes.COMMAND)) {
        commands = Utils.newArrayList();

        for (String commandString : section.getStringList(Nodes.COMMAND)) {
          if (commandString.isEmpty()) {
            continue;
          }
          commands.add(CommandSerializer.matchCommand(commandString));
        }

      } else {
        commands = CommandSerializer.readCommands(section.getString(Nodes.COMMAND));
      }

      icon.setClickHandler(new CommandsClickHandler(commands, closeOnClick));
    }



    double price = section.getDouble(Nodes.PRICE);
    if (price > 0.0) {
      icon.setMoneyPrice(price);
    } else if (price < 0.0) {
      errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
          + "\" has a negative PRICE: " + price);
    }

    int points = section.getInt(Nodes.POINTS);
    if (points > 0) {
      icon.setPlayerPointsPrice(points);
    } else if (points < 0) {
      errorLogger.addError(
          "The icon \"" + iconName + "\" in the menu \"" + menuFileName + "\" has negative POINTS: "
              + points);
    }

    long tokens = section.getLong(Nodes.TOKENS);
    if (tokens > 0) {
      icon.setTokenManagerPrice(tokens);
    } else if (tokens < 0) {
      errorLogger.addError(
          "The icon \"" + iconName + "\" in the menu \"" + menuFileName + "\" has negative TOKENS: "
              + points);
    }

    int levels = section.getInt(Nodes.EXP_LEVELS);
    if (levels > 0) {
      icon.setExpLevelsPrice(levels);
    } else if (levels < 0) {
      errorLogger.addError(
          "The icon \"" + iconName + "\" in the menu \"" + menuFileName + "\" has negative LEVELS: "
              + levels);
    }

    if (section.isConfigurationSection(Nodes.COOLDOWN)) {
      // LEFT CLICK COOLDOWN
      if (section.isSet(Nodes.COOLDOWN_LEFT)) {
        long cooldown = section.getLong(Nodes.COOLDOWN_LEFT);
        icon.setLeftCooldown(cooldown);
      }
      // RIGHT CLICK COOLDOWN
      if (section.isSet(Nodes.COOLDOWN_RIGHT)) {
        long cooldown = section.getLong(Nodes.COOLDOWN_RIGHT);
        icon.setRightCooldown(cooldown);
      }
      // MIDDLE CLICK COOLDOWN
      if (section.isSet(Nodes.COOLDOWN_MIDDLE)) {
        long cooldown = section.getLong(Nodes.COOLDOWN_MIDDLE);
        icon.setMiddleCooldown(cooldown);
      }
    } else if (section.isSet(Nodes.COOLDOWN)) {
      long cooldown = section.getLong(Nodes.COOLDOWN);
      icon.setLeftCooldown(cooldown);
      icon.setRightCooldown(cooldown);
      icon.setMiddleCooldown(cooldown);
      icon.setCooldownAll(true);
    }
    icon.setCooldownMessage(section.getString(Nodes.COOLDOWN_MESSAGE));

    if (section.isConfigurationSection(Nodes.REQUIRED_ITEM)) {
      // LEFT REQUIRED ITEMS
      if (section.isSet(Nodes.REQUIRED_ITEM_LEFT)) {
        List<String> requiredItemsStrings;
        if (section.isList(Nodes.REQUIRED_ITEM_LEFT)) {
          requiredItemsStrings = section.getStringList(Nodes.REQUIRED_ITEM_LEFT);
        } else if (section.isString(Nodes.REQUIRED_ITEM_LEFT) && section
            .getString(Nodes.REQUIRED_ITEM_LEFT)
            .contains(";")) {
          requiredItemsStrings = Arrays
              .asList(section.getString(Nodes.REQUIRED_ITEM_LEFT).split(";"));
        } else {
          requiredItemsStrings = Collections
              .singletonList(section.getString(Nodes.REQUIRED_ITEM_LEFT));
        }

        List<RequiredItem> requiredItems = new ArrayList<>();
        for (String requiredItemText : requiredItemsStrings) {
          try {
            ItemStackReader itemReader = new ItemStackReader(requiredItemText, true);
            RequiredItem requiredItem = new RequiredItem(itemReader);

            requiredItems.add(requiredItem);
          } catch (FormatException e) {
            errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
                + "\" has an invalid REQUIRED-ITEM.LEFT: " + e.getMessage());
          }
        }
        icon.setLeftRequiredItems(requiredItems);
      }
      // RIGHT REQUIRED ITEMS
      if (section.isSet(Nodes.REQUIRED_ITEM_RIGHT)) {
        List<String> requiredItemsStrings;
        if (section.isList(Nodes.REQUIRED_ITEM_RIGHT)) {
          requiredItemsStrings = section.getStringList(Nodes.REQUIRED_ITEM_RIGHT);
        } else if (section.isString(Nodes.REQUIRED_ITEM_RIGHT) && section
            .getString(Nodes.REQUIRED_ITEM_RIGHT)
            .contains(";")) {
          requiredItemsStrings = Arrays
              .asList(section.getString(Nodes.REQUIRED_ITEM_RIGHT).split(";"));
        } else {
          requiredItemsStrings = Collections
              .singletonList(section.getString(Nodes.REQUIRED_ITEM_RIGHT));
        }

        List<RequiredItem> requiredItems = new ArrayList<>();
        for (String requiredItemText : requiredItemsStrings) {
          try {
            ItemStackReader itemReader = new ItemStackReader(requiredItemText, true);
            RequiredItem requiredItem = new RequiredItem(itemReader);

            requiredItems.add(requiredItem);
          } catch (FormatException e) {
            errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
                + "\" has an invalid REQUIRED-ITEM.RIGHT: " + e.getMessage());
          }
        }
        icon.setRightRequiredItems(requiredItems);
      }
      // MIDDLE REQUIRED ITEMS
      if (section.isSet(Nodes.REQUIRED_ITEM_MIDDLE)) {
        List<String> requiredItemsStrings;
        if (section.isList(Nodes.REQUIRED_ITEM_MIDDLE)) {
          requiredItemsStrings = section.getStringList(Nodes.REQUIRED_ITEM_MIDDLE);
        } else if (section.isString(Nodes.REQUIRED_ITEM_MIDDLE) && section
            .getString(Nodes.REQUIRED_ITEM_MIDDLE)
            .contains(";")) {
          requiredItemsStrings = Arrays
              .asList(section.getString(Nodes.REQUIRED_ITEM_MIDDLE).split(";"));
        } else {
          requiredItemsStrings = Collections
              .singletonList(section.getString(Nodes.REQUIRED_ITEM_MIDDLE));
        }

        List<RequiredItem> requiredItems = new ArrayList<>();
        for (String requiredItemText : requiredItemsStrings) {
          try {
            ItemStackReader itemReader = new ItemStackReader(requiredItemText, true);
            RequiredItem requiredItem = new RequiredItem(itemReader);

            requiredItems.add(requiredItem);
          } catch (FormatException e) {
            errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
                + "\" has an invalid REQUIRED-ITEM.MIDDLE: " + e.getMessage());
          }
        }
        icon.setMiddleRequiredItems(requiredItems);
      }
    } else if (section.isSet(Nodes.REQUIRED_ITEM)) {
      List<String> requiredItemsStrings;
      if (section.isList(Nodes.REQUIRED_ITEM)) {
        requiredItemsStrings = section.getStringList(Nodes.REQUIRED_ITEM);
      } else if (section.isString(Nodes.REQUIRED_ITEM) && section.getString(Nodes.REQUIRED_ITEM)
          .contains(";")) {
        requiredItemsStrings = Arrays.asList(section.getString(Nodes.REQUIRED_ITEM).split(";"));
      } else {
        requiredItemsStrings = Collections.singletonList(section.getString(Nodes.REQUIRED_ITEM));
      }

      List<RequiredItem> requiredItems = new ArrayList<>();
      for (String requiredItemText : requiredItemsStrings) {
        try {
          ItemStackReader itemReader = new ItemStackReader(requiredItemText, true);
          RequiredItem requiredItem = new RequiredItem(itemReader);

          requiredItems.add(requiredItem);
        } catch (FormatException e) {
          errorLogger.addError("The icon \"" + iconName + "\" in the menu \"" + menuFileName
              + "\" has an invalid REQUIRED-ITEM: " + e.getMessage());
        }
      }
      icon.setRequiredItems(requiredItems);
    }

    return icon;
  }

  public static Coords loadCoordsFromSection(ConfigurationSection section) {
    Validate.notNull(section, "ConfigurationSection cannot be null");

    Integer x = null;
    Integer y = null;

    if (section.isInt(Nodes.SLOT)) {
      int slot = section.getInt(Nodes.SLOT);
      if (slot > 9) {
        x = slot;
        y = 1;
        while (x > 9) {
          y += 1;
          x -= 9;
        }
      } else {
        x = slot;
        y = 1;
      }
    } else {
      if (section.isInt(Nodes.POSITION_X)) {
        x = section.getInt(Nodes.POSITION_X);
      }

      if (section.isInt(Nodes.POSITION_Y)) {
        y = section.getInt(Nodes.POSITION_Y);
      }
    }

    return new Coords(x, y);
  }

  public static class Coords {

    private Integer x, y;

    protected Coords(Integer x, Integer y) {
      this.x = x;
      this.y = y;
    }

    public boolean isSetX() {
      return x != null;
    }

    public boolean isSetY() {
      return y != null;
    }

    public Integer getX() {
      return x;
    }

    public Integer getY() {
      return y;
    }
  }

  private static class Nodes {

    public static final String
        ID = "ID",
        AMOUNT = "AMOUNT",
        DATA_VALUE = "DATA-VALUE",
        DURABILITY = "DURABILITY",
        NBT_DATA = "NBT-DATA",
        NAME = "NAME",
        LORE = "LORE",
        ENCHANT = "ENCHANTMENT",
        COLOR = "COLOR",
        SKULL_OWNER = "SKULL-OWNER",
        BANNER_COLOR = "BANNER-COLOUR",
        BANNER_PATTERNS = "BANNER-PATTERNS",
        COMMAND = "COMMAND",
        COMMAND_LEFT = "COMMAND.LEFT",
        COMMAND_RIGHT = "COMMAND.RIGHT",
        COMMAND_MIDDLE = "COMMAND.MIDDLE",
        PRICE = "PRICE",
        POINTS = "POINTS",
        TOKENS = "TOKENS",
        EXP_LEVELS = "LEVELS",
        REQUIRED_ITEM = "REQUIRED-ITEM",
        REQUIRED_ITEM_LEFT = "REQUIRED-ITEM.LEFT",
        REQUIRED_ITEM_RIGHT = "REQUIRED-ITEM.RIGHT",
        REQUIRED_ITEM_MIDDLE = "REQUIRED-ITEM.MIDDLE",
        PERMISSION = "PERMISSION",
        PERMISSION_MESSAGE = "PERMISSION-MESSAGE",
        VIEW_PERMISSION = "VIEW-PERMISSION",
        KEEP_OPEN = "KEEP-OPEN",
        SLOT = "SLOT",
        POSITION_X = "POSITION-X",
        POSITION_Y = "POSITION-Y",
        FIREWORK = "FIREWORK",
        COOLDOWN = "COOLDOWN",
        COOLDOWN_LEFT = "COOLDOWN.LEFT",
        COOLDOWN_RIGHT = "COOLDOWN.RIGHT",
        COOLDOWN_MIDDLE = "COOLDOWN.MIDDLE",
        COOLDOWN_MESSAGE = "COOLDOWN-MESSAGE";
  }

}
