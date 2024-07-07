package com.johnson.customfarm;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MiningAndFarmingListener implements Listener {
    //Customfarm
    private final Customfarm plugin;
    private final Random random = new Random();
    private final Set<Block> processingBlocks = new HashSet<>();
    private final Set<Material> crops;
    private final Set<Material> ores;
    private final Map<Material, PickaxeConfig> allowedPickaxes;
    private final Map<String, Integer> rarityChances;
    private final Map<String, ChatColor> rarityColors;
    private final Map<Material, String> oreNames;
    private final Map<Material, String> cropNames;
    private final Database database;

    public MiningAndFarmingListener(Customfarm plugin, Set<Material> crops, Set<Material> ores, Map<Material, PickaxeConfig> allowedPickaxes, Map<String, Integer> rarityChances, Map<String, ChatColor> rarityColors, Map<Material, String> oreNames, Map<Material, String> cropNames, Database database) {
        this.plugin = plugin;
        this.crops = crops;
        this.ores = ores;
        this.allowedPickaxes = allowedPickaxes;
        this.rarityChances = rarityChances;
        this.rarityColors = rarityColors;
        this.oreNames = oreNames;
        this.cropNames = cropNames;
        this.database = database;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // 檢查是否為礦物或農作物
        if (ores.contains(blockType) || crops.contains(blockType)) {

            // 如果是礦物，檢查玩家是否使用允許的鎬子並且有足夠的熟練度
            if (ores.contains(blockType)) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                Material itemType = itemInHand.getType();
                if (!allowedPickaxes.containsKey(itemType)) {
                    player.sendMessage(ChatColor.RED + "你不能使用這種工具來挖礦!");
                    event.setCancelled(true);
                    return;
                }

                PickaxeConfig config = allowedPickaxes.get(itemType);
                int requiredSkill = config.getRequiredSkill();
                int playerSkill = database.getMiningSkill(player.getUniqueId());

                if (playerSkill < requiredSkill) {
                    player.sendMessage(ChatColor.RED + "你的熟練度不足以使用這種工具! 需要的熟練度: " + requiredSkill);
                    event.setCancelled(true);
                    return;
                }
            }

            // 檢查農作物是否成熟
            if (crops.contains(blockType) && !isMature(block)) {
                player.sendMessage(ChatColor.RED + "這個農作物還沒有成熟!");
                event.setCancelled(true);
                return;
            }

            // 檢查是否為正在處理中的方塊
            if (processingBlocks.contains(block)) {
                player.sendMessage(ChatColor.RED + "這個方塊已經在處理中，請稍候!");
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
            processingBlocks.add(block);

            // 讀取鎬子的挖礦時間，如果是農作物則使用默認時間
            int duration = ores.contains(blockType) ? allowedPickaxes.get(player.getInventory().getItemInMainHand().getType()).getDuration() : 5;

            // 顯示讀條
            new BukkitRunnable() {
                int count = 0;

                @Override
                public void run() {
                    if (count >= duration) { // 按照配置文件中的挖礦時間
                        // 生成物品並設置稀有度
                        ItemStack item = new ItemStack(blockType);
                        ItemMeta meta = item.getItemMeta();
                        int rarity = getRarity();
                        String rarityLore = getRarityLore(rarity);
                        ChatColor rarityColor = getRarityColor(rarity);
                        String displayName = ores.contains(blockType) ? oreNames.getOrDefault(blockType, blockType.name()) : cropNames.getOrDefault(blockType, blockType.name());
                        meta.setDisplayName(ChatColor.RESET + displayName);
                        meta.setLore(Arrays.asList(
                                ChatColor.GRAY + "等級: " + getRarityStars(rarity),
                                rarityColor + "稀有度: " + rarityLore,
                                ores.contains(blockType) ? ChatColor.DARK_PURPLE + "類型: 礦物" : ChatColor.DARK_PURPLE + "類型: 農作物" // 類型標識
                        ));
                        item.setItemMeta(meta);

                        // 掉落物品給玩家
                        player.getInventory().addItem(item);
                        player.sendMessage(ChatColor.GREEN + "你獲得了一個" + rarityLore + "的" + displayName + "!");

                        // 增加熟練度
                        if (ores.contains(blockType)) {
                            database.increaseMiningSkill(player.getUniqueId());
                        } else {
                            database.increaseFarmingSkill(player.getUniqueId());
                        }

                        block.setType(Material.AIR); // 清除方塊
                        processingBlocks.remove(block);
                        cancel();
                    } else {
                        player.sendTitle(ChatColor.YELLOW + "正在挖掘 " + blockType.name(), ChatColor.YELLOW + "" + ((count * 100) / duration) + "%", 10, 70, 20);
                    }
                    count++;
                }
            }.runTaskTimer(plugin, 0, 20); // 每秒運行一次
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            List<String> lore = item.getItemMeta().getLore();
            if (lore != null && (lore.contains(ChatColor.DARK_PURPLE + "類型: 礦物") || lore.contains(ChatColor.DARK_PURPLE + "類型: 農作物"))) {
                event.getPlayer().sendMessage(ChatColor.RED + "你不能放置這個自定義物品!");
                event.setCancelled(true);
            }
        }
    }

    private int getRarity() {
        int totalChance = rarityChances.values().stream().mapToInt(Integer::intValue).sum();
        int randomValue = random.nextInt(totalChance);

        int currentSum = 0;
        for (Map.Entry<String, Integer> entry : rarityChances.entrySet()) {
            currentSum += entry.getValue();
            if (randomValue < currentSum) {
                return getRarityValue(entry.getKey());
            }
        }
        return 1; // 默認為普通
    }

    private int getRarityValue(String rarity) {
        switch (rarity) {
            case "COMMON":
                return 1;
            case "RARE":
                return 2;
            case "UNCOMMON":
                return 3;
            case "EPIC":
                return 4;
            case "LEGENDARY":
                return 5;
            default:
                return 1;
        }
    }

    private ChatColor getRarityColor(int rarity) {
        switch (rarity) {
            case 1:
                return rarityColors.getOrDefault("COMMON", ChatColor.WHITE);
            case 2:
                return rarityColors.getOrDefault("RARE", ChatColor.BLUE);
            case 3:
                return rarityColors.getOrDefault("UNCOMMON", ChatColor.GREEN);
            case 4:
                return rarityColors.getOrDefault("EPIC", ChatColor.LIGHT_PURPLE);
            case 5:
                return rarityColors.getOrDefault("LEGENDARY", ChatColor.GOLD);
            default:
                return ChatColor.WHITE;
        }
    }

    private boolean isMature(Block block) {
        if (block.getType() == Material.MELON || block.getType() == Material.PUMPKIN) {
            return true; // 南瓜和西瓜不需要額外的成熟檢查
        }
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
}