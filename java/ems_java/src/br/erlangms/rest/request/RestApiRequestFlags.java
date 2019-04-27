/*
 * 
 */
package br.erlangms.rest.request;

import br.erlangms.rest.exception.RestApiConstraintException;
import br.erlangms.rest.util.RestUtils;

public class RestApiRequestFlags implements IRestApiRequestFlags {
    private boolean noRequestCache = false;
    private boolean noResultCache = false;
    private boolean noCache = false;
    private boolean noNamedQuery = false;
    private boolean slowTest = false;
    private boolean logQueryCreated = false;
    private boolean logStatistics = false;

    @Override
    public boolean isNoRequestCache() {
        return noRequestCache;
    }
    @Override
    public void setNoRequestCache(boolean noRequestCache) {
        this.noRequestCache = noRequestCache;
    }
    @Override
    public boolean isNoResultCache() {
        return noResultCache;
    }
    @Override
    public void setNoResultCache(boolean noResultCache) {
        this.noResultCache = noResultCache;
    }
    @Override
    public boolean isNoCache() {
        return noCache;
    }
    @Override
    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
        if (noCache) {
            this.noRequestCache = true;
            this.noResultCache = true;
        }
    }
    @Override
    public boolean isNoNamedQuery() {
        return noNamedQuery;
    }
    @Override
    public void setNoNamedQuery(boolean noNamedQuery) {
        this.noNamedQuery = noNamedQuery;
    }
    @Override
    public boolean isSlowTest() {
        return slowTest;
    }
    @Override
    public void setSlowTest(boolean slowTest) {
        this.slowTest = slowTest;
    }
    @Override
    public boolean isLogQueryCreated() {
        return logQueryCreated;
    }
    @Override
    public void setLogQueryCreated(boolean logQueryCreated) {
        this.logQueryCreated = logQueryCreated;
    }
    @Override
    public boolean isLogStatistics() {
        return logStatistics;
    }
    @Override
    public void setLogStatistics(boolean logStatistics) {
        this.logStatistics = logStatistics;
    }

    @Override
    public void setFlagsAsString(String flags) {
        if (flags != null) {
            flags = RestUtils.removeAllSpaces(flags);
            flags = RestUtils.unquoteString(flags);
            if (!flags.isEmpty()) {
                String[] flagsList = flags.split(",");
                for (String flagName : flagsList) {
                    switch (flagName) {
                        case "no_cache":
                            setNoCache(true);
                            break;
                        case "no_result_cache":
                            setNoRequestCache(true);
                            break;
                        case "no_request_cache":
                            setNoRequestCache(true);
                            break;
                        case "no_named_query":
                            setNoNamedQuery(true);
                            break;
                        case "slow_test":
                            setSlowTest(true);
                            break;
                        case "log_query_created":
                            setLogQueryCreated(true);
                            break;
                        case "log_statistics":
                            setLogStatistics(true);
                            break;
                        default:
                            throw new RestApiConstraintException(RestApiConstraintException.OPERADOR_FLAGS_INVALIDO);
                    }
                }
            }
        }
    }
}
