public class TrackLabelProvider implements ITableLabelProvider {
    public Image getColumnImage(Object element, int columnIndex) {
        return null;
    }
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof Track) {
            Track track = (Track)element;
            switch (columnIndex) {
                case 0:
                    return track.getName();
                case 1:
                    return Integer.toString(track.getPointCount());
                case 2:
                    long time = track.getFirstPointTime();
                    if (time != -1) {
                        return new Date(time).toString();
                    }
                    break;
                case 3:
                    time = track.getLastPointTime();
                    if (time != -1) {
                        return new Date(time).toString();
                    }
                    break;
                case 4:
                    return track.getComment();
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
