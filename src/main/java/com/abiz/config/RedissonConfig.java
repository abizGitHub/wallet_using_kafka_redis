package com.abiz.config;


import lombok.Getter;
import lombok.Setter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redisson")
@Setter
@Getter
public class RedissonConfig {

    private String address;
    private int connectionMinimumIdleSize;
    private int idleConnectionTimeout;
    private int pingTimeout;
    private int connectTimeout;
    private int timeout;
    private int retryAttempts;
    private int retryInterval;
    private int reconnectionTimeout;
    private int failedAttempts;
    private String password;
    private int subscriptionsPerConnection;
    private String clientName;
    private int subscriptionConnectionMinimumIdleSize;
    private int subscriptionConnectionPoolSize;
    private int connectionPoolSize;
    private int database;
    private boolean dnsMonitoring;
    private int dnsMonitoringInterval;
    private int thread;

    @Bean
    public RedissonClient redisson() throws Exception {
        System.out.println(address);
        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setConnectionPoolSize(connectionPoolSize)
                .setDatabase(database)
                .setDnsMonitoringInterval(dnsMonitoringInterval)
                .setSubscriptionConnectionMinimumIdleSize(subscriptionConnectionMinimumIdleSize)
                .setSubscriptionConnectionPoolSize(subscriptionConnectionPoolSize)
                .setSubscriptionsPerConnection(subscriptionsPerConnection)
                .setClientName(clientName)
                .setRetryAttempts(retryAttempts)
                .setRetryInterval(retryInterval)
                .setTimeout(timeout)
                .setConnectTimeout(connectTimeout)
                .setIdleConnectionTimeout(idleConnectionTimeout)
                .setPassword(password);

        return Redisson.create(config);
    }
}
