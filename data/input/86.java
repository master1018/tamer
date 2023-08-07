public class ExtentPolicyFolder1 implements ExtentPolicy {
    public static final String ID = "Folder1";
    public String getID() {
        return ID;
    }
    public int getNextExtentFolder(int index, int maxNumExtents, VirtualFolders vFolders) {
        int r = -1;
        Integer[] indexes = vFolders.getIndexes(Attribute.READ_WRITE);
        if (indexes.length > 0) r = indexes[0];
        return r;
    }
    public void store(ParameterSet parameterSet) {
        parameterSet.setString(ExtentPolicy.POLICY_ID, getID());
    }
    public void load(ParameterSet parameterSet) {
    }
    public String getHelp() {
        return "Puts all extents ino the first folder-used for testing";
    }
}
