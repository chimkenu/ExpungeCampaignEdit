package me.chimkenu.expungecampaignedit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;

public class EditWorld {
    private final File sourceWorldFolder;
    private File activeWorldFolder;
    private World world;

    public EditWorld(File sourceWorldFolder) throws RuntimeException {
        this.sourceWorldFolder = sourceWorldFolder;
        if (!load()) throw new RuntimeException("World failed to load");
    }

    public boolean load() {
        if (isLoaded()) return true;

        this.activeWorldFolder = new File(Bukkit.getWorldContainer().getParentFile(), sourceWorldFolder.getName() + "_" + System.currentTimeMillis());

        try {
            FileUtils.copyDirectory(sourceWorldFolder.toPath().toString(), activeWorldFolder.toPath().toString());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to copy source to world folder " + activeWorldFolder.getName());
            e.printStackTrace();
            return false;
        }

        world = Bukkit.createWorld(new WorldCreator(activeWorldFolder.getName()));
        if (world != null) world.setAutoSave(false);
        return isLoaded();
    }

    public void unload(boolean save) {
        if (world != null) {
            world.getPlayers().forEach(player -> player.teleport(new Location(Bukkit.getWorld("world"), 0, 0, 0)));
            Bukkit.unloadWorld(world, save);
        }
        if (activeWorldFolder != null) {
            try {
                FileUtils.deleteDirectory(activeWorldFolder.toPath());
            } catch (IOException e) {
                Bukkit.getLogger().severe("Failed to delete " + activeWorldFolder.getName());
                e.printStackTrace();
            }
        }

        world = null;
        activeWorldFolder = null;
    }

    public boolean isLoaded() {
        return world != null;
    }

    public World getWorld() {
        return world;
    }

    public boolean saveToSource() {
        world.save();
        try {
            FileUtils.copyDirectory(activeWorldFolder.toPath().toString(), sourceWorldFolder.toPath().toString());
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save changes from " + activeWorldFolder.getName() + " to " + sourceWorldFolder.getName());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
