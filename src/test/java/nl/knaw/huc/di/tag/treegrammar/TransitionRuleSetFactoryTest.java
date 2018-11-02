package nl.knaw.huc.di.tag.treegrammar;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransitionRuleSetFactoryTest {
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  @Test
  void fromTGS1() {
    String rules = "# => root[A B]\n" +
        "A=>a[_]\n" +
        "B=>b[_]";
    List<TransitionRule> transitionRules = new TransitionRuleSetFactory().fromTGS(rules);
    LOG.info("transitionRules={}", transitionRules);
    assertThat(transitionRules).hasSize(3);
  }

  @Test
  void parseTransitionRuleWithChoiceAndGroup() {
    String rule = "NAME=>name[({FIRST LAST}|ARTIST_NAME)]";
    List<TransitionRule> transitionRules = new TransitionRuleSetFactory().fromTGS(rule);
    LOG.info("transitionRules={}", transitionRules);
    assertThat(transitionRules).hasSize(1);
  }
}