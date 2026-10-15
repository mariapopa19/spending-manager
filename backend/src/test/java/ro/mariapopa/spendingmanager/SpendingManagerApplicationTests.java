package ro.mariapopa.spendingmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
class SpendingManagerApplicationTests {

    @Test
    void contextLoads() {}
}
