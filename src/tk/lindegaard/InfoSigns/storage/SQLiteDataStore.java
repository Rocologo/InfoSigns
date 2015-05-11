package tk.lindegaard.InfoSigns.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import tk.lindegaard.InfoSigns.InfoSigns;

public class SQLiteDataStore extends DatabaseDataStore {
	@Override
	protected Connection setupConnection() throws SQLException,
			DataStoreException {
		try {
			Class.forName("org.sqlite.JDBC"); //$NON-NLS-1$
			return DriverManager
					.getConnection("jdbc:sqlite:" + InfoSigns.instance.getDataFolder().getPath() + "/" + InfoSigns.config().databaseName + ".db"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (ClassNotFoundException e) {
			throw new DataStoreException("SQLite not present on the classpath"); //$NON-NLS-1$
		}
	}

	@Override
	protected void setupTables(Connection connection) throws SQLException {
		Statement create = connection.createStatement();

		// Create new empty tables if they do not exist
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Players (UUID TEXT PRIMARY KEY, NAME TEXT, PLAYER_ID INTEGER NOT NULL)"); //$NON-NLS-1$

		// Setup Database triggers
		setupTrigger(connection);

		// performTableMigrate(connection);

		create.close();
		connection.commit();

	}

	private void setupTrigger(Connection connection) throws SQLException {

		Statement create = connection.createStatement();

		create.executeUpdate("create trigger if not exists mh_DailyInsert after insert on mh_Daily begin insert or ignore into mh_Weekly(ID, PLAYER_ID) values(strftime(\"%Y%W\",\"now\"), NEW.PLAYER_ID); insert or ignore into mh_Monthly(ID, PLAYER_ID) values(strftime(\"%Y%m\",\"now\"), NEW.PLAYER_ID); insert or ignore into mh_Yearly(ID, PLAYER_ID) values(strftime(\"%Y\",\"now\"), NEW.PLAYER_ID); insert or ignore into mh_AllTime(PLAYER_ID) values(NEW.PLAYER_ID); end"); //$NON-NLS-1$

		// Create the cascade update trigger. It will allow us to only modify
		// the Daily table, and the rest will happen automatically

		create.close();

		connection.commit();
	}

	@Override
	protected void setupStatements(Connection connection) throws SQLException {
		mAddPlayerStatement = connection
				.prepareStatement("INSERT OR IGNORE INTO mh_Players VALUES(?, ?, (SELECT IFNULL(MAX(PLAYER_ID),0)+1 FROM mh_Players));"); //$NON-NLS-1$
		mGetPlayerStatement[0] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID=?;"); //$NON-NLS-1$
		mGetPlayerStatement[1] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID IN (?,?);"); //$NON-NLS-1$
		mGetPlayerStatement[2] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID IN (?,?,?,?,?);"); //$NON-NLS-1$
		mGetPlayerStatement[3] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID IN (?,?,?,?,?,?,?,?,?,?);"); //$NON-NLS-1$

		mRecordAchievementStatement = connection
				.prepareStatement("INSERT OR REPLACE INTO mh_Achievements VALUES(?,?,?,?);"); //$NON-NLS-1$

		mAddPlayerStatsStatement = connection
				.prepareStatement("INSERT OR IGNORE INTO mh_Daily(ID, PLAYER_ID) VALUES(strftime(\"%Y%j\",\"now\"),?);"); //$NON-NLS-1$

		mLoadAchievementsStatement = connection
				.prepareStatement("SELECT ACHIEVEMENT, DATE, PROGRESS FROM mh_Achievements WHERE PLAYER_ID = ?;"); //$NON-NLS-1$

		mGetPlayerUUID = connection
				.prepareStatement("SELECT UUID FROM mh_Players WHERE NAME LIKE ?"); //$NON-NLS-1$
		mUpdatePlayerName = connection
				.prepareStatement("UPDATE mh_Players SET NAME=? WHERE UUID=?"); //$NON-NLS-1$
	}

	@Override
	public void savePlayer(Set<PlayerStore> stats) throws DataStoreException {
		try {
			InfoSigns.debug("Saving to Database.", "");
			Statement statement = mConnection.createStatement();

			HashSet<OfflinePlayer> names = new HashSet<OfflinePlayer>();
			for (PlayerStore stat : stats)
				names.add(stat.player);
			Map<UUID, Integer> ids = getPlayerIds(names);

			// Make sure the stats are available for each player
			mAddPlayerStatsStatement.clearBatch();
			for (OfflinePlayer player : names) {
				mAddPlayerStatsStatement.setInt(1,
						ids.get(player.getUniqueId()));
				mAddPlayerStatsStatement.addBatch();
			}
			mAddPlayerStatsStatement.executeBatch();

			// Now add each of the stats
			for (PlayerStore stat : stats)
				statement
						.addBatch(String
								.format("UPDATE mh_Daily SET %1$s = %1$s + %3$d WHERE ID = strftime(\"%%Y%%j\",\"now\") AND PLAYER_ID = %2$d;",
										"", ids.get(stat.player.getUniqueId()),
										stat.amount));
			statement.executeBatch();
			statement.close();
			mConnection.commit();
			InfoSigns.debug("Saved.", "");
		} catch (SQLException e) {
			InfoSigns.debug("Performing Rollback", "");
			rollback();
			throw new DataStoreException(e);
		}
	}

	@Override
	public List<PlayerStore> loadPlayer(TimePeriod period, int count)
			throws DataStoreException {
		String id;
		switch (period) {
		case Day:
			id = "strftime('%Y%j','now')"; //$NON-NLS-1$
			break;
		case Week:
			id = "strftime('%Y%W','now')"; //$NON-NLS-1$
			break;
		case Month:
			id = "strftime('%Y%m','now')"; //$NON-NLS-1$
			break;
		case Year:
			id = "strftime('%Y','now')"; //$NON-NLS-1$
			break;
		default:
			id = null;
			break;
		}
		try {
			Statement statement = mConnection.createStatement();
			ResultSet results = statement
					.executeQuery("SELECT something" + ", mh_Players.UUID from " + period.getTable() + " inner join Players using (PLAYER_ID)" + (id != null ? " where ID=" + id : "") + " order by " + "" + " desc limit " + count); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
			ArrayList<PlayerStore> list = new ArrayList<PlayerStore>();

			while (results.next())
				list.add(new PlayerStore(Bukkit.getOfflinePlayer(UUID
						.fromString(results.getString(2))), results.getInt(1)));
			results.close();
			statement.close();
			return list;
		} catch (SQLException e) {
			throw new DataStoreException(e);
		}
	}

}
