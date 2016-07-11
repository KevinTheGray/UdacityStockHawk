package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;

/**
 * Created by KG on 7/10/16.
 */
public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
  private Cursor mCursor;
  private LineChart mLineChart;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_line_graph);
    mLineChart = (LineChart) findViewById(R.id.linechart);
    Intent intent = getIntent();
    String symbol = intent.getStringExtra(MyStocksActivity.EXTRA_STOCK_SYMBOL);
    Bundle bundle = new Bundle();
    bundle.putString(MyStocksActivity.EXTRA_STOCK_SYMBOL, symbol);
    getLoaderManager().initLoader(0, bundle, this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
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
      null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursor = data;
    ArrayList<Entry> compData = new ArrayList<Entry>();
    ArrayList<String> descValues = new ArrayList<String>();
    int yIndex = 0;
    while (data.moveToNext()) {
      String dateCreated = "" + data.getLong(data.getColumnIndex(QuoteColumns.CREATED));
      Float bidPrice = Float.parseFloat(data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE)));
      descValues.add(dateCreated);
      compData.add(new Entry(bidPrice, yIndex));
      yIndex++;
    }
    ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
    LineDataSet setComp1 = new LineDataSet(compData, "LOL");
    dataSets.add(setComp1);
    LineData lineData = new LineData(dataSets);
    mLineChart.setData(lineData);
    mLineChart.invalidate();
    mLineChart.getXAxis().setDrawGridLines(false);
    data.close();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
  }
}
