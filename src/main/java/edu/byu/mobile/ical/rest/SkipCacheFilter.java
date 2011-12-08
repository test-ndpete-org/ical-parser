package edu.byu.mobile.ical.rest;

import javax.servlet.*;
import net.sf.ehcache.constructs.web.filter.Filter;

import java.io.IOException;

/**
 * @author jmooreoa
 */
public class SkipCacheFilter implements javax.servlet.Filter {
    public static final String SKIP_CACHE_PARAM_KEY = "skipCacheParam";
    public static final String DEFAULT_SKIP_CACHE_PARAM = "skip-cache";

    private String skipCacheParam;

    private boolean disableCache;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        skipCacheParam = filterConfig.getInitParameter(SKIP_CACHE_PARAM_KEY);
        if (skipCacheParam == null || skipCacheParam.isEmpty()) {
            skipCacheParam = DEFAULT_SKIP_CACHE_PARAM;
        }
        disableCache = "true".equalsIgnoreCase(filterConfig.getServletContext().getInitParameter("disable-cache"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (shouldSkip(request)) {
            request.setAttribute(Filter.NO_FILTER, Boolean.TRUE);
        }

        chain.doFilter(request, response);
    }

    private boolean shouldSkip(ServletRequest request) {
        final String skip = request.getParameter(skipCacheParam);
        return disableCache || skip != null && ("true".equalsIgnoreCase(skip) || "yes".equalsIgnoreCase(skip));
    }

    @Override
    public void destroy() {
        skipCacheParam = null;
    }
}
