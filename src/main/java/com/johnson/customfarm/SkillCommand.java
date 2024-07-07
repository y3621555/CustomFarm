package com.johnson.customfarm;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class SkillCommand implements CommandExecutor, TabCompleter {
    private final Database database;

    public SkillCommand(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("skill")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "使用方法: /skill <check|add|remove|top>");
                return true;
            }
            String subCommand = args[0];
            switch (subCommand.toLowerCase()) {
                case "check":
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        UUID uuid = player.getUniqueId();
                        int miningSkill = database.getMiningSkill(uuid);
                        int farmingSkill = database.getFarmingSkill(uuid);
                        player.sendMessage(ChatColor.GREEN + "你的礦物熟練度: " + miningSkill);
                        player.sendMessage(ChatColor.GREEN + "你的農作物熟練度: " + farmingSkill);
                    } else {
                        sender.sendMessage(ChatColor.RED + "只有玩家可以使用此指令.");
                    }
                    break;
                case "add":
                    if (args.length < 4) {
                        sender.sendMessage(ChatColor.RED + "使用方法: /skill add <player> <mining|farming> <amount>");
                        return true;
                    }
                    if (sender.hasPermission("customfarm.skill.modify")) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "找不到玩家: " + args[1]);
                            return true;
                        }
                        UUID targetUuid = target.getUniqueId();
                        String skillType = args[2];
                        int amount;
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "無效的數量: " + args[3]);
                            return true;
                        }
                        for (int i = 0; i < amount; i++) {
                            if (skillType.equalsIgnoreCase("mining")) {
                                database.increaseMiningSkill(targetUuid);
                            } else if (skillType.equalsIgnoreCase("farming")) {
                                database.increaseFarmingSkill(targetUuid);
                            } else {
                                sender.sendMessage(ChatColor.RED + "無效的技能類型: " + skillType);
                                return true;
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "已增加 " + target.getName() + " 的 " + skillType + " 熟練度 " + amount + " 點.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令.");
                    }
                    break;
                case "remove":
                    if (args.length < 4) {
                        sender.sendMessage(ChatColor.RED + "使用方法: /skill remove <player> <mining|farming> <amount>");
                        return true;
                    }
                    if (sender.hasPermission("customfarm.skill.modify")) {
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(ChatColor.RED + "找不到玩家: " + args[1]);
                            return true;
                        }
                        UUID targetUuid = target.getUniqueId();
                        String skillType = args[2];
                        int amount;
                        try {
                            amount = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(ChatColor.RED + "無效的數量: " + args[3]);
                            return true;
                        }
                        for (int i = 0; i < amount; i++) {
                            if (skillType.equalsIgnoreCase("mining")) {
                                database.decreaseMiningSkill(targetUuid);
                            } else if (skillType.equalsIgnoreCase("farming")) {
                                database.decreaseFarmingSkill(targetUuid);
                            } else {
                                sender.sendMessage(ChatColor.RED + "無效的技能類型: " + skillType);
                                return true;
                            }
                        }
                        sender.sendMessage(ChatColor.GREEN + "已減少 " + target.getName() + " 的 " + skillType + " 熟練度 " + amount + " 點.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "你沒有權限使用此指令.");
                    }
                    break;
                case "top":
                    if (args.length < 2) {
                        sender.sendMessage(ChatColor.RED + "使用方法: /skill top <mining|farming>");
                        return true;
                    }
                    String topSkillType = args[1];
                    List<Map.Entry<UUID, Integer>> topPlayers;
                    if (topSkillType.equalsIgnoreCase("mining")) {
                        topPlayers = database.getTopMiningSkills(10);
                    } else if (topSkillType.equalsIgnoreCase("farming")) {
                        topPlayers = database.getTopFarmingSkills(10);
                    } else {
                        sender.sendMessage(ChatColor.RED + "無效的技能類型: " + topSkillType);
                        return true;
                    }
                    sender.sendMessage(ChatColor.GREEN + topSkillType + " 熟練度前 10 名:");
                    for (Map.Entry<UUID, Integer> entry : topPlayers) {
                        Player player = Bukkit.getPlayer(entry.getKey());
                        if (player != null) {
                            sender.sendMessage(ChatColor.GREEN + player.getName() + ": " + entry.getValue());
                        }
                    }
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "無效的子命令.");
                    break;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("skill")) {
            if (args.length == 1) {
                return Arrays.asList("check", "add", "remove", "top");
            } else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            } else if (args.length == 3 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
                return Arrays.asList("mining", "farming");
            }
        }
        return new ArrayList<>();
    }
}
