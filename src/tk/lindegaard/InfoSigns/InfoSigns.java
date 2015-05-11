package tk.lindegaard.InfoSigns;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import tk.lindegaard.InfoSigns.events.AMSOtherEvents;
import tk.lindegaard.InfoSigns.events.AMSSignEvents;
import tk.lindegaard.InfoSigns.storage.DataStore;
import tk.lindegaard.InfoSigns.storage.DataStoreException;
import tk.lindegaard.InfoSigns.storage.DataStoreManager;
import tk.lindegaard.InfoSigns.storage.MySQLDataStore;
import tk.lindegaard.InfoSigns.storage.SQLiteDataStore;

public class InfoSigns extends JavaPlugin {

	// Constants
	public final static String pluginName = "InfoSigns";
	public final static String tablePrefix = "tp_";

	// new
	public static InfoSigns instance;
	private Config mConfig;
	private Economy mEconomy;

	private DataStore mStore;
	private DataStoreManager mStoreManager;

	// old
	public AMSCommands commands;
	public AMSSignEvents signevents;
	public AMSOtherEvents otherevents;
	public SignMethods sm;
	public HashMap<String, List<String>> formats = new HashMap<String, List<String>>();

	private File signsFile = null;
	public FileConfiguration config = null;
	public FileConfiguration signs = null;

	public HashMap<World, List<String>> poweredsigns = new HashMap<World, List<String>>();

	@Override
	public void onEnable() {

		// New
		instance = this;
		mConfig = new Config(new File(getDataFolder(), "config.yml"));

		if (mConfig.load())
			mConfig.save();
		else
			throw new RuntimeException(Messages.getString(pluginName
					+ ".config.fail")); //$NON-NLS-1$

		Messages.exportDefaultLanguages();

		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (economyProvider == null) {
			instance = null;
			getLogger().severe(Messages.getString(pluginName + ".hook.econ")); //$NON-NLS-1$
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		mEconomy = economyProvider.getProvider();

		if (mConfig.databaseType.equalsIgnoreCase("mysql")) //$NON-NLS-1$
			mStore = new MySQLDataStore();
		else
			mStore = new SQLiteDataStore();

		try {
			mStore.initialize();
		} catch (DataStoreException e) {
			e.printStackTrace();

			try {
				mStore.shutdown();
			} catch (DataStoreException e1) {
				e1.printStackTrace();
			}
			setEnabled(false);
			return;
		}

		// Register Commands

		// Register Events

		// old
		commands = new AMSCommands(this);
		signevents = new AMSSignEvents(this);
		otherevents = new AMSOtherEvents(this);
		sm = new SignMethods(this);

		getServer().getPluginManager().registerEvents(signevents, this);
		getServer().getPluginManager().registerEvents(otherevents, this);
		getCommand("amsverify").setExecutor(commands);
		getCommand("amsreload").setExecutor(commands);
		getCommand("amsupgradefromops").setExecutor(commands);
		if (getConfig().getKeys(false).contains("UPGRADINGFROMOPS")) {
			if (!getConfig().getBoolean("UPGRADINGFROMOPS")) {
				sm.updateAll();
			}
		} else {
			sm.updateAll();
		}
	}

	@Override
	public void onDisable() {
	}

	public static Economy getEconomy() {
		return instance.mEconomy;
	}

	public static Config config() {
		return instance.mConfig;
	}

	public static void debug(String text, Object... args) {
		if (instance.mConfig.debug)
			instance.getLogger().info(
					"[" + InfoSigns.pluginName + "][Debug] "
							+ String.format(text, args));
	}

	public boolean reloadSignsConfig() {
		if (signsFile == null) {
			signsFile = new File(getDataFolder(), "signs.yml");
		}
		signs = YamlConfiguration.loadConfiguration(signsFile);
		return true;

		/*
		 * // Look for defaults in the jar InputStream defConfigStream =
		 * this.getResource("signs.yml"); if (defConfigStream != null) {
		 * YamlConfiguration defConfig =
		 * YamlConfiguration.loadConfiguration(defConfigStream);
		 * signs.addDefaults(defConfig); }
		 */
	}

	public FileConfiguration getSignsConfig() {
		if (signs == null) {
			this.reloadSignsConfig();
		}
		return signs;
	}

	public void saveSignsConfig() {
		if (signs == null || signsFile == null) {
			return;
		}
		try {
			signs.save(signsFile);
		} catch (IOException ex) {
			this.getLogger().log(Level.SEVERE,
					"Could not save config to " + signsFile, ex);
		}
	}

	public void saveDefaultSignsConfig() {
		if (signsFile == null) {
			signsFile = new File(getDataFolder(), "signs.yml");
		}
		if (!signsFile.exists()) {
			saveResource("signs.yml", false);
		}
	}

	public boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public void print(String msg) {
		getServer().getConsoleSender().sendMessage(msg);
	}

	public void print(boolean msg) {
		getServer().getConsoleSender().sendMessage(Boolean.toString(msg));
	}

	public void print(int msg) {
		getServer().getConsoleSender().sendMessage(Integer.toString(msg));
	}

	public void print(Object msg) {
		getServer().getConsoleSender().sendMessage(msg.toString());
	}
}
