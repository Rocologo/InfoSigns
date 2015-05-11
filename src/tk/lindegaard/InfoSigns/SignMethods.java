package tk.lindegaard.InfoSigns;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class SignMethods {
	
	private InfoSigns plugin;
	
	public List<Material> supportedmats = new ArrayList<Material>();
	
	public SignMethods(InfoSigns plugin) {
		this.plugin = plugin;
		/*supportedmats.add(Material.REDSTONE_COMPARATOR_OFF);
		supportedmats.add(Material.REDSTONE_COMPARATOR_ON);
		supportedmats.add(Material.REDSTONE_LAMP_OFF);
		supportedmats.add(Material.REDSTONE_LAMP_ON);
		supportedmats.add(Material.REDSTONE_TORCH_OFF);
		supportedmats.add(Material.REDSTONE_TORCH_ON);*/
		supportedmats.add(Material.REDSTONE_WIRE);
		/*supportedmats.add(Material.DISPENSER);
		supportedmats.add(Material.FURNACE);
		supportedmats.add(Material.POWERED_RAIL);
		supportedmats.add(Material.ACTIVATOR_RAIL);
		supportedmats.add(Material.DIODE_BLOCK_OFF);
		supportedmats.add(Material.DIODE_BLOCK_ON);
		supportedmats.add(Material.COMMAND);
		supportedmats.add(Material.FENCE_GATE);
		supportedmats.add(Material.IRON_DOOR);
		supportedmats.add(Material.WOODEN_DOOR);
		supportedmats.add(Material.JUKEBOX);
		supportedmats.add(Material.PISTON_BASE);
		supportedmats.add(Material.PISTON_STICKY_BASE);
		supportedmats.add(Material.TNT);
		supportedmats.add(Material.TRAP_DOOR);*/
	}

	public void update(World w) {
		if (!plugin.signs.getKeys(false).contains("server")) {
			plugin.signs.createSection("server");
		}
		if (!plugin.signs.getKeys(false).contains("world")) {
			plugin.signs.createSection("world");
		}
		if (!plugin.signs.getKeys(false).contains("region")) {
			plugin.signs.createSection("region");
		}
		if (!plugin.signs.getKeys(false).contains("player")) {
			plugin.signs.createSection("player");
		}
		for (SignType type : SignType.values()) {
			/*
			if (plugin.signs.getConfigurationSection(SignTypetoString(type)).getKeys(false).contains(w.getName())) {
				for (String key : plugin.signs.getConfigurationSection(SignTypetoString(type)+"."+w.getName()).getKeys(false)) {
					String[] coords = key.split(",");
					int x;
					int y;
					int z;
					if (plugin.isInt(coords[0]) && plugin.isInt(coords[1]) && plugin.isInt(coords[2])) {
						x = Integer.parseInt(coords[0]);
						y = Integer.parseInt(coords[1]);
						z = Integer.parseInt(coords[2]);
					} else {
						plugin.signs.set(SignTypetoString(type)+"."+w.getName()+key, null);
						plugin.print("Removed invalid line in signs.yml");
						continue;
					}
					update(w, x, y, z, type);
				}
			}*/
			update(type);
		}
		plugin.saveSignsConfig();
	}

	public void update(final World w, final Location loc, final SignType type) {
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				Sign sign;
				if (w.getBlockAt(loc).getType() == Material.SIGN_POST || (w.getBlockAt(loc).getType() == Material.WALL_SIGN)) {
					sign = (Sign) w.getBlockAt(loc).getState();
				} else {
					plugin.print("Block at " + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + ", is no longer a sign.");
					return;
					//return false;
				}
				String coords = loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
				if (!isStoredSign(sign.getBlock())) {
					setMetadata(sign, type);
				}
				List<String> info = plugin.signs.getStringList(SignTypetoString(type)+"."+w.getName()+"."+coords);
				boolean dopower = false;
				if (info.get(0).equalsIgnoreCase("true")) {
					dopower = true;
				}
				String format = info.get(1);
				List<String> lines = new ArrayList<String>();
				if (plugin.formats.containsKey(format)) {
					lines = new ArrayList<String>(plugin.formats.get(format));
				} else {
					lines.add("�cError!");
					lines.add("The format:");
					lines.add("�c" + format);
					lines.add("does not exist.");
				}
				Random rand = new Random();
				int nextint = rand.nextInt(1000000000);
				//boolean plural = false;
				boolean power = false;
				if (dopower) {
					for (int i = 0; i < 4; i++) {
						String newline = variableize(lines.get(i), type, sign, info, nextint);
						if (isPowered(newline) && power == false) {
							power = true;
						}
						/*
						Pattern newp = Pattern.compile("\\(s\\)");
						Matcher newm = newp.matcher(newline);
						Pattern newpp = Pattern.compile("[0-9][0-9]*");
						Matcher newmm = newpp.matcher(newline);
						if (newm.find()) {
							if (newmm.find()) {
								if (newmm.group().equalsIgnoreCase("1")) {
									newline = newline.replaceAll(newp.pattern(), "");
								} else {
									newline = newline.replaceAll(newp.pattern(), "s");
									plural = true;
								}
							}
						}*/
						newline = removePowered(newline);
						lines.set(i, newline);
					}
				} else {
					for (int i = 0; i < 4; i++) {
						String newline = variableize(lines.get(i), type, sign, info, nextint);
						newline = removePowered(newline);
						lines.set(i, newline);
					}
				}
				for (int i = 0; i < 4; i++) {
					sign.setLine(i, lines.get(i));
				}
				sign.update();
				if (power) {
					List<String> coordsforpoweredsigns;
					coordsforpoweredsigns = plugin.poweredsigns.get(sign.getWorld());
					if (coordsforpoweredsigns == null) {
						coordsforpoweredsigns = new ArrayList<String>();
					}
					coordsforpoweredsigns.add(coords);
					plugin.poweredsigns.put(sign.getWorld(), coordsforpoweredsigns);
					setPower(sign, true);
				} else {
					List<String> coordsforpoweredsigns = new ArrayList<String>();
					coordsforpoweredsigns = plugin.poweredsigns.get(sign.getWorld());
					if (coordsforpoweredsigns != null) {
						coordsforpoweredsigns.remove(coords);
					}
					plugin.poweredsigns.put(sign.getWorld(), coordsforpoweredsigns);
					setPower(sign, false);
				}
				//return true;
			}
		}, 2L);
	}
	
	public void update(World w, int x, int y, int z, SignType type) {
		update(w, new Location(w, x, y, z), type);
	}
	
	public void update(SignType signtype) {
		String type = SignTypetoString(signtype);
		
		if (!plugin.signs.getKeys(false).contains("server")) {
			plugin.signs.createSection("server");
		}
		if (!plugin.signs.getKeys(false).contains("world")) {
			plugin.signs.createSection("world");
		}
		if (!plugin.signs.getKeys(false).contains("region")) {
			plugin.signs.createSection("region");
		}
		if (!plugin.signs.getKeys(false).contains("player")) {
			plugin.signs.createSection("player");
		}
		
		for (World w : plugin.getServer().getWorlds()) {
			if (plugin.signs.getConfigurationSection(type).getKeys(false).contains(w.getName())) {
				for (String key : plugin.signs.getConfigurationSection(type+"."+w.getName()).getKeys(false)) {
					String[] coords = key.split(",");
					int x;
					int y;
					int z;
					if (plugin.isInt(coords[0]) && plugin.isInt(coords[1]) && plugin.isInt(coords[2])) {
						x = Integer.parseInt(coords[0]);
						y = Integer.parseInt(coords[1]);
						z = Integer.parseInt(coords[2]);
					} else {
						plugin.signs.set(type+"."+w.getName()+key, null);
						plugin.print("Removed invalid line in signs.yml");
						continue;
					}
					update(w, x, y, z, signtype);
				}
			}
		}
		plugin.saveSignsConfig();
	}
	
	public void updateAll() {
		try {
			for (SignType type : SignType.values()) {
				update(type);
			}
		} catch (Exception ex) {
			plugin.print("�c[AMS] Signs.yml is wrong! If you have just upgraded from OPS use /amsupgradefromops, if you are not upgrading but this just occured, either manually check the signs.yml file or run /amsverify for autofix (Note: This can/will remove something from the signs.yml).");
		}
	}
	
	public void setMetadata(Sign sign, SignType value){
		sign.setMetadata("SignMetaData",new FixedMetadataValue(plugin,value));
	}
	
	public void removeMetadata(Sign sign) {
		sign.removeMetadata("SignMetaData", plugin);
	}
	
	public SignType getMetadata(Sign sign) {
	  List<MetadataValue> values = sign.getMetadata("SignMetaData");
	  for(MetadataValue value : values) {
	     if(value.getOwningPlugin().getDescription().getName().equals(plugin.getDescription().getName())){
	    	 if (value.value() instanceof SignType) {
	    		 return (SignType) value.value();
	    	 }
	     }
	  }
	  return null;//SignType.SERVER;
	}
	
	public String getMetaString(Sign sign) {
		return SignTypetoString(getMetadata(sign));
	}
	
	public String variableize(String s, SignType type, Sign sign, List<String> info, int id) {
		boolean power = false;
		List<String> qargs = new ArrayList<String>();
		switch (type) {
		case PLAYER:
			try {
				int players;
				int totalplayers = plugin.getServer().getOnlinePlayers().size();
				int maxplayers = plugin.getServer().getMaxPlayers();
				String player = info.get(2);
				Player p = plugin.getServer().getPlayerExact(player.replaceAll("&[0-9a-fk-or]", ""));
				World w = null;
				boolean showmsg = false;
				String world = "";
				if (info.size() >= 4) {
					world = info.get(3);
				}
				for (World tempw : plugin.getServer().getWorlds()) {
					if (tempw.getName().equalsIgnoreCase(world.replaceAll("&[0-9a-fk-or]", ""))) {
						w = tempw;
					}
					
					/**for (Player tempp : tempw.getPlayers()) {
						if (!tempp.isOp() && tempp.hasPermission("ams.bypass.normal")) {
							totalplayers--;
						}
					}*/
				}
				if (w != null) {
					players = w.getPlayers().size();
					/**for (Player tempp : w.getPlayers()) {
						if (!tempp.isOp() && tempp.hasPermission("ams.bypass.normal")) {
							players--;
						}
					}*/
				} else {
					players = totalplayers;
				}
				if (p != null) {
					if (p.isOnline()/** && !p.hasPermission("ams.bypass.player") && !p.isOp()*/) {
						showmsg = true;
					}
				}
				s = s.replaceAll("%players%", players + "").replaceAll("%totalplayers%", totalplayers + "").replaceAll("%maxplayers%", maxplayers + "")
						.replaceAll("%world%", world).replaceAll("%player%", player).replaceAll("//[0-9][0-9]*//", "");
				Pattern pattern;
				if (showmsg) {
					s = s.replaceAll("%offmsg=[^%]*%", "");
					pattern = Pattern.compile("%onmsg=[^%]*%");
				} else {
					s = s.replaceAll("%onmsg=[^%]*%", "");
					pattern = Pattern.compile("%offmsg=[^%]*%");
				}
				Matcher m = pattern.matcher(s);
				if (showmsg) {
					while (m.find()) {
						s = s.replaceFirst(pattern.pattern(), m.group().substring(7, m.group().length() - 1));
						m = pattern.matcher(s);
					}
				} else {
					while (m.find()) {
						s = s.replaceFirst(pattern.pattern(), m.group().substring(8, m.group().length() - 1));
						m = pattern.matcher(s);
					}
				}
				if (info.get(0).equalsIgnoreCase("true")) {
					Pattern pp;
					if (p != null/** && (!p.hasPermission("ams.bypass.player") && !p.isOp()) || p != null && p.isOp()*/) {
						if (p.isOnline()) {
							power = true;
							s = s.replaceAll("%poweroff=[^%]*%", "");
							pp = Pattern.compile("%poweron=[^%]*%");
						} else {
							s = s.replaceAll("%poweron=[^%]*%", "");
							pp = Pattern.compile("%poweroff=[^%]*%");
						}
					} else {
						s = s.replaceAll("%poweron=[^%]*%", "");
						pp = Pattern.compile("%poweroff=[^%]*%");
					}
					Matcher pm = pp.matcher(s);
					if (p != null/** && !p.hasPermission("ams.bypass.player") && !p.isOp() || p != null && p.isOp()*/) {
						while (pm.find()) {
							s = s.replaceFirst(pattern.pattern(), pm.group().substring(9, pm.group().length() - 1));
							pm = pp.matcher(s);
						}
					} else {
						while (pm.find()) {
							s = s.replaceFirst(pattern.pattern(), pm.group().substring(10, pm.group().length() - 1));
							pm = pp.matcher(s);
						}
					}
				} else {
					s = s.replaceAll("%poweron=[^%]*%", "");
					s = s.replaceAll("%poweroff=[^%]*%", "");
				}
			} catch(Exception ex) {
				plugin.print("The signs.yml file is invalid. Use the /amsverify command for more details or /amsfix autofix/autoremoval.");
			}
			break;
		case REGION:
			try {
				int players = 0;
				int totalplayers = plugin.getServer().getOnlinePlayers().size();
				int maxplayers = plugin.getServer().getMaxPlayers();
				World w = null;
				String world = info.get(2);
				for (World tempw : plugin.getServer().getWorlds()) {
					if (tempw.getName().equalsIgnoreCase(world.replaceAll("&[0-9a-fk-or]", ""))) {
						w = tempw;
					}
					/**
					for (Player tempp : tempw.getPlayers()) {
						if (tempp.hasPermission("ams.bypass.normal") && !tempp.isOp()) {
							totalplayers--;
						}
					}*/
				}
				Location firstloc;
				Location secondloc;
				String xyzxyz = info.get(3);
				if (w != null) {
					firstloc = new Location(w, Integer.parseInt(xyzxyz.split(",")[0]), Integer.parseInt(xyzxyz.split(",")[1]), Integer.parseInt(xyzxyz.split(",")[2]));
					secondloc = new Location(w, Integer.parseInt(xyzxyz.split(",")[3]), Integer.parseInt(xyzxyz.split(",")[4]), Integer.parseInt(xyzxyz.split(",")[5]));
					for (Player p : w.getPlayers()) {
						if (insideBox(p, firstloc, secondloc)) {
							players++;
						}
					}
				} else {
					players = totalplayers;
				}
				s = s.replaceAll("%players%", players + "").replaceAll("%totalplayers%", totalplayers + "").replaceAll("%maxplayers%", maxplayers + "")
						.replaceAll("%world%", world).replaceAll("%player%", "").replaceAll("%onmsg=[^%]*%", "").replaceAll("%offmsg=[^%]*%", "");
				String question = "";
				for (int i = 4; i < info.size(); i++) {
					question = question + info.get(i).trim() + (i+1==info.size() ? "&" : "");
				}
				question = question.replaceAll("p", players+"").replaceAll("mp", maxplayers+"").replaceAll("tp", totalplayers+"");
				for (String temp : question.split("/|<=|>=|<|>|&|=")) {
					qargs.add(temp);
				}
				power = getAnswer(question);
				if (info.get(0).equalsIgnoreCase("true")) {
					if (power) {
						Pattern on = Pattern.compile("%poweron=[^%]*%");
						s = s.replaceAll("%poweroff=[^%]*%", "");
						Matcher m = on.matcher(s);
						while (m.find()) {
							s = s.replaceFirst(on.pattern(), m.group().substring(9, m.group().length() - 1));
							m = on.matcher(s);
						}
					} else {
						Pattern off = Pattern.compile("%poweroff=[^%]*%");
						s = s.replaceAll("%poweron=[^%]*%", "");
						Matcher m = off.matcher(s);
						while (m.find()) {
							s = s.replaceFirst(off.pattern(), m.group().substring(10, m.group().length() - 1));
							m = off.matcher(s);
						}
					}
				} else {
					s = s.replaceAll("%poweroff=[^%]*%", "");
					s = s.replaceAll("%poweron=[^%]*%", "");
				}
				s = replaceQArgs(s, qargs);
			} catch(Exception ex) {
				ex.printStackTrace();
				plugin.print("The signs.yml file is invalid. Use the /amsverify command for more details or /amsfix autofix/autoremoval.");
			}
			break;
		case SERVER:
			try {
				int totalplayers = plugin.getServer().getOnlinePlayers().size();
				int maxplayers = plugin.getServer().getMaxPlayers();
				
				/**for (Player p : plugin.getServer().getOnlinePlayers()) {
					if (p.hasPermission("ams.bypass.normal") && !p.isOp()) {
						totalplayers--;
					}
				}*/
				
				int players = totalplayers;
				s = s.replaceAll("%players%", players + "").replaceAll("%totalplayers%", totalplayers + "").replaceAll("%maxplayers%", maxplayers + "")
						.replaceAll("%world%", "").replaceAll("%player%", "").replaceAll("%onmsg=[^%]*%", "").replaceAll("%offmsg=[^%]*%", "");
				if (info.get(0).equalsIgnoreCase("true")) {
					Pattern pp;
					String question = "";
					for (int i = 2; i < info.size(); i++) {
						question = question + info.get(i).trim() + (i+1==info.size() ? "&" : "");
					}
					question = question.replaceAll("p", players+"").replaceAll("mp", maxplayers+"").replaceAll("tp", totalplayers+"");
					power = getAnswer(question);
					for (String temp : question.split("/|<=|>=|<|>|&|=")) {
						qargs.add(temp);
					}
					if (power) {
						s = s.replaceAll("%poweroff=[^%]*%", "");
						pp = Pattern.compile("%poweron=[^%]*%");
					} else {
						s = s.replaceAll("%poweron=[^%]*%", "");
						pp = Pattern.compile("%poweroff=[^%]*%");
					}
					Matcher pm = pp.matcher(s);
					if (power) {
						while (pm.find()) {
							s = s.replaceFirst(pp.pattern(), pm.group().substring(9, pm.group().length() - 1));
							pm = pp.matcher(s);
						}
					} else {
						while (pm.find()) {
							s = s.replaceFirst(pp.pattern(), pm.group().substring(10, pm.group().length() - 1));
							pm = pp.matcher(s);
						}
					}
				} else {
					s = s.replaceAll("%poweron=[^%]*%", "");
					s = s.replaceAll("%poweroff=[^%]*%", "");
				}
				s = replaceQArgs(s, qargs);
			} catch(Exception ex) {
				plugin.print("The signs.yml file is invalid. Use the /amsverify command for more details or /amsfix autofix/autoremoval.");
			}
			break;
		case WORLD:
			try {
				int players = 0;
				int totalplayers = plugin.getServer().getOnlinePlayers().size();
				int maxplayers = plugin.getServer().getMaxPlayers();
				World w = null;
				String world = info.get(2);
				for (World tempw : plugin.getServer().getWorlds()) {
					if (tempw.getName().equalsIgnoreCase(world.replaceAll("&[0-9a-fk-or]", ""))) {
						w = tempw;
					}
					/**for (Player tempp : tempw.getPlayers()) {
						if (tempp.hasPermission("ams.bypass.normal") && !tempp.isOp()) {
							totalplayers--;
						}
					}*/
				}
				if (w == null) {
					w = sign.getWorld();
				}
				players = w.getPlayers().size();
				/**for (Player p : w.getPlayers()) {
					/**if (!p.hasPermission("ams.bypass.normal") && !p.isOp()) {
						players++;
					}
					players++;
				}*/
				s = s.replaceAll("%players%", players + "").replaceAll("%totalplayers%", totalplayers + "").replaceAll("%maxplayers%", maxplayers + "")
						.replaceAll("%world%", world).replaceAll("%player%", "").replaceAll("%onmsg=[^%]*%", "").replaceAll("%offmsg=[^%]*%", "");
				String question = "";
				for (int i = 3; i < info.size(); i++) {
					question = question + info.get(i).trim() + (i+1==info.size() ? "&" : "");
				}
				question = question.replaceAll("p", players+"").replaceAll("mp", maxplayers+"").replaceAll("tp", totalplayers+"");
				for (String temp : question.split("/|<=|>=|<|>|&|=")) {
					qargs.add(temp);
				}
				power = getAnswer(question);
				if (info.get(0).equalsIgnoreCase("true")) {
					if (power) {
						Pattern on = Pattern.compile("%poweron=[^%]*%");
						s = s.replaceAll("%poweroff=[^%]*%", "");
						Matcher m = on.matcher(s);
						while (m.find()) {
							s = s.replaceFirst(on.pattern(), m.group().substring(9, m.group().length() - 1));
							m = on.matcher(s);
						}
					} else {
						Pattern off = Pattern.compile("%poweroff=[^%]*%");
						s = s.replaceAll("%poweron=[^%]*%", "");
						Matcher m = off.matcher(s);
						while (m.find()) {
							s = s.replaceFirst(off.pattern(), m.group().substring(10, m.group().length() - 1));
							m = off.matcher(s);
						}
					}
				} else {
					s = s.replaceAll("%poweroff=[^%]*%", "");
					s = s.replaceAll("%poweron=[^%]*%", "");
				}
				s = replaceQArgs(s, qargs);
			} catch(Exception ex) {
				plugin.print("The signs.yml file is invalid. Use the /amsverify command for more details or /amsfix autofix/autoremoval.");
			}
			break;
		}
		s = s.replaceAll("&((?i)[0-9a-fk-or])", "\u00A7$1");
		String[] checks = {"+", "-", "/", "*"};
		for (String check : checks) {
			Pattern pattern = Pattern.compile("[0-9][0-9]*\\" + check + "[0-9][0-9]*");
			Matcher m = pattern.matcher(s);
			int count = 0;
			while (m.find() && count < 15) {
				if (check.equals("+")) {
					try {
						double first = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[0])-1));
						double second = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[1])-1));
						double answer = first + second;
						s = s.replaceFirst(m.group(), (int)answer+"");
					} catch (Exception ex) {}
				} else if (check.equals("-")) {
					try {
						double first = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[0])-1));
						double second = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[1])-1));
						double answer = first - second;
						s = s.replaceFirst(m.group(), (int)answer+"");
					} catch (Exception ex) {}
				} else if (check.equals("/")) {
					try {
						double first = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[0])-1));
						double second = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[1])-1));
						double answer = first / second;
						s = s.replaceFirst(m.group(), (int)answer+"");
					} catch (Exception ex) {}
				} else if (check.equals("*")) {
					try {
						double first = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[0])-1));
						double second = Double.parseDouble(qargs.get(Integer.parseInt(m.group().split(check)[1])-1));
						double answer = first * second;
						s = s.replaceFirst(m.group(), (int)answer+"");
					} catch (Exception ex) {}
				}
				m = pattern.matcher(s);
				count++;
			}
			if (count == 15) {
				plugin.print(ChatColor.RED + "Contact Whitehooder over at the AMS thread and tell him that the count has reached 15. If you ignore this message, just FYI your server is being tremendously slown down if you have any signs using //1// or //2// or any of the next ones...");
			}
		}
		return power + ":" + s;
	}
	
	private String replaceQArgs(String s, List<String> qargs) {
		try {
			Pattern p = Pattern.compile("//[0-9][0-9]*//");
			Matcher m = p.matcher(s);
			while (m.find()) {
				try {
					s = s.replaceFirst(p.pattern(), qargs.get(Integer.parseInt(m.group().substring(2, m.group().length()-2))-1));
					m = p.matcher(s);
					m.reset();
				} catch(Exception ex) {
					ex.printStackTrace();
					System.out.println("Post this stacktrace either in a PM to Whitehooder or as a ticket to AMS.");
					s = s.replaceFirst(p.pattern(), "ERROR");
				}
			}
		} catch(Exception ex) {}
		return s;
	}

	public boolean isPowered(String s) {
		boolean power = false;
		if (s.matches("^true:.*")) {
			power = true;
		}
		return power;
	}
	
	public String removePowered(String s) {
		return s.replaceFirst("^true:", "").replaceFirst("^false:", "");
	}
	
	public String SignTypetoString(SignType type) {
		switch(type) {
		case PLAYER:
			return "player";
		case REGION:
			return "region";
		case SERVER:
			return "server";
		case WORLD:
			return "world";
		default:
			return "server";
		}
	}
	
	public boolean isStoredSign(Block b) {
		if (b.getType() == Material.SIGN_POST || b.getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) b.getState();
			if (getMetadata(sign) != null) {
				return true;
			}
		}
		return false;
	}

	public boolean getAnswer(String question) {
		boolean answer = true;
		question = question.trim();
		for (String s : question.split("&")) {
			if (s.contains("<=")) {
				int first = Integer.parseInt(s.split("<=")[0]);
				int second = Integer.parseInt(s.split("<=")[1]);
				if (first <= second) {
					answer = true;
				} else {
					answer = false;
				}
			} else if (s.contains(">=")) {
				int first = Integer.parseInt(s.split(">=")[0]);
				int second = Integer.parseInt(s.split(">=")[1]);
				if (first >= second) {
					answer = true;
				} else {
					answer = false;
				}
			} else if (s.contains("<")) {
				int first = Integer.parseInt(s.split("<")[0]);
				int second = Integer.parseInt(s.split("<")[1]);
				if (first < second) {
					answer = true;
				} else {
					answer = false;
				}
			} else if (s.contains(">")) {
				int first = Integer.parseInt(s.split(">")[0]);
				int second = Integer.parseInt(s.split(">")[1]);
				if (first > second) {
					answer = true;
				} else {
					answer = false;
				}
			} else if (s.contains("=")) {
				int first = Integer.parseInt(s.split("=")[0]);
				int second = Integer.parseInt(s.split("=")[1]);
				if (first == second) {
					answer = true;
				} else {
					answer = false;
				}
			} else if (s.contains("/")) {
				int first = Integer.parseInt(s.split("/")[0]);
				int second = Integer.parseInt(s.split("/")[1]);
				if (first%second==0) {
					answer = true;
				} else {
					answer = false;
				}
			}
		}
		return answer;
	}

	@SuppressWarnings("deprecation")
	public void setPower(Sign sign, boolean poweron) {
		List<String> signs = plugin.poweredsigns.get(sign.getWorld());
		String coords = sign.getBlock().getX() + "," + sign.getBlock().getY() + "," + sign.getBlock().getZ();
		byte power;
		if (poweron) {
			power = 0xF;
			if (!signs.contains(coords)) {
				signs.add(coords);
			}
		} else {
			if (signs != null) {
				signs.remove(coords);
			}
			power = 0x0;
		}
		plugin.poweredsigns.put(sign.getWorld(), signs);
		Block b = sign.getBlock();
		if (supportedmats.contains(b.getRelative(BlockFace.UP).getType())) {
			b.getRelative(BlockFace.UP).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.UP).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		} else if (supportedmats.contains(b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType())) {
			b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		} else if (supportedmats.contains(b.getRelative(BlockFace.DOWN).getType())) {
			b.getRelative(BlockFace.DOWN).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.DOWN).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		} else if (supportedmats.contains(b.getRelative(BlockFace.NORTH).getType())) {
			b.getRelative(BlockFace.NORTH).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.NORTH).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		} else if (supportedmats.contains(b.getRelative(BlockFace.SOUTH).getType())) {
			b.getRelative(BlockFace.SOUTH).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.SOUTH).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		} else if (supportedmats.contains(b.getRelative(BlockFace.EAST).getType())) {
			b.getRelative(BlockFace.EAST).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.EAST).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		} else if (supportedmats.contains(b.getRelative(BlockFace.WEST).getType())) {
			b.getRelative(BlockFace.WEST).setData(power);
			if (poweron) {
				b.getRelative(BlockFace.WEST).setMetadata("AMSRedStone", new FixedMetadataValue(plugin, 0));
			}
		}
	}
	
    public boolean insideBox(Player p, Location xyz1, Location xyz2){       
        int pX = p.getLocation().getBlockX();
        int pY = p.getLocation().getBlockY();
        int pZ = p.getLocation().getBlockZ();
        if (!p.getWorld().equals(xyz1.getWorld()) || !p.getWorld().equals(xyz2.getWorld())) {
        	return false;
        }
        int sX1 = xyz1.getBlockX();
        int sY1 = xyz1.getBlockY();
        int sZ1 = xyz1.getBlockZ();
       
        int sX2 = xyz2.getBlockX();
        int sY2 = xyz2.getBlockY();
        int sZ2 = xyz2.getBlockZ();
       
        int lowX = 0, highX = 0, lowY = 0, highY = 0, lowZ = 0, highZ = 0;
       
        if (sX1 < sX2){
            lowX = sX1;
            highX = sX2;
        } else if(sX1 > sX2) {
            lowX = sX2;
            highX = sX1;
        }
       
        if (sY1 < sY2){
            lowY = sY1;
            highY = sY2;
        } else if(sY1 > sY2) {
            lowY = sY2;
            highY = sY1;
        }
       
        if (sZ1 < sZ2){
            lowZ = sZ1;
            highZ = sZ2;
        } else if(sZ1 > sZ2) {
            lowZ = sZ2;
            highZ = sZ1;
        }
        
        if (pX >= lowX && pY >= lowY && pZ >= lowZ && pX < highX+1 && pY < highY+1 && pZ < highZ+1) {
			return true;
		} else {
			return false;
		}
    }
}
