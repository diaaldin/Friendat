package com.example.diaaldinkr.friendat2;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DetectorBackgroundTask extends AsyncTask<String, Void, String>{
    //Declare Context
    Context ctx;
    //Set Context
    DetectorBackgroundTask(Context ctx){
        this.ctx = ctx;
    }
    @Override
    protected String doInBackground(String ... text) {
        //String variables
        String textToBeDetected = text[0];

        String jsonString;

        try {
            //Set up the translation call URL
            String yandexKey = "trnsl.1.1.20181227T152408Z.645f1cba0f83e6d3.3e1e439a3a8f1b83134cd7bd3116f9e127e6cccc";
            String yandexUrl = "https://translate.yandex.net/api/v1.5/tr.json/detect?key=" + yandexKey
                    + "&text=" + textToBeDetected ;
            URL yandexTranslateURL = new URL(yandexUrl);

            //Set Http Conncection, Input Stream, and Buffered Reader
            HttpURLConnection httpJsonConnection = (HttpURLConnection) yandexTranslateURL.openConnection();
            InputStream inputStream = httpJsonConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            //Set string builder and insert retrieved JSON result into it
            StringBuilder jsonStringBuilder = new StringBuilder();
            while ((jsonString = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(jsonString + "\n");
            }

            //Close and disconnect
            bufferedReader.close();
            inputStream.close();
            httpJsonConnection.disconnect();

            //Making result human readable
            String resultString = jsonStringBuilder.toString().trim();
            //Getting the characters between , and }
            resultString = resultString.substring(resultString.indexOf(',')+1);
            resultString = resultString.substring(0,resultString.indexOf("}"));
            //Getting the characters after :
            resultString = resultString.substring(resultString.indexOf(':')+1);
            //Getting the characters between " and "
            resultString = resultString.substring(resultString.indexOf("\"")+1);
            resultString = resultString.substring(0,resultString.indexOf("\""));

            Log.d("Detection Result:", resultString);
            return resultString;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
