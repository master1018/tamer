public class RecipientsAdapter extends ResourceCursorAdapter {
    public static final int CONTACT_ID_INDEX = 1;
    public static final int TYPE_INDEX       = 2;
    public static final int NUMBER_INDEX     = 3;
    public static final int LABEL_INDEX      = 4;
    public static final int NAME_INDEX       = 5;
    private static final String[] PROJECTION_PHONE = {
        Phone._ID,                  
        Phone.CONTACT_ID,           
        Phone.TYPE,                 
        Phone.NUMBER,               
        Phone.LABEL,                
        Phone.DISPLAY_NAME,         
    };
    private static final String SORT_ORDER = Contacts.TIMES_CONTACTED + " DESC,"
            + Contacts.DISPLAY_NAME + "," + Phone.TYPE;
    private final Context mContext;
    private final ContentResolver mContentResolver;
    public RecipientsAdapter(Context context) {
        super(context, R.layout.recipient_filter_item, null, false );
        mContext = context;
        mContentResolver = context.getContentResolver();
    }
    @Override
    public final CharSequence convertToString(Cursor cursor) {
        String number = cursor.getString(RecipientsAdapter.NUMBER_INDEX);
        if (number == null) {
            return "";
        }
        number = number.trim();
        String name = cursor.getString(RecipientsAdapter.NAME_INDEX);
        int type = cursor.getInt(RecipientsAdapter.TYPE_INDEX);
        String label = cursor.getString(RecipientsAdapter.LABEL_INDEX);
        CharSequence displayLabel = Phone.getDisplayLabel(mContext, type, label);
        if (name == null) {
            name = "";
        } else {
            name = name.replace(", ", " ")
                       .replace(",", " ");  
        }
        String nameAndNumber = Contact.formatNameAndNumber(name, number);
        SpannableString out = new SpannableString(nameAndNumber);
        int len = out.length();
        if (!TextUtils.isEmpty(name)) {
            out.setSpan(new Annotation("name", name), 0, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            out.setSpan(new Annotation("name", number), 0, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        String person_id = cursor.getString(RecipientsAdapter.CONTACT_ID_INDEX);
        out.setSpan(new Annotation("person_id", person_id), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new Annotation("label", displayLabel.toString()), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        out.setSpan(new Annotation("number", number), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return out;
    }
    @Override
    public final void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        name.setText(cursor.getString(NAME_INDEX));
        TextView label = (TextView) view.findViewById(R.id.label);
        int type = cursor.getInt(TYPE_INDEX);
        CharSequence labelText = Phone.getDisplayLabel(mContext, type,
                cursor.getString(LABEL_INDEX));
        if (labelText.length() == 0 ||
                (labelText.length() == 1 && labelText.charAt(0) == '\u00A0')) {
            label.setVisibility(View.GONE);
        } else {
            label.setText(labelText);
            label.setVisibility(View.VISIBLE);
        }
        TextView number = (TextView) view.findViewById(R.id.number);
        number.setText(cursor.getString(NUMBER_INDEX));
    }
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        String phone = "";
        String cons = null;
        if (constraint != null) {
            cons = constraint.toString();
            if (usefulAsDigits(cons)) {
                phone = PhoneNumberUtils.convertKeypadLettersToDigits(cons);
                if (phone.equals(cons)) {
                    phone = "";
                } else {
                    phone = phone.trim();
                }
            }
        }
        Uri uri = Uri.withAppendedPath(Phone.CONTENT_FILTER_URI, Uri.encode(cons));
        Cursor phoneCursor =
            mContentResolver.query(uri,
                    PROJECTION_PHONE,
                    null, 
                    null,
                    SORT_ORDER);
        if (phone.length() > 0) {
            ArrayList result = new ArrayList();
            result.add(Integer.valueOf(-1));                    
            result.add(Long.valueOf(-1));                       
            result.add(Integer.valueOf(Phone.TYPE_CUSTOM));     
            result.add(phone);                                  
            result.add("\u00A0");                               
            result.add(cons);                                   
            ArrayList<ArrayList> wrap = new ArrayList<ArrayList>();
            wrap.add(result);
            ArrayListCursor translated = new ArrayListCursor(PROJECTION_PHONE, wrap);
            return new MergeCursor(new Cursor[] { translated, phoneCursor });
        } else {
            return phoneCursor;
        }
    }
    private boolean usefulAsDigits(CharSequence cons) {
        int len = cons.length();
        for (int i = 0; i < len; i++) {
            char c = cons.charAt(i);
            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            if ((c == ' ') || (c == '-') || (c == '(') || (c == ')') || (c == '.') || (c == '+')
                    || (c == '#') || (c == '*')) {
                continue;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                continue;
            }
            if ((c >= 'a') && (c <= 'z')) {
                continue;
            }
            return false;
        }
        return true;
    }
}
