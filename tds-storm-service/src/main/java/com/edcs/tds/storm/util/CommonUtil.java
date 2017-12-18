package com.edcs.tds.storm.util;

import com.google.common.base.Joiner;

/**
 * Common utils and functions
 */
public class CommonUtil {

	public static final String COMMA = ",";
	public static final String UNDERLINE = "_";
	public static final String LINE = "-";

	private static Joiner comma_joiner = null;
	private static Joiner underline_joiner = null;
	private static Joiner line_joiner = null;

	static {
		comma_joiner = Joiner.on(COMMA).skipNulls();
		underline_joiner = Joiner.on(UNDERLINE).skipNulls();
		line_joiner = Joiner.on(LINE).skipNulls();

	}

	public static Joiner commaLink() {
		return comma_joiner;
	}

	public static Joiner underlineLink() {
		return underline_joiner;
	}

	public static Joiner lineLink() {
		return line_joiner;
	}

}
