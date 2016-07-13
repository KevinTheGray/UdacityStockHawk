package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;

/**
 * Created by KG on 7/10/16.
 */
public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnChartValueSelectedListener {
  private Cursor mCursor;
  private LineChart mLineChart;
  private TextView mDateLabel;
  private TextView mBidPriceTextView;
  private float mHighlightedX = -1.0f;
  // Maps an entry x value to an index in the cursor
  private ArrayList<Integer> mEntryCursorMap = new ArrayList<Integer>();
  private static String BUNDLE_KEY_HIGHLIGHT_X = "highlight_x";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Typeface robotoLight = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");
    setContentView(R.layout.activity_line_graph);
    mLineChart = (LineChart) findViewById(R.id.linechart);
    mDateLabel = (TextView) findViewById(R.id.dateLabel);
    TextView symbolTextView = (TextView) findViewById(R.id.stock_symbol);
    if (robotoLight != null) {
      mDateLabel.setTypeface(robotoLight);
      if (symbolTextView != null) {
        symbolTextView.setTypeface(robotoLight);
      }
    }
    mBidPriceTextView = (TextView) findViewById(R.id.bid_price);
    TextView changeTextView = (TextView) findViewById(R.id.change);
    if (changeTextView != null) {
      changeTextView.setVisibility(View.GONE);
    }
    Intent intent = getIntent();
    String symbol = intent.getStringExtra(MyStocksActivity.EXTRA_STOCK_SYMBOL);
    if (savedInstanceState != null) {
      mHighlightedX = savedInstanceState.getFloat(BUNDLE_KEY_HIGHLIGHT_X, -1.0f);
    }
    Bundle bundle = new Bundle();
    bundle.putString(MyStocksActivity.EXTRA_STOCK_SYMBOL, symbol);
    symbolTextView.setAllCaps(false);
    symbolTextView.setText(getString(R.string.bid_price));
    setTitle(String.format(getString(R.string.detail_activity_title), symbol.toUpperCase()));
    // Configure the line chart
    mLineChart.setDescription(symbol.toUpperCase());
    mLineChart.setDescriptionColor(getResources().getColor(R.color.white));
    mLineChart.getXAxis().setTextColor(getResources().getColor(R.color.white));
    mLineChart.getAxisLeft().setTextColor(getResources().getColor(R.color.white));
    mLineChart.getAxisRight().setTextColor(getResources().getColor(R.color.white));
    mLineChart.setOnChartValueSelectedListener(this);

    mLineChart.setNoDataTextDescription("");
    // enable touch gestures
    mLineChart.setTouchEnabled(true);
    // enable scaling and dragging
    mLineChart.setDragEnabled(true);
    mLineChart.setScaleEnabled(true);
    // mChart.setScaleXEnabled(true);
    // mChart.setScaleYEnabled(true);
    // if disabled, scaling can be done on x- and y-axis separately
    mLineChart.setPinchZoom(true);

    getLoaderManager().initLoader(0, bundle, this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putFloat(BUNDLE_KEY_HIGHLIGHT_X, mHighlightedX);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args){
    String symbol = args.getString(MyStocksActivity.EXTRA_STOCK_SYMBOL);
    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
      new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.ISCURRENT,
        QuoteColumns.CREATED},
      QuoteColumns.SYMBOL+ " = ?",
      new String[]{symbol},
      QuoteColumns.CREATED+" ASC");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    // Hacky as heck...beware.
    mCursor = data;
    mEntryCursorMap.clear();
    ArrayList<Entry> compData = new ArrayList<Entry>();
    float lastBidPrice = -1.0f;
    int xIndex = 0;
    data.moveToFirst();
    while (!data.isAfterLast()) {
      String dateCreated = "" + data.getLong(data.getColumnIndex(QuoteColumns.CREATED));
      Float bidPrice = Float.parseFloat(data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
      if (bidPrice != lastBidPrice) {
        lastBidPrice = bidPrice;
        compData.add(new Entry(xIndex, bidPrice));
        mEntryCursorMap.add(data.getPosition());
        xIndex++;
      } else {
        // if it's the last index, add it
        if (data.getPosition() == data.getCount() - 1) {
          compData.add(new Entry(xIndex, bidPrice));
          mEntryCursorMap.add(data.getPosition());
          xIndex++;
        } else {
          data.moveToNext();
          Float nextBidPrice = Float.parseFloat(data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
          if (Math.abs(nextBidPrice - bidPrice) > 0.001) {
            compData.add(new Entry(xIndex, bidPrice));
            data.moveToPrevious();
            mEntryCursorMap.add(data.getPosition());
            xIndex++;
          } else {
            data.moveToPrevious();
          }
        }
      }
      data.moveToNext();
    }
    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
    LineDataSet setComp1 = new LineDataSet(compData, "DataSet");
    dataSets.add(setComp1);
    LineData lineData = new LineData(dataSets);
    lineData.setValueTextColor(getResources().getColor(R.color.white));
    mLineChart.setData(lineData);
    mLineChart.invalidate();
    mLineChart.getXAxis().setDrawGridLines(false);
    if (mHighlightedX >= -0.001f) {
      mLineChart.highlightValue(mHighlightedX, 0);
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
  }

  @Override
  public void onValueSelected(Entry e, Highlight h) {
    mCursor.moveToPosition(mEntryCursorMap.get((int)e.getX()));
    long timestampMillis = mCursor.getLong(mCursor.getColumnIndex(QuoteColumns.CREATED));
    String bidPrice = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE));
    String percentChange = mCursor.getString(mCursor.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
    mDateLabel.setText(Utils.timeStampInMillisToGMTDate(timestampMillis));
    mBidPriceTextView.setText(bidPrice);
    mHighlightedX = e.getX();

  }

  @Override
  public void onNothingSelected() {

  }
}
