package edu.byu.mobile.ical.rest;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.ws.rs.ext.Provider;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author jmooreoa
 */
@Provider
public class JsonProvider extends JacksonJsonProvider {
    public JsonProvider() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		df.setTimeZone(TimeZone.getTimeZone("America/Denver"));
		mapper.getSerializationConfig().setDateFormat(df);
        setMapper(mapper);
    }
}
