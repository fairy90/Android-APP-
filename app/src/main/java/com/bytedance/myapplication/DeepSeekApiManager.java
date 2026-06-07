package com.bytedance.myapplication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeepSeekApiManager {
    private static final String TAG = "DeepSeekApiManager";
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String IMAGE_API_URL = "https://api.deepseek.com/v1/images/generations";
    private static final String API_KEY = "sk-b75feee7c26e409cb5bf81bc690f0e5a"; // 需要替换为实际的API Key
    
    private static DeepSeekApiManager instance;
    private ExecutorService executorService;
    
    private DeepSeekApiManager() {
        executorService = Executors.newFixedThreadPool(3);
    }
    
    public static synchronized DeepSeekApiManager getInstance() {
        if (instance == null) {
            instance = new DeepSeekApiManager();
        }
        return instance;
    }
    
    public interface OnSummaryGeneratedListener {
        void onSuccess(String summary);
        void onError(String error);
    }
    
    public interface OnImageGeneratedListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
    
    public void generateSummary(String title, String description, OnSummaryGeneratedListener listener) {
        executorService.execute(() -> {
            try {
                String prompt = "请为以下广告内容生成一段简洁的AI摘要（不超过50字）：\n" +
                               "标题：" + title + "\n" +
                               "描述：" + description;
                
                String result = callDeepSeekApi(prompt);
                
                if (result != null) {
                    listener.onSuccess(result.trim());
                } else {
                    listener.onError("API返回为空");
                }
            } catch (Exception e) {
                Log.e(TAG, "生成摘要失败", e);
                listener.onError(e.getMessage());
            }
        });
    }
    
    public void generateImage(String title, String description, OnImageGeneratedListener listener) {
        executorService.execute(() -> {
            try {
                String prompt = title + " " + description;
                
                String imageUrl = callImageApi(prompt);
                
                if (imageUrl != null) {
                    listener.onSuccess(imageUrl);
                } else {
                    listener.onError("图片生成失败");
                }
            } catch (Exception e) {
                Log.e(TAG, "生成图片失败", e);
                listener.onError(e.getMessage());
            }
        });
    }
    
    private String callDeepSeekApi(String prompt) throws IOException, JSONException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-chat");
        
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.put(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "API请求失败，状态码: " + responseCode);
            return null;
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        JSONObject responseJson = new JSONObject(response.toString());
        JSONArray choices = responseJson.getJSONArray("choices");
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getString("content");
        }
        
        return null;
    }
    
    private String callImageApi(String prompt) throws IOException, JSONException {
        URL url = new URL(IMAGE_API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
        connection.setDoOutput(true);
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "deepseek-ai-image");
        requestBody.put("prompt", prompt);
        requestBody.put("size", "1024x1024");
        requestBody.put("n", 1);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            Log.e(TAG, "图片API请求失败，状态码: " + responseCode);
            return null;
        }
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        
        JSONObject responseJson = new JSONObject(response.toString());
        JSONArray data = responseJson.getJSONArray("data");
        if (data.length() > 0) {
            JSONObject imageData = data.getJSONObject(0);
            return imageData.getString("url");
        }
        
        return null;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
