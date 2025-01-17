package com.gmail.val59000mc.commands;

import com.gmail.val59000mc.gui.ScenarioGUI;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScenarioCommandExecutor implements CommandExecutor{

    private final ScenarioManager scenarioManager;

    public ScenarioCommandExecutor(ScenarioManager scenarioManager){
        this.scenarioManager = scenarioManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (sender instanceof Player){
            ScenarioGUI scenarioGUI = new ScenarioGUI();
            scenarioGUI.open(((Player) sender).getPlayer(), scenarioManager.getEnabledScenarios().size());
        }else {
            sender.sendMessage("Only players can use this command.");
        }
        return true;
    }

}