package org.greenmercury.smax;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Representation of a SMAX document with separated markup and content.
 *<p>
 * A SmaxDocument has markup (starting at the root-node) and text content.
 * The text content may be part of a larger {@code StringBuffer}, therefore it is a {@code SmaxContent}.
 * The {@code content} always corresponds to the content within the {@code markup},
 * and is the part of the underlying buffer between {@code markup.startPos} and {@code markup.endPos}.
 *<p>
 * The {@code startPos} and {@code endPos} of every {@code SmaxElement} in the markup
 * are relative to the underlying {@code StringBuffer} of the {@code SmaxContent} {@code content}.
 * This makes it easier to create sub-documents of a {@code SmaxDocument} without changing start and end indexes.
 * Because of this, the {@code startPos} of a root element does not have to be zero.
 *<p>
 * @author Rakensi
 */
public class SmaxDocument {

  /**
   * The root element of the document, and its content.
   */
  private SmaxElement markup;
  private SmaxContent content;

  /**
   * Construct a {@code SmaxDocument}.
   * @param markup The markup of the document.
   * @param content The content of the document. Only the part pointed to by the markup is used.
   */
  public SmaxDocument(SmaxElement markup, SmaxContent content) {
    this(markup, content.getUnderlyingBuffer());
  }

  /**
   * Construct a {@code SmaxDocument}.
   * @param markup The markup of the document.
   * @param content The content of the document. Only the part pointed to by the markup is used.
   */
  public SmaxDocument(SmaxElement markup, StringBuffer content) {
    this.markup = markup;
    this.content = new SmaxContent(content, markup.getStartPos(), markup.getEndPos());
  }

  /**
   * Construct a {@code SmaxDocument}.
   * @param markup The markup of the document.
   * @param content The content of the document. Only the part pointed to by the markup is used.
   */
  public SmaxDocument(SmaxElement markup, CharSequence content) {
    this.markup = markup;
    this.content = new SmaxContent(content, markup.getStartPos(), markup.getEndPos());
  }

  /**
   * @return the markup of the document
   */
  public SmaxElement getMarkup() {
    return markup;
  }

  /**
   * @return the content of the document as {@code SmaxContent}
   * Note that the character positions of {@code SmaxElement}s in the document are <em>not</em> valid
   * as indexes for the {@code StringBuffer} and {@code CharSequence} methods of the {@code SmaxContent}.
   * These positions are valid for the underlying {@code StringBuffer}.
   */
  public SmaxContent getContent() {
    return content;
  }

  /**
   * @return the content buffer of the document
   * Note that the character positions of {@code SmaxElement}s in the document are valid
   * as indexes into the content buffer.
   */
  public StringBuffer getContentBuffer() {
    return content.getUnderlyingBuffer();
  }

  /**
   * Determine the ancestor nodes in the document for a given node.
   * @param node
   * @return The ancestor nodes of {@code node}, in top-down order (starting at the root node).
   */
  public Stream<SmaxElement> ancestorNodes(SmaxElement node) throws SmaxException {
    return ancestorNodesWithin(node, markup);
  }

  /**
   * Determine the ancestor nodes in the document for a given node, only in the sub-tree below {@code within}.
   * @param node
   * @param within
   * @return The nodes between {@code within} (included) and {@code node} (excluded),
   *         in top-down order (starting at {@code within}).
   */
  private Stream<SmaxElement> ancestorNodesWithin(SmaxElement node, SmaxElement within) throws SmaxException {
    int nodeStartPos = node.getStartPos();
    if (within == node) {
      // Stop when we have arrived at node.
      return Stream.empty();
    } else {
      // Find the child of within that contains node.
      SmaxElement next = within.getChildren().stream()
          .filter(child -> nodeStartPos >= child.getStartPos() && nodeStartPos < child.getEndPos()).findAny()
          .orElseThrow(() -> new SmaxException("The given node is not part of the document."));
      return Stream.concat(Stream.of(within), ancestorNodesWithin(node, next));
    }
  }

  /**
   * Determine the ancestor nodes in the document for a given character position.
   * @param charPos relative character position
   * @return a stream of nodes starting at the root of the document.
   */
  public Stream<SmaxElement> ancestorNodes(int charPos) {
    return ancestorNodesWithin(charPos + markup.getStartPos(), markup);
  }

  /**
   * Determine the ancestor nodes for a given character position, only in the sub-tree below {@code within}.
   * @param charPos absolute character position
   * @param within
   * @return a stream of nodes starting at the root of the document.
   */
  private Stream<SmaxElement> ancestorNodesWithin(int charPos, SmaxElement within) {
    return Stream.concat(Stream.of(within),
      within.getChildren().stream()
        .filter(child -> charPos >= child.getStartPos() && charPos < child.getEndPos())
        .flatMap(child -> ancestorNodesWithin(charPos, child)));
  }

  /**
   * Make an iterator over all {@code SmaxElement}s in the document that conform to a given pattern.
   * @param pattern pattern as a {@code SmaxElement} without children
   * @return an iterable of the matching nodes, in no document order.
   */
  public Iterable<SmaxElement> matchingNodes(SmaxElement pattern) {
    return () -> {
      // A set of nodes to consider. This will be empty when the iterator is exhausted
      Deque<SmaxElement> currentSet = new ArrayDeque<SmaxElement>();
      // Start at the root node.
      currentSet.push(markup);
      return new Iterator<SmaxElement>() {
        // This will be the next node, or null if we don't know yet.
        SmaxElement willBeNext = null;

        @Override
        public boolean hasNext() {
         // If willBeNext exists, it is the next node, otherwise we will have to find the next node.
          return willBeNext != null ? true : (willBeNext = next()) != null;
        }

        @Override
        public SmaxElement next() {
          // If willBeNext exists, it is already the next node.
          if (willBeNext == null) {
            // Look for the next node.
            while ((willBeNext = currentSet.poll()) != null) {
              if (willBeNext.matches(pattern)) {
                // The next node has been found.
                break;
              }
              // Add children of a node that does not match the pattern to the front of the list.
              for (SmaxElement child : willBeNext.getChildren()) {
                currentSet.add(child);
              }
            }
            // When the while loop runs out, the tree has been scanned completely.
          }
          // If there are more matching nodes, the first one is now in willBeNext.
          SmaxElement next = willBeNext;
          // The next node is returned, so we do not know the next next node.
          willBeNext = null;
          return next;
        } // next()
      }; // new Iterator()
    }; // Iterable.iterator()
  }

  /**
   * Insert a {@code SmaxElement} that has no child elements into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement that should not have children.
   * @param balancing the balancing strategy for intersecting nodes.
   * The start and end position of {@code newNode} must be relative to the content of {@code this} SmaxDocument.
   */
  public void insertMarkup(SmaxElement newNode, Balancing balancing) {
    insertMarkup(newNode, balancing, newNode.getStartPos(), newNode.getEndPos(), false);
  }

  /**
   * Insert a {@code SmaxElement} that has no child elements into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement that should not have children.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param sameRangeInner when true, a node with the same character range as an existing node will be nested inside the existing node.
   * The start and end position of {@code newNode} must be relative to the content of {@code this} SmaxDocument.
   */
  public void insertMarkup(SmaxElement newNode, Balancing balancing, boolean sameRangeInner) {
    insertMarkup(newNode, balancing, newNode.getStartPos(), newNode.getEndPos(), sameRangeInner);
  }

  /**
   * Insert a {@code SmaxElement} that has no child elements into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement that should not have children.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param startPos start position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * @param endPos end position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * The {@code startPos} and {@code endPos} position are relative to the content of {@code this} SmaxDocument.
   */
  public void insertMarkup(SmaxElement newNode, Balancing balancing, int startPos, int endPos) {
    insertMarkup(newNode, balancing, startPos, endPos, false);
  }

  /**
   * Insert a {@code SmaxElement} into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param startPos start position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * @param endPos end position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * @param sameRangeInner when true, a node with the same character range as an existing node will be nested inside the existing node.
   * The {@code startPos} and {@code endPos} position are relative to the content of {@code this} SmaxDocument.
   */
  public void insertMarkup(SmaxElement newNode, Balancing balancing, int startPos, int endPos, boolean sameRangeInner) {
    // Make a shallow copy so the children of the original newNode are not changed.
    newNode = newNode.shallowCopy();
    // Set the absolute start and end positions.
    newNode.setStartPos(markup.getStartPos() + startPos).setEndPos(markup.getStartPos() + endPos);
    // Collapse the newNode character span for START or END markers.
    if (balancing == Balancing.START) {
      newNode.setEndPos(newNode.getStartPos());
    } else if (balancing == Balancing.END) {
      newNode.setStartPos(newNode.getEndPos());
    }
    // Insert the node.
    insertMarkupInto(newNode, markup, balancing, sameRangeInner);
    // Adjust namespace prefix.
    if (newNode.getNamespaceUri() != null && !newNode.hasNamespacePrefix()) {
      String prefix = newNode.lookupPrefix(newNode.getNamespaceUri());
      if (prefix != null) {
        newNode.setName(newNode.getNamespaceUri(), newNode.getLocalName(), prefix+":"+newNode.getLocalName());
      }
    }
  }

  /**
   * Insert a SmaxElement into a sub-tree.
   * @param newNode a SmaxElement that should not have child elements.
   * @param subRoot the root of the sub-tree.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param sameRangeInner when true, a node with the same character range as an existing node will be nested inside the existing node.
   * For START and END balancing strategies, the newNode character span must already be collapsed.
   * If {@code newNode} has children, they will be changed, and existing children may be lost.
   */
  private void insertMarkupInto(SmaxElement newNode, SmaxElement subRoot, Balancing balancing, boolean sameRangeInner) {
    int newNodeStartPos = newNode.getStartPos();
    int newNodeEndPos = newNode.getEndPos();
    // A child node of subRoot that contains the newNode.
    SmaxElement containingChild = null;
    // A child-node of subRoot that is intersected by the left of the newNode.
    SmaxElement leftIntersected = null;
    // A child-node of subRoot that is intersected by the right of the newNode.
    SmaxElement rightIntersected = null;
    // Index in children of subRoot of the left-most node that will be contained in newNode (if any) or -1 (if not (yet) found).
    int firstContainedIndex = -1;
    // Index in children of subRoot of the left-most node that comes after newNode, which is the insert index.
    int newNodeInsertIndex = 0;
    // Go through the list of children once, from first to last and collect special nodes and indexes.
    // The newNodeInsertIndex points to the current child.
    for (SmaxElement child : subRoot.getChildren()) {
      int childStartPos = child.getStartPos();
      int childEndPos = child.getEndPos();
      if (childStartPos <= newNodeStartPos && childEndPos >= newNodeEndPos) {
        // The child node contains at least the same content-range as the the newNode.
        if (newNodeEndPos == newNodeStartPos && (childStartPos == newNodeStartPos || childEndPos == newNodeEndPos)) {
          // If the newNode is empty and at the start or end of the child, it is kept outside the child.
          if (newNodeEndPos == childEndPos) {
            // If the empty newNode is at the end of child, move it outside (at the child's beginning, it is outside already).
            ++newNodeInsertIndex;
          }
        } else if (childStartPos < newNodeStartPos || childEndPos > newNodeEndPos) {
          // The child node contains a larger content-range as the the newNode, so the child contains the newNode.
          containingChild = child;
        } else {
          // The child node contains the same content-range as the the newNode.
          // This is not really an intersection, and there is no clearly correct way to nest these nodes.
          // To nest the child inside the new node: firstContainedIndex = newNodeInsertIndex++;
          // To nest the new node inside the child: containingChild = child;
          if (sameRangeInner || balancing == Balancing.INNER) {
            // Nest the newNode inside the child.
            containingChild = child;
          } else {
            // Nest the child inside the new node.
            firstContainedIndex = newNodeInsertIndex++;
          }
        }
        // No need to look further.
        break;
      }
      // Check if the child node overlaps with the newNode, or is contained in the newNode.
      if (childStartPos < newNodeEndPos && childEndPos > newNodeStartPos) {
        if (childStartPos < newNodeStartPos) {
          leftIntersected = child;
        }
        if (childEndPos > newNodeEndPos) {
          rightIntersected = child;
        }
        // Set firstContainedIndex if the child overlaps the newNode.
        if (firstContainedIndex < 0) {
          firstContainedIndex = newNodeInsertIndex;
        }
      }
      // Stop when the scan is past the newNode.
      if (childStartPos >= newNodeEndPos) {
        break;
      }
      // Update insert index to next child.
      ++newNodeInsertIndex;
    }
    // Apply the balancing strategy to the special nodes and indexes.
    switch (balancing) {
    case OUTER:
      if (leftIntersected != null) {
        newNode.setStartPos(leftIntersected.getStartPos());
      }
      if (rightIntersected != null) {
        newNode.setEndPos(rightIntersected.getEndPos());
      }
      break;
    case INNER:
      if (leftIntersected != null) {
        newNode.setStartPos(leftIntersected.getEndPos());
        ++ firstContainedIndex;
      }
      if (rightIntersected != null) {
        newNode.setEndPos(rightIntersected.getStartPos());
        -- newNodeInsertIndex;
      }
      break;
    case START:
    case END:
      break;
    case BALANCE_TO_START:
      if (leftIntersected != null) {
        newNode.setEndPos(newNode.getStartPos());
        containingChild = leftIntersected;
      } else if (rightIntersected != null) {
        newNode.setEndPos(newNode.getStartPos());
        newNodeInsertIndex = firstContainedIndex;
      }
      break;
    case BALANCE_TO_END:
      if (rightIntersected != null) {
        newNode.setStartPos(newNode.getEndPos());
        containingChild = rightIntersected;
      } else if (leftIntersected != null) {
        newNode.setStartPos(newNode.getEndPos());
        firstContainedIndex = newNodeInsertIndex;
      }
      break;
    }
    if (containingChild != null) {
      // Push newNode into containing child-node.
      insertMarkupInto(newNode, containingChild, balancing, sameRangeInner);
    } else {
      // Move contained child-nodes into the newNode.
      if (firstContainedIndex >= 0 && newNodeInsertIndex > firstContainedIndex) {
        List<SmaxElement> newNodeChildren = subRoot.removeChildren(firstContainedIndex, newNodeInsertIndex);
        newNode.setChildren(newNodeChildren);
        newNodeInsertIndex = firstContainedIndex; // Because nodes have been removed.
      }
      // Insert the newNode into the root.
      subRoot.insertChild(newNodeInsertIndex, newNode);
    }
  }

  /**
   * Merge the markup from the {@code newMarkup} document into the markup of the {@code oldMarkup} document.
   * This only works if both documents have exactly the same {@code SmaxContent}, so their text content is the same.
   * @param newMarkup the new markup that will be merged into the existing markup.
   * @param balancing the balancing strategy for intersecting nodes.
   * @throws SmaxException
   */
  public void mergeMarkup(SmaxDocument newMarkup, Balancing balancing)
      throws SmaxException
  {
    if (newMarkup.content != this.content) {
      throw new SmaxException("mergeMarkup: The text content of the parameter document must be the same as the text content of the current document.");
    }
    mergeMarkup(newMarkup.markup, balancing);
  }

  /**
   * Recursively insert {@code newElement} and its children into {@code oldMarkup}.
   * @param newElement the new markup that will be merged into the existing markup.
   * @param balancing the balancing strategy for intersecting nodes.
   * The caller is responsible for ensuring that newElement is defined on the same content as the current document.
   */
  public void mergeMarkup(SmaxElement newElement, Balancing balancing) {
    this.insertMarkup(newElement, balancing, true);
    newElement.getChildren().forEach(child -> mergeMarkup(child, balancing));
  }

}
