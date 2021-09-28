package online.temer.alarm.server.handlers;

import online.temer.alarm.db.TestConnectionProvider;
import online.temer.alarm.dto.DeviceCheckInDto;
import online.temer.alarm.dto.DeviceCheckInQuery;
import online.temer.alarm.dto.DeviceDto;
import online.temer.alarm.dto.DeviceQuery;
import online.temer.alarm.dto.UserDto;
import online.temer.alarm.server.HttpUtil;
import online.temer.alarm.server.ServerTestExtension;
import online.temer.alarm.server.TestUserAuthentication;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@ExtendWith(ServerTestExtension.class)
public class GetCheckInsTest
{
	private DeviceDto device;
	private Connection connection;
	private DeviceQuery deviceQuery;
	private DeviceCheckInQuery deviceCheckInQuery;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		deviceQuery = new DeviceQuery();
		deviceCheckInQuery = new DeviceCheckInQuery();
	}

	@Test
	void noCheckIns_returnsEmptyList()
	{
		createUserWithDevice(TimeZone.getDefault());

		var checkIns = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns");

		Assertions.assertThat(checkIns.length())
				.isEqualTo(0);
	}

	@Test
	void singleCheckIn_returnsIt()
	{
		createUserWithDevice(TimeZone.getDefault());
		LocalDateTime time = LocalDateTime.of(2020, 10, 31, 8, 20);

		DeviceCheckInDto expectedCheckIn = new DeviceCheckInDto(device.id, time, 95);
		deviceCheckInQuery.insertUpdate(connection, expectedCheckIn);

		assertCheckIn(time.atZone(ZoneId.systemDefault()), 95);
	}

	@Test
	void multipleCheckIns_allAreReturned()
	{
		createUserWithDevice(TimeZone.getDefault());
		deviceCheckInQuery.insertUpdate(connection, new DeviceCheckInDto(device.id, LocalDateTime.now(), 95));
		deviceCheckInQuery.insertUpdate(connection, new DeviceCheckInDto(device.id, LocalDateTime.now(), 90));

		var checkIns = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns");
		List<Integer> batteryLevels = new ArrayList<>();
		for (int i = 0; i < checkIns.length(); ++i)
		{
			batteryLevels.add(checkIns.getJSONObject(i).getInt("battery"));
		}

		Assertions.assertThat(batteryLevels).containsExactly(95, 90);
	}

	@Test
	void timeIsReturnedInDeviceTimeZone()
	{
		TimeZone timeZone = TimeZone.getTimeZone(ZoneId.of("Asia/Kolkata"));
		createUserWithDevice(timeZone);

		LocalDateTime serverLocalTime = LocalDateTime.of(2020, 10, 31, 8, 20);
		deviceCheckInQuery.insertUpdate(connection, new DeviceCheckInDto(device.id, serverLocalTime, 95));

		var time = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns")
				.getJSONObject(0)
				.getLong("time");

		Assertions.assertThat(time)
				.isEqualTo(ZonedDateTime.of(serverLocalTime, ZoneId.systemDefault()).toEpochSecond());
	}

	private void createUserWithDevice(TimeZone timeZone)
	{
		device = deviceQuery.generateSaveAndLoadDevice(connection, timeZone, 10L);
		TestUserAuthentication.setAuthenticatedUser(new UserDto(10, "john@example.com", "hash", "salt"));
	}

	private void assertCheckIn(ZonedDateTime expectedTime, int expectedBattery)
	{
		var checkIns = new JSONObject(makeGetRequest().body())
				.getJSONArray("checkIns");

		Assertions.assertThat(checkIns.length())
				.as("checkIns array length")
				.isEqualTo(1);

		var checkIn = checkIns.getJSONObject(0);
		Assertions.assertThat(checkIn.getLong("time"))
				.as("time")
				.isEqualTo(expectedTime.toEpochSecond());

		Assertions.assertThat(checkIn.getInt("battery"))
				.as("battery")
				.isEqualTo(expectedBattery);
	}

	private HttpResponse<String> makeGetRequest()
	{
		try
		{
			return HttpUtil.makeGetRequest(new URI("http://localhost:8765/alarm"));
		}
		catch (URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}
}
