package com.espirit.moddev.cli.commands.schedule;

import de.espirit.firstspirit.access.schedule.ScheduleEntry;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.*;


public class ScheduleStartInformationTest {

	@Test
	public void constructor() {
		//setup
		final ScheduleEntry entry = ScheduleUtils.createScheduleEntry(1, "test task");
		final Date start = new Date();
		final Date finish = new Date(start.getTime() + 100);
		//test
		final ScheduleStartInformation scheduleStartInformation = new ScheduleStartInformation(entry, start, finish);
		//verify
		assertEquals("entry mismatch", entry, scheduleStartInformation.getScheduleEntry());
		assertEquals("start time mismatch", start, scheduleStartInformation.getStartTime());
		assertEquals("finish time mismatch", finish, scheduleStartInformation.getFinishTime());
		assertEquals("duration mismatch", 100, scheduleStartInformation.getDuration());
	}

}