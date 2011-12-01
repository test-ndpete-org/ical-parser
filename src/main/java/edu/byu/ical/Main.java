package edu.byu.ical;

import edu.byu.mobile.ical.rest.MainEndpoint;
import edu.byu.mobile.ical.rest.TimePeriod;
import net.fortuna.ical4j.data.ParserException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author jmooreoa
 */
public class Main {
    @SuppressWarnings({"unchecked"})
    public static void main(String[] args) throws IOException, JSONException, ParserException {
        final Set<String> tagsEncountered = new HashSet<String>();
        final Set<String> extraTagsEncountered = new HashSet<String>();

        for (Calendar each : CALENDARS) {
            final long start = System.currentTimeMillis();
            FileWriter fw = new FileWriter(each.getDesc().replaceAll(" ", "_") + ".json");
            final String json = ICalToJsonConverter.convert(new URL(each.getRssUrl()).openStream());
            fw.write(json);
            fw.close();
            System.out.println("Finished " + each.getName() + " in " + (System.currentTimeMillis() - start) + "ms");
            JSONObject obj = new JSONObject(json);
            JSONArray array = obj.getJSONArray("events");
            for (int i = 0; i < array.length(); i++) {
                final JSONObject event = array.getJSONObject(i);
                for (final Iterator<String> keys = event.keys(); keys.hasNext(); ) {
                    final String key = keys.next();
                    tagsEncountered.add(key);
                    final Object sub = event.get(key);
                    if (!(sub instanceof JSONObject)) {
                        continue;
                    }
                    final JSONObject inner = (JSONObject) sub;
                    for (final Iterator<String> innerKeys = inner.keys(); innerKeys.hasNext();) {
                        extraTagsEncountered.add(String.format("%s.%s", key, innerKeys.next()));
                    }
                }
            }
            new MainEndpoint().parse(new URL(each.getRssUrl()), new TimePeriod(2, TimePeriod.TimeUnit.YEAR), null);
        }

        System.out.println("VEVENT tags encountered: " + tagsEncountered);
        System.out.println("VEVENT extra tags encountered: " + extraTagsEncountered);

    }

    private static final Calendar[] CALENDARS = {
            new Calendar("Academic", "School stuff", "http://calendar.byu.edu/calendar/ical/2"),
            new Calendar("Alumni", "BYU Alumni", "http://calendar.byu.edu/calendar/ical/1"),
            new Calendar("Conferences", "Conferences and Workshops", "http://calendar.byu.edu/calendar/ical/3"),
            new Calendar("Devotionals", "BYU Devotionals and Forums", "http://calendar.byu.edu/calendar/ical/4"),
            new Calendar("Events", "Other Campus Events", "http://calendar.byu.edu/calendar/ical/5"),
            new Calendar("Fine Arts", "BYU Theater, Choir, etc", "http://calendar.byu.edu/calendar/ical/6"),
            new Calendar("Sports", "BYU Cougar Athletics", "http://calendar.byu.edu/calendar/ical/7")
    };
}
