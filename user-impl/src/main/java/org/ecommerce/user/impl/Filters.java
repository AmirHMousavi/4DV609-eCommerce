package org.ecommerce.user.impl;

import play.mvc.EssentialFilter;
import play.filters.hosts.AllowedHostsFilter;
import play.http.DefaultHttpFilters;

import javax.inject.Inject;

public class Filters extends DefaultHttpFilters {
    @Inject public Filters(AllowedHostsFilter allowedHostsFilter) {
        super(allowedHostsFilter);
    }
}