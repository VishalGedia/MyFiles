package com.shanks.ws;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.shanks.interfaces.AsyncInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class WsHttpPostWithNamePair extends AsyncTask<String, Void, String> {

    public static String CIRCULAR_SMALL_DIALOG = "circular_small_dialog";
    private ProgressDialog progressDialog;
    private HttpURLConnection connection = null;
    private Context context;
    private String WSType;
    private Uri.Builder values;
    private AsyncInterface asyncInterface;
    private boolean showProgress;
    private String dialogType;
    private String DEFAULT_DIALOG = "default_dialog";

    public WsHttpPostWithNamePair(Context context, String WSType, Uri.Builder values) {
        this.context = context;
        this.WSType = WSType;
        this.values = values;
        this.showProgress = true;
        this.dialogType = DEFAULT_DIALOG;
        this.asyncInterface = (AsyncInterface) context;
    }

    public WsHttpPostWithNamePair(Context context, String WSType, Uri.Builder values, boolean showProgress) {
        this.context = context;
        this.WSType = WSType;
        this.values = values;
        this.showProgress = showProgress;
        this.dialogType = DEFAULT_DIALOG;
        this.asyncInterface = (AsyncInterface) context;
    }

    public WsHttpPostWithNamePair(Context context, String WSType, Uri.Builder values, String dialogType) {
        this.context = context;
        this.WSType = WSType;
        this.values = values;
        this.showProgress = true;
        this.dialogType = dialogType;
        this.asyncInterface = (AsyncInterface) context;
    }

    public WsHttpPostWithNamePair(Context context, Fragment fragment, String WSType, Uri.Builder values) {
        this.context = context;
        this.values = values;
        this.WSType = WSType;
        this.showProgress = true;
        this.dialogType = DEFAULT_DIALOG;
        this.asyncInterface = (AsyncInterface) fragment;
    }

    public WsHttpPostWithNamePair(Context context, Fragment fragment, String WSType, Uri.Builder values, boolean showProgress) {
        this.context = context;
        this.values = values;
        this.WSType = WSType;
        this.showProgress = showProgress;
        this.dialogType = DEFAULT_DIALOG;
        this.asyncInterface = (AsyncInterface) fragment;
    }

    public WsHttpPostWithNamePair(Context context, Fragment fragment, String WSType, Uri.Builder values, String dialogType) {
        this.context = context;
        this.values = values;
        this.WSType = WSType;
        this.showProgress = true;
        this.dialogType = dialogType;
        this.asyncInterface = (AsyncInterface) fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showProgress) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please Wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            /* START Create connection */
            URL url = new URL(params[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            String query = values.build().getEncodedQuery();

            OutputStream os = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(query);
            writer.flush();
            writer.close();
            os.close();

            /* START Get Response */
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (showProgress) progressDialog.dismiss();
        try {
            asyncInterface.onWSResponse(s, WSType);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
