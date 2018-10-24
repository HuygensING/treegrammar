package nl.knaw.huc.di.tag.treegrammar.expectations;

import javax.xml.stream.events.XMLEvent;

public interface Expectation {
  boolean matches(XMLEvent xmlEvent);
}
