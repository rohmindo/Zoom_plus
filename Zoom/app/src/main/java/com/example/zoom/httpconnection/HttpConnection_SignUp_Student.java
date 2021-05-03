package com.example.zoom.httpconnection;

import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.LinearLayout;

import com.example.zoom.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class HttpConnection_SignUp_Student extends AsyncTask<String, Void, String> {
    public String temp="";
    public String type="";
    public String app_domain="";
    public String name="";
    public String email="";
    public String phtourl="";
    public String school="";
    public String major="";
    public String grade="";
    public String identityID="";
    public HttpConnection_SignUp_Student(String app_domain,String type,String name,String email,String phtourl,String school,String major,String grade,String stid){
        this.app_domain=app_domain;
        this.email=email;
        this.name=name;
        this.type=type;
        this.phtourl=phtourl;
        this.school=school;
        this.major=major;
        this.grade=grade;
        this.identityID=stid;
    }
    public String result() throws ExecutionException, InterruptedException {
        HttpConnection_SignUp_Student insertdata = new HttpConnection_SignUp_Student(app_domain,type,name,email,phtourl,school,major,grade,identityID);
        temp=insertdata.execute(app_domain+"/api/auth/signup",email).get();
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
        //String email_value = params[1];
        String postparameters = "type="+type+"&email="+email+"&name="+name+"&photourl="+phtourl+"&school="+school+"&major="+major
                +"&grade="+grade+"&identityID="+identityID;
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
