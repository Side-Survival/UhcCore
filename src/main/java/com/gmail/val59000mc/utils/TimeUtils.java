package com.gmail.val59000mc.utils;

public class TimeUtils{

	public static final long SECOND_TICKS = 20L;

	public static final long SECOND = 1000;
	public static final long MINUTE = SECOND*60;
	public static final long HOUR = MINUTE*60;

	public static String getFormattedTime(long time) {
		if (time == 0)
			return "";

		int h = (int) time / (60 * 60);
		int m = (int) time / 60;
		int s = (int) time % 60;
		if (s < 0)
			s = 0;

		if (h == 0) {
			return (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
		} else {
			return (h > 9 ? h : "0" + h) + ":" + (m > 9 ? m : "0" + m) + ":" + (s > 9 ? s : "0" + s);
		}
	}
}