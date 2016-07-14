package com.sam_chordas.android.stockhawk.data;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by KG on 7/13/16.
 */
public class QuotesWidgetProvider extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    Log.d("QuotesWidgetProvider", "Heyyyyyyy now!");
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    Log.d("QuotesWidgetProvider", "Heyyyyyyy now 2!");
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  private void setRemoteContentDescription(RemoteViews views, String description) {
    // Todo: - this
  }
}
