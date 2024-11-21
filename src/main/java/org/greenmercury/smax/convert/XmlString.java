package org.greenmercury.smax.convert;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.greenmercury.smax.SmaxDocument;
import org.greenmercury.smax.SmaxException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class XmlString {

  /**
   * Construct a SMAX document from a serialized XML text string.
   * @param xml
   * @throws ParserConfigurationException
   * @throws IOException
   * @throws SAXException
   * @throws SmaxException
   */
  public static SmaxDocument toSmax(String xmlString) throws ParserConfigurationException, SAXException, IOException, SmaxException {
    return DomElement.toSmax(toDomElement(xmlString));
  }

  /**
   * Construct an XML text string from a SMAX document.
   * @param smaxDocument
   * @return
   * @throws Exception
   */
  public static String fromSmax(SmaxDocument smaxDocument) throws Exception {
    try {
      return fromDomElement(DomElement.fromSmax(smaxDocument));
    } catch (DOMException | ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | TransformerException e) {
      throw new Exception("Serializing the SMAX document failed: "+e.getMessage(), e);
    }
  }

  /**
   * Construct a DOM element from a serialized XML text string.
   * @param xmlString
   * @return
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static Element toDomElement(String xmlString) throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try {
      // Make the parser namespace aware.
      factory.setNamespaceAware(true);
      factory.setFeature("http://xml.org/sax/features/namespaces", true);
      factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
      // Protect against well-known XML attacks.
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (ParserConfigurationException pce) {}
    // Get a document builder and parse the document.
    DocumentBuilder builder = factory.newDocumentBuilder();
    InputSource is = new InputSource(new StringReader(xmlString));
    Document xmlDoc = builder.parse(is);
    return xmlDoc.getDocumentElement();
  }

  /**
   * Construct an XML text string from a DOM element.
   * @param domElement
   * @return
   * @throws TransformerException
   */
  public static String fromDomElement(Element domElement) throws TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer trans = tf.newTransformer();
    StringWriter sw = new StringWriter();
    trans.transform(new DOMSource(domElement), new StreamResult(sw));
    return sw.toString();
  }

  /**
   * Construct SAX events from an XML text string.
   * @param xmlString
   * @param handler
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public static void toSax(String xmlString, DefaultHandler handler) throws SAXException, ParserConfigurationException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setNamespaceAware(true);
    XMLReader xmlReader = factory.newSAXParser().getXMLReader();
    xmlReader.setContentHandler(handler);
    xmlReader.setDTDHandler(handler);
    xmlReader.setEntityResolver(handler);
    xmlReader.setErrorHandler(handler);
  }

}
