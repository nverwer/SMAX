package org.greenmercury.smax;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
   * @return an {@code Iterable} of the matching nodes, in no document order.
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
   * @return A shallow copy of {@code newNode} is returned, because some of its properties are changed.
   */
  public SmaxElement insertMarkup(SmaxElement newNode, Balancing balancing) {
    return insertMarkup(newNode, balancing, newNode.getStartPos(), newNode.getEndPos(), null);
  }

  /**
   * Insert a {@code SmaxElement} that has no child elements into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement that should not have children.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param sameRangeReverseBalancing A set of {@code SmaxElement}s.
   *   If {@code newNode} has the same character range as a node in {@code sameRangeReverseBalancing},
   *   the {@code balancing} is changed from OUTER to INNER, or from INNER to OUTER.
   * The start and end position of {@code newNode} must be relative to the content of {@code this} SmaxDocument.
   * @return A shallow copy of {@code newNode} is returned, because some of its properties are changed.
   */
  public SmaxElement insertMarkup(SmaxElement newNode, Balancing balancing, Set<SmaxElement> sameRangeReverseBalancing) {
    return insertMarkup(newNode, balancing, newNode.getStartPos(), newNode.getEndPos(), sameRangeReverseBalancing);
  }

  /**
   * Insert a {@code SmaxElement} that has no child elements into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement that should not have children.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param startPos start position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * @param endPos end position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * The {@code startPos} and {@code endPos} position are relative to the content of {@code this} SmaxDocument.
   * @return A shallow copy of {@code newNode} is returned, because some of its properties are changed.
   */
  public SmaxElement insertMarkup(SmaxElement newNode, Balancing balancing, int startPos, int endPos) {
    return insertMarkup(newNode, balancing, startPos, endPos, null);
  }

  /**
   * Insert a {@code SmaxElement} into the markup tree of a {@code SmaxDocument}.
   * @param newNode a SmaxElement.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param startPos start position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * @param endPos end position of the content within {@code newNode}, relative to the content of the SmaxDocument.
   * @param sameRangeReverseBalancing A set of {@code SmaxElement}s.
   *   If {@code newNode} has the same character range as a node in {@code sameRangeReverseBalancing},
   *   the {@code balancing} is changed from OUTER to INNER, or from INNER to OUTER.
   * The {@code startPos} and {@code endPos} position are relative to the content of {@code this} SmaxDocument.
   * @return A shallow copy of {@code newNode} is returned, because some of its properties are changed.
   */
  public SmaxElement insertMarkup(SmaxElement newNode, Balancing balancing, int startPos, int endPos, Set<SmaxElement> sameRangeReverseBalancing) {
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
    insertMarkupInto(newNode, markup, balancing, sameRangeReverseBalancing);
    // Adjust namespace prefix.
    if (newNode.getNamespaceUri() != null && !newNode.hasNamespacePrefix()) {
      String prefix = newNode.lookupPrefix(newNode.getNamespaceUri());
      if (prefix != null) {
        newNode.setName(newNode.getNamespaceUri(), newNode.getLocalName(), prefix+":"+newNode.getLocalName());
      }
    }
    return newNode;
  }

  /**
   * Insert a SmaxElement into a sub-tree.
   * @param newNode a SmaxElement that must not have child elements.
   *   If {@code newNode} has children, an exception will be thrown.
   * @param subRoot the root of the sub-tree.
   * @param balancing the balancing strategy for intersecting nodes.
   * @param sameRangeReverseBalancing A set of {@code SmaxElement}s.
   *   If {@code newNode} has the same character range as a node in {@code sameRangeReverseBalancing},
   *   the {@code balancing} is changed from OUTER to INNER, or from INNER to OUTER.
   * For START and END balancing strategies, the newNode character span must already be collapsed.
   */
  private void insertMarkupInto(SmaxElement newNode, SmaxElement subRoot, Balancing balancing, Set<SmaxElement> sameRangeReverseBalancing) {
    if (newNode.hasChildNodes()) {
      throw new IllegalArgumentException("The node that is inserted into a markup tree must not have child elements.");
    }
    boolean outerNewNode = balancing == Balancing.OUTER || balancing == Balancing.START || balancing == Balancing.END;
    int newNodeStartPos = newNode.getStartPos();
    int newNodeEndPos = newNode.getEndPos();
    boolean newNodeIsEmpty = newNodeEndPos == newNodeStartPos;
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
      boolean childIsEmpty = childEndPos == childStartPos;
      // The child comes after the newNode if its start position is greater than the newNode's end position, or equal and newNode can not contain the child.
      boolean childIsAfterNewNode = childStartPos > newNodeEndPos ||
          ( childStartPos == newNodeEndPos && ( outerNewNode ? !childIsEmpty : !newNodeIsEmpty ) );
System.out.println(child.toString() + " is after " + newNode.toString() + ": " + childIsAfterNewNode);
      if (childIsAfterNewNode) {
        // No need to look at this and following children, whose start position is after the newNode's end position.
        break;
      }
      if (childStartPos <= newNodeStartPos && childEndPos >= newNodeEndPos) {
        // The child node contains at least the same content-range as the the newNode: `<child>...<newNode>...</newNode>...</child>`.
        if (newNodeIsEmpty && (childStartPos == newNodeStartPos || childEndPos == newNodeEndPos)) {
          // The newNode is empty and at the start or end of the child.
          // For OUTER balancing, the newNode is kept outside the child. If the child is also empty, the newNode comes after the child.
          if (!outerNewNode) {
            // For non-outer balancing, the newNode is contained in the child.
            containingChild = child;
          }
        } else if (childStartPos < newNodeStartPos || childEndPos > newNodeEndPos) {
          // The child node contains a larger content-range than the the newNode, so the child contains the newNode.
          containingChild = child;
        } else {
          // The child node contains the same content-range as the the newNode.
          // This is not a proper intersection, and there is no clearly correct way to nest these nodes.
          // Use the balancing and sameRangeReverseBalancing to determine how to nest the newNode.
          boolean reverseBalancing = sameRangeReverseBalancing != null && sameRangeReverseBalancing.contains(child);
          if ( (balancing == Balancing.INNER && !reverseBalancing) || (balancing == Balancing.OUTER && reverseBalancing) ) {
            // Nest the newNode inside the child.
            containingChild = child;
          } else {
            // Nest the child inside the newNode.
            firstContainedIndex = newNodeInsertIndex++;
          }
        }
      } else {
        // The newNode is not contained in the child, and contains more content at the start or end (or both) of the child.
        // This means that `childStartPos > newNodeStartPos || childEndPos < newNodeEndPos`.
        // Check if the child node overlaps with, or is contained within the newNode.
        // When the child is empty and at the start or end of newNode, it does not overlap, but it may be contained, depending on balancing.
        boolean overlapsOrIscontained = outerNewNode ?
            childStartPos <= newNodeEndPos && childEndPos >= newNodeStartPos :
            childStartPos < newNodeEndPos && childEndPos > newNodeStartPos;
System.out.println(child.toString() + " overlaps or is contained in " + newNode.toString() + ": " + overlapsOrIscontained);
        if (overlapsOrIscontained) {
          // The child is not completely outside the newNode.
          if (childStartPos < newNodeStartPos) {
            // There is an overlap at the left side of the newNode: `<child>...<newNode>...</child>...</newNode>`.
            leftIntersected = child;
          }
          if (childEndPos > newNodeEndPos) {
            // There is an overlap at the right side of the newNode: `<newNode>...<child>...</newNode>...</child>`.
            rightIntersected = child;
          }
          // If neither leftIntersected nor rightIntersected is set, the child is contained within the newNode: `<newNode>...<child>...</child>...</newNode>`.
          // Set firstContainedIndex if this is the first child that overlaps the newNode.
          if (firstContainedIndex < 0) {
            firstContainedIndex = newNodeInsertIndex;
          }
        }
      }
      // Update insert index to next child.
      ++newNodeInsertIndex;
    }
    // Apply the balancing strategy by adjusting positions, indexes or the containing child.
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
      break;
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
      insertMarkupInto(newNode, containingChild, balancing, sameRangeReverseBalancing);
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
    this.insertMarkup(newElement, balancing);
    newElement.getChildren().forEach(child -> mergeMarkup(child, balancing));
  }

}
