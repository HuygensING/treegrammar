package nl.knaw.huc.di.tag.treegrammar.expectations;

import javax.xml.stream.events.XMLEvent;

public class MarkupEndExpectation implements ConcreteExpectation {
  private String tag;

  public MarkupEndExpectation(final String tag) {
    this.tag = tag;
  }

  @Override
  public boolean matches(final XMLEvent xmlEvent) {
    return xmlEvent.isEndElement()
        && xmlEvent.asEndElement().getName().toString().equals(tag);
  }

  @Override
  public int hashCode() {
    return tag.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof MarkupEndExpectation
        && ((MarkupEndExpectation) obj).tag.equals(tag);
  }

  @Override
  public String toString() {
    return "</" + tag + ">";
  }
}
