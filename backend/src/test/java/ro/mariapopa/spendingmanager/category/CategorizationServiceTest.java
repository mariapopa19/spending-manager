package ro.mariapopa.spendingmanager.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

public class CategorizationServiceTest {
  private CategoryRule rule(long id, String pattern, String categoryName) {
    Category c = new Category();
    c.setName(categoryName);
    CategoryRule r = new CategoryRule();
    r.setId(id);
    r.setPattern(pattern);
    r.setCategory(c);
    return r;
  }

  @Test
  void matchesCaseInsensitively() {
    var rules = List.of(rule(1, "mega image", "Mâncare"));
    assertEquals(
        "Mâncare",
        CategorizationService.match(rules, "CARD MEGA IMAGE 123").orElseThrow().getName());
  }

  @Test
  void longestPatternWins() {
    var rules = List.of(rule(1, "mega", "Mâncare"), rule(2, "mega image online", "Lifestyle"));
    assertEquals(
        "Lifestyle",
        CategorizationService.match(rules, "MEGA IMAGE ONLINE").orElseThrow().getName());
  }

  @Test
  void noMatchesReturnEmpty() {
    assertTrue(CategorizationService.match(List.of(rule(1, "mega", "Mâncare")), "OMV").isEmpty());
  }

  @Test
  void equalLengthTieLowestIdWins() {
    var rules = List.of(rule(1, "mega", "Mâncare"), rule(2, "card", "Lifestyle"));
    assertEquals("Mâncare", CategorizationService.match(rules, "MEGA").orElseThrow().getName());
  }

  @Test
  void patternIsTrimmed() {
    var rules = List.of(rule(1, " mega image ", "Mâncare"));
    assertEquals(
        "Mâncare",
        CategorizationService.match(rules, "CARD MEGA IMAGE 123").orElseThrow().getName());
  }

  @Test
  void emptyRuleListReturnEmpty() {
    assertTrue(CategorizationService.match(List.of(), "OMV").isEmpty());
  }
}
