package com.gmail.val59000mc.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RandomUtils {
	private final static Random r = new Random();
	
	public static int randomInteger(int min, int max){
		int realMin = Math.min(min, max);
		int realMax = Math.max(min, max);
		int exclusiveSize = realMax-realMin;
		return r.nextInt(exclusiveSize+1)+min;
	}
	
	public static BlockFace randomAdjacentFace(){
		BlockFace[] faces = new BlockFace[]{
			BlockFace.DOWN,
			BlockFace.UP,
			BlockFace.EAST,
			BlockFace.WEST,
			BlockFace.NORTH,
			BlockFace.SOUTH
		};
		return faces[randomInteger(0,faces.length-1)];
	}

	public static Location newRandomLocation(World world, double maxDistance, List<Location> nearbyLocations, double minDistanceBetween) {
		double squared = minDistanceBetween * minDistanceBetween;

		for (int i = 0; i < 300; i++) {
			double x = 2 * maxDistance * r.nextDouble() - maxDistance;
			double z = 2 * maxDistance * r.nextDouble() - maxDistance;

			Location loc = new Location(world, x, 250, z);
			boolean valid = true;

			for (Location nLoc : nearbyLocations) {
				if (nLoc.distanceSquared(loc) < squared) {
					valid = false;
					break;
				}
			}

			if (valid)
				return loc;
		}

		return null;
	}

	private static Pattern HEX_PATTERN = Pattern.compile("&(#[a-f0-9]{6})", 2);

	public static String color(String input) {
		Matcher m = HEX_PATTERN.matcher(input);
		try {
			ChatColor.class.getDeclaredMethod("of", new Class[] { String.class });
			while (m.find())
				input = input.replace(m.group(), ChatColor.of(m.group(1)).toString());
		} catch (Exception e) {
			while (m.find())
				input = input.replace(m.group(), "");
		}
		return ChatColor.translateAlternateColorCodes('&', input);
	}

	public static List<String> color(List<String> input) {
		List<String> result = new ArrayList<>();

		for (String s : input) {
			Matcher m = HEX_PATTERN.matcher(s);
			try {
				ChatColor.class.getDeclaredMethod("of", new Class[] { String.class });
				while (m.find())
					s = s.replace(m.group(), ChatColor.of(m.group(1)).toString());
			} catch (Exception e) {
				while (m.find())
					s = s.replace(m.group(), "");
			}
			result.add(ChatColor.translateAlternateColorCodes('&', s));
		}

		return result;
	}

	public static ItemStack getColoredChestPlate(int r, int g, int b) {
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(Color.fromRGB(r, g, b));
		item.setItemMeta(meta);
		return item;
	}

	public static boolean isAnnounceTimer(int timer) {
		return switch (timer) {
			case 600, 300, 180, 60, 30, 10, 3, 2, 1 -> true;
			default -> false;
		};
	}

	public static Location getSafePoint(Location loc) {
		boolean unsafe = true;
		while (unsafe) {
			if (loc.getBlock().getType() == Material.AIR && loc.getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
				return loc;
			} else {
				loc.add(0, 1, 0);
				if (loc.getY() > 255)
					unsafe = false;
			}
		}

		return loc;
	}
}
