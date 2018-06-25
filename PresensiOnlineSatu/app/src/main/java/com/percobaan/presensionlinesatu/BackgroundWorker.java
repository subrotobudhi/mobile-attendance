package com.percobaan.presensionlinesatu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class BackgroundWorker extends AsyncTask<String, Void, String> {
    Context context;

    AlertDialog alertDialog;
    BackgroundWorker(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String ... params) {
        String type = params[0];
        String login_url = "https://sword-shaped-splint.000webhostapp.com/android/cekid.php";
        if(type.equals("login")) {
            try {
                String id_android = params [1];
                URL url = new URL(login_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("android_id","UTF-8")+"="+URLEncoder.encode(id_android,"UTF-8");
                bufferedWriter.write(post_data);
                bufferedWriter.flush();
                bufferedWriter.close();

                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line=bufferedReader.readLine()) != null) {
                    result += line;

                }

                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    @Override
    protected void onPreExecute() {

    }
    @Override
    protected void onPostExecute(String result) {
        if (result.equals("Selamat Datang")){
            Intent myIntent = new Intent(context, FingerActivity.class);
            context.startActivity(myIntent);
        }
        else {
            Intent myIntent = new Intent(context, RegisterActivity.class);
            context.startActivity(myIntent);
        }

    }
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

}
