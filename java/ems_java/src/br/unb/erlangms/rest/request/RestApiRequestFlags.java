package br.unb.erlangms.rest.request;

import br.unb.erlangms.rest.exception.RestApiException;
import br.unb.erlangms.rest.schema.RestJoinType;
import br.unb.erlangms.rest.util.RestUtils;

public class RestApiRequestFlags implements IRestApiRequestFlags {
    private static final long serialVersionUID = 8317999742928233820L;

    // true by default
    private boolean logRequest = true;
    private boolean logQuery = true;

    // false by default
    private boolean noRequestCache = false;
    private boolean noResultCache = false;
    private boolean noNamedQuery = false;
    private boolean noCache = false;        // todos os caches desabilitado incluindo named query
    private boolean slowTest = false;
    private boolean allFields = false;
    private boolean noPaginate = false;

    private RestJoinType fetchJoin = null;

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
        this.noRequestCache = noCache;
        this.noResultCache = noCache;
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
    public boolean isLogQuery() {
        return logQuery;
    }

    @Override
    public void setLogQuery(boolean logQueryCreated) {
        this.logQuery = logQueryCreated;
    }

    @Override
    public boolean isLogRequest() {
        return logRequest;
    }

    @Override
    public void setLogRequest(boolean logStatistics) {
        this.logRequest = logStatistics;
    }

    @Override
    public boolean isAllFields() {
        return allFields;
    }

    @Override
    public void setAllFields(boolean allFields) {
        this.allFields = allFields;
    }

    @Override
    public boolean isNoPaginate() {
        return this.noPaginate;
    }
    @Override
    public void setNoPaginate(boolean noLimit) {
        this.noPaginate = noLimit;
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
                        case "log_query":
                            setLogQuery(true);
                            break;
                        case "log_request":
                            setLogRequest(true);
                            break;
                        case "all_fields":
                            setAllFields(true);
                            break;
                        case "no_paginate":
                        case "no_limit":
                            setNoPaginate(true);
                            break;
                        case "leftjoin":
                        case "left_join":
                            setJoinType(RestJoinType.LEFT_JOIN);
                            break;
                        case "rightjoin":
                        case "right_join":
                            setJoinType(RestJoinType.RIGHT_JOIN);
                            break;
                        case "join":
                            setJoinType(RestJoinType.JOIN);
                            break;
                        case "nojoin":
                        case "no_join":
                            setJoinType(RestJoinType.NO_JOIN);
                            break;
                        default:
                            throw new RestApiException(RestApiException.OPERADOR_FLAGS_INVALIDO);
                    }
                }
            }
        }
    }

    @Override
    public void setJoinType(RestJoinType fetchJoin) {
        this.fetchJoin = fetchJoin;
    }

    @Override
    public RestJoinType getJoinType() {
        return fetchJoin;
    }
}
