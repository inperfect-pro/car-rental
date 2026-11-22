package pro.inperfect.car_rental.base;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
    "spring.docker.compose.enabled=false",
    "spring.datasource.hikari.transaction-isolation=TRANSACTION_REPEATABLE_READ"
})
@AutoConfigureRestTestClient
@Import(AbstractWholeContextTest.TestBeans.class)
@ResourceLock("reservation-postgresql")
@Execution(ExecutionMode.SAME_THREAD)
public abstract class AbstractWholeContextTest {

    private static final String POSTGRES_IMAGE = "postgres:17.6-alpine";

    @TestConfiguration
    static class TestBeans {

        @Bean
        @ServiceConnection
        PostgreSQLContainer postgres() {
            return new PostgreSQLContainer(POSTGRES_IMAGE);
        }

        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneOffset.UTC);
        }
    }
}
