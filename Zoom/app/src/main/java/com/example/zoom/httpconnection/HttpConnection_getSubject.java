package com.example.zoom.httpconnection;


import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class HttpConnection_getSubject extends AsyncTask<String, Void, String> {
    public String temp="";
    public String cookie="";

    public String result3() throws ExecutionException, InterruptedException {
        HttpConnection_getSubject insertdata = new HttpConnection_getSubject();
        temp=insertdata.execute("http://disboard13.kro.kr/api/subject/get/mySubjects").get();

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


        Log.d("test41",s);

    }

    @Override
    protected String doInBackground(String... params) {
        String result="";
        String serverurl = params[0];
        //String code_value = params[1];
        String postparameters = "";
        //Log.d("testinputs",postparameters);
        try{
            URL url = new URL(serverurl);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            CookieManager cookieManager = CookieManager.getInstance();
            String cookieString =cookieManager.getCookie("http://disboard13.kro.kr");
            conn.setRequestProperty("Cookie", cookieString);



            conn.setConnectTimeout(10000);
            conn.setUseCaches(false);
            conn.usingProxy();
            conn.setRequestMethod("GET");
            conn.connect();
/*
            OutputStream outputstream = conn.getOutputStream();
            outputstream.write(postparameters.getBytes("UTF-8"));
            outputstream.flush();
            outputstream.close();*/

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
