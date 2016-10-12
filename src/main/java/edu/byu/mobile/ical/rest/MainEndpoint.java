package edu.byu.mobile.ical.rest;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("/")
@Produces({"application/json", "text/json"})
public class MainEndpoint {

	private static final Logger LOG = Logger.getLogger(MainEndpoint.class);

	private static final String UNTIL_NEVER = "never";

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	public static final String DEFAULT_SHOW = "3MO";

	public static final TimePeriod DEFAULT_SHOW_VALUE = TimePeriod.valueOf(DEFAULT_SHOW);
	private static final String START_TODAY = "today";

	private static final boolean isInProd = System.getProperty("byu.app.stage") != null && "prod".equalsIgnoreCase(System.getProperty("byu.app.stage"));

	/**
	 * Parses the given iCal feed and returns it as an array of JSON events.<br /><br />
	 * <p/>
	 * <h3>Parameters</h3>
	 * 'feedUrl' is required.<br />
	 * 'start' is the first date for which events will be shown. It is optional and defaults to 'today'.  It must be in
	 * the form 'yyy-MM-dd'<br />
	 * 'offset' offsets the first date for which events will be shown by a specified amount. It is optional and defaults
	 * to '0'..
	 * 'show' and 'until' are both optional.  If neither is specified, 'show' defaults to '3MO'.
	 * If 'until' is specified, the value of 'show' is ignored.<br />
	 * <br />
	 * 'show' and 'offset' must be in the form of a positive integer, followed by one of the following:
	 * <dl>
	 * <li>D (day)</li>
	 * <li>WK (week)</li>
	 * <li>MO (month)</li>
	 * <li>Y (year)</li>
	 * </dl>
	 * Examples:<br />
	 * <table>
	 * <thead><tr>
	 * <th>Value</th><th>Meaning</th>
	 * </tr></thead>
	 * <tr><td>12D</td><td>Twelve Days</td></tr>
	 * <tr><td>3WK</td><td>Three Weeks</td></tr>
	 * <tr><td>3MO</td><td>Three Months</td></tr>
	 * <tr><td>2Y</td><td>Two Years</td></tr>
	 * </table>
	 * <br />
	 * 'until' must be a date, in the form 'yyyy-MM-dd'.<br /><br />
	 * <p/>
	 * <h3>A Note about Caching</h3>
	 * Please note that all responses are cached by the server. To bypass the cache, add the request param 'skip-cache'<br />
	 * with a value of 'true' or 'yes'.<br /><br />
	 * <p/>
	 * <h3>GZIP</h3>
	 * This service is capable of returning responses with GZIP compression. It is highly recommended that GZIP encoding
	 * be used, as it can yield substantial reductions in file size.<br />
	 * To receive GZIP-compressed responses, include the 'Accept-Encoding: gzip' header in your request.<br /><br />
	 * <p/>
	 * <h3>Example Usage:</h3>
	 * <p/>
	 * <a href="./rest/v1/parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&show=4MO" target="_blank">
	 * parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&show=4MO
	 * </a>
	 * shows the parsed BYU Academic calendar feed for the next four months.<br />
	 * <p/>
	 * <p/>
	 * <a href="./rest/v1/parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&show=4MO" target="_blank">
	 * parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&until=2012-12-21
	 * </a>
	 * shows the same calendar until Dec. 21, 2012<br /><br />
	 * <p/>
	 * <h3>Response Format</h3>
	 * The response is in the form of a JSON Array, similar to the following:
	 * <br />
	 * <pre>[
	 * {
	 * 	"uid":"000000013300@calendar.byu.edu",
	 * 	"summary":"New Year's Day Holiday",
	 * 	"allDay":false,
	 * 	"startDate":"2012-01-02T07:00:00.000+0000",
	 * 	"endDate":"2012-01-03T06:00:00.000+0000",
	 * 	"repeats":true,
	 * 	"repeatRule": {
	 *		"rule": "FREQ=YEARLY;INTERVAL=1",
	 *		"firstOccurrenceEnd": "20110101T000000",
	 *		"firstOccurrenceStart": "20110101T235959",
	 *		"exceptions": null
	 *	},
	 * 	"modified":null,
	 * 	"occurrences":[
	 * 		{
	 * 			"allDay":false,
	 * 			"end":"2012-01-03T06:00:00.000+0000",
	 * 			"start":"2012-01-02T07:00:00.000+0000"
	 * 		}
	 * 	],
	 * 	"description":null,
	 * 	"location":null,
	 * 	"url":"http://calendar.byu.edu/content/new-years-day-holiday"
	 * }
	 * ]</pre>
	 * <br /><b>Please note that these fields may not appear in this exact order.</b><br />
	 * <h4>Description of fields:</h4>
	 * <table>
	 * <tr><th>name</th><th>type</th><th>nullable?</th><th>description</th></tr>
	 * <tr><td>uid</td><td>string</td><td>no</td><td>the iCal uid of this event</td></tr>
	 * <tr><td>summary</td><td>string</td><td>yes</td><td>summary/name of the event</td></tr>
	 * <tr><td>allDay</td><td>boolean</td><td>no</td><td>true if the event is an all-day event.</td></tr>
	 * <tr><td>startDate</td><td>date</td><td>no</td><td>start date/time of the first occurrence of the event</td></tr>
	 * <tr><td>endDate</td><td>date</td><td>no</td><td>end date/time of the first occurrence this event</td></tr>
	 * <tr><td>repeats</td><td>boolean</td><td>no</td><td>true if the event repeats.</td></tr>
	 * <tr><td>repeatRule</td><td>object</td><td>yes</td><td>iCal repeat rule information.</td></tr>
	 * <tr><td>modified</td><td>date</td><td>yes</td><td>the date that this event was last modified</td></tr>
	 * <tr><td>occurrences</td><td>array</td><td>no</td>
	 * <td>
	 * An array of all occurrences between the date of the request and the specified 'show' or 'until' value.<br />
	 * Contains the following fields:
	 * <ul>
	 * <li>allDay: corresponds to the allDay field on the main event object</li>
	 * <li>end: date/time the occurrence ends</li>
	 * <li>start: date/time the occurrence begins</li>
	 * </ul>
	 * </td>
	 * </tr>
	 * <tr><td>description</td><td>string</td><td>yes</td><td>description of the event</td></tr>
	 * <tr><td>location</td><td>string</td><td>yes</td><td>location of the event</td></tr>
	 * <tr><td>url</td><td>string</td><td>yes</td><td>a url containing more information about the event</td></tr>
	 * </table>
	 * <br />
	 * All dates are in ISO-8601 notation, in which the beginning of the Unix Epoch would be formatted "1970-01-01T00:00:00.000+0000".
	 *
	 * @param feedUrl the URL of the feed to parse.
	 * @param start   starting date for the set which will be returned
	 * @param offset  the period for the returned set will start at 'start + offset'
	 * @param show	period for which to calculate occurrences
	 * @param until   date until which occurrences will be calculated
	 * @return all events occurring within the specified time frame
	 */
	@Path("/parse")
	@SuppressWarnings({"unchecked"})
	@GET
	public Event[] parse(
			@QueryParam("feedUrl") URL feedUrl,
			@QueryParam("start") @DefaultValue(START_TODAY) String start,
			@QueryParam("offset") @DefaultValue(TimePeriod.ZERO) TimePeriod offset,
			@QueryParam("show") @DefaultValue(DEFAULT_SHOW) TimePeriod show,
			@QueryParam("until") @DefaultValue(UNTIL_NEVER) String until
	) {
		LOG.debug(String.format("Got request for calendar at '%s' with show = '%s' and until = '%s'", feedUrl, show, until));
		if (feedUrl == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		
		TimeZone.setDefault(TimeZone.getTimeZone("America/Denver"));
		
		final CalendarBuilder builder = new CalendarBuilder();
			

		final Calendar calendar;
		try {
			final String lowerFeedUrl = feedUrl.toString().toLowerCase();
			if (lowerFeedUrl.contains("calendar.byu.edu/ical") || lowerFeedUrl.contains("calendar.byu.edu/calendar/ical") || (!isInProd && lowerFeedUrl.startsWith("file:"))) {
				calendar = builder.build(correctStream(feedUrl.openStream()));
			} else {
				calendar = builder.build(feedUrl.openStream());
			}
		} catch (IOException e) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		} catch (ParserException e) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		
		final Date startDate = calculateStartDate(start, offset);

		final List<Event> events = filterComponents(calendar.getComponents(), startDate, calculateEndDate(startDate, show, until));

		return events.toArray(new Event[events.size()]);
	}

	/**
	 * Calculates the occurrences of a given repeat pattern.<br />
	 * The 'start', 'offset', 'show', and 'until' parameters match their definitions in {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}.
	 *
	 * @param start same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param offset same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param show same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param until same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param ruleStart Start date/time of the event.  Should be in the ical date format.
	 * @param ruleEnd End date/time of the event.  Should be in ical date format.
	 * @param rule Rule the ical repeat rule.
	 * @param exceptions The ical EXDATE value.
	 * @return All occurrences for the specified event in the specified date range.
	 */
	@GET
	@Path("/occurrences")
	public TimeSpan[] calculateOccurrences(
			@QueryParam("start") @DefaultValue(START_TODAY) String start,
			@QueryParam("offset") @DefaultValue(TimePeriod.ZERO) TimePeriod offset,
			@QueryParam("show") @DefaultValue(DEFAULT_SHOW) TimePeriod show,
			@QueryParam("until") @DefaultValue(UNTIL_NEVER) String until,
			@QueryParam("ruleStart") String ruleStart,
			@QueryParam("ruleEnd") String ruleEnd,
			@QueryParam("rule") String rule,
			@QueryParam("exceptions") String exceptions) {
		try {
			return doCalculateOccurrences(start, offset, show, until, ruleStart, ruleEnd, rule, exceptions);
		} catch (ParseException e) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}

	/**
	 * Calculates the occurrences of a given repeat pattern.<br />
	 * The 'start', 'offset', 'show', and 'until' parameters match their definitions in {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}.
	 *
	 * @param start same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param offset same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param show same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param until same as {@link #parse(java.net.URL, String, TimePeriod, TimePeriod, String)}
	 * @param ruleStart Start date/time of the event.  Should be in the ical date format.
	 * @param ruleEnd End date/time of the event.  Should be in ical date format.
	 * @param rule Rule the ical repeat rule.
	 * @param exceptions The ical EXDATE value.
	 * @return All occurrences for the specified event in the specified date range.
	 */
	@POST
	@Path("/occurrences")
	@Consumes("application/x-www-form-urlencoded")
	public TimeSpan[] calculateOccurrencesFromForm(
			@FormParam("start") @DefaultValue(START_TODAY) String start,
			@FormParam("offset") @DefaultValue(TimePeriod.ZERO) TimePeriod offset,
			@FormParam("show") @DefaultValue(DEFAULT_SHOW) TimePeriod show,
			@FormParam("until") @DefaultValue(UNTIL_NEVER) String until,
			@FormParam("ruleStart") String ruleStart,
			@FormParam("ruleEnd") String ruleEnd,
			@FormParam("rule") String rule,
			@FormParam("exceptions") String exceptions) {
		try {
			return doCalculateOccurrences(start, offset, show, until, ruleStart, ruleEnd, rule, exceptions);
		} catch (ParseException e) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
	}

	@SuppressWarnings({"unchecked"})
	private static TimeSpan[] doCalculateOccurrences(
			String start,
			TimePeriod offset,
			TimePeriod show,
			String until,
			String ruleStartDate,
			String ruleEndDate,
			String rule,
			String exceptionDate
	) throws ParseException {
		LOG.debug(String.format("Calculating occurrences with args:%n" +
				"start = '%s'%n" +
				"offset = '%s'%n" +
				"show='%s'%n" +
				"until='%s'%n" +
				"ruleStartDate='%s'%n" +
				"ruleEndDate='%s'%n" +
				"rule='%s'%n" +
				"exceptionDate='%s'", start, offset, show, until, ruleStartDate, ruleEndDate, rule, exceptionDate));

		final Date from = calculateStartDate(start, offset);
		final Date to = calculateEndDate(from, show, until);

		final DateTime ruleStart = new DateTime(ruleStartDate);
		final DateTime ruleEnd = new DateTime(ruleEndDate);

		final Period period = new Period(new DateTime(from), new DateTime(to));
		final VEvent vevent = new VEvent(ruleStart, ruleEnd, "hi");

		vevent.getProperties().add(new RRule(rule));

		if (exceptionDate != null) {
			vevent.getProperties().add(new ExDate(new DateList(exceptionDate, Value.DATE)));
		}

		final PeriodList calculated = vevent.calculateRecurrenceSet(period);
		if (calculated == null || calculated.isEmpty()) {
			return new TimeSpan[0];
		}
		return TimeSpan.fromIcal(new HashSet<Period>(calculated)).toArray(new TimeSpan[calculated.size()]);
	}

	private static Date calculateStartDate(String start, TimePeriod offset) {
		final java.util.Calendar startCal = java.util.Calendar.getInstance();
		final Date startDate = parseDate(start);
		if (startDate == null) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST);
		}
		startCal.setTime(startDate);
		if (offset != null) {
			startCal.add(offset.getUnit().getCalendarField(), offset.getDuration());
		}
		startCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
		startCal.set(java.util.Calendar.MINUTE, 0);
		startCal.set(java.util.Calendar.SECOND, 0);
		startCal.set(java.util.Calendar.MILLISECOND, 0);
		return startCal.getTime();
	}

	private static Date calculateEndDate(Date start, TimePeriod period, String rawUntil) {
		final java.util.Calendar endCal = java.util.Calendar.getInstance();

		final Date until = parseDate(rawUntil);

		if (until != null) {
			endCal.setTime(until);
		} else {
			endCal.setTime(start);
			endCal.add(period.getUnit().getCalendarField(), period.getDuration());
		}
		return endCal.getTime();
	}

	private static List<Event> filterComponents(final List<Component> list, final Date start, final Date end) {
		final Period recurrencePeriod = new Period(new DateTime(start), new DateTime(end));
		final List<Event> res = new LinkedList<Event>();
		for (final Component each : list) {
			if (!(each instanceof VEvent)) {
				continue;
			}
			final PeriodList pl = each.calculateRecurrenceSet(recurrencePeriod);
			if (pl == null || pl.isEmpty()) {
				continue;
			}
			res.add(new Event((VEvent) each, pl));
		}
		return res;
	}

	private static Date parseDate(String date) {
		if (date == null || UNTIL_NEVER.equalsIgnoreCase(date)) {
			return null;
		}
		if (START_TODAY.equalsIgnoreCase(date)) {
			return new Date();
		}
		try {
			return DATE_FORMAT.parse(date);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Unable to parse date " + date, e);
		}
	}

	private static final String BEGIN_VTIMEZONE = "BEGIN:VTIMEZONE";
	private static final String BEGIN_DAYLIGHT = "BEGIN:DAYLIGHT";
	private static final String BEGIN_STANDARD = "BEGIN:STANDARD";
	private static final String END_DAYLIGHT = "END:DAYLIGHT";
	private static final String END_STANDARD = "END:STANDARD";
	private static final String END_VTIMEZONE = "END:VTIMEZONE";
	private static final String BEGIN_EVENT = "BEGIN:VEVENT";
	private static final String END_EVENT = "END:VEVENT";
	private static final String START_DATE = "DTSTART;";
	private static final String END_DATE = "DTEND;";
	private static final String DATE_QUAL = "DATE:";
	private static final String DATE_TIME_QUAL = "DATE-TIME:";
	private static final String TIME_ZONE = "TZID=";
	private static final Pattern DATE_TIME_PTN = Pattern.compile("^.*?(\\d{8}T\\d{6}).*?$");
	private static final Pattern DATE_ONLY_PTN = Pattern.compile("^.*?(\\d{8}).*?$");

	private static Reader correctStream(final InputStream inputStream) throws IOException {
		final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
		final StringBuilder sb = new StringBuilder(4096);

		boolean[] flags = {false, false, false, false};//[in header, in body, in dst, in event]

		String line;
		while ((line = in.readLine()) != null) {
			if (line.equals(BEGIN_VTIMEZONE)) {
				flags[0] = true;
			} else if (line.equals(END_VTIMEZONE)) {
				flags[0] = false;
			} else if (line.equals(BEGIN_DAYLIGHT) || line.equals(BEGIN_STANDARD)) {
				flags[2] = true;
			} else if (line.equals(END_DAYLIGHT) || line.equals(END_STANDARD)) {
				flags[2] = false;
			} else if (line.equals(BEGIN_EVENT)) {
				flags[3] = true;
			} else if (line.equals(END_EVENT)) {
				flags[3] = false;
			}
			if (flags[0] && flags[2]) {//in a definition of TZ for standard or DS time, check for date only and correct to date time
				final Matcher dtm = DATE_TIME_PTN.matcher(line);
				final Matcher dom = DATE_ONLY_PTN.matcher(line);
				if (line.startsWith(START_DATE) && !line.contains(DATE_TIME_QUAL) && !dtm.matches() && dom.matches()) {
						final String date = dom.group(1);
						sb.append(START_DATE);
						sb.append("VALUE=");
						sb.append(DATE_TIME_QUAL);
						sb.append(date);
						sb.append("T000000");
				} else {
					sb.append(line);
				}
			} else if (!flags[0] && flags[3]) {//in an event definition, check for date only
				final Matcher dtm = DATE_TIME_PTN.matcher(line);
				final Matcher dom = DATE_ONLY_PTN.matcher(line);
				final boolean a = line.startsWith(START_DATE);
				final boolean b = line.startsWith(END_DATE);
				if ((a || b) && !line.contains(DATE_TIME_QUAL) && !dtm.matches() && dom.matches()) {
					final String date = dom.group(1);
					sb.append(a ? START_DATE : END_DATE);
					sb.append("VALUE=");
					sb.append(DATE_TIME_QUAL);
					sb.append(date);
					sb.append("T000000");
				} else {
					sb.append(line);
				}
			} else {
				sb.append(line);
			}
			sb.append("\r\n");
		}
		return new StringReader(sb.toString());
	}
}
