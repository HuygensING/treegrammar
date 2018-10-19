package nl.knaw.huc.di.tag.treegrammar.expectations;

import javax.xml.stream.events.XMLEvent;

public class MarkupStartExpectation implements ConcreteExpectation {
  private String tag;

  public MarkupStartExpectation(final String tag) {
    this.tag = tag;
  }

  @Override
  public boolean matches(final XMLEvent xmlEvent) {
    return xmlEvent.isStartElement()
        && xmlEvent.asStartElement().getName().toString().equals(tag);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof MarkupStartExpectation
        && ((MarkupStartExpectation) obj).tag.equals(tag);
  }

  @Override
  public String toString() {
    return "<" + tag + ">";
  }
}
