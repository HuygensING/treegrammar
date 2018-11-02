package nl.knaw.huc.di.tag.treegrammar;
/*
 * Hier schetsen we eerst wat uit...
 * Ik wil een stukje code die stax parse events gooit en verwerkt.
 */

import nl.knaw.huc.di.tag.treegrammar.nodes.Node;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class ValidationTest {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationTest.class);

  @Test
  void testXMLParses() throws XMLStreamException {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    validator.parse("<root><markup>tekst</markup></root>");

    String expected = "root\n" +
        "| markup\n" +
        "| | \"tekst\"";
    assertTreeVisualisation(validator, expected);
  }

  @Test
  void testXMLDoesNotParse1() {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    try {
      validator.parse("<root><markup>tekst and <b>more</b> markup</markup></root>");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Unexpected node b");
    }
  }

  @Test
  void testXMLDoesNotParse2() {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    try {
      validator.parse("<root>plain tekst and <markup>marked-up tekst</markup></root>");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("No match: expected markup but found \"plain tekst and \"");
    }
  }

  private List<TransitionRule> defaultTransitionRules() {
    final List<TransitionRule> defaultTransitionRules = new ArrayList<>();
    defaultTransitionRules.add(parseTransitionRule("# => root[MARKUP]"));
    defaultTransitionRules.add(parseTransitionRule("MARKUP => markup[_]"));
    return defaultTransitionRules;
  }

  @Test
  void testXMLParses2() throws XMLStreamException {
    String[] ruleStrings = {
        "# => person[NAME]",
        "NAME => name[FIRST LAST]",
        "FIRST => first[_]",
        "LAST => last[_]"
    };
    final List<TransitionRule> transitionRules = stream(ruleStrings)
        .map(this::parseTransitionRule)
        .collect(toList());
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(transitionRules);
    validator.parse("<person>" +
        "<name>" +
        "<first>John</first>" +
        "<last>Doe</last>" +
        "</name>" +
        "</person>");

    String expected = "person\n" +
        "| name\n" +
        "| | first\n" +
        "| | | \"John\"\n" +
        "| | last\n" +
        "| | | \"Doe\"";
    assertTreeVisualisation(validator, expected);
  }

  @Test
  void testMultipleTransitionRulesForNonTerminal() throws XMLStreamException {
    String[] ruleStrings = {
        "# => artist[NAME]",
        "NAME => name[({FIRST LAST}|ARTISTNAME)]",
        "FIRST => first[_]",
        "LAST => last[_]",
        "ARTISTNAME => artistname[_]"
    };
//    final List<TransitionRule> transitionRules = stream(ruleStrings)
//        .map(TransitionRuleFactory::fromString)
//        .collect(toList());

    String tgsScript = String.join("\n", ruleStrings);
    List<TransitionRule> transitionRules = new TransitionRuleSetFactory().fromTGS(tgsScript);

    LOG.info("transitionrules={}", transitionRules);
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(transitionRules);
    validator.parse("<artist>" +
        "<name>" +
        "<first>John</first>" +
        "<last>Doe</last>" +
        "</name>" +
        "</artist>");
    String expected = "artist\n" +
        "| name\n" +
        "| | first\n" +
        "| | | \"John\"\n" +
        "| | last\n" +
        "| | | \"Doe\"";
    assertTreeVisualisation(validator, expected);
    LOG.info("transitionrules={}", transitionRules);

    validator.reset();
    validator.parse("<artist>" +
        "<name>" +
        "<artistname>The JohnDoes</artistname>" +
        "</name>" +
        "</artist>");
    String expected2 = "artist\n" +
        "| name\n" +
        "| | artistname\n" +
        "| | | \"The JohnDoes\"\n";
    assertTreeVisualisation(validator, expected2);
  }

  private void assertTreeVisualisation(final XMLValidatorUsingTreeGrammars validator, final String expected) {
    Tree<Node> tree = validator.getTree();
    String asText = TreeVisualizer.asText(tree);
    LOG.info("\n{}", asText);
    assertThat(asText).isEqualTo(expected);
  }

  private TransitionRule parseTransitionRule(final String input) {
    return new TransitionRuleSetFactory().fromTGS(input).get(0);
  }

}