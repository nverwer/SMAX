package org.greenmercury.smax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.greenmercury.smax.SmaxAttributes.SmaxAttr;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Representation of an XML element in the markup part of SMAX.
 *<p>
 * @author Rakensi
 *<p>
 * "If you want creativity, take a zero off your budget. If you want sustainability, take off two zeros." - Jaime Lerner
 */
public class SmaxElement implements org.w3c.dom.Element {
  /**
   * The usual properties of an XML element.
   */
  private String namespaceUri;
  private String namespacePrefix;
  private String localName;
  private String qualifiedName;
  private SmaxAttributes attributes;

  /**
   * An array of namespace prefix mappings, declared on this element.
   * See {@code lookupPrefix} for namespace lookup and inheritance.
   */
  private NamespacePrefixMapping[] namespacePrefixMappings;

  /**
   * The start and end character position of the node.
   * These are int, because the capacity of a StringBuffer (used for text content in SmaxDocument) is int.
   * The {@code startpos} is just before the first character in the text,
   * and the {@code endPos} is just after the last character in the text.
   */
  private int startPos;
  private int endPos;

  /**
   * The parent node (a SmaxElement) of this element, if there is one.
   */
  private SmaxElement parentNode;

  /**
   * Children of this node, which are child elements in the XML.
   * They are ordered according to document order.
   * Their startPos - endPos ranges do not overlap.
   */
  private List<SmaxElement> children;

  /**
   * Constructor for a {@code SmaxElement} without namespace, and without attributes.
   * @param localName
   */
  public SmaxElement(String localName) {
    this(null, localName, localName, null);
  }

  /**
   * Constructor for a {@code SmaxElement} that uses the element name to derive local name and namespace prefix.
   * @param namespaceUri
   * @param qualifiedName
   */
  public SmaxElement(String namespaceUri, String qualifiedName) {
    this(namespaceUri, qualifiedName.contains(":") ? qualifiedName.substring(qualifiedName.indexOf(':')+1) : qualifiedName, qualifiedName, null);
  }

  /**
   * Constructor for a {@code SmaxElement} that uses the element name to derive local name and namespace prefix.
   * @param namespaceUri
   * @param qualifiedName
   * @param attributes
   */
  public SmaxElement(String namespaceUri, String qualifiedName, Attributes attributes) {
    this(namespaceUri, qualifiedName.contains(":") ? qualifiedName.substring(qualifiedName.indexOf(':')+1) : qualifiedName, qualifiedName, attributes);
  }

  /**
   * Constructor for a {@code SmaxElement}.
   * @param namespaceUri
   * @param localName
   * @param qualifiedName
   */
  public SmaxElement(String namespaceUri, String localName, String qualifiedName) {
    this(namespaceUri, localName, qualifiedName, null);
  }

  /**
   * Constructor for a {@code SmaxElement}.
   * @param namespaceUri
   * @param localName
   * @param qualifiedName
   * @param attributes
   */
  public SmaxElement(String namespaceUri, String localName, String qualifiedName, Attributes attributes) {
    setName(namespaceUri, localName, qualifiedName);
    setAttributes(attributes);
    startPos = 0;
    endPos = 0;
    children = new ArrayList<>();
  }

  /**
   * Make a shallow copy (with no children) of a {@code SmaxElement}.
   * This is useful when a {@code SmaxElement} is used as a template for new {@code SmaxElement}s.
   * @return a copy of the {@code SmaxElement} that can be changed without affecting the original.
   */
  public SmaxElement shallowCopy() {
    return new SmaxElement(namespaceUri, localName, qualifiedName, attributes);
  }

  /**
   * @return the start position
   */
  public int getStartPos() {
    return startPos;
  }

  /**
   * Set the start position of the element.
   * @param startPos the startPos to set
   * Warning: Using this method may corrupt the structure of the markup of a document.
   */
  public SmaxElement setStartPos(int startPos) {
    this.startPos = startPos;
    return this;
  }

  /**
   * @return the endPos
   */
  public int getEndPos() {
    return endPos;
  }

  /**
   * Set the end position of the element.
   * @param endPos the endPos to set
   * Warning: Using this method may corrupt the structure of the markup of a document.
   */
  public SmaxElement setEndPos(int endPos) {
    this.endPos = endPos;
    return this;
  }

  /**
   * @return the namespaceUri of this element, or "" (not null) if it is unspecified
   */
  public String getNamespaceUri() {
    return this.namespaceUri != null ? this.namespaceUri : "";
  }

  /**
   * @return the namespacePrefix of this element, or "" (not null) if it is unspecified
   */
  public String getNamespacePrefix() {
    return this.namespacePrefix != null ? this.namespacePrefix : "";
  }

  /**
   * Find out if the element has a namespace prefix.
   * @return whether the element has a namespace prefix.
   */
  public boolean hasNamespacePrefix() {
    return namespacePrefix != null && namespacePrefix.length() > 0;
  }

  /**
   * @return the localName
   */
  @Override
  public String getLocalName() {
    return localName;
  }

  /**
   * @return the qualified name
   */
  public String getQualifiedName() {
    return qualifiedName;
  }

  /**
   * Set the name of the SmaxElement.
   * @param namespaceUri
   * @param localName
   * @param qualifiedName
   * @return the {@code SmaxElement} itself
   */
  public SmaxElement setName(String namespaceUri, String localName, String qualifiedName) {
    this.namespaceUri = namespaceUri;
    this.namespacePrefix = qualifiedName.contains(":") ? qualifiedName.substring(0, qualifiedName.indexOf(':')) : "";
    this.localName = localName;
    this.qualifiedName = qualifiedName;
    return this;
  }

  /**
   * @return the attributes
   * Note: An empty namespaceUri, localName or qualifiedName of an attribute is "" rather than null.
   * Some attribute implementations, such as the on in Xerces, would rather have null.
   */
  @Override
  public SmaxAttributes getAttributes() {
    return attributes;
  }

  /**
   * @param attributes
   * @return the {@code SmaxElement} itself
   */
  public SmaxElement setAttributes(Attributes attributes) {
    this.attributes = new SmaxAttributes(this);
    if (attributes != null) this.attributes.setAttributes(attributes);
    return this;
  }

  /**
   * Add an attribute to this SMAX node.
   * @param namespaceUri
   * @param localName
   * @param qualifiedName
   * @param type The attribute type is one of the strings "CDATA", "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY", "ENTITIES", or "NOTATION" (always in upper case).
   * @param value
   * @return  the {@code SmaxElement} itself, now with an extra attribute
   * @throws SmaxException
   */
  public SmaxElement setAttribute(String namespaceUri, String localName, String qualifiedName, String type, String value)
    throws SmaxException
  {
    // The SAX AttributesImpl does not like nulls.
    if (namespaceUri == null) namespaceUri = "";
    if (localName == null) localName = "";
    if (qualifiedName == null) qualifiedName = "";
    if (localName.length() == 0 && qualifiedName.length() == 0)
      throw new SmaxException("Error in setAttribute. Both localName and qualifiedName are empty.");
    if (localName.length() == 0)
      localName = qualifiedName.contains(":") ? qualifiedName.substring(qualifiedName.indexOf(':')+1) : qualifiedName;
    if (qualifiedName.length() == 0)
      qualifiedName = localName;
    if (type == null || type.length() == 0) type = "CDATA";
    if (value == null) value = "";
    // If this attribute already exists, give it another value. Otherwise create it.
    int attrIndex = attributes.getIndex(namespaceUri, localName);
    if (attrIndex < 0) {
      attributes.addAttribute(namespaceUri, localName, qualifiedName, type, value);
    } else {
      attributes.setAttribute(attrIndex, namespaceUri, localName, qualifiedName, type, value);
    }
    return this;
  }

  /**
   * Add a CDATA attribute with or without namespace to this SMAX node.
   * @param localName
   * @param value
   * @return  the {@code SmaxElement} itself
   * @throws SmaxException
   */
  public SmaxElement setSimpleAttribute(String localName, String value) throws SmaxException {
    return this.setAttribute("", localName, localName, "CDATA", value);
  }

  /**
   * Add a CDATA attribute with or without namespace to this SMAX node, override.
   * @param name
   * @param value
   * @return  the {@code SmaxElement} itself
   */
  @Override
  public void setAttribute(String name, String value) {
    try {
      int prefixColonIndex = name.indexOf(':');
      String prefix = (prefixColonIndex < 0) ? null : name.substring(0, prefixColonIndex);
      String localName = (prefixColonIndex < 0) ? name : name.substring(prefixColonIndex+1);
      if ("xmlns".equals(prefix)) {
        // This is not a real attribute, but a namespace declaration.
        String currentNamespaceUri = this.lookupNamespaceURI(prefix);
        if (currentNamespaceUri == null) {
          int namespaceCount = this.getNamespacePrefixMappings().length;
          this.namespacePrefixMappings = Arrays.copyOf(namespacePrefixMappings, namespaceCount + 1);
          this.namespacePrefixMappings[namespaceCount] = new NamespacePrefixMapping(localName, value);
          // If this is a new namespace, there may already be an attribute using its prefix.
          for (int i = 0; i < attributes.getLength(); ++i) {
            SmaxAttr attr = attributes.item(i);
            if (localName.equals(attr.getPrefix())) {
              attr.setNamespaceURI(value);
            }
          }
        } else if (!currentNamespaceUri.equals(value)) {
          throw new RuntimeException("The namespace URIs for the prefix '"+prefix+"' are different: '"+currentNamespaceUri+"' and '"+value+"'.");
        }
      } else if ("xml".equals(prefix)) {
        // This is a special XML attribute, with an implicit namespace URI.
        this.setAttribute(Attribute.XML_URI, localName, name, "CDATA", value);
      } else if (prefix != null) {
        // A normal attribute in a namespace.
        this.setAttribute(this.lookupNamespaceURI(prefix), localName, name, "CDATA", value);
      } else {
        // A normal attribute without namespace.
        this.setAttribute("", localName, localName, "CDATA", value);
      }
    } catch (SmaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Add a CDATA attribute with namespace to this SMAX node.
   * @param namespaceUri
   * @param localName
   * @param qualifiedName
   * @param value
   * @return  the {@code SmaxElement} itself
   * @throws SmaxException
   */
  public SmaxElement setAttribute(String namespaceUri, String localName, String qualifiedName, String value) throws SmaxException {
    return this.setAttribute(namespaceUri, localName, qualifiedName, "CDATA", value);
  }

  /**
   * Get the namespace prefix mappings.
   * @return an array of namespace prefix mappings
   */
  public NamespacePrefixMapping[] getNamespacePrefixMappings() {
    if (namespacePrefixMappings == null) {
      namespacePrefixMappings = new NamespacePrefixMapping[0];
    }
    return namespacePrefixMappings;
  }

  /**
   * Set the namespace prefix mappings for this element.
   * @param namespacePrefixMappings
   * @return the {@code SmaxElement} itself
   */
  public SmaxElement setNamespacePrefixMappings(NamespacePrefixMapping[] namespacePrefixMappings) {
    this.namespacePrefixMappings = namespacePrefixMappings;
    return this;
  }

  /**
   * Look up the prefix associated to the given namespace URI, starting from this node and moving up in the node tree.
   * @param namespaceURI
   * @return the namespace prefix, or null if it has not been declared.
   */
  @Override
  public String lookupPrefix(String namespaceURI) {
    if (namespacePrefixMappings != null) {
      for (NamespacePrefixMapping nspMapping : namespacePrefixMappings) {
        if (namespaceURI.equals(nspMapping.uri)) {
          // Never allow an empty namespace prefix.
          return ("".equals(nspMapping.prefix)) ? null : nspMapping.prefix;
        }
      }
    }
    if (parentNode != null) {
      return parentNode.lookupPrefix(namespaceURI);
    } else {
      return null;
    }
  }

  /**
   * Look up the namespace URI associated to the given prefix, starting from this node.
   * @param prefix
   * @return the namespace uri, or null if it has not been declared.
   */
  @Override
  public String lookupNamespaceURI(String prefix) {
    if (namespacePrefixMappings != null) {
      for (NamespacePrefixMapping nspMapping : namespacePrefixMappings) {
        if (nspMapping.prefix.equals(prefix)) {
          return nspMapping.uri;
        }
      }
    }
    if (parentNode != null) {
      return parentNode.lookupNamespaceURI(prefix);
    } else {
      return null;
    }
  }

  /**
   * Get the parent element of this element.
   * @return the parent element, or {@code null} if there is none.
   */
  @Override
  public SmaxElement getParentNode() {
    return parentNode;
  }

  /**
   * Set the parent element of this element.'
   * This should only be used when a node is added to a parent node.
   * @param parentNode
   * @return the {@code SmaxElement} itself
   */
  protected SmaxElement setParentNode(SmaxElement parentNode) {
    this.parentNode = parentNode;
    return this;
  }

  public int getIndexInParent() {
    if (this.parentNode == null) return -1;
    return this.parentNode.children.indexOf(this);
  }

  /**
   * @return the children of this node in document order
   * Note that this is not a deep copy of the children. If the children change, the result of this function changes.
   */
  public List<SmaxElement> getChildren() {
    return this.children;
  }

  /**
   * @param children the children (in document order) to set
   * @return the {@code SmaxElement} itself
   */
  public SmaxElement setChildren(List<SmaxElement> children) {
    for (SmaxElement child : children) {
      child.setParentNode(this);
    }
    this.children = children;
    return this;
  }

  /**
   * Add a child to this node.
   * @param child the new child node, which comes after the existing children in document order
   * @return the {@code SmaxElement} itself
   */
  public SmaxElement appendChild(SmaxElement child) {
    child.setParentNode(this);
    this.children.add(child);
    return this;
  }

  /**
   * Insert a child at the given index.
   * @param index
   * @param child
   * @return the {@code SmaxElement} itself
   */
  public SmaxElement insertChild(int index, SmaxElement child) {
    child.setParentNode(this);
    children.add(index, child);
    return this;
  }

  /**
   * Remove the children between the specified fromIndex, inclusive, and toIndex, exclusive.
   * @param fromIndex
   * @param toIndex
   * @return the children that have been removed
   */
  public List<SmaxElement> removeChildren(int fromIndex, int toIndex) {
    List<SmaxElement> orphans = new ArrayList<>(children.subList(fromIndex, toIndex));
    children.subList(fromIndex, toIndex).clear();
    for (SmaxElement orphan : orphans) {
      orphan.setParentNode(null);
    }
    return orphans;
  }

  /**
   * Use toString() for debugging, not for serializing.
   */
  @Override
  public String toString() {
    return "<" + qualifiedName + " @" + startPos + ".." + endPos + ">";
  }

  /**
   * Find out if this element matches a pattern-element.
   * @param pattern a {@code SmaxElement} without children (they will be ignored)
   * @return whether the name and attributes of this element match with those of the {@code pattern}.
   */
  public boolean matches(SmaxElement pattern) {
    Attributes na = getAttributes();
    return this.getNamespaceUri().equals(pattern.getNamespaceUri()) &&
           this.getLocalName().equals(pattern.getLocalName()) &&
           Attribute.stream(pattern.getAttributes())
             .allMatch(pa -> pa.getValue().equals(na.getValue(pa.getURI(), pa.getLocalName())));
  }


  /* Methods for org.w3c.dom.Element */

  @Override
  public String getNodeName() {
    return qualifiedName;
  }

  @Override
  public String getNodeValue() throws DOMException {
    return null;
  }

  @Override
  public void setNodeValue(String nodeValue) throws DOMException {
  }

  @Override
  public short getNodeType() {
    return Node.ELEMENT_NODE;
  }

  @Override
  public NodeList getChildNodes() {
    return new NodeList() {
      @Override
      public Node item(int index) {
        return SmaxElement.this.children.get(index);
      }
      @Override
      public int getLength() {
        return SmaxElement.this.children.size();
      }};
  }

  @Override
  public Node getFirstChild() {
    return this.children.get(0);
  }

  @Override
  public Node getLastChild() {
    return this.children.get(this.children.size() - 1);
  }

  @Override
  public Node getPreviousSibling() {
    int indexInParent = this.getIndexInParent();
    if (indexInParent > 0)
      return this.parentNode.children.get(indexInParent - 1);
    else
      return null;
  }

  @Override
  public Node getNextSibling() {
    int indexInParent = this.getIndexInParent();
    int lastIndexInParent = this.parentNode.children.size() - 1;
    if (indexInParent > 0 && indexInParent < lastIndexInParent)
      return this.parentNode.children.get(indexInParent + 1);
    else
      return null;
  }

  @Override
  public Document getOwnerDocument() {
    return null;
  }

  @Override
  public Node insertBefore(Node newChild, Node refChild) throws DOMException {
    int refIndex = this.children.indexOf(refChild);
    if (refIndex < 0) throw new DOMException(DOMException.NOT_FOUND_ERR, "The reference child is not a child of this element.");
    if (newChild instanceof SmaxElement) {
      this.insertChild(refIndex, (SmaxElement)newChild);
      return newChild;
    } else {
      throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "The inserted node must be a SmaxElement.");
    }
  }

  @Override
  public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
    int refIndex = this.children.indexOf(oldChild);
    if (refIndex < 0) throw new DOMException(DOMException.NOT_FOUND_ERR, "The old child is not a child of this element.");
    if (newChild instanceof SmaxElement) {
      this.removeChildren(refIndex, refIndex);
      this.insertChild(refIndex, (SmaxElement)newChild);
      return oldChild;
    } else {
      throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "The inserted node must be a SmaxElement.");
    }
  }

  @Override
  public Node removeChild(Node oldChild) throws DOMException {
    int refIndex = this.children.indexOf(oldChild);
    if (refIndex < 0) throw new DOMException(DOMException.NOT_FOUND_ERR, "The old child is not a child of this element.");
    this.removeChildren(refIndex, refIndex);
    return oldChild;
  }

  @Override
  public Node appendChild(Node newChild) throws DOMException {
    if (newChild instanceof SmaxElement) {
      this.appendChild((SmaxElement)newChild);
      return newChild;
    } else {
      throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, "The inserted node must be a SmaxElement.");
    }
  }

  @Override
  public boolean hasChildNodes() {
    return !this.children.isEmpty();
  }

  @Override
  public Node cloneNode(boolean deep) {
    if (deep) throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
    return new SmaxElement(this.namespaceUri, this.localName, this.qualifiedName, this.attributes);
  }

  @Override
  public void normalize() {
  }

  @Override
  public boolean isSupported(String feature, String version) {
    return false;
  }

  /**
   * @return the namespace URI of this node, or null if it is unspecified
   * @see org.w3c.dom.Node#getNamespaceURI()
   */
  @Override
  public String getNamespaceURI() {
    return this.namespaceUri;
  }

  @Override
  public String getPrefix() {
    return this.getNamespacePrefix();
  }

  @Override
  public void setPrefix(String prefix) throws DOMException {
    this.namespacePrefix = prefix;
  }

  @Override
  public boolean hasAttributes() {
    return this.attributes.getLength() > 0;
  }

  @Override
  public String getBaseURI() {
    return null;
  }

  @Override
  public short compareDocumentPosition(Node other) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public String getTextContent() throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public void setTextContent(String textContent) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public boolean isSameNode(Node other) {
    return this == other;
  }

  @Override
  public boolean isDefaultNamespace(String namespaceURI) {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public boolean isEqualNode(Node other) {
    return this == other;
  }

  @Override
  public Object getFeature(String feature, String version) {
    return null;
  }

  @Override
  public Object setUserData(String key, Object data, UserDataHandler handler) {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public Object getUserData(String key) {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public String getTagName() {
    return this.qualifiedName;
  }

  @Override
  public String getAttribute(String name) {
    return this.attributes.getValue(name);
  }

  @Override
  public void removeAttribute(String name) throws DOMException {
    this.attributes.removeNamedItem(name);
  }

  @Override
  public Attr getAttributeNode(String name) {
    return this.attributes.getNamedItem(name);
  }

  @Override
  public Attr setAttributeNode(Attr newAttr) throws DOMException {
    return this.attributes.setNamedItem(newAttr);
  }

  @Override
  public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
    return this.attributes.removeNamedItemNS(oldAttr.getNamespaceURI(), oldAttr.getLocalName());
  }

  @Override
  public NodeList getElementsByTagName(String name) {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public String getAttributeNS(String namespaceURI, String localName) throws DOMException {
    return this.attributes.getValue(namespaceURI, localName);
  }

  @Override
  public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
    try {
      if (qualifiedName.indexOf(':') >= 0) {
        this.setAttribute(namespaceURI, qualifiedName.substring(qualifiedName.indexOf(':') + 1), qualifiedName, value);
      } else {
        this.setAttribute(namespaceURI, qualifiedName, qualifiedName, value);
      }
    } catch (SmaxException e) {
      throw new DOMException(DOMException.NAMESPACE_ERR, e.getMessage());
    }
  }

  @Override
  public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
    this.attributes.removeNamedItemNS(namespaceURI, localName);
  }

  @Override
  public Attr getAttributeNodeNS(String namespaceURI, String localName) throws DOMException {
    return this.attributes.getNamedItemNS(namespaceURI, localName);
  }

  @Override
  public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
    return this.attributes.setNamedItemNS(newAttr);
  }

  @Override
  public NodeList getElementsByTagNameNS(String namespaceURI, String localName) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public boolean hasAttribute(String name) {
    return this.attributes.getIndex(name) > 0;
  }

  @Override
  public boolean hasAttributeNS(String namespaceURI, String localName) throws DOMException {
    return this.attributes.getIndex(namespaceURI, localName) > 0;
  }

  @Override
  public TypeInfo getSchemaTypeInfo() {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public void setIdAttribute(String name, boolean isId) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public void setIdAttributeNS(String namespaceURI, String localName, boolean isId) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

  @Override
  public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
  }

}
