package online.temer.alarm.server.authentication;

import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.server.Handler;
import online.temer.alarm.server.QueryParameterReader;
import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class UserAuthentication implements Authentication<DeviceDto>
{
	private final DeviceQuery deviceQuery;

	public UserAuthentication(DeviceQuery deviceQuery)
	{
		this.deviceQuery = deviceQuery;
	}

	public Result<DeviceDto> authenticate(Connection connection, QueryParameterReader queryParameterReader, Request request, Response response)
	{
		if (!request.headers().contains("Authorization")) {
			response.header("WWW-Authenticate", "Basic realm=\"Authenticate to Alarm\"");
			return new Result<>(new Handler.Response(401));
		}

		// No authorization at the moment. Just return any device.
		var device = deviceQuery.get(connection);

		if (device == null)
		{
			return new Result<>(new Handler.Response(401));
		}
		else
		{
			return new Result<>(device);
		}
	}
}
