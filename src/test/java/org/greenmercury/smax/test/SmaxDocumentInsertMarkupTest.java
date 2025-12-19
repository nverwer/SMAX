package org.greenmercury.smax.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.greenmercury.smax.Balancing;
import org.greenmercury.smax.SmaxDocument;
import org.greenmercury.smax.SmaxElement;
import org.greenmercury.smax.convert.XmlString;
import org.junit.jupiter.api.Test;

public class SmaxDocumentInsertMarkupTest {

  private String simplify(SmaxDocument document) throws Exception {
    // Serialize the given document. Remove processing instructions and namespace declarations.
    return XmlString.fromSmax(document).replaceAll("<\\?.*?\\?>", "").replaceAll("\\s*xmlns:.+?=\".*?\"", "");
  }


  @Test
  void test_insertMarkup_around_outer_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 1, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x><p>1</p><r/><q>2</q></x>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_inner_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 1, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x><p>1</p><r/><q>2</q></x>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_toStart_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_START, 1, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x><p>1</p><r/><q>2</q></x>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_toEnd_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_END, 1, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x><p>1</p><r/><q>2</q></x>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_start_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 1, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x/><p>1</p><r/><q>2</q>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_start_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><r/><x/><q>2</q>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_start_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 3, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><r/><q>2</q><x/>3</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_around_end_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p><r/><q>2</q>3</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 1, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><r/><q>2</q><x/>3</doc>";
    assertEquals(expectedOutput, output);
  }



  @Test
  void test_insertMarkup_inside_outer_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p>2<q>3</q>4</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><x>2</x><q>3</q>4</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_inside_inner_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p>2<q>3</q>4</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><x>2</x><q>3</q>4</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_inside_toStart_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p>2<q>3</q>4</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_START, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><x>2</x><q>3</q>4</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_inside_toEnd_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p>2<q>3</q>4</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_END, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><x>2</x><q>3</q>4</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_inside_start_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p>2<q>3</q>4</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p><x/>2<q>3</q>4</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_inside_end_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>1</p>2<q>3</q>4</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 2, 3);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1</p>2<x/><q>3</q>4</doc>";
    assertEquals(expectedOutput, output);
  }



  @Test
  void test_insertMarkup_overlap_outer_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 2, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x><p>12</p><r>3</r><q>45</q></x>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_outer_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 2, 4);
    String output = simplify(document);
    String expectedOutput = "<doc>0<x><p>12</p><r>3</r></x><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_outer_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 3, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><x><r>3</r><q>45</q></x>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_inner_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 2, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><x><r>3</r></x><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_inner_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 2, 4);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><x><r>3</r></x><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_inner_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 3, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><x><r>3</r></x><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_start_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 2, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1<x/>2</p><r>3</r><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_start_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 2, 4);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1<x/>2</p><r>3</r><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_start_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.START, 3, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><x/><r>3</r><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_toStart_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_START, 2, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1<x/>2</p><r>3</r><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_toStart_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_START, 2, 4);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>1<x/>2</p><r>3</r><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_toStart_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.BALANCE_TO_START, 3, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><x/><r>3</r><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_end_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 2, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><r>3</r><q>4<x/>5</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_end_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 2, 4);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><r>3</r><x/><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_end_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 3, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><r>3</r><q>4<x/>5</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_toEnd_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 2, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><r>3</r><q>4<x/>5</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_toEnd_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 2, 4);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><r>3</r><x/><q>45</q>6</doc>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_overlap_toEnd_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<doc>0<p>12</p><r>3</r><q>45</q>6</doc>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.END, 3, 5);
    String output = simplify(document);
    String expectedOutput = "<doc>0<p>12</p><r>3</r><q>4<x/>5</q>6</doc>";
    assertEquals(expectedOutput, output);
  }



  @Test
  void test_insertMarkup_emptyNewElement_outer_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a>.</a></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 0, 0);
    String output = simplify(document);
    String expectedOutput = "<r><x/><a>.</a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_emptyNewElement_inner_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a>.</a></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 0, 0);
    String output = simplify(document);
    String expectedOutput = "<r><a><x/>.</a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_emptyNewElement_outer_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a>.</a></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 1, 1);
    String output = simplify(document);
    String expectedOutput = "<r><a>.</a><x/></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_emptyNewElement_inner_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a>.</a></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 1, 1);
    String output = simplify(document);
    String expectedOutput = "<r><a>.<x/></a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_emptyNewElement_outer_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a/></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 0, 0);
    String output = simplify(document);
    String expectedOutput = "<r><a/><x/></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_emptyNewElement_inner_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a/></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 0, 0);
    String output = simplify(document);
    String expectedOutput = "<r><a><x/></a></r>";
    assertEquals(expectedOutput, output);
  }



  @Test
  void test_insertMarkup_emptyElements_outer_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a/>.<c>.</c>.<a/></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.OUTER, 0, 3);
    String output = simplify(document);
    String expectedOutput = "<r><x><a/>.<c>.</c>.<a/></x></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_emptyElements_inner() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a/>.<c>.</c>.<a/></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 0, 3);
    String output = simplify(document);
    String expectedOutput = "<r><a/><x>.<c>.</c>.</x><a/></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_sameRangeElements_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    Set<SmaxElement> sameRangeReverseBalancing = Set.of(document.getMarkup(), document.getMarkup().getFirstChildElement()); // <r>, <a>
    document.insertMarkup(newNode, Balancing.OUTER, 0, 3, sameRangeReverseBalancing);
    String output = simplify(document);
    String expectedOutput = "<r><a><x><b>...</b></x></a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_sameRangeElements_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    Set<SmaxElement> sameRangeReverseBalancing = Set.of(document.getMarkup().getFirstChildElement().getFirstChildElement()); // <b>
    document.insertMarkup(newNode, Balancing.INNER, 0, 3, sameRangeReverseBalancing);
    String output = simplify(document);
    String expectedOutput = "<r><a><x><b>...</b></x></a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_sameRangeElements_3() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    Set<SmaxElement> sameRangeReverseBalancing = Set.of(document.getMarkup().getFirstChildElement().getFirstChildElement()); // <b>
    document.insertMarkup(newNode, Balancing.INNER, 0, 3, sameRangeReverseBalancing);
    String output = simplify(document);
    String expectedOutput = "<r><a><x><b>...</b></x></a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_sameRangeElements_4() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    Set<SmaxElement> sameRangeReverseBalancing = Set.of(document.getMarkup().getFirstChildElement()); // <a>
    document.insertMarkup(newNode, Balancing.OUTER, 0, 3, sameRangeReverseBalancing);
    String output = simplify(document);
    String expectedOutput = "<r><a><x><b>...</b></x></a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_sameRangeElements_5() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    Set<SmaxElement> sameRangeReverseBalancing = Set.of(document.getMarkup().getFirstChildElement().getFirstChildElement()); // <b>
    document.insertMarkup(newNode, Balancing.OUTER, 0, 3, sameRangeReverseBalancing);
    String output = simplify(document);
    // OUTER will not insert an element outside the root element.
    String expectedOutput = "<r><x><a><b>...</b></a></x></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_within_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    SmaxElement within = document.getMarkup().getFirstChildElement(); // <a>
    document.insertMarkup(newNode, within, Balancing.OUTER, 0, 3);
    String output = simplify(document);
    String expectedOutput = "<r><a><x><b>...</b></x></a></r>";
    assertEquals(expectedOutput, output);
  }

  @Test
  void test_insertMarkup_within_2() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a><b>...</b></a></r>");
    SmaxElement newNode = new SmaxElement("x");
    SmaxElement within = document.getMarkup().getFirstChildElement().getFirstChildElement(); // <b>
    document.insertMarkup(newNode, within, Balancing.OUTER, 0, 3);
    String output = simplify(document);
    String expectedOutput = "<r><a><b><x>...</x></b></a></r>";
    assertEquals(expectedOutput, output);
  }

}
