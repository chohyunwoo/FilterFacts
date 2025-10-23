package com.example.myapplication.network.repository;

import androidx.annotation.NonNull;

import com.example.myapplication.common.ChatItem;
import com.example.myapplication.common.ConversationItem;
import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.common.ApiConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatRepository {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final ApiClient apiClient;

    public ChatRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public interface ConversationCreateCallback {
        void onSuccess(long id);
        void onError(String message);
    }

    public interface AskCallback {
        void onSuccess(String answer);
        void onError(String message);
    }

    public interface ConversationsCallback {
        void onSuccess(List<ConversationItem> list);
        void onError(String message);
    }

    public interface MessagesCallback {
        void onSuccess(List<ChatItem> items, int page, boolean hasMore);
        void onError(String message);
    }

    public void createConversation(String title, ConversationCreateCallback cb) {
        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("title", title);
            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            ApiClient.RequestFactory factory = () -> new Request.Builder()
                    .url(ApiConstants.CONVERSATIONS)
                    .post(body)
                    .build();

            apiClient.enqueueWithAuth(factory, new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    cb.onError("대화 생성 실패: " + e.getMessage());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        cb.onError("대화 생성 응답 오류: " + response.code());
                        return;
                    }
                    String respStr = response.body().string();
                    try {
                        JSONObject respJson = new JSONObject(respStr);
                        long id = respJson.getLong("id");
                        cb.onSuccess(id);
                    } catch (JSONException e) {
                        cb.onError("대화 생성 파싱 실패");
                    }
                }
            });
        } catch (JSONException e) {
            cb.onError("JSON 오류: " + e.getMessage());
        }
    }

    public void ask(Long conversationId, String question, AskCallback cb) {
        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("conversationId", conversationId);
            bodyJson.put("question", question);
            bodyJson.put("role", "USER");

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            ApiClient.RequestFactory factory = () -> new Request.Builder()
                    .url(ApiConstants.ASK)
                    .post(body)
                    .build();

            apiClient.enqueueWithAuth(factory, new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    cb.onError("요청 실패: " + e.getMessage());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        cb.onError("응답 오류: " + response.code());
                        return;
                    }
                    String respStr = response.body().string();
                    try {
                        JSONObject respJson = new JSONObject(respStr);
                        String answer = respJson.getString("answer");
                        cb.onSuccess(answer);
                    } catch (JSONException e) {
                        cb.onError("응답 파싱 실패");
                    }
                }
            });
        } catch (JSONException e) {
            cb.onError("JSON 오류: " + e.getMessage());
        }
    }

    public void fetchConversations(int page, int size, ConversationsCallback cb) {
        String url = ApiConstants.CONVERSATIONS + "?page=" + page + "&size=" + size;

        ApiClient.RequestFactory factory = () -> new Request.Builder()
                .url(url)
                .get()
                .build();

        apiClient.enqueueWithAuth(factory, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cb.onError("채팅 목록 불러오기 실패: " + e.getMessage());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("채팅 목록 응답 오류: " + response.code());
                    return;
                }
                String resp = response.body().string();
                try {
                    JSONObject root = new JSONObject(resp);
                    JSONArray content = root.getJSONArray("content");

                    ArrayList<ConversationItem> tmp = new ArrayList<>();
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject obj = content.getJSONObject(i);
                        long id = obj.getLong("id");
                        String title = obj.optString("title", "(제목 없음)");
                        String createdAt = obj.optString("createdAt", "");
                        tmp.add(new ConversationItem(id, title, createdAt));
                    }
                    cb.onSuccess(tmp);
                } catch (JSONException e) {
                    cb.onError("채팅 목록 파싱 실패");
                }
            }
        });
    }

    public void fetchMessages(Long conversationId, int page, int size, MessagesCallback cb) {
        String url = ApiConstants.MESSAGES
                + "?conversationId=" + conversationId
                + "&page=" + page
                + "&size=" + size;

        ApiClient.RequestFactory factory = () -> new Request.Builder()
                .url(url)
                .get()
                .build();

        apiClient.enqueueWithAuth(factory, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                cb.onError("메시지 불러오기 실패: " + e.getMessage());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    cb.onError("메시지 응답 오류: " + response.code());
                    return;
                }
                String resp = response.body().string();
                try {
                    JSONObject root = new JSONObject(resp);
                    JSONArray content = root.getJSONArray("content");
                    boolean last = root.optBoolean("last", false);
                    int number = root.optInt("number", page);

                    ArrayList<ChatItem> newItems = new ArrayList<>();
                    for (int i = 0; i < content.length(); i++) {
                        JSONObject obj = content.getJSONObject(i);
                        String role = obj.optString("role", "ASSISTANT");
                        String text = obj.optString("content", "");
                        boolean isUser = role.equalsIgnoreCase("USER");
                        newItems.add(new ChatItem(text, isUser));
                    }

                    cb.onSuccess(newItems, number, !last);
                } catch (JSONException e) {
                    cb.onError("메시지 파싱 실패");
                }
            }
        });
    }
}
