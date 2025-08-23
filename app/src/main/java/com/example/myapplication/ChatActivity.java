package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale; // 수정: Locale import 추가
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String API_URL = "http://10.0.2.2:8080/api/chat/ask";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private DrawerLayout drawer;
    private RecyclerView rvMessages;
    private TextInputEditText etQuestion;
    private MaterialButton btnSend, btnNewChat;
    private FloatingActionButton fabToBottom;
    private ChipGroup categoryChipGroup;

    private MessageAdapter adapter;
    private final ArrayList<ChatItem> items = new ArrayList<>();
    private final OkHttpClient http = new OkHttpClient();
    private String currentCategory = "food"; // 이 부분은 소문자로 유지해도 괜찮습니다.

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        drawer = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        rvMessages = findViewById(R.id.rvMessages);
        etQuestion = findViewById(R.id.etQuestion);
        btnSend = findViewById(R.id.btnSend);
        btnNewChat = findViewById(R.id.btnNewChat);
        fabToBottom = findViewById(R.id.fabToBottom);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        adapter = new MessageAdapter(items);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        rvMessages.setAdapter(adapter);

        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);

            if (checkedId == R.id.chipFood) {
                currentCategory = "food";
            } else if (checkedId == R.id.chipCosmetics) {
                currentCategory = "cosmetics";
            }
            Log.d("ChatActivity", "Selected category: " + currentCategory);
        });

        // ... 나머지 리스너는 동일 ...
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                int last = lm.findLastVisibleItemPosition();
                boolean nearBottom = last >= adapter.getItemCount() - 2;
                fabToBottom.setVisibility(nearBottom ? View.GONE : View.VISIBLE);
            }
        });
        fabToBottom.setOnClickListener(v -> rvMessages.scrollToPosition(adapter.getItemCount() - 1));
        btnSend.setOnClickListener(v -> sendCurrentText());
        etQuestion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentText();
                return true;
            }
            return false;
        });
        btnNewChat.setOnClickListener(v -> adapter.clear());
    }

    private void sendCurrentText() {
        String msg = etQuestion.getText() == null ? "" : etQuestion.getText().toString().trim();
        if (msg.isEmpty()) return;

        adapter.add(new ChatItem(msg, true));
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        etQuestion.setText("");

        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("question", msg);
            bodyJson.put("category", currentCategory.toUpperCase(Locale.ROOT));

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            // ✅ TokenManager에서 AccessToken 가져오기
            TokenManager tokenManager = new TokenManager(getApplicationContext());
            String accessToken = tokenManager.getAccessToken();

            // ✅ Authorization 헤더 추가
            Request.Builder reqBuilder = new Request.Builder()
                    .url(API_URL)
                    .post(body);

            if (accessToken != null) {
                reqBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }

            Request req = reqBuilder.build();

            http.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("ChatActivity", "API Call Failed", e);
                    runOnUiThread(() -> {
                        adapter.add(new ChatItem("❌ 요청 실패: " + e.getMessage(), false));
                        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful() || response.body() == null) {
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem("❌ 응답 오류: " + response.code(), false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                        return;
                    }
                    final String respStr = response.body().string();
                    try {
                        JSONObject respJson = new JSONObject(respStr);
                        final String answer = respJson.getString("answer");
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem(answer, false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                    } catch (JSONException e) {
                        Log.e("ChatActivity", "JSON Parsing Failed", e);
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem("❌ 응답 파싱 실패", false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            Toast.makeText(this, "JSON 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}