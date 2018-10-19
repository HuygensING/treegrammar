package nl.knaw.huc.di.tag.treegrammar.expectations;

import javax.xml.stream.events.XMLEvent;

public interface ConcreteExpectation extends Expectation {
  public boolean matches(XMLEvent xmlEvent);
}
