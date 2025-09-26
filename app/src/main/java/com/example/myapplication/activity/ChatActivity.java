package com.example.myapplication.activity;

import android.content.Intent;
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

import com.example.myapplication.common.ChatItem;
import com.example.myapplication.adapter.ConversationAdapter;
import com.example.myapplication.common.ConversationItem;
import com.example.myapplication.adapter.MessageAdapter;
import com.example.myapplication.R;
import com.example.myapplication.common.TokenManager;
import com.example.myapplication.network.common.ApiClient;
import com.example.myapplication.network.common.ApiConstants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    private ApiClient apiClient;

    private MaterialButton btnLogoutDrawer;
    private DrawerLayout drawer;
    private RecyclerView rvMessages;
    private TextInputEditText etQuestion;
    private MaterialButton btnSend, btnNewChat;
    private FloatingActionButton fabToBottom;
    private ChipGroup categoryChipGroup;

    // 네비게이션(채팅 목록)
    private NavigationView navigationView;
    private RecyclerView rvChats;

    // =========================
    // 어댑터/데이터
    // =========================
    private MessageAdapter adapter;
    private ConversationAdapter convAdapter;

    private final ArrayList<ConversationItem> convItems = new ArrayList<>();
    private final ArrayList<ChatItem> items = new ArrayList<>();

    private final OkHttpClient http = new OkHttpClient();

    private TokenManager tokenManager;

    // 대화 상태
    private Long currentConversationId = null;
    private boolean creatingConversation = false;

    // 메시지 페이지네이션 상태
    private static final int MSG_PAGE_SIZE = 10;
    private int msgPage = 0;
    private boolean msgLoading = false;
    private boolean msgHasMore = true;


    // 라이프사이클
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        apiClient = new ApiClient(getApplicationContext());
        tokenManager = new TokenManager(getApplicationContext());

        drawer = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        // 메인 채팅 뷰
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

        // 네비게이션 뷰 안의 rvChats
        navigationView = findViewById(R.id.navigationView);
        rvChats = navigationView.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        convAdapter = new ConversationAdapter(convItems, item -> {
            currentConversationId = item.getId();
            resetMessagePaging();
            openConversationAndLoadFirstPage(currentConversationId);
            Toast.makeText(this, "대화 전환: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.START);
        });
        rvChats.setAdapter(convAdapter);

        // 로그아웃 버튼들
        btnLogoutDrawer = navigationView.findViewById(R.id.btnLogoutDrawer);
        if (btnLogoutDrawer != null) {
            btnLogoutDrawer.setOnClickListener(v -> logout());
        }

        // 카테고리 Chip은 서버 전송 X (UI만)
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {});

        // 위로 스크롤 시 과거 페이지 로딩
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int firstVisible = lm.findFirstVisibleItemPosition();
                if (!msgLoading && msgHasMore && firstVisible <= 2 && currentConversationId != null) {
                    loadMessagesPage(currentConversationId, msgPage + 1, /*prepend*/ true);
                }
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

        btnNewChat.setOnClickListener(v -> {
            adapter.clear();
            currentConversationId = null;
            creatingConversation = false;
            Toast.makeText(this, "새 대화를 시작합니다. 첫 질문 시 채팅방이 생성됩니다.", Toast.LENGTH_SHORT).show();
        });

        // 드로어 열릴 때 목록 갱신
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerView.getId() == R.id.navigationView) {
                    fetchConversations(0, 50);
                }
            }
        });
    }

    private void logout() {
        String refresh = tokenManager.getRefreshToken();
        try {
            JSONObject body = new JSONObject();
            body.put("refreshToken", refresh == null ? "" : refresh);
            RequestBody rb = RequestBody.create(body.toString(), JSON);

            Request req = new Request.Builder()
                    .url(ApiConstants.LOGOUT)
                    .post(rb)
                    .header("Content-Type", "application/json")
                    .build();

            // 요청을 보내고
            http.newCall(req).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    // 네트워크 실패 → 그냥 세션 지우고 로그인으로
                    runOnUiThread(() -> purgeSessionAndGoLogin());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    // 응답이 성공이든 아니든 세션 정리 후 로그인으로 이동
                    response.close();
                    runOnUiThread(() -> purgeSessionAndGoLogin());
                }
            });

        } catch (JSONException ignore) {
            // JSON 에러 시 그냥 세션 지우고 로그인으로
            purgeSessionAndGoLogin();
        }
    }




    /** 토큰/세션 정리 후 로그인 화면으로 이동 */
    private void purgeSessionAndGoLogin() {
        // accessToken, refreshToken 제거
        tokenManager.clearAuthTokens();

        // UI 초기화 (선택)
        items.clear();
        adapter.notifyDataSetChanged();
        currentConversationId = null;
        creatingConversation = false;

        // 로그인 화면으로 이동 (LoginActivity가 존재한다고 가정)
        try {
            Intent i = new Intent(ChatActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } catch (Exception e) {
            // LoginActivity 없으면 현재 액티비티만 종료
            finishAffinity();
        }
    }


    // =========================
    // 전송/대화 생성/질문
    // =========================
    private void sendCurrentText() {
        String msg = etQuestion.getText() == null ? "" : etQuestion.getText().toString().trim();
        if (msg.isEmpty()) return;

        adapter.add(new ChatItem(msg, true));
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        etQuestion.setText("");

        if (currentConversationId == null) {
            startConversationThenAsk(msg);
        } else {
            askQuestion(currentConversationId, msg);
        }
    }

    // 1) 대화 생성 → 2) 질문 전송
    private void startConversationThenAsk(String firstMessage) {
        if (creatingConversation) return;
        creatingConversation = true;

        try {
            String title = makeTitleFrom(firstMessage);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("title", title);
            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            ApiClient.RequestFactory factory = () -> new Request.Builder()
                    .url(ApiConstants.CONVERSATIONS)
                    .post(body)
                    .build();

            apiClient.enqueueWithAuth(factory, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    creatingConversation = false;
                    Log.e("ChatActivity", "Create Conversation Failed", e);
                    runOnUiThread(() -> {
                        adapter.add(new ChatItem("❌ 대화 생성 실패: " + e.getMessage(), false));
                        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    creatingConversation = false;
                    if (!response.isSuccessful() || response.body() == null) {
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem("❌ 대화 생성 응답 오류: " + response.code(), false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                        return;
                    }
                    String respStr = response.body().string();
                    try {
                        JSONObject respJson = new JSONObject(respStr); // {id,title,createdAt}
                        long id = respJson.getLong("id");
                        currentConversationId = id;
                        askQuestion(currentConversationId, firstMessage);
                    } catch (JSONException e) {
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem("❌ 대화 생성 파싱 실패", false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                    }
                }
            });
        } catch (JSONException e) {
            creatingConversation = false;
            Toast.makeText(this, "JSON 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 질문 전송 (/api/chat/ask)
    private void askQuestion(Long conversationId, String question) {
        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("conversationId", conversationId);
            bodyJson.put("question", question);

            // 서버가 요구한다면 주석 해제 (Role enum이 대문자면 "USER", 소문자면 "user")
            bodyJson.put("role", "USER");

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            ApiClient.RequestFactory factory = () -> new Request.Builder()
                    .url(ApiConstants.ASK)
                    .post(body)
                    .build();

            apiClient.enqueueWithAuth(factory, new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("ChatActivity", "Ask Failed", e);
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
                        JSONObject respJson = new JSONObject(respStr); // { "answer": "..." }
                        final String answer = respJson.getString("answer");
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem(answer, false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                    } catch (JSONException e) {
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

    private String makeTitleFrom(String text) {
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() > 30) t = t.substring(0, 30) + "…";
        if (t.isEmpty()) t = "New Chat";
        return t;
    }

    // =========================
    // 대화 목록(드로어)
    // =========================
    private void fetchConversations(int page, int size) {
        String url = ApiConstants.CONVERSATIONS + "?page=" + page + "&size=" + size;

        ApiClient.RequestFactory factory = () -> new Request.Builder()
                .url(url)
                .get()
                .build();

        apiClient.enqueueWithAuth(factory, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ChatActivity", "Fetch Conversations Failed", e);
                runOnUiThread(() ->
                        Toast.makeText(ChatActivity.this, "채팅 목록 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "채팅 목록 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
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

                    runOnUiThread(() -> {
                        convItems.clear();
                        convItems.addAll(tmp);
                        convAdapter.notifyDataSetChanged();
                    });

                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "채팅 목록 파싱 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    // =========================
    // 메시지 페이징
    // =========================
    private void resetMessagePaging() {
        msgPage = 0;
        msgHasMore = true;
        msgLoading = false;
        items.clear();
        adapter.notifyDataSetChanged();
    }

    private void openConversationAndLoadFirstPage(Long conversationId) {
        loadMessagesPage(conversationId, 0, /*prepend*/ false);
    }

    /** 서버에서 메시지 페이지를 받아와 어댑터에 추가 */
    private void loadMessagesPage(Long conversationId, int page, boolean prepend) {
        if (msgLoading) return;
        msgLoading = true;

        String url = ApiConstants.MESSAGES
                + "?conversationId=" + conversationId
                + "&page=" + page
                + "&size=" + MSG_PAGE_SIZE;

        ApiClient.RequestFactory factory = () -> new Request.Builder()
                .url(url)
                .get()
                .build();

        apiClient.enqueueWithAuth(factory, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                msgLoading = false;
                runOnUiThread(() ->
                        Toast.makeText(ChatActivity.this, "메시지 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                msgLoading = false;
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "메시지 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
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

                    runOnUiThread(() -> {
                        LinearLayoutManager lm = (LinearLayoutManager) rvMessages.getLayoutManager();
                        if (lm == null) return;

                        if (prepend) {
                            int firstPosBefore = lm.findFirstVisibleItemPosition();
                            View firstView = lm.findViewByPosition(firstPosBefore);
                            int offsetBefore = (firstView == null) ? 0 : firstView.getTop();

                            items.addAll(0, newItems);
                            adapter.notifyItemRangeInserted(0, newItems.size());
                            lm.scrollToPositionWithOffset(firstPosBefore + newItems.size(), offsetBefore);
                        } else {
                            items.addAll(newItems);
                            adapter.notifyItemRangeInserted(items.size() - newItems.size(), newItems.size());
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        }

                        msgPage = number;
                        msgHasMore = !last;
                    });

                } catch (JSONException e) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "메시지 파싱 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}