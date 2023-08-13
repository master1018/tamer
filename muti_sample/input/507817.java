public class WayPointLabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof WayPoint) {
            WayPoint wayPoint = (WayPoint)element;
            switch (columnIndex) {
                case 0:
                    return wayPoint.getName();
                case 1:
                    return String.format("%.6f", wayPoint.getLongitude());
                case 2:
                    return String.format("%.6f", wayPoint.getLatitude());
                case 3:
                    if (wayPoint.hasElevation()) {
                        return String.format("%.1f", wayPoint.getElevation());
                    } else {
                        return "-";
                    }
                case 4:
                    return wayPoint.getDescription();
            }
        }
        return null;
    }
    public void addListener(ILabelProviderListener listener) {
    }
    public void dispose() {
    }
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }
    public void removeListener(ILabelProviderListener listener) {
    }
}
