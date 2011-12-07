package edu.byu.mobile.ical.rest;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

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

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return super.isWriteable(type, genericType, annotations, mediaType);
    }
}
