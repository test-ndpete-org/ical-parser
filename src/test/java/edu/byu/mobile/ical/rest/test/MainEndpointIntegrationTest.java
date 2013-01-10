package edu.byu.mobile.ical.rest.test;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.TestConstants;
import com.sun.jersey.test.framework.util.ApplicationDescriptor;
import edu.byu.mobile.ical.rest.Event;
import edu.byu.mobile.ical.rest.MainEndpoint;
import edu.byu.mobile.ical.rest.TimePeriod;
import edu.byu.mobile.ical.rest.TimeSpan;
import net.fortuna.ical4j.data.CalendarOutputter;
import org.apache.catalina.logger.SystemOutLogger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

import java.io.*;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jmooreoa
 */
public class MainEndpointIntegrationTest extends JerseyTest {

    public MainEndpointIntegrationTest() throws Exception {
        contextPath = "/ical-test";
        servletPath = "/rest";
        ApplicationDescriptor ad = new ApplicationDescriptor()
                .setRootResourcePackageName(MainEndpoint.class.getPackage().getName())
                .setContextPath(contextPath)
                .setServletPath(servletPath)
                .setContextParams(getContextParams())
                .setServletInitParams(getInitParams());
        setupTestEnvironment(ad);
    }

    private static Map<String, String> getInitParams() {
        return Collections.emptyMap();
    }

    private static Map<String, String> getContextParams() {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("disable-cache", "true");
        params.put("log4jExposeWebAppRoot", "false");
        params.put("log4jConfigLocation", "log4j.properties");
        return params;
    }

    private static TestEventCreator.TestData oneShots;
    private static TestEventCreator.TestData recurring;

    private static File oneShotFile;
    private static File recurringFile;

    @BeforeClass
    public static void generateTestData() throws Exception {
        oneShots = TestEventCreator.generateOneShotEvents();
        recurring = TestEventCreator.generateRecurringEvents();

        oneShotFile = File.createTempFile("ical-test", ".ics");
        oneShotFile.deleteOnExit();

        recurringFile = File.createTempFile("ical-test", ".ics");
        recurringFile.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(oneShotFile);

        CalendarOutputter co = new CalendarOutputter();
        co.output(oneShots.getCalendar(), fos);

        fos.close();

        fos = new FileOutputStream(recurringFile);
        co.output(recurring.getCalendar(), fos);

        fos.close();
    }

    @Test
    public void testOneShots() throws Exception {
        testOneShot(TimePeriod.ONE_DAY);
        testOneShot(TimePeriod.ONE_WEEK);
        testOneShot(TimePeriod.ONE_MONTH);
        testOneShot(TimePeriod.ONE_YEAR);
    }

    @Test
    public void testRecurrence() throws Exception {
        Calendar cal = Calendar.getInstance();
        clearTimePortion(cal);
        for (int i = 0; i <= 52; i++) {
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            testRecurring(cal.getTime());
        }
    }

    @Test
    public void testAgainstRealCal() throws UnsupportedEncodingException, JSONException, ParseException {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        clearTimePortion(cal);

        final Date start = cal.getTime();
        clearTimePortion(cal);
        cal.add(Calendar.MONTH, 3);
        final Date end = cal.getTime();

        final String content = webResource
                .path("/parse")
                .queryParam("feedUrl", URLEncoder.encode("http://calendar.byu.edu/calendar/ical/2", "UTF-8"))
                .queryParam("until", new SimpleDateFormat("yyyy-MM-dd").format(end))
                .get(String.class);

        final JSONArray array = new JSONArray(content);
        for (int i = 0; i < array.length(); i++) {
            final JSONObject object = array.getJSONObject(i);

            final JSONArray occurrences = object.getJSONArray("occurrences");
            for (int j = 0; j < occurrences.length(); j++) {
                final JSONObject obj = occurrences.getJSONObject(j);
                final TimeSpan ts = new TimeSpan(optDate(obj, "start"), optDate(obj, "end"), obj.getBoolean("allDay"));
                assertTrue(ts.fits(start, end));
            }
        }
    }

    protected void testOneShot(TimePeriod howLong) throws Exception {
        final String content = webResource
                .path("/parse")
                .queryParam("feedUrl", URLEncoder.encode(oneShotFile.toURI().toString(), "UTF-8"))
                .queryParam("show", howLong.toString()).get(String.class);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        clearTimePortion(cal);

        final Date start = cal.getTime();

        cal.add(howLong.getUnit().getCalendarField(), howLong.getDuration());

        final Date end = cal.getTime();

        assertEventsEqual(oneShots.getEvents(), new JSONArray(content), start, end);
    }

    protected void testRecurring(Date finalRecurrence) throws Exception {
        final String content = webResource
                .path("/parse")
                .queryParam("feedUrl", URLEncoder.encode(recurringFile.toURI().toString(), "UTF-8"))
                .queryParam("until", new SimpleDateFormat("yyyy-MM-dd").format(finalRecurrence))
                .get(String.class);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        clearTimePortion(cal);

        final Date start = cal.getTime();

        cal.setTime(finalRecurrence);
        clearTimePortion(cal);

        assertEventsEqual(recurring.getEvents(), new JSONArray(content), start, cal.getTime());
    }

    private static void clearTimePortion(Calendar cal) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
    }

    protected void assertEventsEqual(List<Event> events, JSONArray array, Date start, Date end) throws JSONException, ParseException {
        final Set<Event> withinBounds = new HashSet<Event>();
        for (Event each : events) {
            Event copied = new Event(each, start, end);
            if (!copied.getOccurrences().isEmpty()) {
                withinBounds.add(copied);
            }
        }

        assertEquals(withinBounds.size(), array.length());
        for (int i = 0; i < array.length(); i++) {
            final JSONObject each = array.getJSONObject(i);
            final Event found = findMatchingEvent(withinBounds, each.getString("uid"));
            assertNotNull(found);
            assertEventEquals(found, each);
        }
    }

    private static void assertEventEquals(Event event, JSONObject json) throws JSONException, ParseException {
        assertEquals(event.getUid(), nullString(json, "uid"));
        assertEquals(event.getDescription(), nullString(json, "description"));
        assertEquals(event.getLocation(), nullString(json, "location"));
        assertDateEquals(event.getStartDate(), optDate(json, "startDate"));
        assertEquals(event.getSummary(), nullString(json, "summary"));
        assertEquals(event.getUrl(), nullString(json, "url"));
        assertDateEquals(event.getEndDate(), optDate(json, "endDate"));
        assertEquals(event.isAllDay(), json.optBoolean("allDay"));
		//TODO: Add checking of repeat rule here
//        assertEquals(event.getRepeatRule(), nullString(json, "repeatRule"));
//        assertEquals(event.getRepeatExceptionDate(), nullString(json, "repeatExceptionDate"));
        assertDateEquals(event.getModified(), optDate(json, "modified"));
        assertEquals(event.getOccurrences().size(), json.getJSONArray("occurrences").length());
    }

    private static Date optDate(JSONObject json, String key) throws ParseException {
        final String raw = nullString(json, key);
        if (raw == null) {
            return null;
        }
        return DF.parse(raw);
    }

	private static String nullString(JSONObject object, String key) {
		return nullString(object, key, null);
	}

	private static String nullString(JSONObject object, String key, String def) {
		if (object.isNull(key) || !object.has(key)) {
			return def;
		}
		return object.optString(key, def);
	}


    private static void assertDateEquals(Date d1, Date d2) {
        if (d1 == null && d2 == null) {
            return;
        }
        assertTrue(d1 != null && d2 != null);
        assertEquals(0, d1.compareTo(d2));
    }

    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static Event findMatchingEvent(Set<Event> withinBounds, String uid) {
        assertNotNull(withinBounds);
        assertNotNull(uid);
        for (Event each : withinBounds) {
            if (uid.equals(each.getUid())) {
                return each;
            }
        }
        return null;
    }

    @AfterClass
    public static void clearTestData() {
        oneShots = null;
        recurring = null;
        if (oneShotFile != null)
            oneShotFile.delete();
        if (recurringFile != null)
            recurringFile.delete();
    }

    @Override
    protected String getContainerType() {
        return TestConstants.GRIZZLY_WEB_CONTAINER;
    }
}
