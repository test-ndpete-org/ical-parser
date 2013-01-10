package edu.byu.mobile.ical.rest.test;

import edu.byu.mobile.ical.rest.Event;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.UidGenerator;

import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author jmooreoa
 */
public abstract class TestEventCreator {

    private static final long LATEST_EVENT = 63072000000L + System.currentTimeMillis();

    private TestEventCreator() {
    }

    public static TestData generateOneShotEvents() throws Exception {
        return generate(new EventGenerator() {
            @Override
            public List<VEvent> generate() throws Exception {
                java.util.Calendar cal = java.util.Calendar.getInstance();

                final Random random = new Random();
                final List<VEvent> events = new LinkedList<VEvent>();
                for (int i = 0; i < 70; i++) {
                    cal.add(java.util.Calendar.WEEK_OF_YEAR, i);
                    cal.set(java.util.Calendar.DAY_OF_WEEK, random.nextInt(7));
                    VEvent event = generateOneShotEvent(cal.getTime());
                    events.add(event);

                    cal.set(java.util.Calendar.DAY_OF_WEEK, random.nextInt(7));
                    events.add(generateAllDayOneShotEvent(cal.getTime()));
                }
                return events;
            }
        });
    }

    public static TestData generateRecurringEvents() throws Exception {
        return generate(new EventGenerator() {
            @Override
            public List<VEvent> generate() throws Exception {
                final Random random = new Random();
                Recur rule = new Recur(Recur.WEEKLY, new net.fortuna.ical4j.model.Date(LATEST_EVENT));
                final List<Date> dates = new LinkedList<Date>();
                final DateList dl = rule.getDates(new net.fortuna.ical4j.model.Date(System.currentTimeMillis()), new net.fortuna.ical4j.model.Date(LATEST_EVENT), Value.DATE);
                int i = 0;

                while (i < 7) {
                    Date d = (Date) dl.get(random.nextInt(dl.size()));
                    if (dates.contains(d)) {
                        continue;
                    }
                    dates.add(d);
                    i++;
                }

                return Arrays.asList(
                        generateRecurringEvent(
                                new Date(),
                                rule,
                                dates.toArray(new Date[7])
                        ),
                        generateAllDayRecurringEvent(
                                new Date(),
                                rule,
                                dates.toArray(new Date[7])
                        )
                );
            }
        });
    }

    public static VEvent generateRecurringEvent(Date start, Recur rule, Date... exceptions) throws URISyntaxException, UnsupportedEncodingException {
        VEvent result = generateOneShotEvent(start);

        result.getProperties().add(new RRule(rule));
        final DateList ex = new DateList();
        for (final Date each : exceptions) {
            ex.add(new net.fortuna.ical4j.model.Date(each));
        }
        result.getProperties().add(new ExDate(ex));
        return result;
    }

    private static VEvent generateAllDayRecurringEvent(Date start, Recur rule, Date... exceptions) throws URISyntaxException, UnsupportedEncodingException {
        VEvent event = generateRecurringEvent(start, rule, exceptions);
        event.getStartDate().setDate(new net.fortuna.ical4j.model.Date(start));
        event.getProperties().remove(event.getEndDate());

        return event;
    }

    private static VEvent generateOneShotEvent(Date day) throws URISyntaxException, UnsupportedEncodingException {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(day);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 6);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        DateTime start = new DateTime(cal.getTime());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 12);
        DateTime end = new DateTime(cal.getTime());

        Uid uid = uidGenerator.generateUid();
        final String uidString = uid.getValue();

        VEvent result = new VEvent(start, end, uidString);
        PropertyList pl = result.getProperties();
        pl.add(uid);
        pl.add(new Description(uidString));
        pl.add(new Location(uidString));
        pl.add(new Url(new URI("http://www.example.com/uid/" + URLEncoder.encode(uidString, "UTF-8"))));

        return result;
    }

    private static VEvent generateAllDayOneShotEvent(Date day) throws URISyntaxException, UnsupportedEncodingException {
        VEvent event = generateOneShotEvent(day);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(day);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        event.getStartDate().setDate(new DateTime(cal.getTime()));
        cal.add(java.util.Calendar.DATE, 1);
        cal.add(java.util.Calendar.SECOND, -1);
        event.getEndDate().setDate(new DateTime(cal.getTime()));

        return event;
    }

    private static TestData generate(EventGenerator gen) throws Exception {
        Calendar iCal = new Calendar();
        iCal.getProperties().add(new ProdId("-//Test Calendar//ical4j 1.0//EN"));
        iCal.getProperties().add(CalScale.GREGORIAN);
        iCal.getProperties().add(Version.VERSION_2_0);

        TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();

        net.fortuna.ical4j.model.TimeZone tz = registry.getTimeZone(TimeZone.getDefault().getID());

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        final Period recurrence = new Period(new DateTime(cal.getTime()), new DateTime(LATEST_EVENT));
        final List<VEvent> vEvents = gen.generate();
        final List<Event> events = new LinkedList<Event>();

        for (VEvent each : vEvents) {
            if (each.getStartDate().getDate() instanceof DateTime)
                each.getStartDate().setTimeZone(tz);
            iCal.getComponents().add(each);
            events.add(new Event(each, each.calculateRecurrenceSet(recurrence)));
        }

        return new TestData(iCal, events);
    }

    private static interface EventGenerator {
        List<VEvent> generate() throws Exception;
    }

    public static class TestData {
        private final Calendar calendar;
        private final List<Event> events;

        public TestData(Calendar calendar, List<Event> events) {
            this.calendar = calendar;
            this.events = events;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public List<Event> getEvents() {
            return events;
        }
    }

    private static final UidGenerator uidGenerator;

    static {
        try {
            uidGenerator = new UidGenerator("1");
        } catch (SocketException e) {
            throw new IllegalStateException(e);
        }
    }
}
