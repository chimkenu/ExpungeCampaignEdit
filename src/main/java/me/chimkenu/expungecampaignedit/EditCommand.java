package me.chimkenu.expungecampaignedit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditCommand implements CommandExecutor, TabCompleter {
    private final ExpungeCampaignEdit plugin;

    public EditCommand(ExpungeCampaignEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("expunge-campaign-edit")) {
            sender.sendMessage(Component.text("Insufficient permissions.", NamedTextColor.RED));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players may use this command", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(Component.text("Insufficient arguments.", NamedTextColor.RED).append(Component.text(" /edit <load|save|close> [directory]", NamedTextColor.GRAY)));
            return true;
        }

        if (args[0].equalsIgnoreCase("load")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Please provide a directory.", NamedTextColor.RED).append(Component.text(" /edit load <directory>", NamedTextColor.GRAY)));
                return true;
            }

            File map = new File(plugin.EXPUNGE_MAPS, args[1]);
            if (!map.exists()) {
                sender.sendMessage(Component.text("This directory doesn't exist, did you type it correctly?", NamedTextColor.RED));
                return true;
            }

            sender.sendMessage(Component.text("Creating a new world...", NamedTextColor.GREEN)
                    .append(Component.text(" Do ", NamedTextColor.GRAY))
                    .append(Component.text("/edit save", NamedTextColor.YELLOW))
                    .append(Component.text(" to save your work!", NamedTextColor.GRAY)));
            try {
                EditWorld world = new EditWorld(map);
                plugin.worlds.add(world);
                player.teleport(new Location(world.getWorld(), 0, 0, 0));
                player.setGameMode(GameMode.CREATIVE);
                player.setFlying(true);
            } catch (RuntimeException e) {
                sender.sendMessage(Component.text("Something went wrong, please check the logs. Sorry!", NamedTextColor.RED));
                return true;
            }

        } else if (args[0].equalsIgnoreCase("save")) {
            for (EditWorld editWorld : plugin.worlds) {
                if (editWorld.getWorld().equals(player.getWorld())) {
                    sender.sendMessage(Component.text("Saving edit world...", NamedTextColor.YELLOW));
                    if (editWorld.saveToSource()) {
                        sender.sendMessage(Component.text("Saved!", NamedTextColor.GREEN));
                    } else {
                        sender.sendMessage(Component.text("Something went wrong, check the logs for more info.", NamedTextColor.RED));
                    }
                    return true;
                }
            }
            sender.sendMessage(Component.text("You're not in an edit world?", NamedTextColor.RED));

        } else if (args[0].equalsIgnoreCase("close")) {
            if (args.length < 2) {
                sender.sendMessage(Component.text("Are you sure? Make sure to save before running this command! To confirm, type ", NamedTextColor.RED).append(Component.text("/edit close confirm", NamedTextColor.YELLOW)));
                return true;
            }

            if (!args[1].equals("confirm")) {
                sender.sendMessage(Component.text("Did you type that correctly?", NamedTextColor.RED));
                return true;
            }

            EditWorld editWorld = null;
            for (EditWorld world : plugin.worlds) {
                if (world.getWorld().equals(player.getWorld())) {
                    sender.sendMessage(Component.text("Closing edit world...", NamedTextColor.GREEN));
                    world.unload(false);
                    editWorld = world;
                    break;
                }
            }

            if (editWorld != null) {
                plugin.worlds.remove(editWorld);
                return true;
            }
            sender.sendMessage(Component.text("You're not in an edit world?", NamedTextColor.RED));

        } else {
            sender.sendMessage(Component.text("Unknown arguments.", NamedTextColor.RED).append(Component.text(" /edit <load|save|close> [directory]", NamedTextColor.GRAY)));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 1) {
            return List.of("save", "load", "close");
        } else {
            String[] children = plugin.EXPUNGE_MAPS.list();
            if (children == null) return null;

            List<String> dirs = new ArrayList<>();
            for (String s : children) {
                if (s.equalsIgnoreCase("config.yml")) continue;
                String[] childrenChildren = new File(plugin.EXPUNGE_MAPS, s).list();
                if (childrenChildren == null) {
                    dirs.add(s);
                } else {
                    for (String c : childrenChildren) {
                        dirs.add(s + File.separator + c);
                    }
                }
            }
            return dirs;
        }
    }
}
