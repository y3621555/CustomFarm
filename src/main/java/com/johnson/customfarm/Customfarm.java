package com.johnson.customfarm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Customfarm extends JavaPlugin {

    private Set<Material> crops = new HashSet<>();
    private Set<Material> ores = new HashSet<>();
    private Map<Material, PickaxeConfig> allowedPickaxes = new HashMap<>();
    private Map<String, Integer> rarityChances = new HashMap<>();
    private Map<String, ChatColor> rarityColors = new HashMap<>();
    private Map<Material, String> oreNames = new HashMap<>();
    private Map<Material, String> cropNames = new HashMap<>();
    private Database database;

    @Override
    public void onEnable() {
        // 加載配置文件
        saveDefaultConfig();
        loadMaterialsFromConfig();

        // 初始化資料庫
        database = new Database();

        // 註冊事件
        Bukkit.getPluginManager().registerEvents(new MiningAndFarmingListener(this, crops, ores, allowedPickaxes, rarityChances, rarityColors, oreNames, cropNames, database), this);

        // 註冊指令
        SkillCommand skillCommand = new SkillCommand(database);
        if (getCommand("skill") != null) {
            this.getCommand("skill").setExecutor(skillCommand);
            this.getCommand("skill").setTabCompleter(skillCommand);
        } else {
            getLogger().severe("指令 'skill' 未在 plugin.yml 中定義！");
        }

        getLogger().info("CustomMiningAndFarming已啟動!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomMiningAndFarming已停用!");
    }

    private void loadMaterialsFromConfig() {
        FileConfiguration config = getConfig();
        List<String> cropList = config.getStringList("crops");
        List<String> oreList = config.getStringList("ores");

        for (String crop : cropList) {
            try {
                crops.add(Material.valueOf(crop));
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的農作物類型: " + crop);
            }
        }

        for (String ore : oreList) {
            try {
                ores.add(Material.valueOf(ore));
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的礦物類型: " + ore);
            }
        }

        for (String key : config.getConfigurationSection("allowed_pickaxes").getKeys(false)) {
            try {
                Material pickaxe = Material.valueOf(key);
                int duration = config.getInt("allowed_pickaxes." + key + ".duration");
                int requiredSkill = config.getInt("allowed_pickaxes." + key + ".required_skill");
                allowedPickaxes.put(pickaxe, new PickaxeConfig(duration, requiredSkill));
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的鎬子類型: " + key);
            }
        }

        for (String rarity : config.getConfigurationSection("rarity_chances").getKeys(false)) {
            try {
                int chance = config.getInt("rarity_chances." + rarity);
                rarityChances.put(rarity.toUpperCase(), chance);
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的稀有度機率: " + rarity);
            }
        }

        for (String rarity : config.getConfigurationSection("rarity_colors").getKeys(false)) {
            try {
                ChatColor color = ChatColor.valueOf(config.getString("rarity_colors." + rarity));
                rarityColors.put(rarity.toUpperCase(), color);
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的稀有度顏色: " + rarity);
            }
        }

        for (String ore : config.getConfigurationSection("ore_names").getKeys(false)) {
            try {
                Material material = Material.valueOf(ore);
                String name = config.getString("ore_names." + ore);
                oreNames.put(material, name);
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的礦物名稱: " + ore);
            }
        }

        for (String crop : config.getConfigurationSection("crop_names").getKeys(false)) {
            try {
                Material material = Material.valueOf(crop);
                String name = config.getString("crop_names." + crop);
                cropNames.put(material, name);
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的農作物名稱: " + crop);
            }
        }
    }
}
