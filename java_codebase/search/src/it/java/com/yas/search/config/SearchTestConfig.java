package com.yas.search.config;

import com.yas.commonlibrary.IntegrationTestConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class SearchTestConfig extends IntegrationTestConfiguration {

    @Value("${elasticsearch.version}")
    private String elasticSearchVersion;

    @Bean(destroyMethod = "stop")
    @ServiceConnection
    public ElasticTestContainer elasticTestContainer() {
        return new ElasticTestContainer(elasticSearchVersion);
    }
}
