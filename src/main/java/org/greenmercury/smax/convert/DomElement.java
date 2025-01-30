package org.greenmercury.smax.convert;

import java.util.HashMap;
import java.util.Map;

import org.greenmercury.smax.Attribute;
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

  /**
   * Construct a SMAX document from a DOM element.
   * @param element
   * @throws SmaxException
   */
  public static SmaxDocument toSmax(Element element) throws SmaxException {
    /**
     * A map from prefixes to namespace-URIs that are declared in a DOM element.
     * We use one instance to avoid making a new one for every element.
     */
    Map<String, String> namespaces = new HashMap<String, String>();

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
   * @param domElement The element that will be converted to SMAX.
   * @param currentContent The text content of the SMAX document.
   * @param namespaces A list of namespace prefix mappings. This is passed to avoid making new lists recursively.
   * @return The converted SMAX element.
   * @throws SmaxException
   */
  private static SmaxElement domToSmax(Element domElement, StringBuffer currentContent, Map<String, String> namespaces)
    throws SmaxException
  {
    namespaces.clear();
    String elementName = domElement.getNodeName();
    // domElement.getPrefix() does not work in all implementations (specifically, BXNode in BaseX)
    String elementPrefix = (elementName.contains(":")) ? elementName.substring(0, elementName.indexOf(':')) : "";
    //if (elementPrefix == null) elementPrefix = "";
    // If the element is in a namespace, add it to the namespaces declared on this element.
    String elementNamespace = domElement.getNamespaceURI();
    if (elementNamespace !=null && !"".equals(elementNamespace)) {
      namespaces.put(elementPrefix, elementNamespace);
    }
    // Make a SMAX element.
    SmaxElement smaxElement = new SmaxElement(elementNamespace, domElement.getNodeName());
    smaxElement.setStartPos(currentContent.length());
    // Handle attributes and namespace declarations.
    NamedNodeMap domAttributes = domElement.getAttributes();
    for (int i = 0; i < domAttributes.getLength(); ++i) {
      Node attribute = domAttributes.item(i);
      String attributeName = attribute.getNodeName();
      int prefixColonIndex = attributeName.indexOf(':');
      String attributeLocalName = (prefixColonIndex >= 0) ? attributeName.substring(prefixColonIndex + 1) : attributeName;
      String attributePrefix = (prefixColonIndex >= 0) ? attributeName.substring(0, prefixColonIndex) : "";
      String attributeNamespace = attribute.getNamespaceURI();
      if ("xml".equals(attributePrefix)) {
        // This is a special XML attribute, with an implicit namespace URI.
        smaxElement.setAttribute(Attribute.XML_URI, attributeLocalName, attributeName, "CDATA", attribute.getNodeValue());
      } else if (Attribute.XMLNS_URI.equals(attributeNamespace)) {
        // This is not a real attribute, but a namespace declaration.
        String declaredPrefix = Attribute.XMLNS_PREFIX.equals(attributeName) ? "" : attributeLocalName; // xmlns="..." or xmlns:prefix="..."
        if (! namespaces.containsKey(declaredPrefix)) {
          namespaces.put(declaredPrefix, attribute.getNodeValue());
        }
      } else {
        // This is a real attribute.
        // Some implementations (BXNode in BaseX) do not provide namespace declarations as xmlns attributes, so we must extract namespaces from attributes.
        if (! "".equals(attributePrefix) && ! namespaces.containsKey(attributePrefix)) {
          namespaces.put(attributePrefix, attributeNamespace);
        }
        smaxElement.setAttribute(attribute.getNamespaceURI(), attribute.getLocalName(), attributeName, "CDATA", attribute.getTextContent());
      }
    }
    // Set the namespaces.
    if (namespaces.size() > 0) {
      NamespacePrefixMapping[] namespacePrefixMappings =
          namespaces.entrySet().stream().
          map((entry) -> new NamespacePrefixMapping(entry.getKey(), entry.getValue())).
          toArray(NamespacePrefixMapping[]::new);
      smaxElement.setNamespacePrefixMappings(namespacePrefixMappings);
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
   * Construct a DOM document from a SMAX document.
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
    Element rootElement = smaxToDom(domDocument, smaxDocument.getMarkup(), smaxDocument.getContent());
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
  private static Element smaxToDom(Document domDocument, SmaxElement smaxElement, SmaxContent content) {
    // Create a DOM element.
    String elementNamespace = smaxElement.getNamespaceUri();
    Element domElement = (elementNamespace == null || "".equals(elementNamespace))
      ? domDocument.createElement(smaxElement.getLocalName())
      : domDocument.createElementNS(elementNamespace, smaxElement.getQualifiedName());
    // Set the attributes.
    Attributes attributes = smaxElement.getAttributes();
    Map<String, String> attributeNamespaces = new HashMap<String, String>(); // prefix -> URI
    for (int i = 0, nrAttrs = attributes.getLength(); i < nrAttrs; ++i) {
      String attributeNamespace = attributes.getURI(i);
      String attributeLocalName = attributes.getLocalName(i);
      String attributeName = attributes.getQName(i);
      int prefixColonIndex = attributeName.indexOf(':');
      String attributePrefix = (prefixColonIndex >= 0) ? attributeName.substring(0, prefixColonIndex) : "";
      if (attributeNamespace == null || "".equals(attributeNamespace)) {
        domElement.setAttribute(attributeLocalName, attributes.getValue(i));
      } else {
        domElement.setAttributeNS(attributeNamespace, attributeName, attributes.getValue(i));
        attributeNamespaces.put(attributePrefix, attributeNamespace);
      }
    }
    // Add namespace declarations for attributes.
    if (attributeNamespaces.size() > 0) {
      attributeNamespaces.entrySet().stream().
        forEach(entry -> domElement.setAttributeNS(Attribute.XMLNS_URI, Attribute.XMLNS_PREFIX+":"+entry.getKey(), entry.getValue()));
    }
    // Add the content.
    int contentPosition = smaxElement.getStartPos();
    for (SmaxElement child : smaxElement.getChildren()) {
      int childStartPos = child.getStartPos();
      if (contentPosition < childStartPos) {
        domElement.appendChild(domDocument.createTextNode(content.substring(contentPosition, childStartPos)));
        contentPosition = childStartPos;
      }
      domElement.appendChild(smaxToDom(domDocument, child, content));
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
