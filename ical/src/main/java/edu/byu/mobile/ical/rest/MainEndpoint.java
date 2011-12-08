package edu.byu.mobile.ical.rest;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

@Path("/parse")
@Produces({"application/json", "text/json"})
public class MainEndpoint {

    private static final Logger LOG = Logger.getLogger(MainEndpoint.class);

    private static final String UNTIL_NEVER = "never";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final String DEFAULT_SHOW = "3MO";

    public static final TimePeriod DEFAULT_SHOW_VALUE = TimePeriod.valueOf(DEFAULT_SHOW);

	/**
	 *
	 * Parses the given iCal feed and returns it as an array of JSON events.<br /><br />
	 *
	 * <h3>Parameters</h3>
	 * 'feedUrl' is required.<br />
	 * 'show' and 'until' are both optional.  If neither is specified, 'show' defaults to '3MO'.
	 * If 'until' is specified, the value of 'show' is ignored.<br />
	 *<br />
	 * 'show' must be in the form of a positive integer, followed by one of the following:
	 * <dl>
	 *     <li>D (day)</li>
	 *     <li>WK (week)</li>
	 *     <li>MO (month)</li>
	 *     <li>Y (year)</li>
	 * </dl>
	 * Examples:<br />
	 * <table>
	 *     <thead><tr>
	 *         <th>Value</th><th>Meaning</th>
	 *     </tr></thead>
	 *     <tr><td>12D</td><td>Twelve Days</td></tr>
	 *     <tr><td>3WK</td><td>Three Weeks</td></tr>
	 *     <tr><td>3MO</td><td>Three Months</td></tr>
	 *     <tr><td>2Y</td><td>Two Years</td></tr>
	 * </table>
	 *<br />
	 * 'until' must be a date, in the form 'yyyy-MM-dd'.<br /><br />
	 *
	 * <h3>A Note about Caching</h3>
	 * Please note that all responses are cached by the server. To bypass the cache, add the request param 'skip-cache'<br />
	 * with a value of 'true' or 'yes'.<br /><br />
	 *
	 * <h3>GZIP</h3>
	 * This service is capable of returning responses with GZIP compression. It is highly recommended that GZIP encoding
	 * be used, as it can yield substantial reductions in file size.<br />
	 * To receive GZIP-compressed responses, include the 'Accept-Encoding: gzip' header in your request.<br /><br />
	 *
	 * <h3>Example Usage:</h3>
	 *
	 * <a href="./rest/v1/parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&show=4MO" target="_blank">
	 *     parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&show=4MO
	 * </a>
	 * shows the parsed BYU Academic calendar feed for the next four months.<br />
	 *
	 *
	 * <a href="./rest/v1/parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&show=4MO" target="_blank">
	 *     parse?feedUrl=http%3A%2F%2Fcalendar.byu.edu%2Fcalendar%2Fical%2F2&until=2012-12-21
	 * </a>
	 * shows the same calendar until Dec. 21, 2012<br /><br />
	 *
	 * <h3>Response Format</h3>
	 * The response is in the form of a JSON Array, similar to the following:
	 * <br />
	 * <pre>[
	     {
	 	      "uid":"000000013300@calendar.byu.edu",
	 	      "allDay":false,
	 	      "endDate":"2012-01-03T06:00:00.000+0000",
	 	      "repeatRule":"FREQ=YEARLY;INTERVAL=1",
	 	      "repeatExceptionDate":null,
	 	      "modified":null,
	 	      "occurrences":[
	 		      {
	 			      "allDay":false,
	 			      "end":"2012-01-03T06:00:00.000+0000",
	 			      "start":"2012-01-02T07:00:00.000+0000"
	 		      }
	 	      ],
	 	      "description":null,
	 	      "location":null,
	 	      "url":"http://calendar.byu.edu/content/new-years-day-holiday",
	 	      "summary":"New Year's Day Holiday",
	 	      "startDate":"2012-01-02T07:00:00.000+0000"
	     }
	  ]</pre>

	 * <h4>Description of fields:</h4>
	 * <table>
	 *     <tr><th>name</th><th>type</th><th>nullable?</th><th>description</th></tr>
	 *     <tr><td>uid</td><td>string</td><td>no</td><td>the iCal uid of this event</td></tr>
	 *     <tr><td>allDay</td><td>boolean</td><td>no</td><td>true if the event is an all-day event.</td></tr>
	 *     <tr><td>endDate</td><td>date</td><td>no</td><td>end date/time of the first occurrence this event</td></tr>
	 *     <tr><td>repeatRule</td><td>string</td><td>yes</td><td>An exact copy of the contents of the iCal event's 'RRULE' field</td></tr>
	 *     <tr><td>repeatExceptionDate</td><td>string</td><td>yes</td><td>An exact copy of the contents of the iCal event's 'EXDATE' field</td></tr>
	 *     <tr><td>modified</td><td>date</td><td>yes</td><td>the date that this event was last modified</td></tr>
	 *     <tr><td>occurrences</td><td>array</td><td>no</td>
	 *         <td>
	 *             An array of all occurrences between the date of the request and the specified 'show' or 'until' value.<br />
	 *             Contains the following fields:
	 *             <ul>
	 *                 <li>allDay: corresponds to the allDay field on the main event object</li>
	 *                 <li>end: date/time the occurrence ends</li>
	 *                 <li>start: date/time the occurrence begins</li>
	 *             </ul>
	 *         </td>
	 *     </tr>
	 *     <tr><td>description</td><td>string</td><td>yes</td><td>description of the event</td></tr>
	 *     <tr><td>location</td><td>string</td><td>yes</td><td>location of the event</td></tr>
	 *     <tr><td>url</td><td>string</td><td>yes</td><td>a url containing more information about the event</td></tr>
	 *     <tr><td>summary</td><td>string</td><td>yes</td><td>summary/name of the event</td></tr>
	 *     <tr><td>startDAte</td><td>date</td><td>no</td><td>start date/time of the first occurrence of the event</td></tr>
	 * </table>
	 *<br />
	 * All dates are in ISO-8601 notation, in which the beginning of the Unix Epoch would be formatted "1970-01-01T00:00:00.000+0000".
	 *
	 * @param feedUrl the URL of the feed to parse.
	 * @param show period for which to calculate occurrences
	 * @param until date until which occurrences will be calculated
	 * @return all events occurring within the specified time frame
	 */
    @SuppressWarnings({"unchecked"})
    @GET
    public Event[] parse(
            @QueryParam("feedUrl") URL feedUrl,
            @QueryParam("show") @DefaultValue(DEFAULT_SHOW) TimePeriod show,
            @QueryParam("until") @DefaultValue(UNTIL_NEVER) String until
    ) {
        LOG.debug(String.format("Got request for calendar at '%s' with show = '%s' and until = '%s'", feedUrl, show, until));
        final CalendarBuilder builder = new CalendarBuilder();

        final Calendar calendar;
        try {
            calendar = builder.build(feedUrl.openStream());
        } catch (IOException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (ParserException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        final Date startDate = calculateStartDate();

        final List<Event> events = filterComponents(calendar.getComponents(), startDate, calculateEndDate(startDate, show, until));

        return events.toArray(new Event[events.size()]);
    }

    private static Date calculateStartDate() {
        final java.util.Calendar startCal = java.util.Calendar.getInstance();
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
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse date " + date, e);
        }
    }
}
