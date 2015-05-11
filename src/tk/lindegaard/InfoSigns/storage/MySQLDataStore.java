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

public class MySQLDataStore extends DatabaseDataStore {

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
								.format("UPDATE mh_Daily SET %1$s = %1$s + %3$d WHERE ID = DATE_FORMAT(NOW(), '%%Y%%j') AND PLAYER_ID = %2$d;", ids.get(stat.player.getUniqueId()), stat.amount)); //$NON-NLS-1$
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
	protected Connection setupConnection() throws SQLException,
			DataStoreException {
		try {
			Class.forName("com.mysql.jdbc.Driver"); //$NON-NLS-1$
			return DriverManager
					.getConnection(
							"jdbc:mysql://" + InfoSigns.config().databaseHost + "/" + InfoSigns.config().databaseName + "?autoReconnect=true", InfoSigns.config().databaseUsername, InfoSigns.config().databasePassword); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (ClassNotFoundException e) {
			throw new DataStoreException("MySQL not present on the classpath"); //$NON-NLS-1$
		}
	}

	@Override
	protected void setupTables(Connection connection) throws SQLException {
		Statement create = connection.createStatement();

		// Create new empty tables if they do not exist
		create.executeUpdate("CREATE TABLE IF NOT EXISTS mh_Players (UUID CHAR(40) PRIMARY KEY, NAME CHAR(20), PLAYER_ID INTEGER NOT NULL AUTO_INCREMENT, KEY PLAYER_ID (PLAYER_ID))"); //$NON-NLS-1$

		create.close();
		connection.commit();
	}


	@Override
	protected void setupStatements(Connection connection) throws SQLException {
		mAddPlayerStatement = connection
				.prepareStatement("INSERT IGNORE INTO mh_Players(UUID,NAME) VALUES(?,?);"); //$NON-NLS-1$
		mGetPlayerStatement[0] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID=?;"); //$NON-NLS-1$
		mGetPlayerStatement[1] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID IN (?,?);"); //$NON-NLS-1$
		mGetPlayerStatement[2] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID IN (?,?,?,?,?);"); //$NON-NLS-1$
		mGetPlayerStatement[3] = connection
				.prepareStatement("SELECT * FROM mh_Players WHERE UUID IN (?,?,?,?,?,?,?,?,?,?);"); //$NON-NLS-1$

		mRecordAchievementStatement = connection
				.prepareStatement("REPLACE INTO mh_Achievements VALUES(?,?,?,?);"); //$NON-NLS-1$

		mAddPlayerStatsStatement = connection
				.prepareStatement("INSERT IGNORE INTO mh_Daily(ID, PLAYER_ID) VALUES(DATE_FORMAT(NOW(), '%Y%j'),?);"); //$NON-NLS-1$

		mLoadAchievementsStatement = connection
				.prepareStatement("SELECT ACHIEVEMENT, DATE, PROGRESS FROM mh_Achievements WHERE PLAYER_ID = ?;"); //$NON-NLS-1$

		mGetPlayerUUID = connection
				.prepareStatement("SELECT UUID FROM mh_Players WHERE NAME=?"); //$NON-NLS-1$
		mUpdatePlayerName = connection
				.prepareStatement("UPDATE mh_Players SET NAME=? WHERE UUID=?"); //$NON-NLS-1$
	}

	@Override
	public List<PlayerStore> loadPlayer(TimePeriod period, int count)
			throws DataStoreException {
		String id;
		switch (period) {
		case Day:
			id = "DATE_FORMAT(NOW(), '%Y%j')"; //$NON-NLS-1$
			break;
		case Week:
			id = "DATE_FORMAT(NOW(), '%Y%U')"; //$NON-NLS-1$
			break;
		case Month:
			id = "DATE_FORMAT(NOW(), '%Y%c')"; //$NON-NLS-1$
			break;
		case Year:
			id = "DATE_FORMAT(NOW(), '%Y')"; //$NON-NLS-1$
			break;
		default:
			id = null;
			break;
		}
		Statement statement;
		try {
			// test if connection to MySql works properly
			statement = mConnection.createStatement();
			ResultSet rs = statement
					.executeQuery("SELECT Player from `Players` LIMIT 0");
			rs.close();
		} catch (SQLException e) {
			// The connection did not work, try to initialiaze again.
			mConnection = null;
			try {
				mConnection = setupConnection();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			statement = mConnection.createStatement();
			ResultSet results = statement
					.executeQuery("SELECT something" + ", Players.UUID from mh_" + period.getTable() + " inner join mh_Players on mh_Players.PLAYER_ID=" + period.getTable() + ".PLAYER_ID" + (id != null ? " where ID=" + id : "") + " order by something" + " desc limit " + count); //$NON-NLS
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
