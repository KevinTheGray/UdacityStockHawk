package com.sam_chordas.android.stockhawk.data;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by KG on 7/24/16.
 */

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class QuotesWidgetRemoteViewsService extends RemoteViewsService {

  @Override
  public RemoteViewsFactory onGetViewFactory(Intent intent) {

    return new RemoteViewsFactory() {
      private Cursor data = null;
      @Override
      public void onCreate() {

      }

      @Override
      public void onDataSetChanged() {
        if (data != null) {
          data.close();
        }
        // This method is called by the app hosting the widget (e.g., the launcher)
        // However, our ContentProvider is not exported so it doesn't have access to the
        // data. Therefore we need to clear (and finally restore) the calling identity so
        // that calls use our process and permission
        final long identityToken = Binder.clearCallingIdentity();
        data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
          new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
          QuoteColumns.ISCURRENT + " = ?",
          new String[]{"1"},
          null);
        Binder.restoreCallingIdentity(identityToken);

      }

      @Override
      public void onDestroy() {
        if (data != null) {
          data.close();
          data = null;
        }
      }

      @Override
      public int getCount() {
        return data == null ? 0 : data.getCount();
      }

      @Override
      public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
          data == null || !data.moveToPosition(position)) {
          return null;
        }
       RemoteViews views = new RemoteViews(getPackageName(),
          R.layout.widget_quotes_list_item);

        //viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
        //viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));

        views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex("symbol")));

        if (data.getInt(data.getColumnIndex("is_up")) == 1) {
          views.setInt(R.id.change, "setBackgroundResource", R.color.material_green_700);
        } else{
          views.setInt(R.id.change, "setBackgroundResource", R.color.material_red_700);
        }
        views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("percent_change")));
        views.setTextViewText(R.id.bid_price, data.getString(data.getColumnIndex("bid_price")));

        return views;

      }

      @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
      private void setRemoteContentDescription(RemoteViews views, String description) {
        // todo: this
      }


      @Override
      public RemoteViews getLoadingView() {
        return new RemoteViews(getPackageName(), R.layout.widget_quotes_list_item);

      }

      @Override
      public int getViewTypeCount() {
        return 1;
      }

      @Override
      public long getItemId(int position) {
        return position;
      }

      @Override
      public boolean hasStableIds() {
        return true;
      }
    };

  }
}

