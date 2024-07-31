package org.greenmercury.smax.convert;

import java.util.ArrayList;
import java.util.List;

import org.greenmercury.smax.NamespacePrefixMapping;
import org.greenmercury.smax.SmaxContent;
import org.greenmercury.smax.SmaxDocument;
import org.greenmercury.smax.SmaxElement;
import org.greenmercury.smax.SmaxException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.xml.sax.Attributes;

/**
 * SmaxDocument conversions to and from org.w3c.dom.Element
 *
 * @author Rakensi
 */
public class DomElement {

  private static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
  private static final String XMLNS_PREFIX = "xmlns";

  /**
   * Construct a SMAX document from a DOM element.
   * @param element
   * @throws SmaxException
   */
  public static SmaxDocument toSmax(Element element) throws SmaxException {
    /**
     * The namespaces and their prefixes that are declared in a DOM element.
     * We use one list instance to avoid making a new one for every element.
     * Note: namespace declarations are represented as attributes in DOM.
     */
    List<NamespacePrefixMapping> namespaces = new ArrayList<NamespacePrefixMapping>(5); // 5 namespaces should be enough?

    /**
     * The text content of the parsed document up to the current parse position.
     * For very large documents, something else than a StringBuffer must be used to hold text content.
     * A StringBuffer is used, because it is thread-safe. A StringBuilder is faster, but not thread-safe.
     */
    StringBuffer currentContent = new StringBuffer();

    SmaxElement markup = domToSmax(element, currentContent, namespaces);
    return new SmaxDocument(markup, currentContent);
  }

  /**
   * Walk a DOM tree in document order, building up the text content in currentContent, and generating a SmaxElement.
   * @param domElement A DOM element.
   * @param currentContent This is changed during the tree-walk.
   * @return The SmaxElement corresponding to the DOM element.
   * @throws SmaxException
   * @throws
   */
  private static SmaxElement domToSmax(Element domElement, StringBuffer currentContent, List<NamespacePrefixMapping> namespaces)
    throws SmaxException
  {
    namespaces.clear();
    String elementNamespace = domElement.getNamespaceURI();
    String elementPrefix = domElement.getPrefix();
    if (elementPrefix == null) elementPrefix = "";
    // If the element is in a namespace, add it to the namespaces declared on this element.
    if (elementNamespace !=null) {
      namespaces.add(new NamespacePrefixMapping(elementPrefix, elementNamespace));
    }
    // Make a SMAX element.
    SmaxElement smaxElement = new SmaxElement(elementNamespace, domElement.getNodeName());
    smaxElement.setStartPos(currentContent.length());
    // Handle attributes and namespace declarations.
    NamedNodeMap domAttributes = domElement.getAttributes();
    for (int i = 0; i < domAttributes.getLength(); ++i) {
      Node attribute = domAttributes.item(i);
      if (XMLNS_URI.equals(attribute.getNamespaceURI())) {
        // This is not a real attribute, but a namespace declaration.
        String declaredPrefix = XMLNS_PREFIX.equals(attribute.getNodeName()) ? "" : attribute.getLocalName();
        if (! elementPrefix.equals(declaredPrefix)) {
          namespaces.add(new NamespacePrefixMapping(declaredPrefix, attribute.getTextContent()));
        }
      } else {
        // This is a real attribute.
        smaxElement.setAttribute(attribute.getNamespaceURI(), attribute.getLocalName(), attribute.getNodeName(), "CDATA", attribute.getTextContent());
      }
    }
    // Set the namespaces.
    if (namespaces.size() > 0) {
      smaxElement.setNamespacePrefixMappings(namespaces.toArray(new NamespacePrefixMapping[namespaces.size()]));
    }
    // Handle content of the DOM element.
    NodeList childNodes = domElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); ++i) {
      Node childNode = childNodes.item(i);
      if (childNode instanceof Element) {
        smaxElement.appendChild(domToSmax((Element) childNode, currentContent, namespaces));
      } else if (childNode instanceof Text) {
        currentContent.append(childNode.getNodeValue());
      }
    }
    // Set end position after all text content has been processed.
    smaxElement.setEndPos(currentContent.length());
    return smaxElement;
  }

  /**
   * Construct a DOM element from a SMAX document.
   * @param smaxDocument
   * @return
   * @throws ClassCastException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   * @throws DOMException
   */
  public static Element fromSmax(SmaxDocument smaxDocument) throws DOMException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
    return documentFromSmax(smaxDocument).getDocumentElement();
  }

  /**
   * Construct a DOM element from a SMAX document.
   * @param smaxDocument
   * @return
   * @throws ClassCastException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws ClassNotFoundException
   * @throws DOMException
   */
  public static Document documentFromSmax(SmaxDocument smaxDocument) throws DOMException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
    Document domDocument = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 1.0").createDocument(null, null, null);
    Element rootElement = elementFromSmax(domDocument, smaxDocument.getMarkup(), smaxDocument.getContent());
    domDocument.appendChild(rootElement);
    return domDocument;
  }

  /**
   * Walk a SMAX document and create a DOM element.
   * @param domDocument The DOM document which owns the new DOM element.
   * @param smaxElement
   * @param content
   * @param charPos
   * @return
   */
  private static Element elementFromSmax(Document domDocument, SmaxElement smaxElement, SmaxContent content) {
    // Create a DOM element.
    String nsUri = smaxElement.getNamespaceUri();
    Element domElement = (nsUri == null)
      ? domDocument.createElement(smaxElement.getLocalName())
      : domDocument.createElementNS(nsUri, smaxElement.getQualifiedName());
    // Set the attributes.
    Attributes attributes = smaxElement.getAttributes();
    for (int i = 0, nrAttrs = attributes.getLength(); i < nrAttrs; ++i) {
      String aNsUri = attributes.getURI(i);
      if (aNsUri == null) domElement.setAttribute(attributes.getLocalName(i), attributes.getValue(i));
      else domElement.setAttributeNS(aNsUri, attributes.getQName(i), attributes.getValue(i));
    }
    // Add the content.
    int contentPosition = smaxElement.getStartPos();
    for (SmaxElement child : smaxElement.getChildren()) {
      int childStartPos = child.getStartPos();
      if (contentPosition < childStartPos) {
        domElement.appendChild(domDocument.createTextNode(content.substring(contentPosition, childStartPos)));
        contentPosition = childStartPos;
      }
      domElement.appendChild(elementFromSmax(domDocument, child, content));
      contentPosition = child.getEndPos();
    }
    int endPosition = smaxElement.getEndPos();
    if (contentPosition < endPosition) {
      domElement.appendChild(domDocument.createTextNode(content.substring(contentPosition, endPosition)));
    }
    // Return the new DOM element.
    return domElement;
  }

}
