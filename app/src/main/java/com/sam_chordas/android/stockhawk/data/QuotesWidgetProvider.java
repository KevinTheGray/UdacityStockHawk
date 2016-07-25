package com.sam_chordas.android.stockhawk.data;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.service.QuotesWidgetIntentService;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/**
 * Created by KG on 7/13/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class QuotesWidgetProvider extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Perform this loop procedure for each App Widget that belongs to this provider
    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_quotes);

      // Set up the collection
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        setRemoteAdapter(context, views);
      } else {
        setRemoteAdapterV11(context, views);
      }
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    context.startService(new Intent(context, QuotesWidgetIntentService.class));
  }

  @Override
  public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
    context.startService(new Intent(context, QuotesWidgetIntentService.class));
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    Log.d("QuotesWidgetProvider", "Heyyyyyyy now 2!");
    if (StockIntentService.ACTION_DATA_UPDATED.equals(intent.getAction())) {
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
      int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
        new ComponentName(context, getClass()));
      appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
    }
  }

  /**
   * Sets the remote adapter used to fill in the list items
   *
   * @param views RemoteViews to set the RemoteAdapter
   */
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(R.id.widget_list,
      new Intent(context, QuotesWidgetRemoteViewsService.class));
  }

  /**
   * Sets the remote adapter used to fill in the list items
   *
   * @param views RemoteViews to set the RemoteAdapter
   */
  @SuppressWarnings("deprecation")
  private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(0, R.id.widget_list,
      new Intent(context, QuotesWidgetRemoteViewsService.class));
  }
}