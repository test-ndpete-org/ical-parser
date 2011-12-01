package edu.byu.mobile.ical.rest;

import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jmooreoa
 */
public class IcalApplication extends Application{
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(MainEndpoint.class);
//        classes.add(JacksonJsonProvider.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> res = new HashSet<Object>(super.getSingletons());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);

        final JacksonJsonProvider jsonProvider = new JacksonJsonProvider(mapper);

        res.add(jsonProvider);

        return res;
    }
}
