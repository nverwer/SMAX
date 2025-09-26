package org.greenmercury.smax.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
  void test_insertMarkup_emptyElements_inner_1() throws Exception
  {
    SmaxDocument document = XmlString.toSmax("<r><a/>.<c>.</c>.<a/></r>");
    SmaxElement newNode = new SmaxElement("x");
    document.insertMarkup(newNode, Balancing.INNER, 0, 3);
    String output = simplify(document);
    String expectedOutput = "<r><a/><x>.<c>.</c>.</x><a/></r>";
    assertEquals(expectedOutput, output);
  }

}
