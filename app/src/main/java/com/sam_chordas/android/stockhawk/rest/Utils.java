package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockIntentService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;
  public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

  public static ArrayList<ContentProviderOperation> quoteJsonToContentVals(Context context,  String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          ContentProviderOperation operation = buildBatchOperation(context, jsonObject);
          if (operation != null) {
            batchOperations.add(operation);
          }
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              ContentProviderOperation operation = buildBatchOperation(context, jsonObject);
              if (operation != null) {
                batchOperations.add(operation);
              }
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    if (bidPrice == null) {
      return "0.00";
    } else {
      bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
      return bidPrice;
    }
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(final Context context, JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
        QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      final String symbol = jsonObject.getString("symbol").toLowerCase();
      String bidPrice = jsonObject.getString("Bid");
      String changeInPercent = jsonObject.getString("ChangeinPercent");

      if (change != null && !change.equals("null") && changeInPercent != null &&
        !changeInPercent.equals("null") && symbol != null) {
        builder.withValue(QuoteColumns.SYMBOL, symbol);
        // Some stocks were returning null for bid price but
        // were valid stocks, for example, AOL.
        if (bidPrice.equals("null")) {
          builder.withValue(QuoteColumns.BIDPRICE, "0.00");
        } else {
          builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(bidPrice));
        }
        builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          changeInPercent, true));
        builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
        builder.withValue(QuoteColumns.ISCURRENT, 1);
        if (change.charAt(0) == '-'){
          builder.withValue(QuoteColumns.ISUP, 0);
        }else{
          builder.withValue(QuoteColumns.ISUP, 1);
        }
        builder.withValue(QuoteColumns.CREATED, now());
        return builder.build();
      } else {

        if (context instanceof StockIntentService) {
          final StockIntentService stockIntentService = (StockIntentService) context;
          // Make sure it runs on UI thread
          Handler handler = new Handler(Looper.getMainLooper());
          Runnable runnable = new Runnable() {
            @Override
            public void run() {
              int messageId = R.string.invalid_stock_symbol;
              String message = String.format(context.getString(
                messageId,
                symbol));
              Toast toast =
                Toast.makeText(stockIntentService.getApplicationContext(), message,
                  Toast.LENGTH_SHORT);
              toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
              toast.show();
            }
          };
          handler.post(runnable);
        }
        return null;
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static long now() {
    return System.currentTimeMillis();
  }

  public static String timeStampInMillisToGMTDate(long timestamp) {
    SimpleDateFormat sdf = new SimpleDateFormat("MMM d HH:mm:ss, yyyy z"); // the format of your date
    sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
    String formattedDate = sdf.format(timestamp);
    return formattedDate;
  }
}
