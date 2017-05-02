package com.shanks.ws;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.shanks.interfaces.AsyncInterface;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WSHttpGet extends AsyncTask<String, Void, String> {
	ProgressDialog pDialog;
	Context context;
	HttpURLConnection httpConnection;
	AsyncInterface asyncInterface;
	String WSType;

	public WSHttpGet(Context context, String WSType) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.WSType = WSType;
		this.asyncInterface = (AsyncInterface) context;
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		pDialog = new ProgressDialog(context);
		pDialog.setTitle("Connecting...");
		pDialog.setMessage("Please Wait...");
		pDialog.setCancelable(false);
		pDialog.show();
	}

	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		String result = "";
		try {
			URL url = new URL(params[0]);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestProperty("Accept", "application/json");
			httpConnection.setReadTimeout(15000);
			httpConnection.setConnectTimeout(15000);
			httpConnection.setRequestMethod("GET");
			httpConnection.setDoInput(true);
			// httpConnection.setDoOutput(true);

			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream iStream = httpConnection.getInputStream();
				InputStreamReader isReader = new InputStreamReader(iStream);
				BufferedReader br = new BufferedReader(isReader);
				String line;
				while ((line = br.readLine()) != null) {
					result += line;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		pDialog.dismiss();
		// Log.e("response", result);
		asyncInterface.onWSResponse(result, WSType);
	}

}
