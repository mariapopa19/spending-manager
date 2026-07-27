package ro.mariapopa.spendingmanager.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest
@AutoConfigureRestTestClient
public class TransactionControllerTest {
  @Autowired RestTestClient restTestClient;

  @Test
  void whenUnknownSource_thenReturn400() {
    // raw String — the typed record CAN'T carry "BANANA" (the enum forbids it at compile time)
    String body =
        """
        {
        "date":"2026-06-01",
        "amount":-12.50,
        "currency":"RON",
         "description":"x",
         "personId":1,
         "source":"BANANA"
         }
        """;
    restTestClient
        .post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()
        .expectStatus()
        .isBadRequest();
  }

  @Test
  void whenPersonNotFound_thenReturn404() {
    TransactionRequest request =
        new TransactionRequest(
            LocalDate.of(2026, 6, 1),
            new BigDecimal("-12.50"),
            "RON",
            "lunch",
            null,
            null,
            999_999L,
            Source.REVOLUT);

    restTestClient
        .post()
        .uri("/api/transactions")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .exchange()
        .expectStatus()
        .isNotFound();
  }

  @Test
  void whenTransactionNotFound_thenReturn404() {
    restTestClient.get().uri("/api/transactions/9999").exchange().expectStatus().isNotFound();
  }
}
