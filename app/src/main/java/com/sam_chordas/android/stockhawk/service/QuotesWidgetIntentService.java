package com.sam_chordas.android.stockhawk.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.data.QuotesWidgetProvider;

/**
 * Created by KG on 7/24/16.
 */
public class QuotesWidgetIntentService extends IntentService {

  public QuotesWidgetIntentService() {
    super("QuotesWidgetIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Retrieve all of the Today widget ids: these are the widgets we need to update
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
    int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
      QuotesWidgetProvider.class));

    // Get all the current quotes
    Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
      new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
      QuoteColumns.ISCURRENT + " = ?",
      new String[]{"1"},
      null);

    if (data == null) {
      return;
    }

    if (!data.moveToFirst()) {
      data.close();
      return;
    }

    while (data.moveToNext()) {
      String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
      Log.d("Derp", symbol);
    }

    data.close();
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  private void setRemoteContentDescription(RemoteViews views, String description) {
    //views.setContentDescription(R.id.widget_icon, description);
  }
}
