public final class SimpleFileQueueStorage extends ObjectPool implements QueueStorage {
    public static final String itemFileSuffix = ".qi";
    public static final String oldItemFileSuffix = ".old";
    private static final String oldItemFileCompleteSuffix = itemFileSuffix + oldItemFileSuffix;
    private static final int maxAttempts = 3;
    private static final int attemptDelay = 100;
    private StringProperty defaultQueueItemFileNamePrefix;
    private final boolean[] existingSubdirs = new boolean[100];
    private boolean useAlternativeObjectOutputStreamResetMethod = false;
    private final java.io.FilenameFilter queueItemFileFilter = new java.io.FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(itemFileSuffix);
        }
    };
    private final java.io.FilenameFilter oldQueueItemFileFilter = new java.io.FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(oldItemFileCompleteSuffix);
        }
    };
    public SimpleFileQueueStorage(SubComponent parent) {
        this(parent, "SimpleFileQueueStorage");
    }
    public SimpleFileQueueStorage(SubComponent parent, String name) {
        super(parent, name, 100);
        defaultQueueItemFileNamePrefix = new StringProperty(this, "queueItemSavePath", "." + File.separator + parent.getName() + ".storage" + File.separator, StringProperty.MODIFIABLE_OWNER_RESTART);
        addProperty(defaultQueueItemFileNamePrefix);
        if (com.teletalk.jserver.util.JavaBugUtils.isUsingJava1_3_0()) {
            this.setUseAlternativeObjectOutputStreamResetMethod(true);
            logInfo("Defaulting flag for alternative object output stream reset method to true, because Java 2 version 1.3 is used.");
        }
    }
    public void doInitialize() {
        super.doInitialize();
        super.initFromConfiguredProperty(this.defaultQueueItemFileNamePrefix, "Queueitem savepath", false, true);
        if (this.isUsingAlternativeObjectOutputStreamResetMethod()) {
            logWarning("Note: this SimpleFileQueueStorage uses an alternative method for resetting the object output streams used when writing QueueItems to disk. This alternative method may be inefficient.");
        }
        File savePathDir = new File(defaultQueueItemFileNamePrefix.stringValue());
        savePathDir.mkdirs();
        if (!savePathDir.isDirectory() || !savePathDir.exists()) {
            throw new StatusTransitionException("Unable to access queue item save path ('" + defaultQueueItemFileNamePrefix.stringValue() + "')!");
        }
        final StringBuffer saveDir = new StringBuffer(defaultQueueItemFileNamePrefix.stringValue());
        int appendIndex = saveDir.length();
        String dirName = null;
        for (int i = 0; i < existingSubdirs.length; i++) {
            if (i >= 10) dirName = String.valueOf(i); else dirName = "0" + String.valueOf(i);
            if (new File(saveDir.replace(appendIndex, saveDir.length(), dirName).toString()).isDirectory()) {
                existingSubdirs[i] = true;
            } else {
                existingSubdirs[i] = false;
            }
        }
    }
    public final void setUseAlternativeObjectOutputStreamResetMethod(boolean useAlternativeResetMethod) {
        this.useAlternativeObjectOutputStreamResetMethod = useAlternativeResetMethod;
    }
    public final boolean isUsingAlternativeObjectOutputStreamResetMethod() {
        return useAlternativeObjectOutputStreamResetMethod;
    }
    public Object createObject() throws Exception {
        return new SimpleFileQueueStorageFileWriter(this);
    }
    public boolean validateObject(Object obj, boolean cleanUpValidation) {
        return ((SimpleFileQueueStorageFileWriter) obj).validate();
    }
    public void finalizeObject(Object obj) {
        ((SimpleFileQueueStorageFileWriter) obj).destroy();
        obj = null;
    }
    public void propertyModified(Property property) {
        if (property == defaultQueueItemFileNamePrefix) {
            String value = defaultQueueItemFileNamePrefix.stringValue();
            if (!(value.endsWith("/") || value.endsWith("\\"))) {
                defaultQueueItemFileNamePrefix.setNotificationMode(false);
                defaultQueueItemFileNamePrefix.setValue(value + File.separator);
                defaultQueueItemFileNamePrefix.setNotificationMode(true);
            }
        }
        super.propertyModified(property);
    }
    public boolean validatePropertyModification(Property property) {
        if (property == defaultQueueItemFileNamePrefix) {
            String value = defaultQueueItemFileNamePrefix.stringValue();
            if (!(value.endsWith("/") || value.endsWith("\\"))) value = value + File.separator;
            return true;
        } else return super.validatePropertyModification(property);
    }
    public String getQueueItemFileName(final QueueItem item) throws QueueStorageException {
        final StringBuffer fileName = new StringBuffer();
        final String itemId = item.getId();
        String itemIdLast2 = null;
        boolean err = false;
        if (itemId.length() < 2) {
            err = true;
        } else {
            itemIdLast2 = itemId.substring(itemId.length() - 2);
            try {
                final short subDirNo = (short) (Short.parseShort(itemIdLast2, 16) % 100);
                if (subDirNo >= 10) itemIdLast2 = String.valueOf(subDirNo); else itemIdLast2 = "0" + String.valueOf(subDirNo);
                if (!existingSubdirs[subDirNo]) {
                    synchronized (existingSubdirs) {
                        if (!existingSubdirs[subDirNo]) {
                            new File(defaultQueueItemFileNamePrefix.getValueAsString() + itemIdLast2 + File.separator).mkdir();
                            existingSubdirs[subDirNo] = true;
                        }
                    }
                }
            } catch (Exception e) {
                err = true;
            }
        }
        if (err) {
            itemIdLast2 = "00";
            synchronized (existingSubdirs) {
                if (!existingSubdirs[0]) {
                    new File(defaultQueueItemFileNamePrefix.getValueAsString() + itemIdLast2 + File.separator).mkdir();
                    existingSubdirs[0] = true;
                }
            }
        }
        fileName.append(defaultQueueItemFileNamePrefix.getValueAsString());
        fileName.append(itemIdLast2);
        fileName.append(File.separator);
        fileName.append(itemId);
        fileName.append(itemFileSuffix);
        return fileName.toString();
    }
    public void storeQueueItem(final QueueItem item) throws QueueStorageException {
        final String queueItemFileName = getQueueItemFileName(item);
        final SimpleFileQueueStorageFileWriter writer = (SimpleFileQueueStorageFileWriter) this.checkOutWait();
        try {
            writer.store(item, queueItemFileName);
        } catch (IOException e) {
            throw new QueueStorageException("Error while storing QueueItem (" + item + ")", e);
        } finally {
            if (writer != null) this.checkIn(writer);
        }
    }
    public void storeQueueItems(QueueItem[] items) throws QueueStorageException {
        String queueItemFileName;
        final SimpleFileQueueStorageFileWriter writer = (SimpleFileQueueStorageFileWriter) this.checkOutWait();
        try {
            for (int i = 0; i < items.length; i++) {
                queueItemFileName = this.getQueueItemFileName(items[i]);
                writer.store(items[i], queueItemFileName);
            }
        } catch (IOException e) {
            throw new QueueStorageException("Error while storing QueueItems (" + QueueItem.concatIds(items) + ")", e);
        } finally {
            if (writer != null) this.checkIn(writer);
        }
    }
    public void updateStoredQueueItem(final QueueItem item) throws QueueStorageException {
        storeQueueItem(item);
    }
    public void updateQueueItemStatus(final QueueItem item) throws QueueStorageException {
        final String queueItemFileName = getQueueItemFileName(item);
        final SimpleFileQueueStorageFileWriter writer = (SimpleFileQueueStorageFileWriter) this.checkOutWait();
        try {
            writer.updateStatus(item, queueItemFileName);
        } catch (IOException e) {
            throw new QueueStorageException("Error while updating QueueItem (" + item + ")", e);
        } finally {
            if (writer != null) this.checkIn(writer);
        }
    }
    public void removeStoredQueueItem(final QueueItem item) {
        final String queueItemFileNameBase = getQueueItemFileName(item);
        final File itemFile = new File(queueItemFileNameBase);
        itemFile.delete();
    }
    public java.util.List restoreQueueFromStorage() throws QueueStorageException {
        java.util.ArrayList queueItems = new java.util.ArrayList();
        logInfo("Restoring QueueItems from persistent storage.");
        final StringBuffer baseDir = new StringBuffer(defaultQueueItemFileNamePrefix.getValueAsString());
        int appendIndex = baseDir.length();
        int appendIndex2;
        File subDir;
        String[] files = null;
        String[] oldFiles = null;
        QueueItem item;
        String currentFileName;
        String oldFileName;
        for (int i = 0; i < 100; i++) {
            if (existingSubdirs[i]) {
                baseDir.delete(appendIndex, baseDir.length());
                if (i < 10) baseDir.append("0" + String.valueOf(i)); else baseDir.append(String.valueOf(i));
                baseDir.append(File.separator);
                appendIndex2 = baseDir.length();
                subDir = new File(baseDir.toString());
                for (int attempt = 0; ((files == null) || (oldFiles == null)) && (attempt < maxAttempts); attempt++) {
                    files = subDir.list(queueItemFileFilter);
                    oldFiles = subDir.list(oldQueueItemFileFilter);
                    if ((files == null) || (oldFiles == null)) {
                        try {
                            Thread.sleep(attemptDelay);
                        } catch (InterruptedException ie) {
                        }
                    }
                }
                if ((files == null) || (oldFiles == null)) {
                    logError("Unable to list files in directory '" + defaultQueueItemFileNamePrefix.getValueAsString() + "'!");
                    break;
                }
                for (int f = 0; f < files.length; f++) {
                    baseDir.replace(appendIndex2, baseDir.length(), files[f]);
                    currentFileName = baseDir.toString();
                    int matchingOldItemIndex = -1;
                    item = null;
                    for (int o = 0; o < oldFiles.length; o++) {
                        if (oldFiles[o] != null) {
                            if (oldFiles[o].startsWith(files[f])) {
                                matchingOldItemIndex = o;
                                break;
                            }
                        }
                    }
                    if (matchingOldItemIndex >= 0) {
                        baseDir.replace(appendIndex2, baseDir.length(), oldFiles[matchingOldItemIndex]);
                        oldFileName = baseDir.toString();
                    } else oldFileName = null;
                    try {
                        item = readQueueItem(currentFileName);
                        if (matchingOldItemIndex >= 0) {
                            new File(oldFileName).delete();
                            oldFiles[matchingOldItemIndex] = null;
                        }
                    } catch (Exception e) {
                        if (matchingOldItemIndex >= 0) {
                            if (new File(currentFileName).renameTo(new File(currentFileName + ".err"))) {
                                logError("Error while restoring QueueItem from file (" + currentFileName + "). Renaming file to " + currentFileName + ".err" + ". Attempting to replace QueueItem with existing backup copy (" + oldFileName + "). Exception", e);
                            } else {
                                logError("Error while restoring QueueItem from file (" + currentFileName + "). Failed to rename file to " + currentFileName + ".err. Deleting file. Attempting to replace QueueItem with existing backup copy (" + oldFileName + "). Exception", e);
                                new File(currentFileName).delete();
                            }
                            try {
                                item = readQueueItem(oldFileName);
                                new File(oldFileName).renameTo(new File(currentFileName));
                            } catch (Exception e1) {
                                if (new File(oldFileName).renameTo(new File(oldFileName + ".err"))) {
                                    logError("Error while restoring QueueItem from file (" + oldFileName + "). Renaming file to " + oldFileName + ".err" + ".", e1);
                                } else {
                                    logError("Error while restoring QueueItem from file (" + oldFileName + "). Failed to rename file to " + oldFileName + ".err. Deleting file.", e1);
                                    new File(oldFileName).delete();
                                }
                            } finally {
                                oldFiles[matchingOldItemIndex] = null;
                            }
                        } else {
                            if (new File(currentFileName).renameTo(new File(currentFileName + ".err"))) {
                                logError("Error while restoring QueueItem from file (" + currentFileName + "). Renaming file to " + currentFileName + ".err" + ".", e);
                            } else {
                                logError("Error while restoring QueueItem from file (" + currentFileName + "). Failed to rename file to " + currentFileName + ".err. Deleting file.", e);
                                new File(currentFileName).delete();
                            }
                        }
                    } finally {
                        if (item != null) {
                            queueItems.add(item);
                            item.setRecoveredFromPersistentStorage(true);
                        }
                    }
                }
                for (int o = 0; o < oldFiles.length; o++) {
                    if (oldFiles[o] != null) {
                        baseDir.replace(appendIndex2, baseDir.length(), oldFiles[o]);
                        oldFileName = baseDir.toString();
                        try {
                            logWarning("Found single backup QueueItem file (" + oldFileName + "). Trying to restore.");
                            item = readQueueItem(oldFileName);
                            new File(oldFileName).renameTo(new File(oldFileName.substring(0, oldFileName.length() - oldItemFileSuffix.length())));
                            queueItems.add(item);
                            item.setRecoveredFromPersistentStorage(true);
                        } catch (Exception e) {
                            if (new File(oldFileName).renameTo(new File(oldFileName + ".err"))) {
                                logError("Error while restoring QueueItem from single backup file (" + oldFileName + "). Renaming file to " + oldFileName + ".err.", e);
                            } else {
                                logError("Error while restoring QueueItem from single backup file (" + oldFileName + "). Failed to rename file to " + oldFileName + ".err. Deleting file.", e);
                                new File(oldFileName).delete();
                            }
                        }
                    }
                }
                files = null;
                oldFiles = null;
            }
        }
        return queueItems;
    }
    private QueueItem readQueueItem(String fileName) throws Exception {
        IOException error = null;
        QueueItem item = null;
        for (int attempt = 0; (item == null) && (attempt < maxAttempts); attempt++) {
            try {
                item = doReadQueueItem(fileName);
                error = null;
            } catch (IOException e) {
                item = null;
                error = e;
                try {
                    Thread.sleep(attemptDelay);
                } catch (InterruptedException ie) {
                }
            }
        }
        if (error != null) {
            throw error;
        } else {
            return item;
        }
    }
    private QueueItem doReadQueueItem(String fileName) throws Exception {
        QueueItem item = null;
        ObjectInputStream objInput = null;
        byte[] objBytes = null;
        RandomAccessFile queueItemFile = null;
        short status;
        try {
            queueItemFile = new RandomAccessFile(fileName, "r");
            status = queueItemFile.readShort();
            objBytes = new byte[(int) (queueItemFile.length()) - 2];
            queueItemFile.readFully(objBytes);
            queueItemFile.close();
            queueItemFile = null;
            objInput = new NoHeadersObjectInputStream(new ByteArrayInputStream(objBytes));
            item = (QueueItem) objInput.readObject();
            item.forceStatus(status);
            objInput.close();
            objInput = null;
            return item;
        } finally {
            try {
                if (queueItemFile != null) queueItemFile.close();
            } catch (Exception ex1) {
            }
            try {
                if (objInput != null) objInput.close();
            } catch (Exception ex2) {
            }
        }
    }
}
