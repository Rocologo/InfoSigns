package tk.lindegaard.InfoSigns;

import java.io.File;

import org.bukkit.configuration.InvalidConfigurationException;

import tk.lindegaard.InfoSigns.util.AutoConfig;
import tk.lindegaard.InfoSigns.util.ConfigField;

public class Config extends AutoConfig
{
	public Config( File file )
	{
		super(file);
		
		setCategoryComment("mobs", "Here is where you set the base prize in $ for killing a mob of each type"); //$NON-NLS-1$ //$NON-NLS-2$
		setCategoryComment("boss", "Here is where you set the base prize in $ for killing the bosses"); //$NON-NLS-1$ //$NON-NLS-2$
		setCategoryComment("bonus", "These are bonus multipliers that can modify the base prize. \nREMEMBER: These are not in $ but they are a multiplier. Setting to 1 will disable them."); //$NON-NLS-1$ //$NON-NLS-2$
		setCategoryComment("penalty", "These are penalty multipliers that can modify the base prize. \nREMEMBER: These are not in $ but they are a multiplier. Setting to 1 will disable them."); //$NON-NLS-1$ //$NON-NLS-2$
		
		setCategoryComment("special", "Here is where you set the prize in $ for achieving a special kill. \nFor each achievment you can run a console command to give the player a reward. \nYou can you the following variables {player},{world}.\nAn example could be to give the player permission to fly \nfor 1 hour or use give command to the player items.\nYou can also specify the message send to the player.\nYou can run many console commands on each line, each command\nmust be separated by |"); //$NON-NLS-1$ //$NON-NLS-2$
		setCategoryComment("pvp", "Pvp configuration. Set pvp-allowed = true if you want give the players a reward when they kill eachother. \n You can alsp run a console command when this happens to give the player a reward or punish him. \nYou can you the following variables {player},{world}.\nAn example could be to give the player permission to fly \nfor 1 hour or use give command to the player items.\nYou can also specify the message send to the player.\nYou can run many console commands on each line, each command\nmust be separated by |"); //$NON-NLS-1$ //$NON-NLS-2$
		
	}
	
	@ConfigField(name="type", category="database", comment="Type of database to use. Valid values are: sqlite, mysql")
	public String databaseType = "sqlite"; //$NON-NLS-1$
	@ConfigField(name="username", category="database")
	public String databaseUsername = "user"; //$NON-NLS-1$
	@ConfigField(name="password", category="database")
	public String databasePassword = "password"; //$NON-NLS-1$
	
	@ConfigField(name="host", category="database")
	public String databaseHost = "localhost:3306"; //$NON-NLS-1$
	@ConfigField(name="database", category="database")
	public String databaseName = "infosigns"; //$NON-NLS-1$
	
	@ConfigField(name="save-period", category="general", comment="Time between saves in ticks (20 ticks~1 sec)")
	public int savePeriod = 1200;
	
	@ConfigField(name="language", category="general", comment="The language (file) to use. You can put the name of the language file as the language code (eg. en_US, de_DE, fr_FR, ect.) or you can specify the name of a custom file without the .lang\nPlease check the lang/ folder for a list of all available translations.")
	public String language = "en_US"; //$NON-NLS-1$

	@ConfigField(name="update-check", category="general", comment="Check if there is a new version of the plugin available.")
	public boolean updateCheck = true;

	@ConfigField(name="debug", category="general", comment="Debug information is written in console output.")
	public boolean debug = false;
	
	@Override
	protected void onPostLoad() throws InvalidConfigurationException
	{
		Messages.setLanguage(language);
	}
}
