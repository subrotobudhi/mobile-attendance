package com.percobaan.presensionlinesatu;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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


public class RegisterActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;
    String nama;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        textView = (TextView) findViewById(R.id.textView3);
        button = (Button) findViewById(R.id.button2);
        //button.setOnClickListener(new View.OnClickListener());
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        textView.setText("ID Device : "+id);


    }

    public void onClickButton (View v) {
        EditText id_peg = (EditText) findViewById(R.id.editText);
        String id_pegawai = id_peg.getText().toString();

        String type = "register";

        BackgroundRegistrasi backgroundRegistrasi = new BackgroundRegistrasi();
        backgroundRegistrasi.execute(type, id_pegawai);

    }



    private class BackgroundRegistrasi extends AsyncTask<String, Void, String> {

        Context context;
        String JSON_URL, JSON_STRING, nama_pegawai, id_peg;
//        JSONObject jsonObject;
        TextView textView = (TextView) findViewById(R.id.textView2);
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        @Override
        protected void onPreExecute() {
            JSON_URL = "https://sword-shaped-splint.000webhostapp.com/android/infouser.php";
        }

        @Override
        protected String doInBackground(String... params) {
            String type = params[0];
            if (type.equals("register")) {
                try {
                    String id_pegawai = params[1];
                    URL url = new URL(JSON_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("id_pegawai","UTF-8")+"="+URLEncoder.encode(id_pegawai,"UTF-8");
                    bufferedWriter.write(post_data);
                    bufferedWriter.flush();
                    bufferedWriter.close();


                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((JSON_STRING = bufferedReader.readLine()) != null) {

                        stringBuilder.append(JSON_STRING + "\n");

                    }

                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();
                    return stringBuilder.toString().trim();


                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            JSON_STRING = result;

            if (JSON_STRING.equals("No Pegawai Tidak Dikenal")){
                Toast.makeText(getApplicationContext(), "Maaf No Pegawai Tidak DIkenal", Toast.LENGTH_LONG).show();
            }
            else if (JSON_STRING.equals("User Sudah Terasosiasi")) {
                Toast.makeText(getApplicationContext(), "Maaf Pegawai Sudah Terasosiasi", Toast.LENGTH_LONG).show();
            }
            else {
                try {
                    JSONObject jsonObject = new JSONObject(JSON_STRING);
                    JSONArray data = jsonObject.getJSONArray("hasil data");

                    JSONObject JO = data.getJSONObject(0);
                    nama_pegawai = JO.getString("nama_pegawai");
                    id_peg = JO.getString("id");
                    textView.setText("Nama      : " + nama_pegawai);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new AlertDialog.Builder(RegisterActivity.this)
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah anda yakin ?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
//                                Toast.makeText(RegisterActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                                BackgroundInsertData backgroundInsertData = new BackgroundInsertData();
                                backgroundInsertData.execute(nama_pegawai);
//                                nama=nama_depan;


                            }})
                        .setNegativeButton(android.R.string.no, null).show();

//                button.setEnabled(false);

            }
        }
    }


    private class BackgroundInsertData extends AsyncTask <String, Void, String> {

        Context context;
        String DATA_URL;
        EditText id_peg = (EditText) findViewById(R.id.editText);
        String id_pegawai = id_peg.getText().toString();
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        android.app.AlertDialog alertDialog;


        @Override
        protected void onPreExecute() {
            DATA_URL = "https://sword-shaped-splint.000webhostapp.com/android/reguser.php";
        }

        public void showToast(String message) {
            final String msg = message;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }


        @Override
        protected String doInBackground(String... params) {
            String nama = params[0];

            try {
                URL url =  new URL(DATA_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data1 = URLEncoder.encode("nama_pegawai","UTF-8")+"="+URLEncoder.encode(nama,"UTF-8");
                post_data1 += "&"+URLEncoder.encode("id_device","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                post_data1 += "&"+URLEncoder.encode("id_pegawai","UTF-8")+"="+URLEncoder.encode(id_pegawai,"UTF-8");
                bufferedWriter.write(post_data1);
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


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
/*            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Status")
                    .setMessage(result)
                    .setIcon(android.R.drawable.ic_dialog_alert);
*/

            showToast(result);
            finishAffinity();
        }

    }



/*
    private class BackgroundInsertData extends AsyncTask <String, Void, String> {

        String DATA_URL;
        EditText id_peg = (EditText) findViewById(R.id.editText);
        String id_pegawai = id_peg.getText().toString();
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


        @Override
        protected void onPreExecute() {
            DATA_URL = "https://sword-shaped-splint.000webhostapp.com/android/reguser.php";
        }

        @Override
        protected String doInBackground(String... params) {
            String nama = params[0];
            try {
                URL url =  new URL(DATA_URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);


                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data1 = URLEncoder.encode("id_pegawai","UTF-8")+"="+URLEncoder.encode(id_pegawai,"UTF-8");
                String post_data2 = URLEncoder.encode("id_device","UTF-8")+"="+URLEncoder.encode(id,"UTF-8");
                String post_data3 = URLEncoder.encode("nama","UTF-8")+"="+URLEncoder.encode(nama,"UTF-8");
                bufferedWriter.write(post_data1+post_data2+post_data3);
//                bufferedWriter.flush();
//                bufferedWriter.write(post_data2);
//                bufferedWriter.flush();
//                bufferedWriter.write(post_data3);
                bufferedWriter.flush();
                bufferedWriter.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Status")
                    .setMessage(result)
                    .setIcon(android.R.drawable.ic_dialog_alert);

        }
    }

*/

}