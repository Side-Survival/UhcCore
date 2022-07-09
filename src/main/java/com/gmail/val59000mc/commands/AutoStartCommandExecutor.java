package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.threads.PreStartThread;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.Date;

public class AutoStartCommandExecutor implements CommandExecutor{

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("You need to provide the start time with format hh:mm");
            return true;
        }

        String[] timeParts = args[0].split(":");
        if (timeParts.length < 2) {
            sender.sendMessage("You need to provide the start time with format hh:mm");
            return true;
        }

        Date start = new Date();
        try {
            start.setSeconds(0);
            start.setHours(Integer.parseInt(timeParts[0]));
            start.setMinutes(Integer.parseInt(timeParts[1]));
        } catch (NumberFormatException e) {
            sender.sendMessage("You need to provide the start time with format hh:mm");
            return true;
        }

        PreStartThread.startUntil(start.getTime() / 1000);
        return true;
    }
}