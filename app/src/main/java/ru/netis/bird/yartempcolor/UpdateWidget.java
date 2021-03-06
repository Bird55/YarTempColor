package ru.netis.bird.yartempcolor;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
//import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by bird on 25.04.2015
 */
public class UpdateWidget {
    Context context;
    AppWidgetManager appWidgetManager;

    UpdateWidget(Context context, AppWidgetManager appWidgetManager) {
//        Log.d(Constants.LOG_TAG, "YarTempColorWidget constructor");
        this.context = context;
        this.appWidgetManager = appWidgetManager;
    }

    void updateWidget(int widgetID) {
//        Log.d(Constants.LOG_TAG, "YarTempColorWidget function updateWidget");
        // Проверяем подключение к сети
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            ParseURL parseURL = new ParseURL();
            parseURL.setContext(context);
            parseURL.setAm(appWidgetManager);
            parseURL.setWidgetID(widgetID);
            parseURL.execute(context.getString(R.string.temp_data_URL));
        } else {

            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }
    static class ParseURL extends AsyncTask<String, Void, String[]> {
        Context context;
        AppWidgetManager am;
        int widgetID;
        RemoteViews widgetView;

        void setContext(Context context) {this.context = context;}
        void setAm(AppWidgetManager am) {this.am = am;}
        void setWidgetID(int widgetID) {this.widgetID = widgetID;}

        @Override
        protected String[] doInBackground(String... strings) {
            String[] a;
            BufferedReader reader = null;
            widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            widgetView.setTextViewText(R.id.Time, context.getString(R.string.loading));
            StringBuilder buffer = new StringBuilder();
//            if (Constants.DEBUG)
//                Log.d(Constants.LOG_TAG, "YarTempColorWidget function doInBackground Connecting to [" + strings[0] + "]");
            try {
                URL site = new URL(strings[0]);
                reader = new BufferedReader(new InputStreamReader(site.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    assert reader != null;
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            a = buffer.toString().split(";");

            return a;
        }

        @Override
        protected void onPostExecute(String s[]) {
            super.onPostExecute(s);
            float temperature = Float.valueOf(s[0]);
            float delta = Float.valueOf(s[2]);
            int color;
            String TempStr = context.getString(R.string.degrees_celsius); // "°C"
            String DeltaStr = context.getString(R.string.degrees_delta_celsius); // "°C/h"
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

//            widgetView = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            widgetView.setInt(R.id.linearLayout, "setBackgroundColor", Constants.getBackColor(s[0]));
            if (temperature < +15f && temperature > -5f) color = Color.BLACK;
            else color = Color.WHITE;
            widgetView.setInt(R.id.Temp, "setTextColor", color);
            widgetView.setInt(R.id.Delta, "setTextColor", color);
            widgetView.setInt(R.id.Time, "setTextColor", color);
            widgetView.setTextViewText(R.id.Temp, String.format("%+2.0f%s", RoundSing(temperature, 3), TempStr));
            widgetView.setTextViewText(R.id.Delta, String.format("%+2.0f%s", RoundSing(delta, 2), DeltaStr));
            widgetView.setTextViewText(R.id.Time, sdf.format(new Date()));
//            Log.d(Constants.LOG_TAG, "onPostExecute: temperature=" + temperature +
//                    String.format(" BackgroundColor=%08x color=%08x", Constants.getBackColor(s[0]), color) + " \n"
//                    + String.format("%+2.0f %s", RoundSing(temperature, 3), TempStr));
            am.updateAppWidget(widgetID, widgetView);
        }
    }

    static float RoundSing(float x, int n){
        if (x == 0f) return 0f;
        int digits = n - (int)Math.log10((double)x) - 1;
        if (x < 1) digits++;
        return (float)(Math.round(x * Math.pow(10, digits)) / Math.pow(10, digits));    }
}
