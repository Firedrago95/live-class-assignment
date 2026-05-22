package com.liveclass.notification.infrastructure.persistence;

import com.liveclass.notification.TestcontainersConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AbstractRepositoryTest {

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", TestcontainersConfiguration.postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", TestcontainersConfiguration.postgresContainer::getUsername);
        registry.add("spring.datasource.password", TestcontainersConfiguration.postgresContainer::getPassword);
    }
}
