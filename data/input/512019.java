public class ContactAccessorSdk5 extends ContactAccessor {
    @Override
    public Intent getPickContactIntent() {
        return new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    }
    @Override
    public ContactInfo loadContact(ContentResolver contentResolver, Uri contactUri) {
        ContactInfo contactInfo = new ContactInfo();
        long contactId = -1;
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{Contacts._ID, Contacts.DISPLAY_NAME}, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                contactId = cursor.getLong(0);
                contactInfo.setDisplayName(cursor.getString(1));
            }
        } finally {
            cursor.close();
        }
        cursor = contentResolver.query(Phone.CONTENT_URI,
                new String[]{Phone.NUMBER},
                Phone.CONTACT_ID + "=" + contactId, null, Phone.IS_SUPER_PRIMARY + " DESC");
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
