public class PositionTesterContextMenuListener implements OnCreateContextMenuListener {
    private int groupPosition, childPosition;
    private static final int ADAPTER_TYPE = -1;
    private int testType; 
    private String errorMessage;
    public void expectGroupContextMenu(int groupPosition) {
        this.groupPosition = groupPosition;
        testType = ExpandableListView.PACKED_POSITION_TYPE_GROUP;
    }
    public void expectChildContextMenu(int groupPosition, int childPosition) {
        this.groupPosition = groupPosition;
        this.childPosition = childPosition;
        testType = ExpandableListView.PACKED_POSITION_TYPE_CHILD;
    }
    public void expectAdapterContextMenu(int flatPosition) {
        this.groupPosition = flatPosition;
        testType = ADAPTER_TYPE;
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        errorMessage = null;
        if (testType == ADAPTER_TYPE) {
            if (!isTrue("MenuInfo is not an AdapterContextMenuInfo",
                    menuInfo instanceof AdapterContextMenuInfo)) {
                return;
            }
            AdapterContextMenuInfo adapterContextMenuInfo = (AdapterContextMenuInfo) menuInfo;
            if (!areEqual("Wrong flat position", groupPosition, adapterContextMenuInfo.position)) {
                return;
            }
        } else {
            if (!isTrue("MenuInfo is not an ExpandableListContextMenuInfo",
                    menuInfo instanceof ExpandableListView.ExpandableListContextMenuInfo)) {
                return;
            }
            ExpandableListView.ExpandableListContextMenuInfo elvMenuInfo =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            long packedPosition = elvMenuInfo.packedPosition;
            int packedPositionType = ExpandableListView.getPackedPositionType(packedPosition);
            if (!areEqual("Wrong packed position type", testType, packedPositionType)) {
                return;
            }
            int packedPositionGroup = ExpandableListView.getPackedPositionGroup(packedPosition);
            if (!areEqual("Wrong group position", groupPosition, packedPositionGroup)) {
                return;
            }
            if (testType == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                int packedPositionChild = ExpandableListView.getPackedPositionChild(packedPosition);
                if (!areEqual("Wrong child position", childPosition, packedPositionChild)) {
                    return;
                }
            }
        }
    }
    private boolean areEqual(String message, int expected, int actual) {
        if (expected != actual) {
            errorMessage = String.format(message + " (%d vs %d", expected, actual);
            return false;
        }
        return true;
    }
    private boolean isTrue(String message, boolean value) {
        if (!value) {
            errorMessage = message;
            return false;
        }
        return true;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
}
