package com.gmail.val59000mc.languages;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.YamlFile;
import com.gmail.val59000mc.scenarios.Scenario;
import com.gmail.val59000mc.utils.FileUtils;
import com.gmail.val59000mc.utils.RandomUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.util.*;

public class Lang{

	public static String GAME_ENOUGH_TEAMS_READY;
	public static String GAME_STARTING;
	public static String GAME_PLEASE_WAIT_TELEPORTING;
	public static String GAME_STARTING_IN;
	public static String GAME_STARTING_CANCELLED;
	public static String GAME_STARTED_TITLE;
	public static String GAME_STARTED_SUBTITLE;
	public static String GAME_FINISHED_TITLE;
	public static String GAME_FINISHED_SUBTITLE;
	public static String GAME_END_STOPPED;
	public static String GAME_SHUTDOWN;
	public static String GAME_DEATHMATCH_IN;
	public static String GAME_START_DEATHMATCH;
	public static String GAME_BORDER_IN;
	public static String GAME_BORDER_START_SHRINKING;
	public static String GAME_BORDER_NEAR;
	public static String GAME_FINAL_HEAL;
	public static String GAME_NO_PEARLS;
	public static String GAME_STAGE_PVP;
	public static String GAME_STAGE_DEATHMATCH;
	public static String GAME_STAGE_BORDER;
	public static String GAME_STAGE_GLOWING;
	public static String GAME_PLAYER_JOINED;
	public static String GAME_PLAYER_LEFT;
	public static String GAME_FULL;

	public static String PLAYERS_NETHER_OFF;
	public static String PLAYERS_BUILD_HEIGHT;
	public static String PLAYERS_WELCOME_NEW;
	public static String PLAYERS_WELCOME_BACK_IN_GAME;
	public static String PLAYERS_WELCOME_BACK_SPECTATING;
	public static String PLAYERS_ELIMINATED;
	public static String PLAYERS_WON_SOLO;
	public static String PLAYERS_WON_TEAM;
	public static String PLAYERS_ALL_HAVE_LEFT;
	public static String PLAYERS_FF_OFF;
	public static String PLAYERS_SEND_BUNGEE;
	public static String PLAYERS_SEND_BUNGEE_NOW;
	public static String PLAYERS_SEND_BUNGEE_DISABLED;


	public static String DISPLAY_MESSAGE_PREFIX;
	public static String RED_MESSAGE_PREFIX;
	public static String DISPLAY_EPISODE_MARK;
	public static String DISPLAY_SPECTATOR_CHAT;
	public static String DISPLAY_MOTD_LOADING;
	public static String DISPLAY_MOTD_WAITING;
	public static String DISPLAY_MOTD_STARTING;
	public static String DISPLAY_MOTD_PLAYING;
	public static String DISPLAY_MOTD_ENDED;
	public static String DISPLAY_CHAT_TIP;

	public static String KICK_LOADING;
	public static String KICK_STARTING;
	public static String KICK_PLAYING;
	public static String KICK_ENDED;
	public static String KICK_DEAD;

	public static String COMMAND_CHAT_GLOBAL;
	public static String COMMAND_CHAT_TEAM;
	public static String COMMAND_CHAT_HELP;
	public static String COMMAND_CHAT_ERROR;
	public static String COMMAND_SPECTATING_TELEPORT_ERROR;
	public static String COMMAND_SPECTATING_TELEPORT;
	public static String COMMAND_SPECTATING_HELP;
	public static String COMMAND_RECIPES_ERROR;
	public static String COMMAND_TOP_HEADER;
	public static String COMMAND_TOP_ERROR_PLAYING;
	public static String COMMAND_TOP_ERROR_NETHER;
	public static String COMMAND_SPECTATE_ERROR;
	public static String COMMAND_SPECTATE_PLAYING;
	public static String COMMAND_SPECTATE_SPECTATING;

	// Team Inventories
	public static String TEAM_INVENTORY_MAIN;
	public static String TEAM_INVENTORY_TEAMS_LIST;
	public static String TEAM_INVENTORY_TEAM_VIEW;
	public static String TEAM_INVENTORY_SETTINGS;
	public static String TEAM_INVENTORY_RENAME;
	public static String TEAM_INVENTORY_INVITES;
	public static String TEAM_INVENTORY_REPLY_INVITE;
	public static String TEAM_INVENTORY_INVITE_PLAYER;
	public static String TEAM_INVENTORY_COLOR;

	// Team Items
	public static String TEAM_ITEM_LEAVE;
	public static String TEAM_ITEM_SETTINGS;
	public static String TEAM_ITEM_RENAME;
	public static String TEAM_ITEM_INVITE;
	public static String TEAM_ITEM_INVITE_SEARCH;
	public static String TEAM_ITEM_INVITES;
	public static String TEAM_ITEM_INVITE_ACCEPT;
	public static String TEAM_ITEM_INVITE_DENY;
	public static String TEAM_ITEM_READY_TOGGLE;
	public static String TEAM_ITEM_COLOR;
	public static String TEAM_ITEM_READY;
	public static String TEAM_ITEM_NOT_READY;

	// Team Messages
	public static String TEAM_MESSAGE_PLAYER_NOT_ONLINE;
	public static String TEAM_MESSAGE_NOW_READY;
	public static String TEAM_MESSAGE_NOW_NOT_READY;
	public static String TEAM_MESSAGE_NOT_LEADER;
	public static String TEAM_MESSAGE_JOIN_AS_PLAYER;
	public static String TEAM_MESSAGE_PLAYER_JOINS;
	public static String TEAM_MESSAGE_LEAVE_AS_LEADER;
	public static String TEAM_MESSAGE_LEAVE_AS_PLAYER;
	public static String TEAM_MESSAGE_PLAYER_LEAVES;
	public static String TEAM_MESSAGE_LEADER_LEAVES;
	public static String TEAM_MESSAGE_CANT_LEAVE;
	public static String TEAM_MESSAGE_DENY_REQUEST;
	public static String TEAM_MESSAGE_NO_LONGER_EXISTS;
	public static String TEAM_MESSAGE_INVITE_ALREADY_SENT;
	public static String TEAM_MESSAGE_INVITE_RECEIVE;
	public static String TEAM_MESSAGE_INVITE_RECEIVE_HOVER;
	public static String TEAM_MESSAGE_ALREADY_IN_TEAM;
	public static String TEAM_MESSAGE_PLAYER_ALREADY_IN_TEAM;
	public static String TEAM_MESSAGE_FULL;
	public static String TEAM_MESSAGE_COLOR_ALREADY_SELECTED;
	public static String TEAM_MESSAGE_COLOR_UNAVAILABLE;
	public static String TEAM_MESSAGE_COLOR_CHANGED;
	public static String TEAM_MESSAGE_NAME_CHANGED;
	public static String TEAM_MESSAGE_NAME_CHANGED_ERROR;
	public static String TEAM_CHAT_PREFIX;

	public static String ITEMS_DEF_LORE;
	public static String ITEMS_CLICK_LORE;
	public static String ITEMS_TEAM_LIST;
	public static String ITEMS_SWORD;
	public static String ITEMS_BUNGEE;
	public static String ITEMS_REGEN_HEAD;
	public static String ITEMS_REGEN_HEAD_ACTION;
	public static String ITEMS_GOLDEN_HEAD_SKULL_NAME;
	public static String ITEMS_GOLDEN_HEAD_SKULL_HELP;
	public static String ITEMS_GOLDEN_HEAD_APPLE_NAME;
	public static String ITEMS_GOLDEN_HEAD_APPLE_HELP;
	public static String ITEMS_COMPASS_PLAYING;
	public static String ITEMS_COMPASS_PLAYING_ERROR;
	public static String ITEMS_COMPASS_PLAYING_COOLDOWN;
	public static String ITEMS_COMPASS_PLAYING_POINTING;
	public static String ITEMS_SPECTATOR_SPAWN;
	public static String ITEMS_SPECTATOR_PLAYERS;
	public static String ITEMS_RANDOM_PLAYER;
	public static String ITEMS_OPEN_INV;
	public static String ITEMS_KIT_SELECTION;
	public static String ITEMS_KIT_INVENTORY;
	public static String ITEMS_KIT_SELECTED;
	public static String ITEMS_KIT_NO_PERMISSION;
	public static String ITEMS_KIT_SCOREBOARD_NO_KIT;
	public static String ITEMS_CRAFT_NO_PERMISSION;
	public static String ITEMS_CRAFT_CRAFTED;
	public static String ITEMS_CRAFT_LEFT_CLICK;
	public static String ITEMS_CRAFT_LIMIT;
	public static String ITEMS_CRAFT_BOOK;
	public static String ITEMS_CRAFT_BOOK_INVENTORY;
	public static String ITEMS_CRAFT_BOOK_BACK;
	public static String ITEMS_POTION_BANNED;
	public static String ITEMS_REVIVE_SUCCESS;
	public static String ITEMS_REVIVE_ERROR;

	public static String PVP_ENABLED;
	public static String PVP_START_IN;
	public static String GLOWING_ENABLED;
	public static String GLOWING_START_IN;
	public static String WITHERING_ENABLED;

	public static String EVENT_TIME_REWARD;
	public static String EVENT_KILL_REWARD;
	public static String EVENT_WIN_REWARD;

	public static String SCENARIO_GLOBAL_DESCRIPTION_HEADER;
	public static String SCENARIO_GLOBAL_DESCRIPTION_PREFIX;
	public static String SCENARIO_GLOBAL_INVENTORY;
	public static String SCENARIO_GLOBAL_INVENTORY_EDIT;
	public static String SCENARIO_GLOBAL_INVENTORY_VOTE;
	public static String SCENARIO_GLOBAL_ITEM_EDIT;
	public static String SCENARIO_GLOBAL_ITEM_BACK;
	public static String SCENARIO_GLOBAL_ITEM_NEXT;
	public static String SCENARIO_GLOBAL_ITEM_HOTBAR;
	public static String SCENARIO_GLOBAL_ITEM_COLOR;
	public static String SCENARIO_GLOBAL_ITEM_INFO;
	public static String SCENARIO_GLOBAL_ITEM_DISABLED;
	public static String SCENARIO_GLOBAL_ITEM_DISABLED_PL;
	public static String SCENARIO_GLOBAL_VOTE_MAX;
	public static String SCENARIO_GLOBAL_TITLE;
	public static String SCENARIO_GLOBAL_SUBTITLE;
	public static String SCENARIO_GLOBAL_HEADER;
	public static String SCENARIO_VOTE;
	public static String SPECTATOR_GUI_TITLE;
	public static String SPECTATOR_GUI_ITEM;
	public static List<String> SPECTATOR_GUI_LORE;

	public static String SCENARIO_BESTPVE_ADDED;
	public static String SCENARIO_BESTPVE_REMOVED;
	public static String SCENARIO_BESTPVE_BACK;
	public static String SCENARIO_BOWLESS_ERROR;
	public static String SCENARIO_HORSELESS_ERROR;
	public static String SCENARIO_NOCLEAN_INVULNERABLE;
	public static String SCENARIO_NOCLEAN_VULNERABLE;
	public static String SCENARIO_NOCLEAN_ERROR;
	public static String SCENARIO_RODLESS_ERROR;
	public static String SCENARIO_SHIELDLESS_ERROR;
	public static String SCENARIO_SWITCHEROO_SWITCH;
	public static String SCENARIO_LOVEATFIRSTSIGHT_JOIN_ERROR;
	public static String SCENARIO_LOVEATFIRSTSIGHT_JOIN_BROADCAST;
	public static String SCENARIO_SKYHIGH_DAMAGE;
	public static String SCENARIO_TEAMINVENTORY_ERROR;
	public static String SCENARIO_TEAMINVENTORY_DISABLED;
	public static String SCENARIO_TEAMINVENTORY_OPEN;
	public static String SCENARIO_TEAMINVENTORY_ITEM;
	public static String SCENARIO_SILENTNIGHT_ERROR;
	public static String SCENARIO_WEAKESTLINK_KILL;
	public static String SCENARIO_NOGOINGBACK_ERROR;
	public static String SCENARIO_MONSTERSINC_ERROR;
	public static String SCENARIO_TIMEBOMB_CHEST;

	public static String DEATH_CUSTOM;
	public static String DEATH_CONTACT;
	public static String DEATH_DROWNING;
	public static String DEATH_EXPLOSION;
	public static String DEATH_FALL;
	public static String DEATH_FALLING_BLOCK;
	public static String DEATH_FIRE;
	public static String DEATH_FLY_INTO_WALL;
	public static String DEATH_LAVA;
	public static String DEATH_MAGIC;
	public static String DEATH_MOB;
	public static String DEATH_PROJECTILE;
	public static String DEATH_PLAYER;
	public static String DEATH_PLAYER_PROJECTILE;
	public static String DEATH_SUICIDE;
	public static String DEATH_STARVATION;
	public static String DEATH_OTHER_CAUSE;

	public static String TEAM_FULL_NAME;
	public static String TEAM_GUI_TITLE;
	public static List<String> TEAM_GUI_LORE;

	public Lang(){
		loadLangConfig();
	}

	private void loadLangConfig() {
		File langFile = new File(UhcCore.getPlugin().getDataFolder(),"lang.yml");
		if(!langFile.exists()) {
			try {
				langFile.createNewFile();
			}catch (IOException ex){
				Bukkit.getLogger().severe("[UhcCore] Failed to create " + langFile.toString());
				ex.printStackTrace();
				return;
			}
		}

		YamlFile lang;
		boolean pathChanges = false;

		try{
			lang = FileUtils.saveResourceIfNotAvailable(UhcCore.getPlugin(), "lang.yml");
		}catch (InvalidConfigurationException ex){
			ex.printStackTrace();
			return;
		}

		// Game
		GAME_ENOUGH_TEAMS_READY = getString(lang, "game.enough-teams-ready", "Ok, enough teams are ready.");
		GAME_STARTING = getString(lang, "game.starting", "Starting the game now!");
		GAME_STARTING_IN = getString(lang, "game.starting-in", "Starting in %time% seconds.");
		GAME_STARTING_CANCELLED = getString(lang, "game.starting-cancelled", "Game starting was cancelled because not enough teams are ready");
		GAME_STARTED_TITLE = getString(lang, "game.started-title", "&aGood luck!");
		GAME_STARTED_SUBTITLE = getString(lang, "game.started-subtitle", "&fThe game has started");
		GAME_FINISHED_TITLE = getString(lang, "game.finished-title", "The game has finished!");
		GAME_FINISHED_SUBTITLE = getString(lang, "game.finished-subtitle", "Winner: %team% team");
		GAME_END_STOPPED = getString(lang, "game.end-stopped", "Game ending stopped");
		GAME_SHUTDOWN = getString(lang, "game.shutdown", "Server will shutdown in %time% seconds.");
		GAME_DEATHMATCH_IN = getString(lang, "game.deathmatch-in", "Deathmatch will start in %time%");
		GAME_PLEASE_WAIT_TELEPORTING = getString(lang, "game.please-wait-teleporting", "Please wait while all players are being teleported.");
		GAME_START_DEATHMATCH = getString(lang, "game.start-deathmatch", "Starting the deathmatch! Prepare yourself until PVP is enabled!");
		GAME_BORDER_IN = getString(lang, "game.border-in", "The border will start to shrink in %time%");
		GAME_BORDER_START_SHRINKING = getString(lang, "game.border-start-shrinking", "The border will now begin to shrink");
		GAME_BORDER_NEAR = getString(lang, "game.border-near", "&cWarning! &fWorld border is only %blocks% away");
		GAME_FINAL_HEAL = getString(lang, "game.final-heal", "All players have been healed to full health");
		GAME_NO_PEARLS = getString(lang, "game.no-pearls", "&cYour Ender Pearls were removed from your inventory for deathmatch!");
		GAME_STAGE_PVP = getString(lang, "game.stage.pvp", "PvP");
		GAME_STAGE_DEATHMATCH = getString(lang, "game.stage.deathmatch", "Deathmatch");
		GAME_STAGE_BORDER = getString(lang, "game.stage.border", "Border");
		GAME_STAGE_GLOWING = getString(lang, "game.stage.glowing", "Glowing");
		GAME_PLAYER_JOINED = getString(lang, "game.player-join", "%player% joined the game");
		GAME_PLAYER_LEFT = getString(lang, "game.player-quit", "%player% left the game");
		GAME_FULL = getString(lang, "game.full", "Server is full!");

		// Players
		PLAYERS_NETHER_OFF = getString(lang, "players.nether-off", "&cThe nether has been deactivated for this game.");
		PLAYERS_BUILD_HEIGHT = getString(lang, "players.build-height", "&cMax build height reached!");
		PLAYERS_WELCOME_NEW = getString(lang, "players.welcome-new", "Welcome to UHC, please select your team");
		PLAYERS_WELCOME_BACK_IN_GAME = getString(lang, "players.welcome-back-in-game", "You logged back in the game");
		PLAYERS_WELCOME_BACK_SPECTATING = getString(lang, "players.welcome-back-spectating", "You are dead and are now spectating.");
		PLAYERS_ELIMINATED = getString(lang, "players.eliminated", "%player% has been eliminated!");
		PLAYERS_WON_SOLO = getString(lang, "players.won-solo", "%player% won the game!");
		PLAYERS_WON_TEAM = getString(lang, "players.won-team", "Team %team% won the game!");
		PLAYERS_ALL_HAVE_LEFT = getString(lang, "players.all-have-left", "All players have left, game will end in %time%");
		PLAYERS_FF_OFF = getString(lang, "players.ff-off", "&7Friendly-Fire is disabled");
		PLAYERS_SEND_BUNGEE = getString(lang, "players.send-bungee", "&eSending you to the hub in %time%");
		PLAYERS_SEND_BUNGEE_NOW = getString(lang, "players.send-bungee-now", "&eConnecting to Hub ...");
		PLAYERS_SEND_BUNGEE_DISABLED = getString(lang, "players.send-bungee-disabled", "&cThis command is disabled");

		// Display
		DISPLAY_MESSAGE_PREFIX = getString(lang, "display.message-prefix", "&a[UhcCore]&r");
		RED_MESSAGE_PREFIX = getString(lang, "display.red-prefix", "&c[UhcCore]&r");
		DISPLAY_EPISODE_MARK = getString(lang, "display.episode-mark", "End of episode %episode%!");
		DISPLAY_SPECTATOR_CHAT = getString(lang, "display.spectator-chat", "&7[Spec] &r%player%&r: %message%");
		DISPLAY_MOTD_LOADING  = getString(lang, "display.motd-loading", "Loading ...");
		DISPLAY_MOTD_WAITING  = getString(lang, "display.motd-waiting", "Waiting ...");
		DISPLAY_MOTD_STARTING  = getString(lang, "display.motd-starting", "Starting");
		DISPLAY_MOTD_PLAYING  = getString(lang, "display.motd-playing", "Playing");
		DISPLAY_MOTD_ENDED  = getString(lang, "display.motd-ended", "Ended");
		DISPLAY_CHAT_TIP  = getString(lang, "display.chat-tip", "Type @ at the start of the message to speak in the public chat!");

		// Kick
		KICK_LOADING = getString(lang, "kick.loading", "Loading. Please retry in a few minutes.");
		KICK_STARTING = getString(lang, "kick.starting", "Starting ... Too late to join.");
		KICK_PLAYING = getString(lang, "kick.playing", "Playing ... You can't join.");
		KICK_ENDED = getString(lang, "kick.ended", "Ended ... Please retry in a few minutes.");
		KICK_DEAD = getString(lang, "kick.dead", "You are dead!");

		// Command
		COMMAND_CHAT_GLOBAL = getString(lang, "command.chat-global", "&aYou are now talking to everyone");
		COMMAND_CHAT_TEAM = getString(lang, "command.chat-team", "&aYou are now talking to your team");
		COMMAND_CHAT_HELP = getString(lang, "command.chat-help", "&7Type '/chat' or '/c' to toggle global chat");
		COMMAND_CHAT_ERROR = getString(lang, "command.chat-error", "&cYou can only use that command while playing");
		COMMAND_SPECTATING_HELP = getString(lang, "command.spectating-help", "Use '/teleport <player>' to teleport to a playing player");
		COMMAND_SPECTATING_TELEPORT = getString(lang, "command.spectating-teleport", "&aTeleporting to %player%");
		COMMAND_SPECTATING_TELEPORT_ERROR = getString(lang, "command.spectating-teleport-error", "&cYou can't teleport to that player");
		COMMAND_RECIPES_ERROR = getString(lang, "command.recipes-error", "&cThere are no custom recipes for this game.");
		COMMAND_TOP_HEADER = getString(lang, "command.top-header", "&aTOP teams:");
		COMMAND_TOP_ERROR_PLAYING = getString(lang, "command.top-error-playing", "&cYou can only use this command while playing.");
		COMMAND_TOP_ERROR_NETHER = getString(lang, "command.top-error-nether", "&cYou can only use this command in the overworld.");
		COMMAND_SPECTATE_ERROR = getString(lang, "command.spectate.error", "&cYou may only toggle to spectating mode while the game has not yet started.");
		COMMAND_SPECTATE_PLAYING = getString(lang, "command.spectate.playing", "&aYou're now playing!");
		COMMAND_SPECTATE_SPECTATING = getString(lang, "command.spectate.spectating", "&aYou're now spectating!");

		// Team Inventories
		TEAM_INVENTORY_MAIN  = getString(lang, "team.inventory.main", "&2Team Menu", 32);
		TEAM_INVENTORY_TEAMS_LIST  = getString(lang, "team.inventory.teams-list", "&2Teams List", 32);
		TEAM_INVENTORY_TEAM_VIEW  = getString(lang, "team.inventory.team-view", "&2Viewing Team", 32);
		TEAM_INVENTORY_SETTINGS  = getString(lang, "team.inventory.settings", "&2Team Settings", 32);
		TEAM_INVENTORY_RENAME  = getString(lang, "team.inventory.rename", "&2Rename Team", 32);
		TEAM_INVENTORY_INVITES  = getString(lang, "team.inventory.invites", "&2Team Invites", 32);
		TEAM_INVENTORY_REPLY_INVITE  = getString(lang, "team.inventory.reply-invite", "&2Reply To Invite", 32);
		TEAM_INVENTORY_INVITE_PLAYER  = getString(lang, "team.inventory.invite-player", "&2Invite a Player", 32);
		TEAM_INVENTORY_COLOR = getString(lang, "team.inventory.color", "&2Select a team color", 32);

		// Team Items
		TEAM_ITEM_LEAVE = getString(lang, "team.item.leave", "&cLeave your team");
		TEAM_ITEM_SETTINGS = getString(lang, "team.item.settings", "&aChange Team Settings");
		TEAM_ITEM_RENAME = getString(lang, "team.item.rename", "&aRename Team");
		TEAM_ITEM_INVITE = getString(lang, "team.item.invite", "&aInvite a Player");
		TEAM_ITEM_INVITE_SEARCH = getString(lang, "team.item.invite-search", "&aSearch player to invite");
		TEAM_ITEM_INVITES = getString(lang, "team.item.invites", "&aView Invites");
		TEAM_ITEM_INVITE_ACCEPT = getString(lang, "team.item.invite-accept", "&aAccept");
		TEAM_ITEM_INVITE_DENY = getString(lang, "team.item.invite-deny", "&cDeny");
		TEAM_ITEM_READY_TOGGLE = getString(lang, "team.item.ready-toggle", "&7Click to change");
		TEAM_ITEM_COLOR = getString(lang, "team.item.color", "&aSelect Team Color");
		TEAM_ITEM_READY = getString(lang, "team.item.ready", "&aReady");
		TEAM_ITEM_NOT_READY = getString(lang, "team.item.not-ready", "&cNot Ready");

		// Team Messages
		TEAM_MESSAGE_PLAYER_NOT_ONLINE = getString(lang, "team.message.player-not-online", "&c%player% isn't online.");
		TEAM_MESSAGE_NOW_READY = getString(lang, "team.message.now-ready", "&6Your team is now ready!");
		TEAM_MESSAGE_NOW_NOT_READY = getString(lang, "team.message.now-not-ready", "&6Your team is now NOT ready!");
		TEAM_MESSAGE_NOT_LEADER = getString(lang, "team.message.not-leader", "&cYou are not the leader of that team");
		TEAM_MESSAGE_JOIN_AS_PLAYER = getString(lang, "team.message.join-as-player", "&aYou have joined %leader%'s team");
		TEAM_MESSAGE_PLAYER_JOINS = getString(lang, "team.message.player-joins", "&a%player% has joined the team");
		TEAM_MESSAGE_LEAVE_AS_LEADER = getString(lang, "team.message.leave-as-leader", "&aYou have left your team, %newleader% will be the new leader");
		TEAM_MESSAGE_LEAVE_AS_PLAYER = getString(lang, "team.message.leave-as-player", "&aYou have left the team");
		TEAM_MESSAGE_PLAYER_LEAVES = getString(lang, "team.message.player-leaves", "&a%player% has left the team");
		TEAM_MESSAGE_LEADER_LEAVES = getString(lang, "team.message.leader-leaves", "&aTeam leader %leader% has left the team, %newleader% is the new leader");
		TEAM_MESSAGE_CANT_LEAVE = getString(lang, "team.message.cant-leave", "&cYou can't leave your team, you are alone.");
		TEAM_MESSAGE_DENY_REQUEST = getString(lang, "team.message.deny-request", "&cRequest denied.");
		TEAM_MESSAGE_NO_LONGER_EXISTS = getString(lang, "team.message.no-longer-exists", "&cThat team no longer exists.");
		TEAM_MESSAGE_INVITE_ALREADY_SENT = getString(lang, "team.message.invite-already-sent", "&cYou have already sent a invite to that player");
		TEAM_MESSAGE_INVITE_RECEIVE = getString(lang, "team.message.invite-receive", "&aYou got invited to team %name%, you can respond to the invite in the team menu.");
		TEAM_MESSAGE_INVITE_RECEIVE_HOVER = getString(lang, "team.message.invite-receive-hover", "&aClick to reply");
		TEAM_MESSAGE_ALREADY_IN_TEAM = getString(lang, "team.message.already-in-team", "&cThat player is already in the team.");
		TEAM_MESSAGE_PLAYER_ALREADY_IN_TEAM = getString(lang, "team.message.player-already-in-team", "&c%player% is already in a team");
		TEAM_MESSAGE_FULL = getString(lang, "team.message.full", "&c%player% cannot join %leader%'s team because the team is full (%limit% players)");
		TEAM_MESSAGE_COLOR_ALREADY_SELECTED = getString(lang, "team.message.color-already-selected", "&cYou already selected this color.");
		TEAM_MESSAGE_COLOR_UNAVAILABLE = getString(lang, "team.message.color-unavailable", "&cThis color is no longer available.");
		TEAM_MESSAGE_COLOR_CHANGED = getString(lang, "team.message.color-changed", "&aYour team color was successfully changed.");
		TEAM_MESSAGE_NAME_CHANGED = getString(lang, "team.message.name-changed", "&aYour team name was successfully changed.");
		TEAM_MESSAGE_NAME_CHANGED_ERROR = getString(lang, "team.message.name-changed-error", "&cThat team name is not allowed!");
		TEAM_CHAT_PREFIX  = getString(lang, "team.chat-prefix", "Team chat");

		// Items
		ITEMS_DEF_LORE = getString(lang, "items.def-lore", "&7Click to open!");
		ITEMS_CLICK_LORE = getString(lang, "items.click-lore", "&7Click to run!");
		ITEMS_TEAM_LIST = getString(lang, "items.team-list", "&9Right click to view teams");
		ITEMS_SWORD = getString(lang, "items.sword", "&bRight click to choose your team");
		ITEMS_BUNGEE = getString(lang, "items.bungee", "&cRight click to go back to the Hub");
		ITEMS_REGEN_HEAD = getString(lang, "items.regen-head", "&cRight click to regen your team for 5 seconds");
		ITEMS_REGEN_HEAD_ACTION = getString(lang, "items.regen-head-action", "&aYou get a 5 seconds regen effect for eating a player head");
		ITEMS_GOLDEN_HEAD_SKULL_NAME = getString(lang, "items.golden-head.skull-name", "&6%player%'s head");
		ITEMS_GOLDEN_HEAD_SKULL_HELP = getString(lang, "items.golden-head.skull-help", "&eCombine the head with gold to get a golden head.");
		ITEMS_GOLDEN_HEAD_APPLE_NAME = getString(lang, "items.golden-head.apple-name", "&6Golden Head");
		ITEMS_GOLDEN_HEAD_APPLE_HELP = getString(lang, "items.golden-head.apple-help", "&eEat this to heal 4 hearts and get 2 absorption hearts.");
		ITEMS_COMPASS_PLAYING = getString(lang, "items.compass-playing", "&aRight click to point to a teammate");
		ITEMS_COMPASS_PLAYING_ERROR = getString(lang, "items.compass-playing-error", "&cThere is no playing teammate to point to.");
		ITEMS_COMPASS_PLAYING_COOLDOWN = getString(lang, "items.compass-playing-cooldown", "&cYou're clicking the compass too fast, please wait!");
		ITEMS_COMPASS_PLAYING_POINTING = getString(lang, "items.compass-playing-pointing", "&aPointing towards %player%'s last location (%distance% blocks)");
		ITEMS_SPECTATOR_SPAWN = getString(lang, "items.compass-spectator-spawn", "&eWorld spawnpoint");
		ITEMS_SPECTATOR_PLAYERS = getString(lang, "items.compass-spectator-players", "&aAlive players");
		ITEMS_RANDOM_PLAYER = getString(lang, "items.random-player", "&cRandom player");
		ITEMS_OPEN_INV = getString(lang, "items.open-inv", "&6Open inv");
		ITEMS_KIT_SELECTION = getString(lang, "items.kit-selection", "&aRight click to choose a kit");
		ITEMS_KIT_INVENTORY = getString(lang, "items.kit-inventory", "&2Kit selection", 32);
		ITEMS_KIT_SELECTED =  getString(lang, "items.kit-selected", "&aYou selected the kit %kit%");
		ITEMS_KIT_NO_PERMISSION = getString(lang, "items.kit-no-permission", "&cYou don't have the permission to use that kit");
		ITEMS_KIT_SCOREBOARD_NO_KIT = getString(lang, "items.kit-scoreboard-no-kit", "No kit");
		ITEMS_CRAFT_NO_PERMISSION = getString(lang, "items.craft-no-permission", "&cYou don't have the permission to craft %craft%");
		ITEMS_CRAFT_CRAFTED = getString(lang, "items.craft-crafted", "&aYou have crafted a %craft%");
		ITEMS_CRAFT_LEFT_CLICK = getString(lang, "items.craft-left-click", "&cYou can only craft one %craft% at a time (left click).");
		ITEMS_CRAFT_LIMIT = getString(lang, "items.craft-limit", "&cYou have used all of your %limit% %craft% crafts.");
		ITEMS_CRAFT_BOOK = getString(lang, "items.craft-book", "&dRight click to see the custom crafts");
		ITEMS_CRAFT_BOOK_INVENTORY = getString(lang, "items.craft-book-inventory", "&2Custom crafts", 32);
		ITEMS_CRAFT_BOOK_BACK = getString(lang, "items.craft-book-back", "&7Back to crafts list");
		ITEMS_POTION_BANNED = getString(lang, "items.potion-banned", "&cSorry, level 2 potions are banned.");
		ITEMS_REVIVE_SUCCESS = getString(lang, "items.revive-success", "&a%player% has been revived!");
		ITEMS_REVIVE_ERROR = getString(lang, "items.revive-error", "&cNo dead team members found!");

		// PVP
		PVP_ENABLED = getString(lang, "pvp.enabled", "PVP enabled!");
		PVP_START_IN = getString(lang, "pvp.start-in", "PVP will start in %time%");

		// Glowing
		GLOWING_ENABLED = getString(lang, "glowing.enabled", "Glowing enabled!");
		GLOWING_START_IN = getString(lang, "glowing.start-in", "Glowing will start in %time%");

		WITHERING_ENABLED = getString(lang, "wintering-enabled", "All alive players are now being withered!");

		// Event
		EVENT_TIME_REWARD = getString(lang, "event.time-reward", "&eYou have received %money% in your account for playing %time% , total playing time %totaltime%");
		EVENT_KILL_REWARD = getString(lang, "event.kill-reward", "&eYou have received %money% in your account for killing a player");
		EVENT_WIN_REWARD = getString(lang, "event.win-reward", "&eYou have received %money% in your account for winning the game");

		// Scenarios
		SCENARIO_GLOBAL_DESCRIPTION_HEADER = getString(lang, "scenarios.global.description-header", "&5%scenario%&7:", 32);
		SCENARIO_GLOBAL_DESCRIPTION_PREFIX = getString(lang, "scenarios.global.description-prefix", "&7- ", 32);
		SCENARIO_GLOBAL_INVENTORY = getString(lang, "scenarios.global.inventory", "&6&lScenarios &7(Click for info)", 32);
		SCENARIO_GLOBAL_INVENTORY_EDIT = getString(lang, "scenarios.global.inventory-edit", "&6&lScenarios &7(Edit)", 32);
		SCENARIO_GLOBAL_INVENTORY_VOTE = getString(lang, "scenarios.global.inventory-vote", "&6&lVote &7(Toggle votes)", 32);
		SCENARIO_GLOBAL_ITEM_EDIT = getString(lang, "scenarios.global.item-edit", "&6Edit");
		SCENARIO_GLOBAL_ITEM_BACK = getString(lang, "scenarios.global.item-back", "&6Back");
		SCENARIO_GLOBAL_ITEM_NEXT = getString(lang, "scenarios.global.item-next", "&6Next");
		SCENARIO_GLOBAL_ITEM_HOTBAR = getString(lang, "scenarios.global.item-hotbar", "&6Right click to view active scenarios");
		SCENARIO_GLOBAL_ITEM_COLOR = getString(lang, "scenarios.global.item-color", "&5");
		SCENARIO_GLOBAL_ITEM_INFO = getString(lang, "scenarios.global.item-info", "&7(Right click for info)");
		SCENARIO_GLOBAL_ITEM_DISABLED = getString(lang, "scenarios.global.item-disabled", "&7Recently played, can vote after %amount% games");
		SCENARIO_GLOBAL_ITEM_DISABLED_PL = getString(lang, "scenarios.global.item-disabled-pl", "&7Recently played, can vote after %amount% games");
		SCENARIO_GLOBAL_VOTE_MAX = getString(lang, "scenarios.global.vote-max", "&cMax votes reached (%max%)");
		SCENARIO_GLOBAL_TITLE = getString(lang, "scenarios.global.title", "&eScenarios:");
		SCENARIO_GLOBAL_SUBTITLE = getString(lang, "scenarios.global.subtitle", "&f%scenarios%");
		SCENARIO_GLOBAL_HEADER = getString(lang, "scenarios.global.header", "&eEnabled scenarios:");
		SCENARIO_VOTE = getString(lang, "scenarios.vote", "&fVote for scenarios!");

		// Spectators
		SPECTATOR_GUI_TITLE = getString(lang, "spectators.teleport.title", "All alive players");
		SPECTATOR_GUI_ITEM = getString(lang, "spectators.teleport.item", "%color% &f%player%");
		SPECTATOR_GUI_LORE = getStringList(lang, "spectators.teleport.lore", new ArrayList<>(List.of("&7Click to teleport!")));

		// load scenario info
		JsonObject defaultInfo = getDefaultScenarioInfo();
		for (Scenario scenario : Scenario.BUILD_IN_SCENARIOS){
			JsonObject scenarioDefault = defaultInfo.get(scenario.getKey()).getAsJsonObject();
			String defaultName = scenarioDefault.get("name").getAsString();
			JsonArray defaultDescription = scenarioDefault.get("description").getAsJsonArray();

			String path = "scenarios." + scenario.getKey();

			ConfigurationSection section = lang.getConfigurationSection(path);
			if (section == null){
				// Perhaps stored under old path
				String oldPath = "scenarios." + scenario.getKey().replace("_", "");
				section = lang.getConfigurationSection(oldPath);

				// TODO: Remove conversion system on future update!
				if (section != null){
					lang.set(path, section);
					lang.remove(oldPath);
					pathChanges = true;
				}
			}

			Scenario.Info info = new Scenario.Info(
					getString(lang, path + ".name", defaultName),
					getStringList(lang, path + ".description", defaultDescription)
			);

			scenario.setInfo(info);
		}

		SCENARIO_BESTPVE_ADDED = getString(lang, "scenarios.best_pve.added", "&4[Best PvE] &aYou are added to the PvE list.");
		SCENARIO_BESTPVE_REMOVED = getString(lang, "scenarios.best_pve.removed", "&4[Best PvE] &cYou are now removed from the PvE list. Getting a kill will add you back to the list.");
		SCENARIO_BESTPVE_BACK = getString(lang, "scenarios.best_pve.back", "&4[Best PvE] &aYou are added back to the PvE list.");
		SCENARIO_BOWLESS_ERROR = getString(lang, "scenarios.bowless.error", "&4[Bowless] &cYou can't craft bows!");
		SCENARIO_HORSELESS_ERROR = getString(lang, "scenarios.horseless.error", "&4[Horseless] &cYou can't ride that horse!");
		SCENARIO_NOCLEAN_INVULNERABLE = getString(lang, "scenarios.no_clean.invulnerable", "&4[NoClean] &aYou are now invulnerable for 30 seconds!");
		SCENARIO_NOCLEAN_VULNERABLE = getString(lang, "scenarios.no_clean.vulnerable", "&4[NoClean] &cYou can now take damage again!");
		SCENARIO_NOCLEAN_ERROR = getString(lang, "scenarios.no_clean.error", "&4[NoClean] &cYou can't damage this player!");
		SCENARIO_RODLESS_ERROR = getString(lang, "scenarios.rodless.error", "&4[Rodless] &cYou can't craft fishing rods!");
		SCENARIO_SHIELDLESS_ERROR = getString(lang, "scenarios.shieldless.error", "&4[Shieldless] &cYou can't craft shields!");
		SCENARIO_SWITCHEROO_SWITCH = getString(lang, "scenarios.switcheroo.switch", "&4[Switcheroo] &6You have switched positions with &3%player%");
		SCENARIO_LOVEATFIRSTSIGHT_JOIN_ERROR = getString(lang, "scenarios.love_at_first_sight.join-error", "&cCan't join teams, Love at first sight is enabled!");
		SCENARIO_LOVEATFIRSTSIGHT_JOIN_BROADCAST = getString(lang, "scenarios.love_at_first_sight.join-broadcast", "&4[Love At First Sight] &a%player% has joined %leader%'s team");
		SCENARIO_SKYHIGH_DAMAGE = getString(lang, "scenarios.sky_high.damage", "&4[Sky High] &cYou're taking damage as your under y=120");
		SCENARIO_TEAMINVENTORY_ERROR = getString(lang, "scenarios.team_inventory.error", "&cYou may only open your team's inventory while playing!");
		SCENARIO_TEAMINVENTORY_ERROR = getString(lang, "scenarios.team_inventory.disabled", "&cTeam Inventory is currently disabled!");
		SCENARIO_TEAMINVENTORY_OPEN = getString(lang, "scenarios.team_inventory.open", "&aOpening team inventory ...");
		SCENARIO_TEAMINVENTORY_ITEM = getString(lang, "scenarios.team_inventory.item", "&6Team inventory");
		SCENARIO_SILENTNIGHT_ERROR = getString(lang, "scenarios.silent_night.error", "&4[Silent Night] &cSilent Night is enabled");
		SCENARIO_WEAKESTLINK_KILL = getString(lang, "scenarios.weakest_link.kill", "&4[Weakest Link] &c%player% has been killed!");
		SCENARIO_NOGOINGBACK_ERROR = getString(lang, "scenarios.no_going_back.error", "&4[No Going Back] &cYou are stuck in the nether!");
		SCENARIO_MONSTERSINC_ERROR = getString(lang, "scenarios.monsters_inc.error", "&4[Monsters Inc.] &cStop that!");
		SCENARIO_TIMEBOMB_CHEST = getString(lang, "scenarios.timebomb.chest", "&6&l%player%'s Timebomb");

		DEATH_CUSTOM = getString(lang, "death.custom", "%player% &fdied");
		DEATH_CONTACT = getString(lang, "death.contact", "%player% &fdied from spikes");
		DEATH_DROWNING = getString(lang, "death.drowning", "%player% &fdrowned");
		DEATH_EXPLOSION = getString(lang, "death.explosion", "%player% &fdied from explosion");
		DEATH_FALL = getString(lang, "death.fall", "%player% &fdied by falling");
		DEATH_FALLING_BLOCK = getString(lang, "death.falling-block", "%player% &fsuffocated");
		DEATH_FIRE = getString(lang, "death.fire", "%player% &fdied by fire");
		DEATH_FLY_INTO_WALL = getString(lang, "death.fly-into-wall", "%player% &fdied from flying into wall");
		DEATH_LAVA = getString(lang, "death.lava", "%player% &fdied by lava");
		DEATH_MAGIC = getString(lang, "death.magic", "%player% &fdied by magic");
		DEATH_MOB = getString(lang, "death.mob", "%player% &fdied by %mob%");
		DEATH_PROJECTILE = getString(lang, "death.projectile", "%player% &fwas shot");
		DEATH_PLAYER = getString(lang, "death.player", "%killer% &fkilled %player%");
		DEATH_PLAYER_PROJECTILE = getString(lang, "death.player-projectile", "%killer% &fkilled %player% with an arrow");
		DEATH_SUICIDE = getString(lang, "death.suicide", "%player% &fdied");
		DEATH_STARVATION = getString(lang, "death.starvation", "%player% &fdied from starvation");
		DEATH_OTHER_CAUSE = getString(lang, "death.other-cause", "%player% &fdied");

		TEAM_FULL_NAME = getString(lang, "team.name_full", "%color%&7 Team");
		TEAM_GUI_TITLE = getString(lang, "team.gui_title", "Choose team");
		TEAM_GUI_LORE = getStringList(lang, "team.gui_lore", new ArrayList<>(List.of("&7Members:")));

		if (lang.addedDefaultValues() || pathChanges) {
			try {
				lang.save(langFile);
			} catch (IOException ex) {
				Bukkit.getLogger().severe("[UhcCore] Failed to edit " + langFile.toString());
				ex.printStackTrace();
			}
		}
	}

	private String getString(FileConfiguration lang, String path, String def, int maxLenth){
		String string = RandomUtils.color(lang.getString(path, def));

		if (maxLenth != -1 && string.length() > maxLenth){
			Bukkit.getLogger().severe("[UhcCore] The message " + path + " is too long, max length is " + maxLenth + " characters!");
			string = string.substring(0, maxLenth);
		}

		return string;
	}

	private String getString(FileConfiguration lang, String path, String def){
		return getString(lang, path, def, -1);
	}

	private List<String> getStringList(FileConfiguration lang, String path, List<String> def){
		List<String> list = lang.getStringList(path);
		if (list.isEmpty()){
			list = def;
			lang.set(path, def);
		}

		// Translate color codes.
		for (int i = 0; i < list.size(); i++) {
			list.set(i, RandomUtils.color(list.get(i)));
		}

		return list;
	}

	private List<String> getStringList(FileConfiguration lang, String path, JsonArray def){
		List<String> defList = new ArrayList<>();
		def.forEach(e -> defList.add(e.getAsString()));
		return getStringList(lang, path, defList);
	}

	private JsonObject getDefaultScenarioInfo(){
		try{
			InputStream in = getClass().getResourceAsStream("/scenario-descriptions.json");
			Validate.notNull(in);
			JsonObject json = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
			in.close();
			return json;
		}catch (Exception ex){
			ex.printStackTrace();
		}

		return new JsonObject();
	}

}