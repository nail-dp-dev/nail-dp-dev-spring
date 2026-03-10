package com.backend.naildp.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;

@TestConfiguration
@PropertySource(value = "file:${user.dir}/local.env", ignoreResourceNotFound = true)
public class TestEnvConfig {

}
