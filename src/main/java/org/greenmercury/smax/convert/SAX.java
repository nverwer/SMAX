package org.greenmercury.smax.convert;

import org.greenmercury.smax.NamespacePrefixMapping;
import org.greenmercury.smax.SmaxDocument;
import org.greenmercury.smax.SmaxElement;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * SmaxDocument conversion to (not yet from) org.xml.sax.*.ContentHandler.
 *
 * @author Rakensi
 */
public class SAX {

  public static void fromSMAX(SmaxDocument smaxDocument, ContentHandler saxHandler)
    throws SAXException
  {
    SmaxElement root = smaxDocument.getMarkup();
    saxHandler.startDocument();
    SmaxElementToSax(root, smaxDocument.getContentBuffer(), root.getStartPos(), saxHandler);
    saxHandler.endDocument();
  }

  private static int SmaxElementToSax(SmaxElement smaxElement, CharSequence content, int currentCharPos, ContentHandler saxHandler)
    throws SAXException
  {
    int startPos = smaxElement.getStartPos();
    int endPos = smaxElement.getEndPos();
    // Send text before element.
    if (startPos > currentCharPos) {
      sendCharacters(content, currentCharPos, startPos - currentCharPos, saxHandler);
      currentCharPos = startPos;
    }
    // Start namespace prefix mappings. See https://sourceforge.net/p/saxon/mailman/message/35548184/
    for (NamespacePrefixMapping nspMapping : smaxElement.getNamespacePrefixMappings()) {
      saxHandler.startPrefixMapping(nspMapping.prefix, nspMapping.uri);
    }
    // Send element and its content.
    saxHandler.startElement(smaxElement.getNamespaceUri(), smaxElement.getLocalName(), smaxElement.getQualifiedName(), smaxElement.getAttributes());
    for (SmaxElement child : smaxElement.getChildren()) {
      currentCharPos = SmaxElementToSax(child, content, currentCharPos, saxHandler);
    }
    if (endPos > currentCharPos) {
      sendCharacters(content, currentCharPos, endPos - currentCharPos, saxHandler);
      currentCharPos = endPos;
    }
    saxHandler.endElement(smaxElement.getNamespaceUri(), smaxElement.getLocalName(), smaxElement.getQualifiedName());
    // End namespace prefix mappings.
    for (NamespacePrefixMapping nspMapping : smaxElement.getNamespacePrefixMappings()) {
      saxHandler.endPrefixMapping(nspMapping.prefix);
    }
    return currentCharPos;
  }

  /**
   * Convert content from the SMAX document into a SAX event.
   * @param start
   * @param length
   */
  private static void sendCharacters(CharSequence content, int start, int length, ContentHandler saxHandler) throws SAXException {
    char[] out = new char[length];
    for (int i = 0; i < length; ++i) {
      out[i] = content.charAt(start+i);
    }
    saxHandler.characters(out, 0, length);
  }

}
