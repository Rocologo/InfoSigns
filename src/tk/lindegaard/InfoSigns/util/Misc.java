package tk.lindegaard.InfoSigns.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Misc
{
	public static boolean isAxe(ItemStack item)
	{
		return (item.getType() == Material.DIAMOND_AXE || item.getType() == Material.GOLD_AXE || item.getType() == Material.IRON_AXE || item.getType() == Material.STONE_AXE || item.getType() == Material.WOOD_AXE);
	}
	
	public static boolean isSword(ItemStack item)
	{
		return (item.getType() == Material.DIAMOND_SWORD || item.getType() == Material.GOLD_SWORD || item.getType() == Material.IRON_SWORD || item.getType() == Material.STONE_SWORD || item.getType() == Material.WOOD_SWORD);
	}
	
	public static boolean isPick(ItemStack item)
	{
		return (item.getType() == Material.DIAMOND_PICKAXE || item.getType() == Material.GOLD_PICKAXE || item.getType() == Material.IRON_PICKAXE || item.getType() == Material.STONE_PICKAXE || item.getType() == Material.WOOD_PICKAXE);
	}
	
	
	public static Map<String, Object> toMap(Location loc)
	{
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("X", loc.getX());
		map.put("Y", loc.getY());
		map.put("Z", loc.getZ());
		
		map.put("Yaw", (double)loc.getYaw());
		map.put("Pitch", (double)loc.getPitch());
		
		if(loc.getWorld() != null)
			map.put("W", loc.getWorld().getUID().toString());
		
		return map;
	}
	
	public static Location fromMap(Map<String, Object> map)
	{
		double x,y,z;
		float yaw,pitch;
		UUID world;
		
		x = (Double)map.get("X");
		y = (Double)map.get("Y");
		z = (Double)map.get("Z");
		
		yaw = (float)(double)(Double)map.get("Yaw");
		pitch = (float)(double)(Double)map.get("Pitch");
		
		if(map.containsKey("W"))
		{
			world = UUID.fromString((String)map.get("W"));
			return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
		}
		else
			return new Location(null, x, y, z, yaw, pitch);
	}
}
