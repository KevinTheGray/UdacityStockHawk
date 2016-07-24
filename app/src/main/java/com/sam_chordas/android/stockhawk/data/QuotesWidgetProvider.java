package com.sam_chordas.android.stockhawk.data;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sam_chordas.android.stockhawk.service.QuotesWidgetIntentService;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

/**
 * Created by KG on 7/13/16.
 */
public class QuotesWidgetProvider extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    Log.d("QuotesWidgetProvider", "Heyyyyyyy now!");
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
      context.startService(new Intent(context, QuotesWidgetIntentService.class));
    }

  }
}
