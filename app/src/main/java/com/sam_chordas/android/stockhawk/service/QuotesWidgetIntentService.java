package com.sam_chordas.android.stockhawk.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * Created by KG on 7/24/16.
 */
public class QuotesWidgetIntentService extends IntentService {

  public QuotesWidgetIntentService() {
    super("QuotesWidgetIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d("Die", "Due");
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
  private void setRemoteContentDescription(RemoteViews views, String description) {
    //views.setContentDescription(R.id.widget_icon, description);
  }
}
