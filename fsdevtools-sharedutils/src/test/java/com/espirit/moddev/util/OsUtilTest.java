package com.espirit.moddev.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OsUtilTest {

	@Test
	public void asWindowsCommands() {
		final List<String> windowsCommands = OsUtil.asWindowsCommands(Arrays.asList("bin", "myFile.exe"));
		assertEquals(4, windowsCommands.size());
		assertEquals("cmd", windowsCommands.get(0));
		assertEquals("/c", windowsCommands.get(1));
		assertEquals("bin", windowsCommands.get(2));
		assertEquals("myFile.exe", windowsCommands.get(3));
	}

	@Test
	public void asUnixCommands() {
		final List<String> unixCommands = OsUtil.asUnixCommands(Arrays.asList("bin", "myFile.sh"));
		assertEquals(3, unixCommands.size());
		assertEquals("sh", unixCommands.get(0));
		assertEquals("bin", unixCommands.get(1));
		assertEquals("myFile.sh", unixCommands.get(2));
	}

}