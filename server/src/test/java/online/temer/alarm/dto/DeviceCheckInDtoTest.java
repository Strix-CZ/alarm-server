package online.temer.alarm.dto;

import online.temer.alarm.db.DbTestExtension;
import online.temer.alarm.db.TestConnectionProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(DbTestExtension.class)
class DeviceCheckInDtoTest
{
	private DeviceCheckInQuery query;
	private DeviceQuery deviceQuery;
	private long deviceId;
	private Connection connection;

	@BeforeEach
	void setUp()
	{
		connection = new TestConnectionProvider().get();
		query = new DeviceCheckInQuery();
		deviceQuery = new DeviceQuery();

		deviceId = deviceQuery.insertDevice(connection, DeviceDto.generateDevice());
	}

	@Test
	public void whenNoUpdates_getLatestReturnsNull()
	{
		Assertions.assertNull(query.getLatest(connection, 1));
	}

	@Test
	void whenSingleUpdate_returnsIt()
	{
		LocalDateTime time = LocalDateTime.now().withNano(0);
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, time, 100));

		var deviceCheckInDto = query.getLatest(connection, deviceId);

		Assertions.assertNotNull(deviceCheckInDto, "deviceCheckInDto should not be null");
		Assertions.assertTrue(deviceCheckInDto.id > 0, "id");
		Assertions.assertEquals(deviceId, deviceCheckInDto.device, "deviceId");
		Assertions.assertEquals(time, deviceCheckInDto.time, "time");
		Assertions.assertEquals(100, deviceCheckInDto.battery, "battery");
	}

	@Test
	void whenMultipleUpdates_latestIsReturned()
	{
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 100));
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 90));

		var checkIn = query.getLatest(connection, deviceId);
		Assertions.assertEquals(90, checkIn.battery, "battery");
	}

	@Test
	void whenMultipleDevicesUpdates_latestOfTheDeviceIsReturned()
	{
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 100));

		long deviceId2 = deviceQuery.insertDevice(connection, DeviceDto.generateDevice());
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId2, LocalDateTime.now(), 50));

		Assertions.assertEquals(100, query.getLatest(connection, deviceId).battery, "battery of first device is 100");
		Assertions.assertEquals(50, query.getLatest(connection, deviceId2).battery, "battery of second device is 50");
	}

	@Test
	void getNewestWithLargeLimit_returnsAll()
	{
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 100));
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 90));

		assertCheckInBatteryLevels(query.getNewestCheckIns(connection, deviceId, 1000, 100), 100, 90);
	}

	@Test
	void getNewestWithSmallLimit_returnsNewest()
	{
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 100));
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 90));

		assertCheckInBatteryLevels(query.getNewestCheckIns(connection, deviceId, 1, 100), 90);
	}

	@Test
	void getNewestWithMaxAge_filtersOldResult()
	{
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.of(2020, 1, 1, 0, 0, 0), 100));
		query.insertUpdate(connection, new DeviceCheckInDto(deviceId, LocalDateTime.now(), 90));

		assertCheckInBatteryLevels(query.getNewestCheckIns(connection, deviceId, 1000, 5), 90);
	}

	private void assertCheckInBatteryLevels(List<DeviceCheckInDto> checkIns, Integer... expectedBatteryLevels)
	{
		var batteryLevels = checkIns.stream()
				.map(c -> c.battery)
				.collect(Collectors.toList());

		org.assertj.core.api.Assertions.assertThat(batteryLevels).containsExactly(expectedBatteryLevels);
	}
}
