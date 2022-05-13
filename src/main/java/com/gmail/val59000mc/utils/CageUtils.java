package com.gmail.val59000mc.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;

public class CageUtils {

    private static Clipboard glassCage = null;
    private static Clipboard noCage = null;

    private static void loadCages() {
        File file = WorldEdit.getInstance().getWorkingDirectoryPath("schematics/cage.schem").toFile();
        ClipboardFormat format = ClipboardFormats.findByFile(file);

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            glassCage = reader.read();
        } catch (Exception ignored) {}

        file = WorldEdit.getInstance().getWorkingDirectoryPath("schematics/air.schem").toFile();
        format = ClipboardFormats.findByFile(file);

        try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
            noCage = reader.read();
        } catch (Exception ignored) {}
    }

    public static void placeCage(Location location) {
        if (glassCage == null)
            loadCages();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            Operation operation = new ClipboardHolder(glassCage)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(location))
                    .build();
            Operations.complete(operation);
            editSession.commit();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }

    public static void removeCage(Location location) {
        if (glassCage == null)
            loadCages();

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
            Operation operation = new ClipboardHolder(noCage)
                    .createPaste(editSession)
                    .to(BukkitAdapter.asBlockVector(location))
                    .build();
            Operations.complete(operation);
            editSession.commit();
        } catch (WorldEditException e) {
            e.printStackTrace();
        }
    }
}
