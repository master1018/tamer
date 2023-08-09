public class ScreenShotDialog extends Dialog {
    private Label mBusyLabel;
    private Label mImageLabel;
    private Button mSave;
    private IDevice mDevice;
    private RawImage mRawImage;
    private Clipboard mClipboard;
    public ScreenShotDialog(Shell parent) {
        this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
        mClipboard = new Clipboard(parent.getDisplay());
    }
    public ScreenShotDialog(Shell parent, int style) {
        super(parent, style);
    }
    public void open(IDevice device) {
        mDevice = device;
        Shell parent = getParent();
        Shell shell = new Shell(parent, getStyle());
        shell.setText("Device Screen Capture");
        createContents(shell);
        shell.pack();
        shell.open();
        updateDeviceImage(shell);
        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
    private void createContents(final Shell shell) {
        GridData data;
        final int colCount = 5;
        shell.setLayout(new GridLayout(colCount, true));
        Button refresh = new Button(shell, SWT.PUSH);
        refresh.setText("Refresh");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        refresh.setLayoutData(data);
        refresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateDeviceImage(shell);
            }
        });
        Button rotate = new Button(shell, SWT.PUSH);
        rotate.setText("Rotate");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        rotate.setLayoutData(data);
        rotate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mRawImage != null) {
                    mRawImage = mRawImage.getRotated();
                    updateImageDisplay(shell);
                }
            }
        });
        mSave = new Button(shell, SWT.PUSH);
        mSave.setText("Save");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        mSave.setLayoutData(data);
        mSave.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveImage(shell);
            }
        });
        Button copy = new Button(shell, SWT.PUSH);
        copy.setText("Copy");
        copy.setToolTipText("Copy the screenshot to the clipboard");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        copy.setLayoutData(data);
        copy.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                copy();
            }
        });
        Button done = new Button(shell, SWT.PUSH);
        done.setText("Done");
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.widthHint = 80;
        done.setLayoutData(data);
        done.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                shell.close();
            }
        });
        mBusyLabel = new Label(shell, SWT.NONE);
        mBusyLabel.setText("Preparing...");
        data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = colCount;
        mBusyLabel.setLayoutData(data);
        mImageLabel = new Label(shell, SWT.BORDER);
        data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        data.horizontalSpan = colCount;
        mImageLabel.setLayoutData(data);
        Display display = shell.getDisplay();
        mImageLabel.setImage(ImageHelper.createPlaceHolderArt(
                display, 50, 50, display.getSystemColor(SWT.COLOR_BLUE)));
        shell.setDefaultButton(done);
    }
    private void copy() {
        mClipboard.setContents(
                new Object[] {
                        mImageLabel.getImage().getImageData()
                }, new Transfer[] {
                        ImageTransfer.getInstance()
                });
    }
    private void updateDeviceImage(Shell shell) {
        mBusyLabel.setText("Capturing...");     
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
        mRawImage = getDeviceImage();
        updateImageDisplay(shell);
    }
    private void updateImageDisplay(Shell shell) {
        Image image;
        if (mRawImage == null) {
            Display display = shell.getDisplay();
            image = ImageHelper.createPlaceHolderArt(
                    display, 320, 240, display.getSystemColor(SWT.COLOR_BLUE));
            mSave.setEnabled(false);
            mBusyLabel.setText("Screen not available");
        } else {
            PaletteData palette = new PaletteData(
                    mRawImage.getRedMask(),
                    mRawImage.getGreenMask(),
                    mRawImage.getBlueMask());
            ImageData imageData = new ImageData(mRawImage.width, mRawImage.height,
                    mRawImage.bpp, palette, 1, mRawImage.data);
            image = new Image(getParent().getDisplay(), imageData);
            mSave.setEnabled(true);
            mBusyLabel.setText("Captured image:");
        }
        mImageLabel.setImage(image);
        mImageLabel.pack();
        shell.pack();
        shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
    }
    private RawImage getDeviceImage() {
        try {
            return mDevice.getScreenshot();
        }
        catch (IOException ioe) {
            Log.w("ddms", "Unable to get frame buffer: " + ioe.getMessage());
            return null;
        }
    }
    private void saveImage(Shell shell) {
        FileDialog dlg = new FileDialog(shell, SWT.SAVE);
        String fileName;
        dlg.setText("Save image...");
        dlg.setFileName("device.png");
        dlg.setFilterPath(DdmUiPreferences.getStore().getString("lastImageSaveDir"));
        dlg.setFilterNames(new String[] {
            "PNG Files (*.png)"
        });
        dlg.setFilterExtensions(new String[] {
            "*.png" 
        });
        fileName = dlg.open();
        if (fileName != null) {
            DdmUiPreferences.getStore().setValue("lastImageSaveDir", dlg.getFilterPath());
            Log.d("ddms", "Saving image to " + fileName);
            ImageData imageData = mImageLabel.getImage().getImageData();
            try {
                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[] { imageData };
                loader.save(fileName, SWT.IMAGE_PNG);
            }
            catch (SWTException e) {
                Log.w("ddms", "Unable to save " + fileName + ": " + e.getMessage());
            }
        }
    }
}
