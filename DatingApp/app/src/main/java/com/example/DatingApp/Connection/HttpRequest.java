package com.example.DatingApp.Connection;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class HttpRequest {



    public static String httpGet(String spec){
        URL url = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try{
            url = new URL(spec);
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(false);
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode != 200){
                return null;
            }
            inputStream = connection.getInputStream();
            StringBuilder stringBuilder = getStringBuilder(inputStream);
            String response = stringBuilder.toString();
            return response;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }


    public static void httpPostFCM(String spec, String message) throws IOException{
        Log.d("MainActivity", "httpPostFCM: entering");
        URL url = new URL(spec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "key=AIzaSyBKbGdTUt-Yz-id65hiRPLvjbJ1BObMyW0");
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(message);
        Log.d("MainActivity", "httpPostFCM: wrote message: >> "+ message);
        outputStream.flush();
        outputStream.close();
        Log.d("MainActivity", "httpPostFCM: connection closed");
        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            String response = inputstreamToString(connection.getInputStream());
            Log.d("MainActivity", "httpPostFCM: Message sent to Firebase for delivery, response:");
            Log.d("MainActivity", "httpPostFCM: "+response);
        } else {
            Log.d("MainActivity","Unable to send message to Firebase:");
            String response = inputstreamToString(connection.getErrorStream());
            Log.d("MainActivity", response);
        }
    }

    private static String inputstreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            stringBuilder.append(scanner.nextLine());
        }
        Log.d("MainActivity", "inputstreamToString: string builder" + stringBuilder.toString());
        return stringBuilder.toString();
    }

    private static StringBuilder getStringBuilder(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder stringBuilder = new StringBuilder();
        int actuallyRead;
        while ((actuallyRead = inputStream.read(buffer)) != -1){
            stringBuilder.append(new String(buffer, 0, actuallyRead));
        }
        return stringBuilder;
    }
}
