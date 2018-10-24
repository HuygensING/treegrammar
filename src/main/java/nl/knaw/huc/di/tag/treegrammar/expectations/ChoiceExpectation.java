package nl.knaw.huc.di.tag.treegrammar.expectations;

import javax.xml.stream.events.XMLEvent;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class ChoiceExpectation implements AbstractExpectation {
  public List<List<Expectation>> choices;

  public ChoiceExpectation(List<List<Expectation>> choices) {
    this.choices = choices;
  }

  public boolean matches(XMLEvent xmlEvent) {
    return choices.stream()
        .map(le -> le.get(0))
        .peek(System.out::println)
        .anyMatch(e -> e.matches(xmlEvent));
  }

  @Override
  public String toString() {
    return choices.stream()
        .map(Object::toString)
        .collect(joining(" | ","[ "," ]"));
  }
}
