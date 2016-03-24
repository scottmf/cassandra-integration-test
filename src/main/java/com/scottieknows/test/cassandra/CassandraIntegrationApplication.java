package com.scottieknows.test.cassandra;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.datastax.driver.core.Cluster;

@SpringBootApplication
public class CassandraIntegrationApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(CassandraIntegrationApplication.class, args);
    }
}
