package online.temer.alarm.db;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListHandler<T> implements ResultSetHandler<List<T>>
{
	private final ResultSetHandler<T> rowHandler;

	public ListHandler(ResultSetHandler<T> rowHandler)
	{
		this.rowHandler = rowHandler;
	}

	@Override
	public List<T> handle(ResultSet rs) throws SQLException
	{
		List<T> results = new ArrayList<>();
		T item;

		while ((item = rowHandler.handle(rs)) != null)
		{
			results.add(item);
		}

		return results;
	}
}
