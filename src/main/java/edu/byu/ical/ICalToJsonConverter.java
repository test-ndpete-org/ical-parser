package edu.byu.ical;

import org.json.JSONException;
import org.json.JSONStringer;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jmooreoa
 */
public abstract class ICalToJsonConverter {
    private ICalToJsonConverter() {}

    public static String convert(InputStream ical) throws IOException, JSONException {

        final BufferedReader br = new BufferedReader(new InputStreamReader(ical));

        final JSONStringer writer = new JSONStringer();

        boolean inEvents = false;
        String line;
        while ((line = br.readLine()) != null) {
            if ("BEGIN:VCALENDAR".equals(line)) {
                writer.object();
            } else if ("END:VCALENDAR".equals(line)) {
                writer.endArray();//end the VEVENT array
                writer.endObject();
                break;
            } else if ("BEGIN:VEVENT".equals(line)) {
                if (!inEvents) {
                    writer.key("events");
                    writer.array();
                }
                inEvents = true;
                writer.object();
            } else if (line.startsWith("BEGIN:")) {
                writer.key(line.substring(6));
                writer.object();
            } else if (line.startsWith("END:")) {
                writer.endObject();
            } else {
                final String[] split = line.split(":", 2);
                final String key = split[0];
                final String value = split[1];

                if (key.contains(";")) {
                    final String[] splitKey = key.split(";");
                    final String realKey = splitKey[0].toLowerCase();
                    writer.key(realKey);
                    writer.object();
                    writer.key("value");
                    writeTagValue(writer, value);

                    for (int i = 1; i < splitKey.length; i++) {
                        final String[] splitExtra = splitKey[i].split("=", 2);
                        String extraKey = splitExtra[0];
                        if ("VALUE".equals(extraKey)) {
                            extraKey = "value-type";
                        }
                        writer.key(extraKey.toLowerCase());

                        writeTagValue(writer, splitExtra[1]);
                    }

                    writer.endObject();
                } else {
                    writer.key(key.toLowerCase());
                    writeTagValue(writer, value);
                }
            }
        }
        return writer.toString();
    }

    private static final Pattern KEY_VALUE_PAIR = Pattern.compile("([A-Z]+)=([A-Z0-9]+)(?:;)?");
    private static final Pattern LIST_OF_KEY_VALUE_PAIRS = Pattern.compile(String.format("(%s)+", KEY_VALUE_PAIR.pattern()));

    private static void writeTagValue(JSONStringer writer, String value) throws JSONException {
        final Matcher whole = LIST_OF_KEY_VALUE_PAIRS.matcher(value);
        if (!whole.matches()) {
            writer.value(clean(value));
        } else {
            writer.object();
            writer.key("raw-value").value(clean(value));
            final Matcher m = KEY_VALUE_PAIR.matcher(value);
            while (m.find()) {
                writer.key(m.group(1)).value(clean(m.group(2)));
            }
            writer.endObject();
        }
    }

    private static String clean(String value) {
        return value.replaceAll("\\\\", "");
    }
}
