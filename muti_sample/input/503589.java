class FileLabelProvider implements ILabelProvider, ITableLabelProvider {
    private Image mFileImage;
    private Image mFolderImage;
    private Image mPackageImage;
    private Image mOtherImage;
    public FileLabelProvider(Image fileImage, Image folderImage,
            Image packageImage, Image otherImage) {
        mFileImage = fileImage;
        mFolderImage = folderImage;
        mOtherImage = otherImage;
        if (packageImage != null) {
            mPackageImage = packageImage;
        } else {
            mPackageImage = fileImage;
        }
    }
    public FileLabelProvider() {
    }
    public Image getImage(Object element) {
        return null;
    }
    public String getText(Object element) {
        return null;
    }
    public Image getColumnImage(Object element, int columnIndex) {
        if (columnIndex == 0) {
            if (element instanceof FileEntry) {
                FileEntry entry = (FileEntry)element;
                switch (entry.getType()) {
                    case FileListingService.TYPE_FILE:
                    case FileListingService.TYPE_LINK:
                        if (entry.isApplicationPackage()) {
                            return mPackageImage;
                        }
                        return mFileImage;
                    case FileListingService.TYPE_DIRECTORY:
                    case FileListingService.TYPE_DIRECTORY_LINK:
                        return mFolderImage;
                }
            }
            return mOtherImage;
        }
        return null;
    }
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof FileEntry) {
            FileEntry entry = (FileEntry)element;
            switch (columnIndex) {
                case 0:
                    return entry.getName();
                case 1:
                    return entry.getSize();
                case 2:
                    return entry.getDate();
                case 3:
                    return entry.getTime();
                case 4:
                    return entry.getPermissions();
                case 5:
                    return entry.getInfo();
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
