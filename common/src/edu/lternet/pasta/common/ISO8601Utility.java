/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author servilla
 * @since Oct 25, 2012
 * 
 *        ISO8601Utility converts a Java Date objects to a true ISO8601
 *        date/time, year, and date String.
 * 
 *        This class is based on the information provide by
 *        http://developer.marklogic.com/learn/2004-09-dates.
 * 
 */
public class ISO8601Utility {
	/*
	 * Class variables
	 */

	private static DateFormat m_ISO8601DateTime = new SimpleDateFormat(
	    "yyyy-MM-dd'T'HH:mm:ss");

	private static DateFormat m_ISO8601Year = new SimpleDateFormat("yyyy");

	private static DateFormat m_ISO8601Date = new SimpleDateFormat("yyyy-MM-dd");

	/*
	 * Class methods
	 */

	/**
	 * Formats the current Date object to an ISO8601 date/time string.
	 * 
	 * @return ISO8601 date/time as a String object.
	 */
	public static String formatDateTime() {
		return formatDateTime(new Date());
	}

	/**
	 * Formats a Date object to an ISO8601 date/time string.
	 * 
	 * @param date
	 * @return ISO8601 date/time as a String object.
	 */
	public static String formatDateTime(Date date) {
		if (date == null) {
			return formatDateTime(new Date());
		}

		// format in (almost) ISO8601 format
		String dateStr = m_ISO8601DateTime.format(date);
		return dateStr;
	}

	/**
	 * Formats the current Date object to an ISO8601 year string.
	 * 
	 * @return ISO8601 year as a String object.
	 */
	public static String formatYear() {
		return formatYear(new Date());
	}

	/**
	 * Formats a Date object to an ISO8601 year string.
	 * 
	 * @param date
	 * @return ISO8601 year as a String object.
	 */
	public static String formatYear(Date date) {
		if (date == null) {
			return formatYear(new Date());
		}

		// format in (almost) ISO8601 format
		String dateStr = m_ISO8601Year.format(date);
		return dateStr;
	}

	/**
	 * Formats the current Date object to an ISO8601 date string.
	 * 
	 * @return ISO8601 date as a String object.
	 */
	public static String formatDate() {
		return formatDate(new Date());
	}

	/**
	 * Formats a Date object to an ISO8601 date string.
	 * 
	 * @param date
	 * @return ISO8601 date as a String object.
	 */
	public static String formatDate(Date date) {
		if (date == null) {
			return formatDate(new Date());
		}

		// format in (almost) ISO8601 format
		String dateStr = m_ISO8601Date.format(date);
		return dateStr;
	}

	public static void main(String[] arg) {

		System.out.println(ISO8601Utility.formatDateTime());
		System.out.println(ISO8601Utility.formatYear());
		System.out.println(ISO8601Utility.formatDate());

	}

	/*
	 * Instance methods
	 */

}