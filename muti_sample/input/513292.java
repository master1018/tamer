public class WordWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        context.startService(new Intent(context, UpdateService.class));
    }
    public static class UpdateService extends Service {
        @Override
        public void onStart(Intent intent, int startId) {
            RemoteViews updateViews = buildUpdate(this);
            ComponentName thisWidget = new ComponentName(this, WordWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(thisWidget, updateViews);
        }
        public RemoteViews buildUpdate(Context context) {
            Resources res = context.getResources();
            String[] monthNames = res.getStringArray(R.array.month_names);
            Time today = new Time();
            today.setToNow();
            String pageName = res.getString(R.string.template_wotd_title,
                    monthNames[today.month], today.monthDay);
            RemoteViews updateViews = null;
            String pageContent = "";
            try {
                SimpleWikiHelper.prepareUserAgent(context);
                pageContent = SimpleWikiHelper.getPageContent(pageName, false);
            } catch (ApiException e) {
                Log.e("WordWidget", "Couldn't contact API", e);
            } catch (ParseException e) {
                Log.e("WordWidget", "Couldn't parse API response", e);
            }
            Pattern pattern = Pattern.compile(SimpleWikiHelper.WORD_OF_DAY_REGEX);
            Matcher matcher = pattern.matcher(pageContent);
            if (matcher.find()) {
                updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_word);
                String wordTitle = matcher.group(1);
                updateViews.setTextViewText(R.id.word_title, wordTitle);
                updateViews.setTextViewText(R.id.word_type, matcher.group(2));
                updateViews.setTextViewText(R.id.definition, matcher.group(3).trim());
                String definePage = res.getString(R.string.template_define_url,
                        Uri.encode(wordTitle));
                Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(definePage));
                PendingIntent pendingIntent = PendingIntent.getActivity(context,
                        0 , defineIntent, 0 );
                updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
            } else {
                updateViews = new RemoteViews(context.getPackageName(), R.layout.widget_message);
                CharSequence errorMessage = context.getText(R.string.widget_error);
                updateViews.setTextViewText(R.id.message, errorMessage);
            }
            return updateViews;
        }
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
