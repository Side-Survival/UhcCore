package com.gmail.val59000mc.scenarios;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.configuration.YamlFile;
import com.gmail.val59000mc.customitems.GameItem;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.utils.FileUtils;
import com.gmail.val59000mc.utils.NMSUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class ScenarioManager {

    private static final int ROW = 9;

    private final List<Scenario> registeredScenarios;
    private final Map<Scenario, ScenarioListener> enabledScenarios;
    private YamlFile scenarioData = null;
    private Map<Scenario, Integer> previousScenarios;

    public ScenarioManager(){
        registeredScenarios = new ArrayList<>();
        enabledScenarios = new HashMap<>();
        Collections.addAll(registeredScenarios, Scenario.BUILD_IN_SCENARIOS);

        try {
            scenarioData = FileUtils.saveResourceIfNotAvailable(UhcCore.getPlugin(), "scenario-data.yml");
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        previousScenarios = new HashMap<>();
        if (scenarioData.getConfigurationSection("previous") == null || !GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE))
            return;

        for (String key : scenarioData.getConfigurationSection("previous").getKeys(false)) {
            Optional<Scenario> scenario = registeredScenarios.stream().filter(s -> s.getKey().equalsIgnoreCase(key)).findFirst();
            scenario.ifPresent(value -> previousScenarios.put(value, scenarioData.getInt("previous." + key)));
        }
    }

    /**
     * Used to check if an scenario is registered in UhcCore.
     * @param scenario Scenario to check.
     * @return Returns true if the scenario is registered.
     */
    public boolean isRegistered(Scenario scenario) {
        return registeredScenarios.contains(scenario);
    }

    /**
     * Used to register a third party scenario into UhcCore.
     * @param scenario The scenario to register.
     */
    public void registerScenario(Scenario scenario) {
        Validate.notNull(scenario.getInfo(), "Scenario info cannot be null!");
        Validate.isTrue(getScenarioByKey(scenario.getKey()).isEmpty(), "An scenario with the key " + scenario.getKey() + " is already registered!");
        registeredScenarios.add(scenario);
    }

    /**
     * Used to un-register a scenario
     * @param key The scenario key of the scenario to un-register
     */
    public void unRegisterScenario(String key) {
        Validate.notNull(key);
        Optional<Scenario> scenario = getScenarioByKey(key);
        Validate.isTrue(scenario.isPresent(), "There are no scenarios registered with that key!");
        registeredScenarios.remove(scenario.get());
    }

    /**
     * Used to activate an scenario.
     * @param scenario Scenario to activate.
     */
    public void enableScenario(Scenario scenario){
        Validate.isTrue(isRegistered(scenario), "The specified scenario ("+scenario.getKey()+") is not registered!");

        if (isEnabled(scenario)){
            return;
        }

        Class<? extends ScenarioListener> listenerClass = scenario.getListener();

        try {
            ScenarioListener scenarioListener = null;
            if (listenerClass != null) {
                scenarioListener = listenerClass.newInstance();
            }

            enabledScenarios.put(scenario, scenarioListener);

            if (scenarioListener != null) {
                loadScenarioOptions(scenario, scenarioListener);
                scenarioListener.onEnable();

                // If disabled in the onEnable method don't register listener.
                if (isEnabled(scenario)) {
                    Bukkit.getServer().getPluginManager().registerEvents(scenarioListener, UhcCore.getPlugin());
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /**
     * Used to deactivate an scenario.
     * @param scenario Scenario to deactivate.
     */
    public void disableScenario(Scenario scenario){
        Validate.isTrue(isRegistered(scenario), "The specified scenario is not registered!");

        ScenarioListener scenarioListener = enabledScenarios.get(scenario);
        enabledScenarios.remove(scenario);

        if (scenarioListener != null) {
            HandlerList.unregisterAll(scenarioListener);
            scenarioListener.onDisable();
        }
    }

    /**
     * Used to toggle a scenario.
     * @param scenario The scenario to toggle.
     * @return Returns true if the scenario got enabled, false when disabled.
     */
    public boolean toggleScenario(Scenario scenario){
        if (isEnabled(scenario)){
            disableScenario(scenario);
            return false;
        }

        enableScenario(scenario);
        return true;
    }

    /**
     * Used to obtain the scenario object matching a certain name.
     * @param name Name of the scenario to be searched.
     * @return Returns a scenario object matching the name, or null when not found.
     * @deprecated Replaced by {@link #getScenarioByName(String)}
     */
    @Nullable
    @Deprecated
    public Scenario getScenario(String name) {
        return getScenarioByName(name).orElse(null);
    }

    /**
     * Used to obtain the scenario object matching a certain name.
     * @param name Name of the scenario to be searched.
     * @return Returns a scenario object matching the name, or null when not found.
     */
    public Optional<Scenario> getScenarioByName(String name) {
        return registeredScenarios.stream()
                .filter(s -> name.contains(s.getInfo().getName()))
                .findFirst();
    }

    /**
     * Used to obtain the scenario object matching a certain key.
     * @param key Key of the scenario to be searched.
     * @return Returns a scenario object matching the key.
     */
    public Optional<Scenario> getScenarioByKey(String key) {
        return registeredScenarios.stream().filter(s -> s.getKey().equals(key)).findFirst();
    }

    /**
     * Used to obtain the scenario object matching a certain key.
     * @param key Key of the scenario to be searched.
     * @return Returns a scenario object matching the key.
     * @deprecated Use {@link #getScenarioByKey(String)}
     */
    @Deprecated
    public Optional<Scenario> getScenarioByOldKey(String key) {
        return registeredScenarios.stream().filter(s -> s.getKey().replace("_", "").equalsIgnoreCase(key)).findFirst();
    }

    /**
     * Used to obtain enabled scenarios.
     * @return Returns {@link Set} of scenarios.
     */
    public synchronized Set<Scenario> getEnabledScenarios(){
        return enabledScenarios.keySet();
    }

    public List<Scenario> getRegisteredScenarios() {
        return registeredScenarios;
    }

    /**
     * Used to check if a scenario is enabled.
     * @param scenario Scenario to check.
     * @return Returns true if the scenario is enabled.
     */
    public boolean isEnabled(Scenario scenario){
        return enabledScenarios.containsKey(scenario);
    }

    /**
     * Used to obtain the {@link ScenarioListener} instance of an scenario.
     * @param scenario Enabled scenario to return the listener of.
     * @return Returns an {@link ScenarioListener}, null if the scenario doesn't have one or it's not enabled.
     */
    public ScenarioListener getScenarioListener(Scenario scenario){
        return enabledScenarios.get(scenario);
    }

    public void loadDefaultScenarios(MainConfig cfg){
        if (cfg.get(MainConfig.ENABLE_DEFAULT_SCENARIOS)){
            List<String> defaultScenarios = cfg.get(MainConfig.DEFAULT_SCENARIOS);
            for (String scenarioKey : defaultScenarios) {
                Optional<Scenario> scenario = getScenarioByKey(scenarioKey);
                if (scenario.isPresent()) {
                    Bukkit.getLogger().info("[UhcCore] Loading " + scenario.get().getKey());
                    enableScenario(scenario.get());
                }else {
                    Bukkit.getLogger().warning("[UhcCore] Scenario with key " + scenarioKey + " can't be found!");
                }
            }
        }
    }

    public void disableAllScenarios(){
        Set<Scenario> active = new HashSet<>(getEnabledScenarios());
        for (Scenario scenario : active){
            disableScenario(scenario);
        }
    }

    public void countVotes() {
        Map<Scenario, Integer> votes = new HashMap<>();

        List<String> blacklist = GameManager.getGameManager().getConfig().get(MainConfig.SCENARIO_VOTING_BLACKLIST);
        for (Scenario scenario : registeredScenarios){
            if (!blacklist.contains(scenario.getKey())) {
                votes.put(scenario, 0);
            }
        }

        for (UhcPlayer uhcPlayer : GameManager.getGameManager().getPlayerManager().getPlayersList()){
            for (Scenario scenario : uhcPlayer.getScenarioVotes()){
                int totalVotes = votes.getOrDefault(scenario, 0) + 1;
                votes.put(scenario, totalVotes);
            }
        }

        int scenarioCount = GameManager.getGameManager().getConfig().get(MainConfig.ELECTED_SCENARIO_COUNT);
        while (scenarioCount > 0){
            // get scenario with most votes
            Scenario scenario = null;
            int scenarioVotes = 0;

            for (Scenario s : votes.keySet()){
                // Don't let people vote for scenarios that are enabled by default.
                if (isEnabled(s)){
                    continue;
                }

                if (scenario == null || votes.get(s) > scenarioVotes){
                    scenario = s;
                    scenarioVotes = votes.get(s);
                }
            }

            enableScenario(scenario);
            votes.remove(scenario);
            scenarioCount--;
        }
    }

    private void loadScenarioOptions(Scenario scenario, ScenarioListener listener) throws ReflectiveOperationException, IOException, InvalidConfigurationException{
        List<Field> optionFields = NMSUtils.getAnnotatedFields(listener.getClass(), Option.class);

        if (optionFields.isEmpty()){
            return;
        }

        YamlFile cfg = FileUtils.saveResourceIfNotAvailable(UhcCore.getPlugin(), "scenarios.yml");

        for (Field field : optionFields){
            Option option = field.getAnnotation(Option.class);
            String key = option.key().isEmpty() ? field.getName() : option.key();
            Object value = cfg.get(scenario.getKey() + "." + key, field.get(listener));
            field.set(listener, value);
        }

        if (cfg.addedDefaultValues()){
            cfg.saveWithComments();
        }
    }

    public Map<Scenario, Integer> getPreviousScenarios() {
        return previousScenarios;
    }

    public void updatePrevious() {
        List<Scenario> toRemove = new ArrayList<>();
        for (Map.Entry<Scenario, Integer> entry : previousScenarios.entrySet()) {
            if (entry.getValue() == 1) {
                toRemove.add(entry.getKey());
                continue;
            }

            previousScenarios.put(entry.getKey(), entry.getValue() - 1);
        }

        for (Scenario scenario : toRemove) {
            previousScenarios.remove(scenario);
        }

        int disabledFor = GameManager.getGameManager().getConfig().get(MainConfig.SCENARIO_PREVIOUS_COUNT);
        for (Scenario scenario : enabledScenarios.keySet()) {
            previousScenarios.put(scenario, disabledFor);
        }

        scenarioData.set("previous", null);
        for (Map.Entry<Scenario, Integer> entry : previousScenarios.entrySet()) {
            scenarioData.set("previous." + entry.getKey().getKey(), entry.getValue());
        }

        try {
            scenarioData.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearPrevious() {
        previousScenarios.clear();
        scenarioData.set("previous", null);

        try {
            scenarioData.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}