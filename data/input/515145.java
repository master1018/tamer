public class ContactAccessorSdk3_4 extends ContactAccessor {
    @Override
    public Intent getPickContactIntent() {
        return new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
    }
    @Override
    public ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri) {
        ContactInfo contactInfo = new ContactInfo();
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{People.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactInfo.setDisplayName(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        Uri phoneUri = Uri.withAppendedPath(contactUri, Phones.CONTENT_DIRECTORY);
        cursor = contentResolver.query(phoneUri,
                new String[]{Phones.NUMBER}, null, null, Phones.ISPRIMARY + " DESC");
        try {
            if (cursor.moveToFirst()) {
                contactInfo.setPhoneNumber(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        return contactInfo;
    }
}
