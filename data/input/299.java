public class ProgramNodeExpander extends TreeNodeExpander {
    public ProgramNodeExpander() {
    }
    public void expand(IndexedTypedTreeNode node) {
        Object[] tempIDs;
        DbIterator rowIt;
        try {
            tempIDs = node.getID();
            rowIt = prv.getFileTypesByProgram(NumberUtils.number2long(tempIDs[0])).iterator();
            TreeUtils.fillTree((MediTreeNode) node, rowIt, fileTypeIndexes, fileTypeIDIndexes, "FileType", ": ", false);
        } catch (DbException e) {
            System.out.println(e.getMessage());
        }
    }
}
