package edu.byu.mobile.ical.rest;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.ext.Provider;

/**
 * @author jmooreoa
 */
@Provider
public class JsonProvider extends JacksonJsonProvider {
    public JsonProvider() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        setMapper(mapper);
    }
}
