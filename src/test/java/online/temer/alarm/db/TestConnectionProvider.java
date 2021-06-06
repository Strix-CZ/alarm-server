package online.temer.alarm.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnectionProvider
{
	private static final String URL = "jdbc:mariadb://localhost/alarm_clock_test";
	private static final String USER = "test";
	private static final String PASS = "lekcmxl2ei08sx4jalcmfuryfm";

	private static Connection connection;

	public static Connection getConnection()
	{
		if (connection == null)
		{
			try
			{
				connection = DriverManager.getConnection(URL, USER, PASS);
			}
			catch (SQLException e)
			{
				throw new RuntimeException(e);
			}
		}

		return connection;
	}

	public static void close()
	{
		try
		{
			if (connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
