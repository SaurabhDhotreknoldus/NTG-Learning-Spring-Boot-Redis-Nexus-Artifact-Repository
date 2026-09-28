package com.nashtech.learning.redisapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private long employeeTtlSeconds = 60;
    private long listTtlSeconds = 30;

    public long getEmployeeTtlSeconds() {
        return employeeTtlSeconds;
    }

    public void setEmployeeTtlSeconds(long employeeTtlSeconds) {
        this.employeeTtlSeconds = employeeTtlSeconds;
    }

    public long getListTtlSeconds() {
        return listTtlSeconds;
    }

    public void setListTtlSeconds(long listTtlSeconds) {
        this.listTtlSeconds = listTtlSeconds;
    }
}
