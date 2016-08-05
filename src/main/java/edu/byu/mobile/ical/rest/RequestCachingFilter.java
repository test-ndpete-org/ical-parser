package edu.byu.mobile.ical.rest;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.constructs.web.filter.SimpleCachingHeadersPageCachingFilter;

import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jmooreoa
 */
public class RequestCachingFilter extends SimpleCachingHeadersPageCachingFilter {
	public static final String SKIP_CACHE_PARAM_KEY = "skipCacheParam";
	public static final String DEFAULT_SKIP_CACHE_PARAM = "skip-cache";

	private String skipCacheParam;

	private boolean disableCache;

	@Override
	public void doInit(FilterConfig filterConfig) throws CacheException {
		super.doInit(filterConfig);
		skipCacheParam = filterConfig.getInitParameter(SKIP_CACHE_PARAM_KEY);
		if (skipCacheParam == null || skipCacheParam.isEmpty()) {
			skipCacheParam = DEFAULT_SKIP_CACHE_PARAM;
		}
		disableCache = "true".equalsIgnoreCase(filterConfig.getServletContext().getInitParameter("disable-cache"));
	}

	@Override
	protected boolean filterNotDisabled(HttpServletRequest httpRequest) {
		return super.filterNotDisabled(httpRequest) && !shouldSkip(httpRequest);
	}

    private boolean shouldSkip(HttpServletRequest request) {
        final String skip = request.getParameter(skipCacheParam);
        return disableCache
				|| !request.getMethod().equalsIgnoreCase("get")
				|| skip != null && ("true".equalsIgnoreCase(skip)
				|| "yes".equalsIgnoreCase(skip));
    }

    @Override
    public void doDestroy() {
		super.doDestroy();
        skipCacheParam = null;
    }
}
