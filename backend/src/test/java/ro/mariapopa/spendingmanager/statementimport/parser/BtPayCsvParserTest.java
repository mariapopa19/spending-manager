package ro.mariapopa.spendingmanager.statementimport.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ro.mariapopa.spendingmanager.transaction.Source;

class BtPayCsvParserTest {
  private final BtPayCsvParser parser = new BtPayCsvParser();

  private static final String[] PREAMBLE = {
    "Tranzactii,,,,,,",
    "Criterii de cautare,,,,,,",
    "Numar cont:,RO49BTRLRONCRT0123456789 RON,,,,,",
    "Perioada:,01.06.2026-30.06.2026,,,,,",
    ",,,,,,",
    ",,,,,,",
    "Cont,,,,,,",
    "Utilizator:,POPA MARIA,,,,,",
    "Client:,POPA MARIA,,,,,",
    "Numar cont:,RO49BTRLRONCRT0123456789 RON,,,,,",
    "Gasite:,108 Tranzactii,,,,,",
    "Rezultat cautare,,,,,,"
  };

  private static final String HEADER =
      "Data procesarii,Data tranzacției,Descriere,Referința tranzacției,Debit,Credit,Suma";
  private static final String MANGLED_HEADER =
      "Data procesarii,Data tranzac?iei,Descriere,Referin?a tranzac?ie,Debit,Credit,Suma";
  private static final String ROUND_UP_ROW =
      "30.06.2026,29.06.2026,Round Up;EPOS 30/06/2026 0640F2201,064POSI,1.16,,\"1,128.00\"";
  private static final String POS_ROW =
      "30.06.2026,29.06.2026,Plata la POS;EPOS 30/06/2026 0640F22,064POSI,138.84,,989.16";
  private static final String CREDIT_ROW =
      "30.06.2026,29.06.2026,P2P BTP - Transfer din cont 1442,043P2P,,220.00,\"1,209.16\"";

  @Test
  @DisplayName("the parser reports BT Pay as its source")
  void source_returnsRevolut() {
    assertThat(parser.source()).isEqualTo(Source.BT_PAY);
  }

  @Test
  void debitRow_returnsNegativeAmount() {}
}
