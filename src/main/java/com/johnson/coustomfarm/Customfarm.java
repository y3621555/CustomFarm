package com.johnson.coustomfarm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Customfarm extends JavaPlugin {

    private Set<Material> crops = new HashSet<>();
    private Set<Material> ores = new HashSet<>();
    private Map<Material, Integer> allowedPickaxes = new HashMap<>();

    @Override
    public void onEnable() {
        // 加載配置文件
        saveDefaultConfig();
        loadMaterialsFromConfig();

        // 註冊事件
        Bukkit.getPluginManager().registerEvents(new MiningAndFarmingListener(this, crops, ores, allowedPickaxes), this);
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
                int duration = config.getInt("allowed_pickaxes." + key);
                allowedPickaxes.put(pickaxe, duration);
            } catch (IllegalArgumentException e) {
                getLogger().warning("配置文件中無效的鎬子類型: " + key);
            }
        }
    }
}
