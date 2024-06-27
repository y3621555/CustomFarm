package com.johnson.coustomfarm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MiningAndFarmingListener implements Listener {
    private final Customfarm plugin;
    private final Random random = new Random();
    private final Set<Block> processingBlocks = new HashSet<>();

    public MiningAndFarmingListener(Customfarm plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // 檢查是否為正在處理中的方塊
        if (processingBlocks.contains(block)) {
            player.sendMessage(ChatColor.RED + "這個方塊已經在處理中，請稍候!");
            event.setCancelled(true);
            return;
        }

        // 檢查是否為礦物或農作物
        if (isOre(blockType) || isCrop(blockType)) {
            if (isCrop(blockType) && !isMature(block)) {
                player.sendMessage(ChatColor.RED + "這個農作物還沒有成熟!");
                return;
            }

            event.setCancelled(true);
            processingBlocks.add(block);

            // 顯示讀條
            new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count >= 5) { // 假設讀條持續5秒
                        // 生成物品並設置稀有度
                        ItemStack item = new ItemStack(blockType);
                        ItemMeta meta = item.getItemMeta();
                        int rarity = random.nextInt(5) + 1;
                        String rarityLore = getRarityLore(rarity);
                        String rarityStars = getRarityStars(rarity);
                        meta.setDisplayName(ChatColor.YELLOW + blockType.name() + " " + rarityStars);
                        meta.setLore(Collections.singletonList(ChatColor.GRAY + "稀有度: " + rarityLore));
                        item.setItemMeta(meta);

                        // 掉落物品給玩家
                        player.getInventory().addItem(item);
                        player.sendMessage(ChatColor.GREEN + "你獲得了一個" + rarityLore + "的" + blockType.name() + "!");

                        // 增加熟練度
                        if (isOre(blockType)) {
                            addMiningExperience(player);
                        } else {
                            addFarmingExperience(player);
                        }

                        block.setType(Material.AIR); // 清除方塊
                        processingBlocks.remove(block);
                        cancel();
                    } else {
                        player.sendTitle(ChatColor.YELLOW + "正在挖掘 " + blockType.name(), ChatColor.YELLOW + "" + (count * 20) + "%", 10, 70, 20);
                    }
                    count++;
                }
            }.runTaskTimer(plugin, 0, 20); // 每秒運行一次
        }
    }

    private boolean isOre(Material material) {
        // 定義哪些是礦物
        return material == Material.COAL_ORE || material == Material.IRON_ORE || material == Material.GOLD_ORE;
    }

    private boolean isCrop(Material material) {
        // 定義哪些是農作物
        return material == Material.WHEAT || material == Material.POTATO || material == Material.CARROT;
    }

    private boolean isMature(Block block) {
        if (block.getBlockData() instanceof Ageable) {
            Ageable ageable = (Ageable) block.getBlockData();
            return ageable.getAge() == ageable.getMaximumAge();
        }
        return false;
    }

    private String getRarityLore(int rarity) {
        switch (rarity) {
            case 1:
                return "普通";
            case 2:
                return "稀有";
            case 3:
                return "罕見";
            case 4:
                return "史詩";
            case 5:
                return "傳說";
            default:
                return "未知";
        }
    }

    private String getRarityStars(int rarity) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rarity; i++) {
            stars.append("⭐");
        }
        return stars.toString();
    }

    private void addMiningExperience(Player player) {
        // 增加玩家的礦物熟練度
    }

    private void addFarmingExperience(Player player) {
        // 增加玩家的農作物熟練度
    }
}