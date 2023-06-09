package org.exist.storage.repair;

import org.exist.collections.Collection;
import org.exist.dom.BinaryDocument;
import org.exist.dom.DocumentImpl;
import org.exist.dom.ElementImpl;
import org.exist.dom.StoredNode;
import org.exist.numbering.NodeId;
import org.exist.security.PermissionDeniedException;
import org.exist.stax.EmbeddedXMLStreamReader;
import org.exist.storage.DBBroker;
import org.exist.storage.NativeBroker;
import org.exist.storage.btree.BTreeCallback;
import org.exist.storage.btree.Value;
import org.exist.storage.index.CollectionStore;
import org.exist.storage.io.VariableByteInput;
import org.exist.storage.lock.Lock;
import org.exist.xmldb.XmldbURI;
import org.exist.xquery.TerminatedException;
import org.w3c.dom.Node;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ConsistencyCheck {

    private Stack elementStack = new Stack();

    private static class ElementNode {

        ElementImpl elem;

        int childCount = 0;

        NodeId prevSibling = null;

        ElementNode(ElementImpl element) {
            this.elem = element;
        }
    }

    private DBBroker broker;

    public ConsistencyCheck(DBBroker broker) {
        this.broker = broker;
    }

    public int getDocumentCount() {
        DocumentCallback cb = new DocumentCallback(null, null, false);
        broker.getResourcesFailsafe(cb);
        return cb.docCount;
    }

    public List checkDocuments(ProgressCallback progress) {
        List errors = new ArrayList();
        DocumentCallback cb = new DocumentCallback(errors, progress, true);
        broker.getResourcesFailsafe(cb);
        return errors;
    }

    public void checkDocument(String path) throws PermissionDeniedException, InconsistentDocException {
        DocumentImpl doc = broker.getXMLResource(XmldbURI.create(path), Lock.READ_LOCK);
        try {
            checkXMLTree(doc);
        } finally {
            doc.getUpdateLock().release(Lock.READ_LOCK);
        }
    }

    private ErrorReport checkXMLTree(DocumentImpl doc) {
        try {
            ElementImpl root = (ElementImpl) doc.getDocumentElement();
            EmbeddedXMLStreamReader reader = doc.getBroker().getXMLStreamReader(root, true);
            NodeId nodeId = null;
            while (reader.hasNext()) {
                int status = reader.next();
                nodeId = (NodeId) reader.getProperty(EmbeddedXMLStreamReader.PROPERTY_NODE_ID);
                ElementNode parent = null;
                if (status != XMLStreamReader.END_ELEMENT && !elementStack.isEmpty()) {
                    parent = (ElementNode) elementStack.peek();
                    parent.childCount++;
                    if (parent.prevSibling != null && !(nodeId.isSiblingOf(parent.prevSibling) && nodeId.compareTo(parent.prevSibling) > 0)) {
                        return new ErrorReport(ErrorReport.INCORRECT_NODE_ID, "Node " + nodeId + " is not a sibling of " + parent.prevSibling);
                    }
                    parent.prevSibling = nodeId;
                }
                switch(status) {
                    case XMLStreamReader.START_ELEMENT:
                        StoredNode node = reader.getNode();
                        if (node.getNodeType() != Node.ELEMENT_NODE) return new ErrorReport(ErrorReport.INCORRECT_NODE_TYPE, "Expected an element node, received node of type " + node.getNodeType());
                        elementStack.push(new ElementNode((ElementImpl) node));
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        if (elementStack.isEmpty()) return new ErrorReport(ErrorReport.NODE_HIERARCHY, "Error in node hierarchy: received END_ELEMENT event " + "but stack was empty!");
                        ElementNode lastElem = (ElementNode) elementStack.pop();
                        if (lastElem.childCount != lastElem.elem.getChildCount()) return new ErrorReport(ErrorReport.NODE_HIERARCHY, "Element reports incorrect child count: expected " + lastElem.elem.getChildCount() + " but found " + lastElem.childCount);
                        break;
                    default:
                        if (parent != null) {
                            if (!nodeId.isChildOf(parent.elem.getNodeId())) return new ErrorReport(ErrorReport.NODE_HIERARCHY, "Node " + nodeId + " is not a child of " + parent.elem.getNodeId());
                        }
                }
            }
            if (!elementStack.isEmpty()) {
                return new ErrorReport(ErrorReport.NODE_HIERARCHY, "Error in node hierarchy: reached end of tree but " + "stack was not empty!");
            }
            return null;
        } catch (IOException e) {
            return new ErrorReport(ErrorReport.ACCESS_FAILED, e.getMessage(), e);
        } catch (XMLStreamException e) {
            return new ErrorReport(ErrorReport.ACCESS_FAILED, e.getMessage(), e);
        }
    }

    private class DocumentCallback implements BTreeCallback {

        private List errors;

        private ProgressCallback progress;

        private int docCount = 0;

        private boolean checkDocs;

        private DocumentCallback(List errors, ProgressCallback progress, boolean checkDocs) {
            this.errors = errors;
            this.progress = progress;
            this.checkDocs = checkDocs;
        }

        public boolean indexInfo(Value key, long pointer) throws TerminatedException {
            CollectionStore store = (CollectionStore) ((NativeBroker) broker).getStorage(NativeBroker.COLLECTIONS_DBX_ID);
            int collectionId = CollectionStore.DocumentKey.getCollectionId(key);
            int docId = CollectionStore.DocumentKey.getDocumentId(key);
            try {
                byte type = key.data()[key.start() + Collection.LENGTH_COLLECTION_ID + DocumentImpl.LENGTH_DOCUMENT_TYPE];
                VariableByteInput istream = store.getAsStream(pointer);
                DocumentImpl doc = null;
                if (type == DocumentImpl.BINARY_FILE) doc = new BinaryDocument(broker); else doc = new DocumentImpl(broker);
                doc.read(istream);
                docCount++;
                if (checkDocs) {
                    if (progress != null) progress.startDocument(doc.getFileURI().toString());
                    if (type == DocumentImpl.XML_FILE) {
                        ErrorReport report = checkXMLTree(doc);
                        if (report != null) {
                            report.setDocumentId(docId);
                            report.setCollectionId(collectionId);
                            errors.add(report);
                            if (progress != null) progress.error(report);
                        }
                    }
                }
            } catch (Exception e) {
                ErrorReport error = new ErrorReport(ErrorReport.ACCESS_FAILED, e.getMessage(), e);
                errors.add(error);
                if (progress != null) progress.error(error);
            }
            return true;
        }
    }

    public interface ProgressCallback {

        public void startDocument(String path);

        public void error(ErrorReport error);
    }
}
