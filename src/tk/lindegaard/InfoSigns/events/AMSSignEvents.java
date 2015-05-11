package tk.lindegaard.InfoSigns.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import tk.lindegaard.InfoSigns.InfoSigns;
import tk.lindegaard.InfoSigns.SignType;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class AMSSignEvents implements Listener {
	
	private InfoSigns plugin;
	
	public AMSSignEvents(InfoSigns plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onSignChange(SignChangeEvent e) {
		if (e.getBlock().getType() == Material.SIGN_POST || e.getBlock().getType() == Material.WALL_SIGN) {
			if (e.getLine(0).replaceFirst("\\+", "").equalsIgnoreCase("[ams]")) {
				if (e.getPlayer().hasPermission("ams.create.server")) {
					if (!e.getLine(1).isEmpty()) {
						World world = e.getBlock().getWorld();
						String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
						String type = e.getLine(1).toLowerCase();
						List<String> infolist = new ArrayList<String>();
						infolist.add(String.valueOf(e.getLine(0).equalsIgnoreCase("[ams+]")));
						infolist.add(type);
						if (infolist.get(1).isEmpty()) {
							e.getPlayer().sendMessage("�cPlease specify a format on the 2nd line.");
							return;
						}
						if (!e.getLine(2).isEmpty() && Boolean.getBoolean(infolist.get(0))) {
							e.getPlayer().sendMessage("�cPut the statement on the last line. Same goes for every sign.");
							return;
						}
						String lastlineusage = "�cOn the last line you can only use '<', '>', '<=', '>=', '=', '/' or '&'. On each side of this you will place the two numbers to compare between. Use a lowercase 'p' to specify the varying amount of players within the region and any number or 'mp', for maxplayers on the server, on the other side. You can also do this by commands, which opens up for new features like the & operator, which lets you check if multiple things are true.";
						if (e.getLine(3) != "" && e.getLine(0).equalsIgnoreCase("[ams+]")) {
							for (String l : e.getLine(3).split("&")) {
								if (l.contains("<=") || l.contains("=<")) {
									if (l.split("<=").length == 2) {
										String la[] = l.split("<=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp") && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp")  || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("<=")[0].trim() + "<=" + l.split("<=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											return;
										}
									} else if (l.split("=<").length == 2) {
										String la[] = l.split("=<");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=<")[0].trim() + "<=" + l.split("=<")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains(">=") || l.contains("=>")) {
									if (l.split(">=").length == 2) {
										String la[] = l.split(">=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split(">=")[0].trim() + ">=" + l.split(">=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("=>").length == 2) {
										String la[] = l.split("=>");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=>")[0].trim() + ">=" + l.split("=>")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("<")) {
									if (l.split("<").length == 2) {
										String la[] = l.split("<");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("<")[0].trim() + "<" + l.split("<")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains(">")) {
									if (l.split(">").length == 2) {
										String la[] = l.split(">");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split(">")[0].trim() + ">" + l.split(">")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("=") || l.contains("==")) {
									if (l.split("=").length == 2) {
										String la[] = l.split("=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=")[0].trim() + "=" + l.split("=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("==").length == 2) {
										String la[] = l.split("==");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("==")[0].trim() + "=" + l.split("==")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("/") || l.contains("//")) {
									if (l.split("/").length == 2) {
										String la[] = l.split("/");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("/")[0].trim() + "/" + l.split("/")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("//").length == 2) {
										String la[] = l.split("//");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("//")[0].trim() + "/" + l.split("//")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								}
							}
						}
						plugin.signs.set("server."+world.getName()+"."+coords, infolist);
						e.getPlayer().sendMessage(ChatColor.GREEN + "A server sign has been created. Using the format: " + type);
						plugin.saveSignsConfig();
						plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.SERVER);
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
					}
				} else {
					e.getPlayer().sendMessage("You don't have permission to create a world specific sign.");
					
				}
			} else if (e.getLine(0).replaceFirst("\\+", "").equalsIgnoreCase("[amsw]")) {
				if (e.getPlayer().hasPermission("ams.create.world")) {
					if (!e.getLine(1).isEmpty()) {
						World monitorworld = null;
						World world = e.getBlock().getWorld();
						boolean addworldstring = false;
						String worldstring = e.getLine(2);
						if (!worldstring.isEmpty()) {
							for (World w : plugin.getServer().getWorlds()) {
								plugin.print(w.getName() + " ? " + worldstring.replaceAll("&[0-9a-fk-or]", ""));
								if (w.getName().equalsIgnoreCase(worldstring.replaceAll("&[0-9a-fk-or]", ""))) {
									monitorworld = w;
									addworldstring = true;
									break;
								}
							}
							if (!addworldstring) {
								e.getPlayer().sendMessage("�aThe world specified does not exist. Using current world instead.");
								monitorworld = e.getBlock().getWorld();
							}
						} else {
							monitorworld = e.getBlock().getWorld();
							worldstring = monitorworld.getName();
						}
						String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
						String type = e.getLine(1).toLowerCase();
						List<String> infolist = new ArrayList<String>();
						infolist.add(String.valueOf(e.getLine(0).equalsIgnoreCase("[amsw+]")));
						infolist.add(type);
						if (addworldstring) {
							infolist.add(worldstring);
						} else {
							infolist.add(monitorworld.getName());
						}						
						if (infolist.get(1).isEmpty()) {
							e.getPlayer().sendMessage("�cPlease specify a format on the 2nd line.");
							return;
						}
						String lastlineusage = "�cOn the last line you can only use '<', '>', '<=', '>=', '=', '/' or '&'. On each side of this you will place the two numbers to compare between. Use a lowercase 'p' to specify the varying amount of players within the region and any number or 'mp', for maxplayers on the server, on the other side. You can also do this by commands, which opens up for new features like the & operator, which lets you check if multiple things are true.";
						if (e.getLine(3) != "" && e.getLine(0).equalsIgnoreCase("[amsr+]")) {
							for (String l : e.getLine(3).split("&")) {
								if (l.contains("<=") || l.contains("=<")) {
									if (l.split("<=").length == 2) {
										String la[] = l.split("<=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("<=")[0].trim() + "<=" + l.split("<=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("=<").length == 2) {
										String la[] = l.split("=<");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=<")[0].trim() + "<=" + l.split("=<")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains(">=") || l.contains("=>")) {
									if (l.split(">=").length == 2) {
										String la[] = l.split(">=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split(">=")[0].trim() + ">=" + l.split(">=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("=>").length == 2) {
										String la[] = l.split("=>");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=>")[0].trim() + ">=" + l.split("=>")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("<")) {
									if (l.split("<").length == 2) {
										String la[] = l.split("<");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("<")[0].trim() + "<" + l.split("<")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains(">")) {
									if (l.split(">").length == 2) {
										String la[] = l.split(">");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split(">")[0].trim() + ">" + l.split(">")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("=") || l.contains("==")) {
									if (l.split("=").length == 2) {
										String la[] = l.split("=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=")[0].trim() + "=" + l.split("=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("==").length == 2) {
										String la[] = l.split("==");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("==")[0].trim() + "=" + l.split("==")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("/") || l.contains("//")) {
									if (l.split("/").length == 2) {
										String la[] = l.split("/");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("/")[0].trim() + "/" + l.split("/")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("//").length == 2) {
										String la[] = l.split("//");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("//")[0].trim() + "/" + l.split("//")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								}
							}
						}
						plugin.signs.set("world."+world.getName()+"."+coords, infolist);
						e.getPlayer().sendMessage(ChatColor.GREEN + "A world sign has been created. Using the format: " + type);
						plugin.saveSignsConfig();
						plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.WORLD);
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
					}
				} else {
					e.getPlayer().sendMessage("You don't have permission to create a world specific sign.");
					
				}
			} else if (e.getLine(0).replaceFirst("\\+", "").equalsIgnoreCase("[amsr]")) {
				if (e.getPlayer().hasPermission("ams.create.region")) {
					if (!e.getLine(1).isEmpty()) {
						World world = e.getBlock().getWorld();
						String type = e.getLine(1).toLowerCase();
						String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
						String xy = "";
						String monitorworld;
						if (!e.getLine(2).isEmpty()) {
							if (e.getLine(2).split(",").length == 6) {
								for (int i = 0; i < 6; i++) {
									if (plugin.isInt(e.getLine(2).split(",")[i])) {
										xy = xy + e.getLine(2).split(",")[i] + ",";
									}
								}
								xy = xy.replaceAll("$,", "");
								monitorworld = e.getBlock().getWorld().getName();
							} else {
								e.getPlayer().sendMessage("Either specify coords like x1,y1,z1,x2,y2,z2 on the third line or leave it blank for worldedit selection.");
								return;
							}
						} else 
						// Worldedit here
						if (plugin.getServer().getPluginManager().isPluginEnabled("WorldEdit")) {
							WorldEditPlugin worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
							Selection sel = worldEdit.getSelection(e.getPlayer());
							if (sel == null) {
								e.getPlayer().sendMessage("Please select a region using worldedit before you make the sign, or specify x1,y1,z1,x2,y2,z2 on line three.");
								return;
							} else if (!(sel instanceof CuboidSelection)) {
								e.getPlayer().sendMessage("The selection has to be a cuboid.");
								return;
							}
							xy = sel.getMinimumPoint().getBlockX() + "," + sel.getMinimumPoint().getBlockY() + "," + sel.getMinimumPoint().getBlockZ() + "," + sel.getMaximumPoint().getBlockX() + "," + sel.getMaximumPoint().getBlockY() + "," + sel.getMaximumPoint().getBlockZ();
							monitorworld = sel.getWorld().getName();
						} else {
							e.getPlayer().sendMessage(ChatColor.RED + "WorldEdit is not enabled on the server.");
							return;
						}
						
						List<String> infolist = new ArrayList<String>();
						infolist.add(String.valueOf(e.getLine(0).equalsIgnoreCase("[amsr+]")));
						infolist.add(type);
						infolist.add(monitorworld);
						infolist.add(xy);						
						String lastlineusage = "�cOn the last line you can only use '<', '>', '<=', '>=', '=', '/' or '&'. On each side of this you will place the two numbers to compare between. Use a lowercase 'p' to specify the varying amount of players within the region and any number or 'mp', for maxplayers on the server, on the other side. You can also do this by commands, which opens up for new features like the & operator, which lets you check if multiple things are true.";
						if (e.getLine(3) != "" && e.getLine(0).equalsIgnoreCase("[amsr+]")) {
							for (String l : e.getLine(3).split("&")) {
								if (l.contains("<=") || l.contains("=<")) {
									if (l.split("<=").length == 2) {
										String la[] = l.split("<=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("<=")[0].trim() + "<=" + l.split("<=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("=<").length == 2) {
										String la[] = l.split("=<");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=<")[0].trim() + "<=" + l.split("=<")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains(">=") || l.contains("=>")) {
									if (l.split(">=").length == 2) {
										String la[] = l.split(">=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split(">=")[0].trim() + ">=" + l.split(">=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("=>").length == 2) {
										String la[] = l.split("=>");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=>")[0].trim() + ">=" + l.split("=>")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("<")) {
									if (l.split("<").length == 2) {
										String la[] = l.split("<");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("<")[0].trim() + "<" + l.split("<")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains(">")) {
									if (l.split(">").length == 2) {
										String la[] = l.split(">");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split(">")[0].trim() + ">" + l.split(">")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("=") || l.contains("==")) {
									if (l.split("=").length == 2) {
										String la[] = l.split("=");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("=")[0].trim() + "=" + l.split("=")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("==").length == 2) {
										String la[] = l.split("==");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("==")[0].trim() + "=" + l.split("==")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								} else if (l.contains("/") || l.contains("//")) {
									if (l.split("/").length == 2) {
										String la[] = l.split("/");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("/")[0].trim() + "/" + l.split("/")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											
											return;
										}
									} else if (l.split("//").length == 2) {
										String la[] = l.split("//");
										if (plugin.isInt(la[0].trim()) || la[0].trim().equalsIgnoreCase("p") || la[0].trim().equalsIgnoreCase("mp")  && plugin.isInt(la[1].trim()) || la[1].trim().equalsIgnoreCase("p") || la[1].trim().equalsIgnoreCase("mp") ) {
											infolist.add(l.split("//")[0].trim() + "/" + l.split("//")[1].trim());
										} else {
											e.getPlayer().sendMessage("�cThe sides of the statement has to be either a variable or an integer.");
											return;
										}
									} else {
										e.getPlayer().sendMessage(lastlineusage);
									}
								}
							}
						}
						if (infolist.get(1).isEmpty()) {
							e.getPlayer().sendMessage("�cPlease specify a format on the 2nd line.");
							
							return;
						}
						plugin.signs.set("region."+world.getName()+"."+coords, infolist);
						e.getPlayer().sendMessage(ChatColor.GREEN + "A region sign has been created. Using the format: " + type);
						plugin.saveSignsConfig();
						plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.REGION);
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
						
					}
				} else {
					e.getPlayer().sendMessage("You don't have permission to create a region specific sign.");
					
				}
			} else if (e.getLine(0).replaceFirst("\\+", "").equalsIgnoreCase("[amsp]")) {
				if (e.getPlayer().hasPermission("ams.create.player")) {
					if (!e.getLine(1).isEmpty()) {
						World monitorworld = null;
						World world = e.getBlock().getWorld();
						String playerstring = e.getLine(2);
						if (e.getLine(2).isEmpty()) {
							e.getPlayer().sendMessage("You have to provide a player name on the third line.");
						}
						if (!e.getLine(3).isEmpty()) {
							for (World w : plugin.getServer().getWorlds()) {
								if (w.getName().equalsIgnoreCase(e.getLine(3))) {
									monitorworld = w;
									break;
								}
							}
						}
						String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
						String type = e.getLine(1).toLowerCase();
						List<String> infolist = new ArrayList<String>();
						infolist.add(String.valueOf(e.getLine(0).equalsIgnoreCase("[amsp+]")));
						infolist.add(type);
						infolist.add(playerstring);
						if (monitorworld != null) {
							infolist.add(monitorworld.getName());
						}
						if (infolist.get(1).isEmpty()) {
							e.getPlayer().sendMessage("�cPlease specify a format on the 2nd line.");
							
							return;
						}
						plugin.signs.set("player."+world.getName()+"."+coords, infolist);
						e.getPlayer().sendMessage(ChatColor.GREEN + "A player sign has been created. Using the format: " + type);
						plugin.saveSignsConfig();
						plugin.sm.update(world, e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), SignType.PLAYER);
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "You have to specify a type on line 2.");
						
					}
				} else {
					e.getPlayer().sendMessage("You don't have permission to create a player specific sign.");
					
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onSignBreak(BlockBreakEvent e) {
		if (e.getBlock().getType() == Material.SIGN_POST || e.getBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) e.getBlock().getState();
			if (plugin.sm.isStoredSign(sign.getBlock())) {
				switch(plugin.sm.getMetadata(sign)) {
				case PLAYER:
					if (!e.getPlayer().hasPermission("ams.break.player")) {
						e.getPlayer().sendMessage("�cYou are not allowed to break this sign.");
						return;
					}
					break;
				case REGION:
					if (!e.getPlayer().hasPermission("ams.break.region")) {
						e.getPlayer().sendMessage("�cYou are not allowed to break this sign.");
						return;
					}
					break;
				case SERVER:
					if (!e.getPlayer().hasPermission("ams.break.server")) {
						e.getPlayer().sendMessage("�cYou are not allowed to break this sign.");
						return;
					}
					break;
				case WORLD:
					if (!e.getPlayer().hasPermission("ams.break.world")) {
						e.getPlayer().sendMessage("�cYou are not allowed to break this sign.");
						return;
					}
					break;
				}
				String type = plugin.sm.getMetaString(sign);
				String coords = e.getBlock().getX() + "," + e.getBlock().getY() + "," + e.getBlock().getZ();
				plugin.signs.set(type+"."+e.getBlock().getWorld().getName()+"."+coords, null);
				plugin.saveSignsConfig();
				plugin.sm.removeMetadata(sign);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onSignChangedByEntity(EntityChangeBlockEvent e) {
		if (plugin.sm.isStoredSign(e.getBlock())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onSignExplodedByEntity(EntityExplodeEvent e) {
		for (int c = 0; c<e.blockList().size(); c++) {
			if (plugin.sm.isStoredSign(e.blockList().get(c))) {
				e.blockList().remove(e.blockList().get(c));
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onSignPhysics(BlockPhysicsEvent e) {
		if (plugin.sm.isStoredSign(e.getBlock())) {
			e.setCancelled(true);
		}
	}
}