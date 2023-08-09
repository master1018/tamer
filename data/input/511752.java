public class AlertDialogSamples extends Activity {
    private static final int DIALOG_YES_NO_MESSAGE = 1;
    private static final int DIALOG_YES_NO_LONG_MESSAGE = 2;
    private static final int DIALOG_LIST = 3;
    private static final int DIALOG_PROGRESS = 4;
    private static final int DIALOG_SINGLE_CHOICE = 5;
    private static final int DIALOG_MULTIPLE_CHOICE = 6;
    private static final int DIALOG_TEXT_ENTRY = 7;
    private static final int DIALOG_MULTIPLE_CHOICE_CURSOR = 8;
    private static final int MAX_PROGRESS = 100;
    private ProgressDialog mProgressDialog;
    private int mProgress;
    private Handler mProgressHandler;
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_YES_NO_MESSAGE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_title)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_YES_NO_LONG_MESSAGE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_two_buttons_msg)
                .setMessage(R.string.alert_dialog_two_buttons2_msg)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNeutralButton(R.string.alert_dialog_something, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        case DIALOG_LIST:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setTitle(R.string.select_dialog)
                .setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String[] items = getResources().getStringArray(R.array.select_dialog_items);
                        new AlertDialog.Builder(AlertDialogSamples.this)
                                .setMessage("You selected: " + which + " , " + items[which])
                                .show();
                    }
                })
                .create();
        case DIALOG_PROGRESS:
            mProgressDialog = new ProgressDialog(AlertDialogSamples.this);
            mProgressDialog.setIcon(R.drawable.alert_dialog_icon);
            mProgressDialog.setTitle(R.string.select_dialog);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(MAX_PROGRESS);
            mProgressDialog.setButton(getText(R.string.alert_dialog_hide), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            mProgressDialog.setButton2(getText(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            return mProgressDialog;
        case DIALOG_SINGLE_CHOICE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_single_choice)
                .setSingleChoiceItems(R.array.select_dialog_items2, 0, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
               .create();
        case DIALOG_MULTIPLE_CHOICE:
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.ic_popup_reminder)
                .setTitle(R.string.alert_dialog_multi_choice)
                .setMultiChoiceItems(R.array.select_dialog_items3,
                        new boolean[]{false, true, false, true, false, false, false},
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton,
                                    boolean isChecked) {
                            }
                        })
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
               .create();
            case DIALOG_MULTIPLE_CHOICE_CURSOR:
                String[] projection = new String[] {
                        Contacts.People._ID,
                        Contacts.People.NAME,
                        Contacts.People.SEND_TO_VOICEMAIL
                };
                Cursor cursor = managedQuery(Contacts.People.CONTENT_URI, projection, null, null, null);
                return new AlertDialog.Builder(AlertDialogSamples.this)
                    .setIcon(R.drawable.ic_popup_reminder)
                    .setTitle(R.string.alert_dialog_multi_choice_cursor)
                    .setMultiChoiceItems(cursor,
                            Contacts.People.SEND_TO_VOICEMAIL,
                            Contacts.People.NAME,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton,
                                        boolean isChecked) {
                                    Toast.makeText(AlertDialogSamples.this,
                                            "Readonly Demo Only - Data will not be updated",
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                   .create();
        case DIALOG_TEXT_ENTRY:
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.alert_dialog_text_entry, null);
            return new AlertDialog.Builder(AlertDialogSamples.this)
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(R.string.alert_dialog_text_entry)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .create();
        }
        return null;
    }
    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog);
        Button twoButtonsTitle = (Button) findViewById(R.id.two_buttons);
        twoButtonsTitle.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_MESSAGE);
            }
        });
        Button twoButtons2Title = (Button) findViewById(R.id.two_buttons2);
        twoButtons2Title.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_YES_NO_LONG_MESSAGE);
            }
        });
        Button selectButton = (Button) findViewById(R.id.select_button);
        selectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_LIST);
            }
        });
        Button progressButton = (Button) findViewById(R.id.progress_button);
        progressButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_PROGRESS);
                mProgress = 0;
                mProgressDialog.setProgress(0);
                mProgressHandler.sendEmptyMessage(0);
            }
        });
        Button radioButton = (Button) findViewById(R.id.radio_button);
        radioButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_SINGLE_CHOICE);
            }
        });
        Button checkBox = (Button) findViewById(R.id.checkbox_button);
        checkBox.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_MULTIPLE_CHOICE);
            }
        });
        Button checkBox2 = (Button) findViewById(R.id.checkbox_button2);
        checkBox2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_MULTIPLE_CHOICE_CURSOR);
            }
        });
        Button textEntry = (Button) findViewById(R.id.text_entry_button);
        textEntry.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_TEXT_ENTRY);
            }
        });
        mProgressHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (mProgress >= MAX_PROGRESS) {
                    mProgressDialog.dismiss();
                } else {
                    mProgress++;
                    mProgressDialog.incrementProgressBy(1);
                    mProgressHandler.sendEmptyMessageDelayed(0, 100);
                }
            }
        };
    }
}
