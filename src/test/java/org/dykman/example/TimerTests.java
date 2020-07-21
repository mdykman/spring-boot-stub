package org.dykman.example;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.dykman.example.DbUtil;
import org.dykman.example.springboot.BeanDefinitions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={BeanDefinitions.class})

public class TimerTests {
	
	public static final long ONE_HOUR_IN_MILLIS = 3600000L;
	@Test
	public void testTimer() throws Exception {
		long inow= System.currentTimeMillis();
		Date now = new Date(inow);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String mask = format.format(now);
		TimeZone userTz = TimeZone.getDefault();
		boolean b = DbUtil.isNow(new Timestamp(inow),now,0,mask,userTz.getDisplayName());
		assertTrue(b);
	}
	@Test
	public void testTimerWithCustom() throws Exception {
		long inow= System.currentTimeMillis();
		Date now = new Date(inow);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String mask = format.format(now);
		TimeZone userTz = TimeZone.getDefault();
		userTz.getDisplayName();
		boolean b = DbUtil.isNow(new Timestamp(inow),now,0,mask,"UTC -05:00");
		//assertTrue(b);
	}
	
	@Test
	public void testTimerWithCustomTimezone() throws Exception {
		// 2019-12-11T01:59:47.984+00:00 in ISO 8601
		// 2019-12-11T03:00:01.990+00:00 in ISO 8601
		boolean b = DbUtil.isNow(new Timestamp(1576029587984L),new Date(1576033201990L),1,"12:00","UTC +09:00");
		assertTrue(b);
	}

	@Test
	public void testTimerWithAnotherCustomTimezone() throws Exception {
		long now = System.currentTimeMillis();
		Date fnow = new Date(now);
		DateFormat format = new SimpleDateFormat("HH:mm");
		String mask = format.format(fnow);
		// 2019-12-11T01:59:47.984+00:00 in ISO 8601
		// 2019-12-11T03:00:01.990+00:00 in ISO 8601
		System.out.println(">>>>>>>>>>>>>>>>>>>>");
		boolean b = DbUtil.isNow(new Timestamp(now),new Date(now),0,mask,"UTC -05:00");
		System.out.println(">>>>>>>>>>>>>>>>>>>>");
		//assertTrue(b);
	}

	
	
	@Test
	public void testTimerInTheFuture() throws Exception {
		long inow= System.currentTimeMillis();
		Date now = new Date(inow);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String mask = format.format(now);
		TimeZone userTz = TimeZone.getDefault();
		Date fnow = new Date(inow+ONE_HOUR_IN_MILLIS);
		boolean b = DbUtil.isNow(new Timestamp(inow),fnow,0,mask,userTz.getDisplayName());
		assertFalse(b);
	}

	@Test
	public void testTimerTomorrow() throws Exception {
		long inow= System.currentTimeMillis();
		Date now = new Date(inow);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String mask = format.format(now);
		TimeZone userTz = TimeZone.getDefault();
		Date fnow = new Date(inow+ (24*ONE_HOUR_IN_MILLIS));
		boolean b = DbUtil.isNow(new Timestamp(inow),fnow,1,mask,userTz.getDisplayName());
		assertTrue(b);
	}
	

	@Test
	public void testTimerInThePast() throws Exception {
		long inow= System.currentTimeMillis();
		Date now = new Date(inow);
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		String mask = format.format(now);
		TimeZone userTz = TimeZone.getDefault();
		Date fnow = new Date(inow-ONE_HOUR_IN_MILLIS);
		boolean b = DbUtil.isNow(new Timestamp(inow),fnow,0,mask,userTz.getDisplayName());
		assertFalse(b);
	}
	
}
