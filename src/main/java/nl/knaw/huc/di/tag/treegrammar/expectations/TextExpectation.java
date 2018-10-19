package nl.knaw.huc.di.tag.treegrammar.expectations;

import javax.xml.stream.events.XMLEvent;

public class TextExpectation implements ConcreteExpectation {

  @Override
  public boolean matches(final XMLEvent xmlEvent) {
    return xmlEvent.isCharacters();
  }

  @Override
  public int hashCode() {
    return TextExpectation.class.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof TextExpectation;
  }

  @Override
  public String toString() {
    return ".+";
  }
}
