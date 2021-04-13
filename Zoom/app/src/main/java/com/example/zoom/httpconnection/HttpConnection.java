package com.example.zoom.httpconnection;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class HttpConnection extends AsyncTask<String, Void, String> {
    public String temp="";
    public String result() throws ExecutionException, InterruptedException {
        HttpConnection insertdata = new HttpConnection();
        temp=insertdata.execute("http://13.125.234.161:3000/auth/login","shalseh@gmail.com").get();

        return temp;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d("test!","please wait...\n");
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);


        Log.d("test4",s);

    }

    @Override
    protected String doInBackground(String... params) {
        String result="";
        String serverurl = params[0];
        String email_value = params[1];
        String postparameters = "email="+email_value;
        Log.d("testinput",postparameters);
        try{
            URL url = new URL(serverurl);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.connect();

            OutputStream outputstream = conn.getOutputStream();
            outputstream.write(postparameters.getBytes("UTF-8"));
            outputstream.flush();
            outputstream.close();

            InputStream inputstream;

            if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
                inputstream = conn.getInputStream();
            }else{
                inputstream = conn.getErrorStream();
            }

            InputStreamReader inputreader = new InputStreamReader(inputstream, "UTF-8");
            BufferedReader bufferedreader = new BufferedReader(inputreader);

            StringBuilder sb = new StringBuilder();
            String line = null;

            int a=1;
            while((line = bufferedreader.readLine())!=null){
                sb.append(line);
                a++;
            }

            bufferedreader.close();
            Log.d("testresultidcheck",sb.toString());
            return sb.toString();

        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
