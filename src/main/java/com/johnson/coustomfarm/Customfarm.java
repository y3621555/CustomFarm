package com.johnson.coustomfarm;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Customfarm extends JavaPlugin {

    @Override
    public void onEnable() {
        // 註冊事件
        Bukkit.getPluginManager().registerEvents(new MiningAndFarmingListener(this), this);

        getLogger().info("CustomMiningAndFarming已啟動!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomMiningAndFarming已停用!");
    }
}
