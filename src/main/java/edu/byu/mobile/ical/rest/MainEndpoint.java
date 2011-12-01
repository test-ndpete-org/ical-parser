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

/**
 * @author jmooreoa
 */
@Path("/")
@Produces("application/json")
public class MainEndpoint {

    private static final Logger LOG = Logger.getLogger(MainEndpoint.class);

    private static final String UNTIL_NEVER = "never";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static final String DEFAULT_SHOW = "3MO";

    public static final TimePeriod DEFAULT_SHOW_VALUE = TimePeriod.valueOf(DEFAULT_SHOW);

    @SuppressWarnings({"unchecked"})
    @GET
    public List<Event> parse(
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
            throw new WebApplicationException(e);
        }

        final Date startDate = calculateStartDate();

        return filterComponents(calendar.getComponents(), startDate, calculateEndDate(startDate, show, until));
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
