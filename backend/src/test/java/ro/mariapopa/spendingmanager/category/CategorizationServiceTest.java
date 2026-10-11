package ro.mariapopa.spendingmanager.category;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CategorizationServiceTest {
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
  void matchReturnsCategoryWhenCaseDiffers() {
    var rules = List.of(rule(1, "mega image", "Mâncare"));
    assertThat(CategorizationService.match(rules, "CARD MEGA IMAGE 123"))
        .map(Category::getName)
        .hasValue("Mâncare");
  }

  @Test
  void matchReturnsCategoryOfLongestPattern() {
    var rules = List.of(rule(1, "mega", "Mâncare"), rule(2, "mega image online", "Lifestyle"));
    assertThat(CategorizationService.match(rules, "MEGA IMAGE ONLINE"))
        .map(Category::getName)
        .hasValue("Lifestyle");
  }

  @Test
  void matchReturnsEmptyWhenNoRuleMatches() {
    assertThat(CategorizationService.match(List.of(rule(1, "mega", "Mâncare")), "OMV"))
        .isEmpty();
  }

  @Test
  void matchReturnsLowestIdCategoryWhenPatternLengthsTie() {
    var rules = List.of(rule(2, "mega", "Mâncare"), rule(1, "card", "Lifestyle"));
    assertThat(CategorizationService.match(rules, "CARD MEGA IMAGE"))
        .map(Category::getName)
        .hasValue("Lifestyle");
  }

  @Test
  void matchReturnsCategoryWhenPatternHasSurroundingSpaces() {
    var rules = List.of(rule(1, " mega ", "Mâncare"));
    assertThat(CategorizationService.match(rules, "MEGA IMAGE"))
        .map(Category::getName)
        .hasValue("Mâncare");
  }

  @Test
  void matchReturnsEmptyWhenRuleListIsEmpty() {
    assertThat(CategorizationService.match(List.of(), "OMV"))
        .isEmpty();
  }

  @Test
  void matchReturnsEmptyWhenDescriptionIsNull() {
    assertThat(CategorizationService.match(List.of(rule(1, "mega", "Mâncare")), null))
        .isEmpty();
  }
}
