package online.temer.alarm.admin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserManagementTest
{
	@Test
	void unknownString_invalidCommand()
	{
		Output output = execute("invalid");
		assertFails(output, "Invalid command");
	}

	@Test
	void noArguments_invalidCommand()
	{
		Output output = execute();
		assertFails(output, "Invalid command");
	}

	@Test
	void addingUserWithoutEmail_incorrectArguments()
	{
		Output output = execute("add");
		assertFails(output, "Incorrect arguments: add email");
	}

	private Output execute(String... command)
	{
		return new UserManagement().execute(command);
	}

	private void assertFails(Output output, String expectedMessage)
	{
		Assertions.assertThat(output.exitCode)
				.as("exit code")
				.isEqualTo(1);

		Assertions.assertThat(output.lines)
				.as("output")
				.containsExactly(expectedMessage);
	}
}
