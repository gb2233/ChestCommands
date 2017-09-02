package com.gmail.filoghost.chestcommands.api;

import com.gmail.filoghost.chestcommands.bridge.PlaceholderAPIBridge;
import com.gmail.filoghost.chestcommands.internal.Variable;
import com.gmail.filoghost.chestcommands.util.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("unused")
public class Icon {

    protected boolean closeOnClick;
    private Material material;
    private int amount;
    private short dataValue;
    private String name;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments;
    private Color color;
    private EntityType eggType;
    private String skullOwner;
    private DyeColor bannerColour;
    private List<Pattern> bannerPatterns;
    private ClickHandler clickHandler;

    private Set<Variable> nameVariables;
    private Map<Integer, Set<Variable>> loreVariables;
    private ItemStack cachedItem; // When there are no variables, we don't recreate the item.

    public Icon() {
        enchantments = new HashMap<>();
        closeOnClick = true;
    }

    public boolean hasVariables() {
        return nameVariables != null || loreVariables != null
                || PlaceholderAPIBridge.hasVariable(name) || PlaceholderAPIBridge.hasVariable(lore);
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        if (material == Material.AIR) material = null;
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        if (amount < 1) amount = 1;
        else if (amount > 127) amount = 127;

        this.amount = amount;
    }

    public short getDataValue() {
        return dataValue;
    }

    public void setDataValue(short dataValue) {
        if (dataValue < 0) dataValue = 0;

        this.dataValue = dataValue;
    }

    public void setName(String name) {
        this.name = name;
        this.nameVariables = null; // Reset the variables

        if (name != null) {
            for (Variable variable : Variable.values()) {
                if (name.contains(variable.getText())) {

                    if (nameVariables == null) {
                        nameVariables = new HashSet<>();
                    }

                    nameVariables.add(variable);
                }
            }
        }
    }

    public boolean hasName() {
        return name != null;
    }

    public void setLore(String... lore) {
        if (lore != null) {
            setLore(Arrays.asList(lore));
        }
    }

    public boolean hasLore() {
        return lore != null && lore.size() > 0;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
        this.loreVariables = null; // Reset the variables

        if (lore != null) {
            for (int i = 0; i < lore.size(); i++) {
                for (Variable variable : Variable.values()) {
                    if (lore.get(i).contains(variable.getText())) {

                        if (loreVariables == null) {
                            loreVariables = new HashMap<>();
                        }

                        Set<Variable> lineVariables = loreVariables.get(i);

                        if (lineVariables == null) {
                            lineVariables = new HashSet<>();
                            loreVariables.put(i, lineVariables);
                        }

                        lineVariables.add(variable);
                    }
                }
            }
        }
    }

    public Map<Enchantment, Integer> getEnchantments() {
        return new HashMap<>(enchantments);
    }

    public void setEnchantments(Map<Enchantment, Integer> enchantments) {
        if (enchantments == null) {
            this.enchantments.clear();
            return;
        }
        this.enchantments = enchantments;
    }

    public void addEnchantment(Enchantment ench) {
        addEnchantment(ench, 1);
    }

    public void addEnchantment(Enchantment ench, Integer level) {
        enchantments.put(ench, level);
    }

    public void removeEnchantment(Enchantment ench) {
        enchantments.remove(ench);
    }

    public void clearEnchantments() {
        enchantments.clear();
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public EntityType getEggType() {
        return eggType;
    }

    public void setEggType(EntityType eggType) {
        this.eggType = eggType;
    }

    public String getSkullOwner() {
        return skullOwner;
    }

    public void setSkullOwner(String skullOwner) {
        this.skullOwner = skullOwner;
    }

    public DyeColor getBannerColour() {
        return bannerColour;
    }

    public void setBannerColour(DyeColor bannerColour) {
        this.bannerColour = bannerColour;
    }

    public List<Pattern> getBannerPatterns() {
        return bannerPatterns;
    }

    public void setBannerPatterns(List<Pattern> bannerPatterns) {
        this.bannerPatterns = bannerPatterns;
    }

    public void setCloseOnClick(boolean closeOnClick) {
        this.closeOnClick = closeOnClick;
    }

    public ClickHandler getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(ClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    protected String calculateName(Player pov) {
        if (hasName()) {

            String name = this.name;

            if (pov != null && nameVariables != null) {
                for (Variable nameVariable : nameVariables) {
                    name = name.replace(nameVariable.getText(), nameVariable.getReplacement(pov));
                }
            }

            if (name.isEmpty()) {
                // Add a color to display the name empty.
                return ChatColor.WHITE.toString();
            } else {
                return name;
            }
        }

        return null;
    }

    protected List<String> calculateLore(Player pov) {

        List<String> output = null;

        if (hasLore()) {

            output = Utils.newArrayList();

            if (pov != null && loreVariables != null) {
                for (int i = 0; i < lore.size(); i++) {

                    String line = lore.get(i);

                    Set<Variable> lineVariables = loreVariables.get(i);
                    if (lineVariables != null) {
                        for (Variable lineVariable : lineVariables) {
                            line = line.replace(lineVariable.getText(), lineVariable.getReplacement(pov));
                        }
                    }

                    output.add(line);
                }
            } else {
                // Otherwise just copy the lines.
                output.addAll(lore);
            }
        }

        if (material == null) {

            if (output == null) {
                output = Utils.newArrayList();
            }

            // Add an error message.
            output.add(ChatColor.RED + "(Invalid material)");
        }

        return output;
    }

    public ItemStack createItemstack(Player pov) {

        if (!this.hasVariables() && cachedItem != null) {
            // Performance.
            return cachedItem;
        }

        // If the material is not set, display BEDROCK.
        ItemStack itemStack = (material != null) ? new ItemStack(material, amount, dataValue) : new ItemStack(Material.BEDROCK, amount);

        // Apply name, lore and color.
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(calculateName(pov));
        itemMeta.setLore(calculateLore(pov));

        if (color != null && itemMeta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) itemMeta).setColor(color);
        }

        if (eggType != null && itemMeta instanceof SpawnEggMeta) {
            ((SpawnEggMeta) itemMeta).setSpawnedType(eggType);
        }

        if (skullOwner != null && itemMeta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) itemMeta;
            if(skullOwner.equalsIgnoreCase("{player}")) {
                skullMeta.setOwner(pov.getName());
            } else {
                skullMeta.setOwner(skullOwner);
            }
        }

        if (bannerColour != null && itemMeta instanceof BannerMeta) {
            BannerMeta bannerMeta = (BannerMeta) itemMeta;
            bannerMeta.setBaseColor(bannerColour);
            if (bannerPatterns != null) {
                ((BannerMeta) itemMeta).setPatterns(bannerPatterns);
            }
        }

        itemStack.setItemMeta(itemMeta);

        // Apply enchants.
        if (enchantments.size() > 0) {
            for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                itemStack.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            }
        }

        if (!this.hasVariables()) {
            // If there are no variables, cache the item.
            cachedItem = itemStack;
        }

        // Apply placeholders
        itemStack = PlaceholderAPIBridge.replace(pov, itemStack);

        return itemStack;
    }

    public boolean onClick(Player whoClicked) {
        if (clickHandler != null) {
            return clickHandler.onClick(whoClicked);
        }

        return closeOnClick;
    }
}
