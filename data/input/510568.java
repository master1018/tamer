public class AttachImage extends Activity {
    private static final int REQUEST_PICK_CONTACT = 1;
    private static final int REQUEST_CROP_PHOTO = 2;
    private static final String RAW_CONTACT_URIS_KEY = "raw_contact_uris";
    public AttachImage() {
    }
    private Long[] mRawContactIds;
    private ContentResolver mContentResolver;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (icicle != null) {
            mRawContactIds = toClassArray(icicle.getLongArray(RAW_CONTACT_URIS_KEY));
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType(Contacts.CONTENT_ITEM_TYPE);
            startActivityForResult(intent, REQUEST_PICK_CONTACT);
        }
        mContentResolver = getContentResolver();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mRawContactIds != null && mRawContactIds.length != 0) {
            outState.putLongArray(RAW_CONTACT_URIS_KEY, toPrimativeArray(mRawContactIds));
        }
    }
    private static long[] toPrimativeArray(Long[] in) {
        if (in == null) {
            return null;
        }
        long[] out = new long[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }
    private static Long[] toClassArray(long[] in) {
        if (in == null) {
            return null;
        }
        Long[] out = new Long[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i];
        }
        return out;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }
        if (requestCode == REQUEST_PICK_CONTACT) {
            Intent myIntent = getIntent();
            Intent intent = new Intent("com.android.camera.action.CROP", myIntent.getData());
            if (myIntent.getStringExtra("mimeType") != null) {
                intent.setDataAndType(myIntent.getData(), myIntent.getStringExtra("mimeType"));
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", 96);
            intent.putExtra("outputY", 96);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, REQUEST_CROP_PHOTO);
            final long contactId = ContentUris.parseId(result.getData());
            final ArrayList<Long> rawContactIdsList = ContactsUtils.queryForAllRawContactIds(
                    mContentResolver, contactId);
            mRawContactIds = new Long[rawContactIdsList.size()];
            mRawContactIds = rawContactIdsList.toArray(mRawContactIds);
            if (mRawContactIds == null || rawContactIdsList.isEmpty()) {
                Toast.makeText(this, R.string.contactSavedErrorToast, Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == REQUEST_CROP_PHOTO) {
            final Bundle extras = result.getExtras();
            if (extras != null && mRawContactIds != null) {
                Bitmap photo = extras.getParcelable("data");
                if (photo != null) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                    final ContentValues imageValues = new ContentValues();
                    imageValues.put(Photo.PHOTO, stream.toByteArray());
                    imageValues.put(RawContacts.Data.IS_SUPER_PRIMARY, 1);
                    for (Long rawContactId : mRawContactIds) {
                        boolean shouldUpdate = false;
                        final Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI,
                                rawContactId);
                        final Uri rawContactDataUri = Uri.withAppendedPath(rawContactUri,
                                RawContacts.Data.CONTENT_DIRECTORY);
                        insertPhoto(imageValues, rawContactDataUri, true);
                    }
                }
            }
            finish();
        }
    }
    private void insertPhoto(ContentValues values, Uri rawContactDataUri,
            boolean assertAccount) {
        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();
        if (assertAccount) {
            operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                    .withSelection(Photo.MIMETYPE + "=? AND "
                            + RawContacts.ACCOUNT_TYPE + " IN (?,?)",
                            new String[] {Photo.CONTENT_ITEM_TYPE, GoogleSource.ACCOUNT_TYPE,
                            ExchangeSource.ACCOUNT_TYPE})
                            .withExpectedCount(0).build());
        }
        values.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE);
        operations.add(ContentProviderOperation.newInsert(rawContactDataUri)
                .withValues(values).build());
        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            if (assertAccount) {
                updatePhoto(values, rawContactDataUri, false);
            } else {
                throw new IllegalStateException("Problem inserting photo into raw_contacts/data", e);
            }
        }
    }
    private void updatePhoto(ContentValues values, Uri rawContactDataUri,
            boolean allowInsert) {
        ArrayList<ContentProviderOperation> operations =
            new ArrayList<ContentProviderOperation>();
        values.remove(Photo.MIMETYPE);
        operations.add(ContentProviderOperation.newAssertQuery(rawContactDataUri)
                .withSelection(Photo.MIMETYPE + "=?", new String[] {
                    Photo.CONTENT_ITEM_TYPE
                }).withExpectedCount(1).build());
        operations.add(ContentProviderOperation.newUpdate(rawContactDataUri).withSelection(Photo.MIMETYPE + "=?", new String[] {
                    Photo.CONTENT_ITEM_TYPE}).withValues(values).build());
        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException e) {
            throw new IllegalStateException("Problem querying raw_contacts/data", e);
        } catch (OperationApplicationException e) {
            if (allowInsert) {
                insertPhoto(values, rawContactDataUri, false);
            } else {
                throw new IllegalStateException("Problem inserting photo raw_contacts/data", e);
            }
        }
    }
}
