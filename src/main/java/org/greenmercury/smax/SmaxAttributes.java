package org.greenmercury.smax;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.Attributes;

/**
 * This class implements Attributes, AttributesImpl and NamedNodeMap, so it is compatible with those often used attributes implementations.
 */
public class SmaxAttributes extends org.xml.sax.helpers.AttributesImpl implements Attributes, NamedNodeMap {

  public interface SmaxAttr extends Attr {
    public void setNamespaceURI(String uri);
  }

  private SmaxElement parent;

  @SuppressWarnings("unused")
  private SmaxAttributes() {
    // Don't use parameterless constructor.
  }

  public SmaxAttributes(SmaxElement parent) {
    this.parent = parent;
  }

  @Override
  public SmaxAttr getNamedItem(String name) {
    return this.item(this.getIndex(name));
  }

  @Override
  public SmaxAttr getNamedItemNS(String namespaceURI, String localName) throws DOMException {
    return this.item(this.getIndex(namespaceURI, localName));
  }

  @Override
  public SmaxAttr setNamedItem(Node node) throws DOMException {
    SmaxAttr existing = this.getNamedItem(node.getNodeName());
    if (existing != null) {
      this.setAttribute(this.getIndex(node.getNodeName()), node.getNamespaceURI(), node.getLocalName(), node.getNodeName(), "CDATA", node.getNodeValue());
      return existing;
    } else {
      this.addAttribute(node.getNamespaceURI(), node.getLocalName(), node.getNodeName(), "CDATA", node.getNodeValue());
      return null;
    }
  }

  @Override
  public SmaxAttr setNamedItemNS(Node node) throws DOMException {
    SmaxAttr existing = this.getNamedItemNS(node.getNamespaceURI(), node.getLocalName());
    if (existing != null) {
      this.setAttribute(this.getIndex(node.getNodeName()), node.getNamespaceURI(), node.getLocalName(), node.getNodeName(), "CDATA", node.getNodeValue());
      return existing;
    } else {
      this.addAttribute(node.getNamespaceURI(), node.getLocalName(), node.getNodeName(), "CDATA", node.getNodeValue());
      return null;
    }
  }

  @Override
  public SmaxAttr removeNamedItem(String name) throws DOMException {
    SmaxAttr existing = this.getNamedItem(name);
    if (existing != null) {
      this.removeAttribute(this.getIndex(name));
    }
    return existing;
  }

  @Override
  public SmaxAttr removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
    int index = this.getIndex(namespaceURI, localName);
    SmaxAttr existing = this.item(index);
    if (existing != null) {
      this.removeAttribute(index);
    }
    return existing;
  }

  @Override
  public SmaxAttr item(int index) {
    if (index < 0 || index >= this.getLength()) return null;
    // Returns a node that corresponds to a single attribute.
    return new SmaxAttr() {
      @Override
      public String getNodeName() {
        return SmaxAttributes.this.getQName(index);
      }
      @Override
      public String getNodeValue() throws DOMException {
        return SmaxAttributes.this.getValue(index);
      }
      @Override
      public void setNodeValue(String nodeValue) throws DOMException {
        SmaxAttributes.this.setAttribute(index, this.getNamespaceURI(), this.getLocalName(), this.getNodeName(), "CDATA", nodeValue);
      }
      @Override
      public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
      }
      @Override
      public Node getParentNode() {
        return parent;
      }
      @Override
      public NodeList getChildNodes() {
        return new NodeList() {
          @Override
          public Node item(int index) {
            return null;
          }
          @Override
          public int getLength() {
            return 0;
          }};
      }
      @Override
      public Node getFirstChild() {
        return null;
      }
      @Override
      public Node getLastChild() {
        return null;
      }
      @Override
      public Node getPreviousSibling() {
        if (index > 0)
          return SmaxAttributes.this.item(index - 1);
        else
          return null;
      }
      @Override
      public Node getNextSibling() {
        if (index < SmaxAttributes.this.getLength() - 1)
          return SmaxAttributes.this.item(index + 1);
        else
          return null;
      }
      @Override
      public NamedNodeMap getAttributes() {
        return null;
      }
      @Override
      public Document getOwnerDocument() {
        return null;
      }
      @Override
      public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Not possible fo an attribute.");
      }
      @Override
      public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Not possible fo an attribute.");
      }
      @Override
      public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Not possible fo an attribute.");
      }
      @Override
      public Node appendChild(Node newChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Not possible fo an attribute.");
      }
      @Override
      public boolean hasChildNodes() {
        return false;
      }
      @Override
      public Node cloneNode(boolean deep) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
      }
      @Override
      public void normalize() {
      }
      @Override
      public boolean isSupported(String feature, String version) {
        return false;
      }
      @Override
      public String getNamespaceURI() {
        return SmaxAttributes.this.getURI(index);
      }
      @Override
      public void setNamespaceURI(String uri) {
        SmaxAttributes.this.setURI(index, uri);
      }
      @Override
      public String getPrefix() {
        String qname = SmaxAttributes.this.getQName(index);
        return qname.substring(0, qname.indexOf(':'));
      }
      @Override
      public void setPrefix(String prefix) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Not implemented.");
      }
      @Override
      public String getLocalName() {
        return SmaxAttributes.this.getLocalName(index);
      }
      @Override
      public boolean hasAttributes() {
        return false;
      }
      @Override
      public String getBaseURI() {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Not implemented.");
      }
      @Override
      public short compareDocumentPosition(Node other) throws DOMException {
        return 0;
      }
      @Override
      public String getTextContent() throws DOMException {
        return SmaxAttributes.this.getValue(index);
      }
      @Override
      public void setTextContent(String textContent) throws DOMException {
        SmaxAttributes.this.setAttribute(index, this.getNamespaceURI(), this.getLocalName(), this.getNodeName(), "CDATA", textContent);
      }
      @Override
      public boolean isSameNode(Node other) {
        return this == other;
      }
      @Override
      public String lookupPrefix(String namespaceURI) {
        return parent.lookupPrefix(namespaceURI);
      }
      @Override
      public boolean isDefaultNamespace(String namespaceURI) {
        return false;
      }
      @Override
      public String lookupNamespaceURI(String prefix) {
        return parent.lookupNamespaceURI(prefix);
      }
      @Override
      public boolean isEqualNode(Node node) {
        if (node == null) return false;
        return this.getNodeName().equals(node.getNodeName()) && this.getNodeValue().equals(node.getNodeValue());
      }
      @Override
      public Object getFeature(String feature, String version) {
        return null;
      }
      @Override
      public Object setUserData(String key, Object data, UserDataHandler handler) {
        return null;
      }
      @Override
      public Object getUserData(String key) {
        return null;
      }
      @Override
      public String getName() {
        return this.getNodeName();
      }
      @Override
      public boolean getSpecified() {
        return true;
      }
      @Override
      public String getValue() {
        return this.getNodeValue();
      }
      @Override
      public void setValue(String value) throws DOMException {
        this.setNodeValue(value);
      }
      @Override
      public Element getOwnerElement() {
        return parent;
      }
      @Override
      public TypeInfo getSchemaTypeInfo() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not implemented.");
      }
      @Override
      public boolean isId() {
        return false;
      }
    };
  }

}
