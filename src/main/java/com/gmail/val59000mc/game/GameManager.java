package com.gmail.val59000mc.game;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.commands.*;
import com.gmail.val59000mc.configuration.Dependencies;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.customitems.CraftsManager;
import com.gmail.val59000mc.events.UhcGameStateChangedEvent;
import com.gmail.val59000mc.events.UhcStartingEvent;
import com.gmail.val59000mc.events.UhcStartedEvent;
import com.gmail.val59000mc.exceptions.UhcPlayerNotOnlineException;
import com.gmail.val59000mc.game.handlers.*;
import com.gmail.val59000mc.languages.Lang;
import com.gmail.val59000mc.listeners.*;
import com.gmail.val59000mc.maploader.MapLoader;
import com.gmail.val59000mc.players.PlayerManager;
import com.gmail.val59000mc.players.TeamManager;
import com.gmail.val59000mc.players.UhcPlayer;
import com.gmail.val59000mc.players.UhcTeam;
import com.gmail.val59000mc.scenarios.ScenarioManager;
import com.gmail.val59000mc.scenarios.scenariolisteners.CutCleanListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.FastLeavesDecayListener;
import com.gmail.val59000mc.scenarios.scenariolisteners.TimberListener;
import com.gmail.val59000mc.scoreboard.ScoreboardLayout;
import com.gmail.val59000mc.scoreboard.ScoreboardManager;
import com.gmail.val59000mc.threads.*;
import com.gmail.val59000mc.tournament.AssignManager;
import com.gmail.val59000mc.tournament.MatchListeners;
import com.gmail.val59000mc.tournament.PointManager;
import com.gmail.val59000mc.utils.*;
import lv.side.objects.EventMatch;
import lv.side.objects.SimpleTeam;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class GameManager{

	// GameManager Instance
	private static GameManager gameManager;

	// Managers
	private final PlayerManager playerManager;
	private final TeamManager teamManager;
	private final ScoreboardManager scoreboardManager;
	private final ScoreboardLayout scoreboardLayout;
	private final ScenarioManager scenarioManager;
	private final MainConfig config;
	private final MapLoader mapLoader;

	// Handlers
	private final CustomEventHandler customEventHandler;
	private final ScoreboardHandler scoreboardHandler;
	private final DeathmatchHandler deathmatchHandler;
	private final PlayerDeathHandler playerDeathHandler;
	private final StatsHandler statsHandler;
	private final PointHandler pointHandler;

    private GameState gameState;
	private boolean pvp;
	private boolean glowing;
	private boolean withering;
	private boolean gameIsEnding;
	private boolean isDeathmatch = false;
	private int episodeNumber;
	private long remainingTime;
	private long elapsedTime;
	private int startPlayerAmount = 0;
	private int maxBorderSize;

	private UUID lastFight = null;

	public static PotionEffect witherEffect;

	static{
	    gameManager = null;
    }

	public GameManager() {
		gameManager = this;
		config = new MainConfig();
		scoreboardLayout = new ScoreboardLayout();
		customEventHandler = new CustomEventHandler(config);
		scoreboardHandler = new ScoreboardHandler(gameManager, config, scoreboardLayout);
		playerManager = new PlayerManager(customEventHandler, scoreboardHandler);
		teamManager = new TeamManager();
		scoreboardManager = new ScoreboardManager(scoreboardHandler, scoreboardLayout);
		scenarioManager = new ScenarioManager();
		mapLoader = new MapLoader(config);

		deathmatchHandler = new DeathmatchHandler(this, config, playerManager, mapLoader);
		playerDeathHandler = new PlayerDeathHandler(this, scenarioManager, playerManager, config, customEventHandler);
		statsHandler = new StatsHandler(UhcCore.getPlugin(), config, mapLoader, scenarioManager);
		pointHandler = new PointHandler();

		episodeNumber = 0;
		elapsedTime = 0;

		witherEffect = new PotionEffect(PotionEffectType.WITHER, 999999, 9, true, true, true);
	}

	public static GameManager getGameManager(){
		return gameManager;
	}

	public PlayerManager getPlayerManager(){
		return playerManager;
	}

	public TeamManager getTeamManager() {
		return teamManager;
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public ScenarioManager getScenarioManager() {
		return scenarioManager;
	}

	public MainConfig getConfig() {
		return config;
	}

	public MapLoader getMapLoader(){
		return mapLoader;
	}

	public synchronized GameState getGameState(){
		return gameState;
	}

	public boolean getGameIsEnding() {
		return gameIsEnding;
	}

	public synchronized long getRemainingTime(){
		return remainingTime;
	}

	public synchronized long getElapsedTime(){
		return elapsedTime;
	}

	public int getEpisodeNumber(){
		return episodeNumber;
	}

	public void setEpisodeNumber(int episodeNumber){
		this.episodeNumber = episodeNumber;
	}

	public long getTimeUntilNextEpisode(){
		return episodeNumber * config.get(MainConfig.EPISODE_MARKERS_DELAY) - getElapsedTime();
	}

	public String getFormattedRemainingTime() {
		return TimeUtils.getFormattedTime(getRemainingTime());
	}

	public synchronized void setRemainingTime(long time){
		remainingTime = time;
	}

	public synchronized void setElapsedTime(long time){
		elapsedTime = time;
	}

	public boolean getPvp() {
		return pvp;
	}

	public void setPvp(boolean state) {
		pvp = state;
	}

	public boolean isGlowing() {
		return glowing;
	}

	public void setGlowing(boolean glowing) {
		this.glowing = glowing;
	}

	public boolean isWithering() {
		return withering;
	}

	public void setWithering(boolean withering) {
		this.withering = withering;
	}

	public void setGameState(GameState gameState){
        Validate.notNull(gameState);

        if (this.gameState == gameState){
            return; // Don't change the game state when the same.
        }

        GameState oldGameState = this.gameState;
        this.gameState = gameState;

        // Call UhcGameStateChangedEvent
        Bukkit.getPluginManager().callEvent(new UhcGameStateChangedEvent(oldGameState, gameState));
    }

	public void loadNewGame() {
		statsHandler.startRegisteringStats();

		loadConfig();
		setGameState(GameState.LOADING);
		teamManager.init();

		registerListeners();
		registerCommands();

		if(config.get(MainConfig.ENABLE_BUNGEE_SUPPORT)) {
			UhcCore.getPlugin().getServer().getMessenger().registerOutgoingPluginChannel(UhcCore.getPlugin(), "BungeeCord");
		}

		boolean debug = config.get(MainConfig.DEBUG);
		mapLoader.loadWorlds(debug);

		if(config.get(MainConfig.ENABLE_PRE_GENERATE_WORLD) && !debug) {
			mapLoader.generateChunks(Environment.NORMAL);
		} else {
			startWaitingPlayers();
		}
	}

	public void startWaitingPlayers() {
		mapLoader.prepareWorlds();

		setPvp(false);
		setGameState(GameState.WAITING);

		// Enable default scenarios
		scenarioManager.loadDefaultScenarios(config);

		Bukkit.getLogger().info(Lang.DISPLAY_MESSAGE_PREFIX+" Players are now allowed to join");
		Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new PreStartThread(this),0);

		if (!GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE))
			AssignManager.get().updateAvailableArenas();
	}

	public void startGame() {
		setGameState(GameState.STARTING);

		// scenario voting
		if (config.get(MainConfig.ENABLE_SCENARIO_VOTING) && GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE)) {
			scenarioManager.countVotes();
		}

//		maxBorderSize = config.get(MainConfig.BORDER_START_SIZE) * 2;
//		int teamCount = getTeamManager().getNotEmptyUhcTeams().size();
//		if (teamCount < 36) {
//			if (teamCount <= 18)
//				maxBorderSize = maxBorderSize / 3;
//			else
//				maxBorderSize = maxBorderSize / 3 * 2;
//		}

//		World overworld = getMapLoader().getUhcWorld(Environment.NORMAL);
//		getMapLoader().setBorderSize(overworld, 0, 0, maxBorderSize);

		Bukkit.getPluginManager().callEvent(new UhcStartingEvent());

//		broadcastInfoMessage(Lang.GAME_STARTING);
		broadcastInfoMessage(Lang.GAME_PLEASE_WAIT_TELEPORTING);
		playerManager.randomTeleportTeams();
		gameIsEnding = false;
	}

	public void startWatchingEndOfGame(){
		if (GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE))
			scenarioManager.updatePrevious();
		setGameState(GameState.PLAYING);

		for (UhcTeam team : GameManager.getGameManager().getTeamManager().getNotEmptyUhcTeams()) {
			startPlayerAmount += team.getMemberCount();
			UhcCore.getPlugin().getLogger().info("Team " + team.getPrefix() + ": " + String.join(", ", team.getMembersNames()));

			if (team.getStartingLocation() == null) {
				UhcCore.getPlugin().getLogger().log(Level.SEVERE, "Could not find start location for team " + team.getTeamName() + " (" + team.getMembers().get(0));
				continue;
			}

			Location location = team.getStartingLocation().clone().add(0, 6, 0);
			CageUtils.removeCage(location);

			for (UhcPlayer uhcPlayer : team.getOnlinePlayingMembers()) {
				for (PotionEffect effect : gameManager.getConfig().get(MainConfig.POTION_EFFECT_ON_START)) {
					try {
						uhcPlayer.getPlayer().addPotionEffect(effect);
					} catch (UhcPlayerNotOnlineException ignored) {}
				}
			}
		}

		gameManager.broadcastInfoMessage(Lang.GAME_STARTED_TITLE + " " + Lang.GAME_STARTED_SUBTITLE);
		gameManager.getPlayerManager().sendTitleAll(
				Lang.GAME_STARTED_TITLE,
				Lang.GAME_STARTED_SUBTITLE,
				5, 60, 5
		);
		gameManager.getPlayerManager().playSoundAll(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

		mapLoader.setWorldsStartGame();

		playerManager.startWatchPlayerPlayingThread();
		Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new ElapsedTimeThread(this, customEventHandler));
		Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new EnablePVPThread(this));
		Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new EnableGlowingThread(this));

		if (config.get(MainConfig.ENABLE_EPISODE_MARKERS)){
			Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new EpisodeMarkersThread(this));
		}

		if(config.get(MainConfig.ENABLE_DEATHMATCH)){
			Bukkit.getScheduler().runTaskAsynchronously(UhcCore.getPlugin(), new TimeBeforeDeathmatchThread(this, deathmatchHandler));
		}

		if (config.get(MainConfig.ENABLE_DAY_NIGHT_CYCLE) && config.get(MainConfig.TIME_BEFORE_PERMANENT_DAY) != -1){
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new EnablePermanentDayThread(mapLoader), config.get(MainConfig.TIME_BEFORE_PERMANENT_DAY)*20);
		}

		if (config.get(MainConfig.ENABLE_FINAL_HEAL)){
            Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new FinalHealThread(this, playerManager), config.get(MainConfig.FINAL_HEAL_DELAY)*20);
        }

		Bukkit.getPluginManager().callEvent(new UhcStartedEvent());
		statsHandler.addGameToStatistics();

		for (UhcPlayer uhcPlayer : playerManager.getOnlineSpectators()) {
			playerManager.setPlayerSpectateAtLobby(uhcPlayer);
		}

		PointManager.get().init(GameManager.getGameManager().getTeamManager().getNotEmptyUhcTeams());

		if (!GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE) && AssignManager.get().assignedMatch != null) {
			EventMatch match = AssignManager.get().assignedMatch;

			AssignManager.get().callMatchStartEvent(match.getCount(), 1);

			PointManager.get().getHistory().add("----------------");
			PointManager.get().getHistory().add("Komandas:");
			for (Map.Entry<UhcTeam, SimpleTeam> entry : AssignManager.get().assignedTeams.entrySet()) {
				PointManager.get().getHistory().add(entry.getKey().getTeamName() + " - " + entry.getValue().getName());
				UhcCore.getPlugin().getLogger().info(entry.getKey().getTeamName() + " - " + entry.getValue().getName());
			}
			PointManager.get().getHistory().add("----------------");
		}
	}

	public void broadcastMessage(String message){
		for(UhcPlayer player : playerManager.getPlayersList()){
			player.sendMessage(message);
		}
		UhcCore.getPlugin().getLogger().info(message);
	}

	public void broadcastInfoMessage(String message){
		broadcastMessage(Lang.DISPLAY_MESSAGE_PREFIX+" "+message);
	}

	public void broadcastRedMessage(String message){
		broadcastMessage(Lang.RED_MESSAGE_PREFIX+" "+message);
	}

	public void loadConfig(){
		new Lang();

		try{
			File configFile = FileUtils.getResourceFile(UhcCore.getPlugin(), "config.yml");
			config.setConfigurationFile(configFile);
			config.load();
		}catch (InvalidConfigurationException | IOException ex){
			ex.printStackTrace();
			return;
		}

		// Dependencies
		Dependencies.loadWorldEdit();
		Dependencies.loadVault();
		Dependencies.loadProtocolLib();

		// Map loader
		mapLoader.loadWorldUuids();

		// Config
		config.preLoad();

		// Set remaining time
		if(config.get(MainConfig.ENABLE_DEATHMATCH)){
			setRemainingTime(config.get(MainConfig.DEATHMATCH_DELAY));
		}

		// Load crafts
		CraftsManager.loadBannedCrafts();
		CraftsManager.loadCrafts();
//		if (config.get(MainConfig.ENABLE_GOLDEN_HEADS)){
//			CraftsManager.registerGoldenHeadCraft();
//		}
	}

	private void registerListeners() {
		// Registers Listeners
		List<Listener> listeners = new ArrayList<>();
		listeners.add(new PlayerConnectionListener(this, playerManager, playerDeathHandler, scoreboardHandler));
		listeners.add(new PlayerChatListener(playerManager, config));
		listeners.add(new PlayerDamageListener(this));
		listeners.add(new ItemsListener(gameManager, config, playerManager, teamManager, scenarioManager, scoreboardHandler));
		listeners.add(new TeleportListener());
		listeners.add(new PlayerDeathListener(playerDeathHandler));
		listeners.add(new EntityDeathListener(playerManager, config, playerDeathHandler));
		listeners.add(new CraftListener());
		listeners.add(new PingListener());
		listeners.add(new BlockListener(this));
		listeners.add(new WorldListener());
		listeners.add(new EntityDamageListener(this));
		listeners.add(new PlayerHungerGainListener(playerManager));

		listeners.add(new CutCleanListener());
		listeners.add(new TimberListener());
		listeners.add(new FastLeavesDecayListener());

		for(Listener listener : listeners){
			Bukkit.getServer().getPluginManager().registerEvents(listener, UhcCore.getPlugin());
		}

		if (!GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE))
			Bukkit.getServer().getPluginManager().registerEvents(new MatchListeners(), UhcCore.getPlugin());
	}

	private void registerCommands(){
		// Registers CommandExecutor
		registerCommand("uhccore", new UhcCommandExecutor(this));
//		registerCommand("chat", new ChatCommandExecutor(playerManager));
		registerCommand("teleport", new TeleportCommandExecutor(this));
		registerCommand("start", new StartCommandExecutor());
		registerCommand("autostart", new AutoStartCommandExecutor());
		registerCommand("scenarios", new ScenarioCommandExecutor(scenarioManager));
		registerCommand("teaminventory", new TeamInventoryCommandExecutor(playerManager, scenarioManager));
//		registerCommand("iteminfo", new ItemInfoCommandExecutor());
		registerCommand("revive", new ReviveCommandExecutor(this));
		registerCommand("top", new TopCommandExecutor(playerManager));
		registerCommand("spectate", new SpectateCommandExecutor(this, scoreboardHandler));
//		registerCommand("upload", new UploadCommandExecutor());
		registerCommand("deathmatch", new DeathmatchCommandExecutor(this, deathmatchHandler));
		registerCommand("deathmatch", new DeathmatchCommandExecutor(this, deathmatchHandler));
		registerCommand("gotofight", new GoToFightCmd(this));
		registerCommand("gototeam", new GoToTeamCmd(this));
		registerCommand("freeze", new FreezeCommandExecutor(this));
		registerCommand("unfreeze", new UnfreezeCommandExecutor(this));
	}

	private void registerCommand(String commandName, CommandExecutor executor){
		PluginCommand command = UhcCore.getPlugin().getCommand(commandName);
		if (command == null){
			Bukkit.getLogger().warning("[UhcCore] Failed to register " + commandName + " command!");
			return;
		}

		command.setExecutor(executor);
	}

	public void endGame() {
		if (gameState.equals(GameState.PLAYING) || gameState.equals(GameState.DEATHMATCH)){
			setGameState(GameState.ENDED);
			pvp = false;
			gameIsEnding = true;

			UhcTeam winners = gameManager.getPointHandler().getWinnerUhcTeam();
			broadcastInfoMessage(Lang.GAME_FINISHED_TITLE + " " + Lang.GAME_FINISHED_SUBTITLE.replace("%team%", winners.getFullPrefix()));
			gameManager.getPlayerManager().sendTitleAll(
					Lang.GAME_FINISHED_TITLE,
					Lang.GAME_FINISHED_SUBTITLE.replace("%team%", winners.getFullPrefix()),
					5, 100, 5
			);

			playerManager.playSoundToAll(UniversalSound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
			playerManager.setAllPlayersEndGame();
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), new StopRestartThread(),20);

			// TOP 3 IN CHAT

			broadcastMessage(Lang.COMMAND_TOP_HEADER);

			for (String place : gameManager.getPointHandler().getTop3()) {
				broadcastMessage(place);
			}

			// POINTS AND HISTORY

			if (!GameManager.getGameManager().getConfig().get(MainConfig.PRACTICE_MODE)) {
				Map<UhcTeam, Integer> points = PointManager.get().getPoints();
				List<String> history = PointManager.get().getHistory();

				for (String s : history) {
					UhcCore.getPlugin().getLogger().info(s);
				}

				EventMatch match = AssignManager.get().assignedMatch;
				if (match != null) {
					match.setLog(history);
					for (Map.Entry<UhcTeam, Integer> entry : points.entrySet()) {
						SimpleTeam simpleTeam = AssignManager.get().assignedTeams.get(entry.getKey());
						if (simpleTeam != null) {
							match.getTeams().put(simpleTeam, entry.getValue());
						}
					}

					AssignManager.get().callMatchFinishedEvent(match);
				}
			}
		}
	}

	public void startEndGameThread() {
		if(!gameIsEnding && (gameState.equals(GameState.DEATHMATCH) || gameState.equals(GameState.PLAYING))){
			gameIsEnding = true;
			EndThread.start();
		}
	}

	public void stopEndGameThread(){
		if(gameIsEnding && (gameState.equals(GameState.DEATHMATCH) || gameState.equals(GameState.PLAYING))){
			gameIsEnding = false;
			EndThread.stop();
		}
	}

	public PointHandler getPointHandler() {
		return pointHandler;
	}

	public DeathmatchHandler getDeathmatchHandler() {
		return deathmatchHandler;
	}

	public void setDeathmatch(boolean deathmatch) {
		isDeathmatch = deathmatch;
	}

	public boolean isDeathmatch() {
		return isDeathmatch;
	}

	public UUID getLastFight() {
		return lastFight;
	}

	public void setLastFight(UUID lastFight) {
		this.lastFight = lastFight;
	}

	public int getStartPlayerAmount() {
		return startPlayerAmount;
	}

	public int getMaxBorderSize() {
		return maxBorderSize;
	}
}
