package tk.lindegaard.InfoSigns;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class AMSCommands implements CommandExecutor {
	
	private InfoSigns plugin;
	
	public HashMap<Player, List<String>> signsnotyetselected = new HashMap<Player, List<String>>();
	
	public AMSCommands(InfoSigns plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = null;
		if (sender instanceof Player) {
			p = (Player) sender;
		}
		
		 if (cmd.getName().equalsIgnoreCase("amsverify")) {
			if (sender.hasPermission("ams.verify")) {
				boolean fix;
				if (args.length >= 1) {
					if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("rem") || args[0].equalsIgnoreCase("del")) {
						fix = false;
					} else if (args[0].equalsIgnoreCase("fix")) {
						fix = true;
					} else {
						sender.sendMessage(ChatColor.RED + cmd.getUsage());
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED + cmd.getUsage());
					return true;
				}
				int wrongnodes = 0;
				for (String type : plugin.signs.getKeys(false)) {
					for (String world : plugin.signs.getConfigurationSection(type).getKeys(false)) {
						World tempw = null;
						for (World w : plugin.getServer().getWorlds()) {
							if (w.getName().equalsIgnoreCase(world)) {
								tempw = w;
								break;
							}
						}
						if (tempw == null) {
							plugin.signs.set(type+"."+world, null);
							wrongnodes++;
							continue;
						}
						for (String coords : plugin.signs.getConfigurationSection(type+"."+world).getKeys(false)) {
							int x = Integer.parseInt(coords.split(",")[0]);
							int y = Integer.parseInt(coords.split(",")[1]);
							int z = Integer.parseInt(coords.split(",")[2]);
							if (!tempw.getBlockAt(x, y, z).getType().toString().equalsIgnoreCase("SIGN_POST") && !tempw.getBlockAt(x, y, z).getType().toString().equalsIgnoreCase("WALL_SIGN")) {
								sender.sendMessage(tempw.getBlockAt(x, y, z).getType().toString());
								if (fix) {
									plugin.getServer().getWorld(world).getBlockAt(x, y, z).setType(Material.SIGN_POST);
								} else {
									plugin.signs.set(type+"."+world+"."+coords, null);
								}
								wrongnodes++;
								continue;
							}
						}
					}
				}
				plugin.saveSignsConfig();
				if (fix) {
					plugin.sm.updateAll();
					sender.sendMessage("�aFixed " + wrongnodes + (wrongnodes == 1 ? " sign." : " signs."));
				} else {
					sender.sendMessage("�aRemoved " + wrongnodes + (wrongnodes == 1 ? " node." : " nodes."));
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + cmd.getPermissionMessage());
				return true;
			}
		} else if (cmd.getName().equalsIgnoreCase("amsupgradefromops")) {
			if (sender.hasPermission("ams.upgrade")) {
				if (plugin.config.contains("UPGRADINGFROMOPS")) {
					if (plugin.config.getBoolean("UPGRADINGFROMOPS")) {
					    File opssigns = new File(plugin.getDataFolder().getParentFile().getPath()+File.separator+"Online Player Signs"+File.separator+"signs.yml");
						if (opssigns.exists()) {
							plugin.signs = YamlConfiguration.loadConfiguration(opssigns);
						} else {
							sender.sendMessage(ChatColor.RED + "Couldn't find any signs.yml file in the OPS directory.");
							return true;
						}
						HashMap<String, HashMap<String, HashMap<String, List<String>>>> newvalues = new HashMap<String, HashMap<String,HashMap<String,List<String>>>>();
						newvalues.put("player", new HashMap<String, HashMap<String,List<String>>>());
						newvalues.put("world", new HashMap<String, HashMap<String,List<String>>>());
						for (String key : plugin.signs.getKeys(false)) {
							for (String values : plugin.signs.getStringList(key)) {
								String world = key;
								String monitoring = "";
								String coords = "";
								String[] array = values.split(",");
								coords = array[0] + "," + array[1] + "," + array[2];
								monitoring = array[3];
								if (monitoring.startsWith("PlayerOnlineCheck:")) {
									if (!newvalues.get("player").containsKey(world)) {
										HashMap<String, HashMap<String, List<String>>> temphash = newvalues.get("player");
										temphash.put(world, new HashMap<String, List<String>>());
										newvalues.put("player", temphash);
									}
									HashMap<String, List<String>> temphash = newvalues.get("player").get(world);
									List<String> templist = new ArrayList<String>();
									templist.add("false");
									templist.add("player");
									templist.add(monitoring.replaceFirst("^PlayerOnlineCheck:", ""));
									temphash.put(coords, templist);
									HashMap<String, HashMap<String, List<String>>> lasthash = newvalues.get("player");
									lasthash.put(world, temphash);
									newvalues.put("player", lasthash);
								} else {
									if (!newvalues.get("world").containsKey(world)) {
										HashMap<String, HashMap<String, List<String>>> temphash = newvalues.get("world");
										temphash.put(world, new HashMap<String, List<String>>());
										newvalues.put("world", temphash);
									}
									HashMap<String, List<String>> temphash = newvalues.get("world").get(world);
									List<String> templist = new ArrayList<String>();
									templist.add("false");
									templist.add("world");
									templist.add(monitoring);
									temphash.put(coords, templist);
									HashMap<String, HashMap<String, List<String>>> lasthash = newvalues.get("world");
									lasthash.put(world, temphash);
									newvalues.put("world", lasthash);
								}
							}
						}
						for (String key : plugin.signs.getKeys(false)) {
							plugin.signs.set(key, null);
						}
						plugin.signs.createSection("server");
						plugin.signs.createSection("world");
						plugin.signs.createSection("player");
						plugin.signs.createSection("region");
						for (String type : newvalues.keySet()) {
							for (String world : newvalues.get(type).keySet()) {
								plugin.signs.createSection(type+"."+world);
								for (String coords : newvalues.get(type).get(world).keySet()) {
									plugin.signs.set(type+"."+world+"."+coords, newvalues.get(type).get(world).get(coords));
								}
							}
						}
						plugin.saveSignsConfig();
						sender.sendMessage(ChatColor.GREEN + "Done!");
						plugin.config.set("UPGRADINGFROMOPS", null);
						plugin.saveConfig();
					} else {
						sender.sendMessage(ChatColor.RED + "If you really want this add the node 'UPGRADINGFROMOPS: true' to your config.yml.");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "If you really want this add the node 'UPGRADINGFROMOPS: true' to your config.yml.");
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + cmd.getPermissionMessage());
				return true;
			}
//		} else if (cmd.getName().equalsIgnoreCase("amscreate")) {
//			if (p == null) {
//				sender.sendMessage("Only players can use this command.");
//				return true;
//			}
//			List<String> infolist = new ArrayList<String>();
//			if (args.length > 0) {
//				if (args[0].replaceAll("+", "").equalsIgnoreCase("ams") || args[0].replaceAll("+", "").equalsIgnoreCase("amsw") || args[0].replaceAll("+", "").equalsIgnoreCase("amsp") || args[0].replaceAll("+", "").equalsIgnoreCase("amsr") || args[0].replaceAll("+", "").equalsIgnoreCase("amswarp")) {
//					infolist.add(args[0].replaceAll("+", ""));
//				} else {
//					sender.sendMessage("The type \"" + args[0].replaceAll("+", "") + "\" does not exist.");
//					return true;
//				}
//			}
//			
//			if (!sender.hasPermission("ams.create."+(infolist.get(0).equalsIgnoreCase("ams")?"server":(infolist.get(0).equalsIgnoreCase("amsw")?"world":(infolist.get(0).equalsIgnoreCase("amsp")?"player":(infolist.get(0).equalsIgnoreCase("amsr")?"region":"warp")))))) {
//				sender.sendMessage(ChatColor.RED + "You do not have permission.");
//				return true;
//			}
//			
//			if (args.length < 2) {
//				if (infolist.get(0).equalsIgnoreCase("ams")) {
//					sender.sendMessage("Syntax error! /amscreate ams[+] <format> [ex. p>=2]");
//				} else if (infolist.get(0).equalsIgnoreCase("amsw")) {
//					sender.sendMessage("Syntax error! /amscreate amsw[+] <format> [worldname] [ex. p>=2]");
//				} else if (infolist.get(0).equalsIgnoreCase("amsr")) {
//					sender.sendMessage("Syntax error! /amscreate amsr[+] <format> [worldguard-region/x1,y1,z1,x2,y2,z2] [ex. p>=2]");
//				} else if (infolist.get(0).equalsIgnoreCase("amsp")) {
//					sender.sendMessage("Syntax error! /amscreate amsp[+] <format> [playername (supports color)]");
//				} else if (infolist.get(0).equalsIgnoreCase("amswarp")) {
//					sender.sendMessage("Syntax error! /amscreate ams[+] <format> [] [ex. p>=2]");
//				}
//				return true;
//			}
//			
//			
//				if (args[0].replaceFirst("\\+", "").equalsIgnoreCase("[ams]")) {
//					if (sender.hasPermission("ams.create.server")) {
//						if (!args[1].isEmpty()) {
//							World world = e.getBlock().getWorld();
//							String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
//							String type = args[1].toLowerCase();
//							List<String> infolist = new ArrayList<String>();
//							infolist.add(String.valueOf(args[0].equalsIgnoreCase("[ams+]")));
//							infolist.add(type);
//							if (infolist.get(1).isEmpty()) {
//								sender.sendMessage("�cPlease specify a format on the 2nd line.");
//								return;
//							}
//							if (!args[2].isEmpty() && Boolean.getBoolean(infolist.get(0))) {
//								sender.sendMessage("�cPut the statement on the last line. Same goes for every sign.");
//								return;
//							}
//							String lastlineusage = "�cOn the last line you can only use '<', '>', '<=', '>=', '=', '/' or '&'. On each side of this you will place the two numbers to compare between. Use a lowercase 'p' to specify the varying amount of players within the region and any number or 'mp', for maxplayers on the server, on the other side. You can also do this by commands, which opens up for new features like the & operator, which lets you check if multiple things are true.";
//							if (args[3] != "" && args[0].equalsIgnoreCase("[ams+]")) {
//								for (String l : args[3].split("&")) {
//									if (l.contains("<=") || l.contains("=<")) {
//										if (l.split("<=").length == 2) {
//											String la[] = l.split("<=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp") && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp")  || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("<=")[0].trim() + "<=" + l.split("<=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												return;
//											}
//										} else if (l.split("=<").length == 2) {
//											String la[] = l.split("=<");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=<")[0].trim() + "<=" + l.split("=<")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains(">=") || l.contains("=>")) {
//										if (l.split(">=").length == 2) {
//											String la[] = l.split(">=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split(">=")[0].trim() + ">=" + l.split(">=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("=>").length == 2) {
//											String la[] = l.split("=>");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=>")[0].trim() + ">=" + l.split("=>")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("<")) {
//										if (l.split("<").length == 2) {
//											String la[] = l.split("<");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("<")[0].trim() + "<" + l.split("<")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains(">")) {
//										if (l.split(">").length == 2) {
//											String la[] = l.split(">");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split(">")[0].trim() + ">" + l.split(">")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("=") || l.contains("==")) {
//										if (l.split("=").length == 2) {
//											String la[] = l.split("=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=")[0].trim() + "=" + l.split("=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("==").length == 2) {
//											String la[] = l.split("==");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("==")[0].trim() + "=" + l.split("==")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("/") || l.contains("//")) {
//										if (l.split("/").length == 2) {
//											String la[] = l.split("/");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("/")[0].trim() + "/" + l.split("/")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("//").length == 2) {
//											String la[] = l.split("//");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("//")[0].trim() + "/" + l.split("//")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									}
//								}
//							}
//							plugin.signs.set("server."+world.getName()+"."+coords, infolist);
//							sender.sendMessage(ChatColor.GREEN + "A server sign has been created. Using the format: " + type);
//							plugin.saveSignsConfig();
//							plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.SERVER);
//						} else {
//							sender.sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
//						}
//					} else {
//						sender.sendMessage("You don't have permission to create a world specific sign.");
//						
//					}
//				} else if (args[0].replaceFirst("\\+", "").equalsIgnoreCase("[amsw]")) {
//					if (sender.hasPermission("ams.create.world")) {
//						if (!args[1].isEmpty()) {
//							World monitorworld = null;
//							World world = e.getBlock().getWorld();
//							boolean addworldstring = false;
//							String worldstring = args[2];
//							if (!worldstring.isEmpty()) {
//								for (World w : plugin.getServer().getWorlds()) {
//									plugin.print(w.getName() + " ? " + worldstring.replaceAll("&[0-9a-fk-or]", ""));
//									if (w.getName().equalsIgnoreCase(worldstring.replaceAll("&[0-9a-fk-or]", ""))) {
//										monitorworld = w;
//										addworldstring = true;
//										break;
//									}
//								}
//								if (!addworldstring) {
//									sender.sendMessage("�aThe world specified does not exist. Using current world instead.");
//									monitorworld = e.getBlock().getWorld();
//								}
//							} else {
//								monitorworld = e.getBlock().getWorld();
//								worldstring = monitorworld.getName();
//							}
//							String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
//							String type = args[1].toLowerCase();
//							List<String> infolist = new ArrayList<String>();
//							infolist.add(String.valueOf(args[0].equalsIgnoreCase("[amsw+]")));
//							infolist.add(type);
//							if (addworldstring) {
//								infolist.add(worldstring);
//							} else {
//								infolist.add(monitorworld.getName());
//							}						
//							if (infolist.get(1).isEmpty()) {
//								sender.sendMessage("�cPlease specify a format on the 2nd line.");
//								return;
//							}
//							String lastlineusage = "�cOn the last line you can only use '<', '>', '<=', '>=', '=', '/' or '&'. On each side of this you will place the two numbers to compare between. Use a lowercase 'p' to specify the varying amount of players within the region and any number or 'mp', for maxplayers on the server, on the other side. You can also do this by commands, which opens up for new features like the & operator, which lets you check if multiple things are true.";
//							if (args[3] != "" && args[0].equalsIgnoreCase("[amsr+]")) {
//								for (String l : args[3].split("&")) {
//									if (l.contains("<=") || l.contains("=<")) {
//										if (l.split("<=").length == 2) {
//											String la[] = l.split("<=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("<=")[0].trim() + "<=" + l.split("<=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("=<").length == 2) {
//											String la[] = l.split("=<");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=<")[0].trim() + "<=" + l.split("=<")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains(">=") || l.contains("=>")) {
//										if (l.split(">=").length == 2) {
//											String la[] = l.split(">=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split(">=")[0].trim() + ">=" + l.split(">=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("=>").length == 2) {
//											String la[] = l.split("=>");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=>")[0].trim() + ">=" + l.split("=>")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("<")) {
//										if (l.split("<").length == 2) {
//											String la[] = l.split("<");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("<")[0].trim() + "<" + l.split("<")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains(">")) {
//										if (l.split(">").length == 2) {
//											String la[] = l.split(">");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split(">")[0].trim() + ">" + l.split(">")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("=") || l.contains("==")) {
//										if (l.split("=").length == 2) {
//											String la[] = l.split("=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=")[0].trim() + "=" + l.split("=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("==").length == 2) {
//											String la[] = l.split("==");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("==")[0].trim() + "=" + l.split("==")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("/") || l.contains("//")) {
//										if (l.split("/").length == 2) {
//											String la[] = l.split("/");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("/")[0].trim() + "/" + l.split("/")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("//").length == 2) {
//											String la[] = l.split("//");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("//")[0].trim() + "/" + l.split("//")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									}
//								}
//							}
//							plugin.signs.set("world."+world.getName()+"."+coords, infolist);
//							sender.sendMessage(ChatColor.GREEN + "A world sign has been created. Using the format: " + type);
//							plugin.saveSignsConfig();
//							plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.WORLD);
//						} else {
//							sender.sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
//						}
//					} else {
//						sender.sendMessage("You don't have permission to create a world specific sign.");
//						
//					}
//				} else if (args[0].replaceFirst("\\+", "").equalsIgnoreCase("[amsr]")) {
//					if (sender.hasPermission("ams.create.region")) {
//						if (!args[1].isEmpty()) {
//							World world = e.getBlock().getWorld();
//							String type = args[1].toLowerCase();
//							String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
//							String xy = "";
//							String monitorworld;
//							if (!args[2].isEmpty()) {
//								for (int i = 0; i < 6; i++) {
//									if (plugin.isInt(args[2].split(",")[i])) {
//										xy = xy + args[2].split(",")[i] + ",";
//									}
//								}
//								xy = xy.replaceAll("$,", "");
//								monitorworld = e.getBlock().getWorld().getName();
//								sender.sendMessage("Either specify coords like x1,y1,z1,x2,y2,z2 on the third line or leave it blank for worldedit selection.");
//								return;
//							}
//							// Worldedit here
//							if (plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
//								WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
//								Selection sel = worldEdit.getSelection(sender);
//								if (sel == null) {
//									sender.sendMessage("Please select a region using worldedit before you make the sign, or specify x1,y1,z1,x2,y2,z2 on line three.");
//									return;
//								} else if (!(sel instanceof CuboidSelection)) {
//									sender.sendMessage("The selection has to be a cuboid.");
//									return;
//								}
//								xy = sel.getMinimumPoint().getBlockX() + "," + sel.getMinimumPoint().getBlockY() + "," + sel.getMinimumPoint().getBlockZ() + "," + sel.getMaximumPoint().getBlockX() + "," + sel.getMaximumPoint().getBlockY() + "," + sel.getMaximumPoint().getBlockZ();
//								monitorworld = sel.getWorld().getName();
//							} else {
//								sender.sendMessage(ChatColor.RED + "WorldEdit is not enabled on the server.");
//								return;
//							}
//							
//							List<String> infolist = new ArrayList<String>();
//							infolist.add(String.valueOf(args[0].equalsIgnoreCase("[amsr+]")));
//							infolist.add(type);
//							infolist.add(monitorworld);
//							infolist.add(xy);						
//							String lastlineusage = "�cOn the last line you can only use '<', '>', '<=', '>=', '=', '/' or '&'. On each side of this you will place the two numbers to compare between. Use a lowercase 'p' to specify the varying amount of players within the region and any number or 'mp', for maxplayers on the server, on the other side. You can also do this by commands, which opens up for new features like the & operator, which lets you check if multiple things are true.";
//							if (args[3] != "" && args[0].equalsIgnoreCase("[amsr+]")) {
//								for (String l : args[3].split("&")) {
//									if (l.contains("<=") || l.contains("=<")) {
//										if (l.split("<=").length == 2) {
//											String la[] = l.split("<=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("<=")[0].trim() + "<=" + l.split("<=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("=<").length == 2) {
//											String la[] = l.split("=<");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=<")[0].trim() + "<=" + l.split("=<")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains(">=") || l.contains("=>")) {
//										if (l.split(">=").length == 2) {
//											String la[] = l.split(">=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split(">=")[0].trim() + ">=" + l.split(">=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("=>").length == 2) {
//											String la[] = l.split("=>");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=>")[0].trim() + ">=" + l.split("=>")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("<")) {
//										if (l.split("<").length == 2) {
//											String la[] = l.split("<");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("<")[0].trim() + "<" + l.split("<")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains(">")) {
//										if (l.split(">").length == 2) {
//											String la[] = l.split(">");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split(">")[0].trim() + ">" + l.split(">")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("=") || l.contains("==")) {
//										if (l.split("=").length == 2) {
//											String la[] = l.split("=");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("=")[0].trim() + "=" + l.split("=")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("==").length == 2) {
//											String la[] = l.split("==");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("==")[0].trim() + "=" + l.split("==")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									} else if (l.contains("/") || l.contains("//")) {
//										if (l.split("/").length == 2) {
//											String la[] = l.split("/");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("/")[0].trim() + "/" + l.split("/")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else if (l.split("//").length == 2) {
//											String la[] = l.split("//");
//											if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
//												infolist.add(l.split("//")[0].trim() + "/" + l.split("//")[1].trim());
//											} else {
//												sender.sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
//												
//												return;
//											}
//										} else {
//											sender.sendMessage(lastlineusage);
//										}
//									}
//								}
//							}
//							if (infolist.get(1).isEmpty()) {
//								sender.sendMessage("�cPlease specify a format on the 2nd line.");
//								
//								return;
//							}
//							plugin.signs.set("region."+world.getName()+"."+coords, infolist);
//							sender.sendMessage(ChatColor.GREEN + "A region sign has been created. Using the format: " + type);
//							plugin.saveSignsConfig();
//							plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.REGION);
//						} else {
//							sender.sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
//							
//						}
//					} else {
//						sender.sendMessage("You don't have permission to create a region specific sign.");
//						
//					}
//				} else if (args[0].replaceFirst("\\+", "").equalsIgnoreCase("[amsp]")) {
//					if (sender.hasPermission("ams.create.player")) {
//						if (!args[1].isEmpty()) {
//							World monitorworld = null;
//							World world = e.getBlock().getWorld();
//							String playerstring = args[2];
//							if (args[2].isEmpty()) {
//								sender.sendMessage("You have to provide a player name on the third line.");
//							}
//							if (!args[3].isEmpty()) {
//								for (World w : plugin.getServer().getWorlds()) {
//									if (w.getName().equalsIgnoreCase(args[3])) {
//										monitorworld = w;
//										break;
//									}
//								}
//							}
//							String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
//							String type = args[1].toLowerCase();
//							List<String> infolist = new ArrayList<String>();
//							infolist.add(String.valueOf(args[0].equalsIgnoreCase("[amsp+]")));
//							infolist.add(type);
//							infolist.add(playerstring);
//							if (monitorworld != null) {
//								infolist.add(monitorworld.getName());
//							}
//							if (infolist.get(1).isEmpty()) {
//								sender.sendMessage("�cPlease specify a format on the 2nd line.");
//								
//								return;
//							}
//							plugin.signs.set("player."+world.getName()+"."+coords, infolist);
//							sender.sendMessage(ChatColor.GREEN + "A player sign has been created. Using the format: " + type);
//							plugin.saveSignsConfig();
//							plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.PLAYER);
//						} else {
//							sender.sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
//							
//						}
//					} else {
//						sender.sendMessage("You don't have permission to create a player specific sign.");
//						
//					}
//				}
//			}
		}
		return false;
	}
}
