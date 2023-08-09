 class ErrorConsoleView extends LinearLayout {
    public static final int SHOW_MINIMIZED = 0;
    public static final int SHOW_MAXIMIZED = 1;
    public static final int SHOW_NONE      = 2;
    private TextView mConsoleHeader;
    private ErrorConsoleListView mErrorList;
    private LinearLayout mEvalJsViewGroup;
    private EditText mEvalEditText;
    private Button mEvalButton;
    private WebView mWebView;
    private int mCurrentShowState = SHOW_NONE;
    private boolean mSetupComplete = false;
    private Vector<ConsoleMessage> mErrorMessageCache;
    public ErrorConsoleView(Context context) {
        super(context);
    }
    public ErrorConsoleView(Context context, AttributeSet attributes) {
        super(context, attributes);
    }
    private void commonSetupIfNeeded() {
        if (mSetupComplete) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.error_console, this);
        mConsoleHeader = (TextView) findViewById(R.id.error_console_header_id);
        mErrorList = (ErrorConsoleListView) findViewById(R.id.error_console_list_id);
        mEvalJsViewGroup = (LinearLayout) findViewById(R.id.error_console_eval_view_group_id);
        mEvalEditText = (EditText) findViewById(R.id.error_console_eval_text_id);
        mEvalButton = (Button) findViewById(R.id.error_console_eval_button_id);
        mEvalButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mWebView != null) {
                    mWebView.loadUrl("javascript:" + mEvalEditText.getText());
                }
                mEvalEditText.setText("");
            }
        });
        mConsoleHeader.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mCurrentShowState == SHOW_MINIMIZED) {
                    showConsole(SHOW_MAXIMIZED);
                } else {
                    showConsole(SHOW_MINIMIZED);
                }
            }
        });
        if (mErrorMessageCache != null) {
            for (ConsoleMessage msg : mErrorMessageCache) {
                mErrorList.addErrorMessage(msg);
            }
            mErrorMessageCache.clear();
        }
        mSetupComplete = true;
    }
    public void addErrorMessage(ConsoleMessage consoleMessage) {
        if (mSetupComplete) {
            mErrorList.addErrorMessage(consoleMessage);
        } else {
            if (mErrorMessageCache == null) {
                mErrorMessageCache = new Vector<ConsoleMessage>();
            }
            mErrorMessageCache.add(consoleMessage);
        }
    }
    public void clearErrorMessages() {
        if (mSetupComplete) {
            mErrorList.clearErrorMessages();
        } else if (mErrorMessageCache != null) {
            mErrorMessageCache.clear();
        }
    }
    public int numberOfErrors() {
        if (mSetupComplete) {
            return mErrorList.getCount();
        } else {
            return (mErrorMessageCache == null) ? 0 : mErrorMessageCache.size();
        }
    }
    public void setWebView(WebView webview) {
        mWebView = webview;
    }
    public void showConsole(int show_state) {
        commonSetupIfNeeded();
        switch (show_state) {
            case SHOW_MINIMIZED:
                mConsoleHeader.setVisibility(View.VISIBLE);
                mConsoleHeader.setText(R.string.error_console_header_text_minimized);
                mErrorList.setVisibility(View.GONE);
                mEvalJsViewGroup.setVisibility(View.GONE);
                break;
            case SHOW_MAXIMIZED:
                mConsoleHeader.setVisibility(View.VISIBLE);
                mConsoleHeader.setText(R.string.error_console_header_text_maximized);
                mErrorList.setVisibility(View.VISIBLE);
                mEvalJsViewGroup.setVisibility(View.VISIBLE);
                break;
            case SHOW_NONE:
                mConsoleHeader.setVisibility(View.GONE);
                mErrorList.setVisibility(View.GONE);
                mEvalJsViewGroup.setVisibility(View.GONE);
                break;
        }
        mCurrentShowState = show_state;
    }
    public int getShowState() {
        if (mSetupComplete) {
            return mCurrentShowState;
        } else {
            return SHOW_NONE;
        }
    }
    private static class ErrorConsoleListView extends ListView {
        private ErrorConsoleMessageList mConsoleMessages;
        public ErrorConsoleListView(Context context, AttributeSet attributes) {
            super(context, attributes);
            mConsoleMessages = new ErrorConsoleMessageList(context);
            setAdapter(mConsoleMessages);
        }
        public void addErrorMessage(ConsoleMessage consoleMessage) {
            mConsoleMessages.add(consoleMessage);
            setSelection(mConsoleMessages.getCount());
        }
        public void clearErrorMessages() {
            mConsoleMessages.clear();
        }
        private class ErrorConsoleMessageList extends android.widget.BaseAdapter
                implements android.widget.ListAdapter {
            private Vector<ConsoleMessage> mMessages;
            private LayoutInflater mInflater;
            public ErrorConsoleMessageList(Context context) {
                mMessages = new Vector<ConsoleMessage>();
                mInflater = (LayoutInflater)context.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
            }
            public void add(ConsoleMessage consoleMessage) {
                mMessages.add(consoleMessage);
                notifyDataSetChanged();
            }
            public void clear() {
                mMessages.clear();
                notifyDataSetChanged();
            }
            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }
            @Override
            public boolean isEnabled(int position) {
                return false;
            }
            public long getItemId(int position) {
                return position;
            }
            public Object getItem(int position) {
                return mMessages.get(position);
            }
            public int getCount() {
                return mMessages.size();
            }
            @Override
            public boolean hasStableIds() {
                return true;
            }
            public View getView(int position, View convertView, ViewGroup parent) {
                View view;
                ConsoleMessage error = mMessages.get(position);
                if (error == null) {
                    return null;
                }
                if (convertView == null) {
                    view = mInflater.inflate(android.R.layout.two_line_list_item, parent, false);
                } else {
                    view = convertView;
                }
                TextView headline = (TextView) view.findViewById(android.R.id.text1);
                TextView subText = (TextView) view.findViewById(android.R.id.text2);
                headline.setText(error.sourceId() + ":" + error.lineNumber());
                subText.setText(error.message());
                switch (error.messageLevel()) {
                    case ERROR:
                        subText.setTextColor(Color.RED);
                        break;
                    case WARNING:
                        subText.setTextColor(Color.rgb(255,192,0));
                        break;
                    case TIP:
                        subText.setTextColor(Color.BLUE);
                        break;
                    default:
                        subText.setTextColor(Color.LTGRAY);
                        break;
                }
                return view;
            }
        }
    }
}
