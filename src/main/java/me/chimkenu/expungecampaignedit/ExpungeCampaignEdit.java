package me.chimkenu.expungecampaignedit;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ExpungeCampaignEdit extends JavaPlugin {
    public File EXPUNGE_MAPS;
    public Set<EditWorld> worlds;

    @Override
    public void onEnable() {
        EXPUNGE_MAPS = Objects.requireNonNull(getServer().getPluginManager().getPlugin("Expunge")).getDataFolder();
        System.out.println(EXPUNGE_MAPS);

        worlds = new HashSet<>();

        Objects.requireNonNull(getCommand("edit")).setExecutor(new EditCommand(this));
    }

    @Override
    public void onDisable() {
        for (EditWorld world : worlds) {
            world.unload(true);
        }
    }
}
