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
    // ---- 서버 엔드포인트 ----
    private static final String BASE_URL = "http://10.0.2.2:8080/api/chat";
    private static final String ASK_URL = BASE_URL + "/ask";
    private static final String CONVERSATIONS_URL = BASE_URL + "/conversations";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");


    // ---- UI ----
    private DrawerLayout drawer;
    private RecyclerView rvMessages;
    private TextInputEditText etQuestion;
    private MaterialButton btnSend, btnNewChat;
    private FloatingActionButton fabToBottom;
    private ChipGroup categoryChipGroup;

    // ✅ 네비게이션 뷰 내부의 채팅 목록 RecyclerView
    private NavigationView navigationView;
    private RecyclerView rvChats;

    // ---- 어댑터 ----
    private MessageAdapter adapter;
    private ConversationAdapter convAdapter;
    private final ArrayList<ConversationItem> convItems = new ArrayList<>();
    private final ArrayList<ChatItem> items = new ArrayList<>();

    private final OkHttpClient http = new OkHttpClient();

    // ---- 대화 상태 ----
    private Long currentConversationId = null;
    private boolean creatingConversation = false;

    // 메시지 페이지네이션 상태
    private static final int MSG_PAGE_SIZE = 50;
    private int msgPage = 0;
    private boolean msgLoading = false;
    private boolean msgHasMore = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

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

        // ✅ 네비게이션 뷰 안의 rvChats 초기화
        navigationView = findViewById(R.id.navigationView);
        rvChats = navigationView.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        convAdapter = new ConversationAdapter(convItems, item -> {
            // 선택한 대화로 전환
            currentConversationId = item.getId();

            // 메시지 목록 초기화 & 첫 페이지 로딩
            resetMessagePaging();
            openConversationAndLoadFirstPage(currentConversationId);

            Toast.makeText(this, "대화 전환: " + item.getTitle(), Toast.LENGTH_SHORT).show();
            drawer.closeDrawer(GravityCompat.START);
        });
        rvChats.setAdapter(convAdapter);

        // 카테고리 Chip은 서버 전송 X (UI만)
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {});

        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                // 이미 있는 아래쪽 FAB 처리 로직은 그대로 두고,
                // 위로 거의 다 올라갔고 더 불러올 페이지가 있으면 추가 로딩
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

        // ✅ 드로어 열릴 때 목록 갱신
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerView.getId() == R.id.navigationView) {
                    fetchConversations(0, 50); // page=0, size=50
                }
            }
        });
    }

    // ... (sendCurrentText, startConversationThenAsk, askQuestion, makeTitleFrom는 기존 그대로) ...
    private void sendCurrentText() {
        String msg = etQuestion.getText() == null ? "" : etQuestion.getText().toString().trim();
        if (msg.isEmpty()) return;

        // 사용자 메시지 먼저 UI에 추가
        adapter.add(new ChatItem(msg, true));
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
        etQuestion.setText("");

        if (currentConversationId == null) {
            // 아직 대화방 없음 → 생성 후 질문 전송
            startConversationThenAsk(msg);
        } else {
            // 이미 대화방 존재 → 바로 질문 전송
            askQuestion(currentConversationId, msg);
        }
    }

    // 1) 대화 생성 → 2) 질문 전송
    private void startConversationThenAsk(String firstMessage) {
        if (creatingConversation) return; // 중복 방지
        creatingConversation = true;

        try {
            // 타이틀은 첫 메시지 앞부분을 요약해서 사용
            String title = makeTitleFrom(firstMessage);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("title", title);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);
            Request.Builder reqBuilder = new Request.Builder()
                    .url(CONVERSATIONS_URL)
                    .post(body);

            // AccessToken (있으면 Authorization 헤더 추가)
            TokenManager tokenManager = new TokenManager(getApplicationContext());
            String accessToken = tokenManager.getAccessToken();
            if (accessToken != null) {
                reqBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }

            http.newCall(reqBuilder.build()).enqueue(new Callback() {
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
                        // StartConversationResponse: { id, title, createdAt }
                        JSONObject respJson = new JSONObject(respStr);
                        long id = respJson.getLong("id");
                        currentConversationId = id;

                        // 방 생성 성공 → 이어서 질문 요청
                        askQuestion(currentConversationId, firstMessage);
                    } catch (JSONException e) {
                        Log.e("ChatActivity", "Create Conv JSON Parse Failed", e);
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

    // /api/chat/ask 호출
    private void askQuestion(Long conversationId, String question) {
        try {
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("conversationId", conversationId);
            bodyJson.put("question", question);

            RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

            Request.Builder reqBuilder = new Request.Builder()
                    .url(ASK_URL)
                    .post(body);

            TokenManager tokenManager = new TokenManager(getApplicationContext());
            String accessToken = tokenManager.getAccessToken();
            if (accessToken != null) {
                reqBuilder.addHeader("Authorization", "Bearer " + accessToken);
            }

            http.newCall(reqBuilder.build()).enqueue(new Callback() {
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
                        // AnswerResponse { "answer": "..." }
                        JSONObject respJson = new JSONObject(respStr);
                        final String answer = respJson.getString("answer");
                        runOnUiThread(() -> {
                            adapter.add(new ChatItem(answer, false));
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        });
                    } catch (JSONException e) {
                        Log.e("ChatActivity", "Ask JSON Parse Failed", e);
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

    // 첫 메시지에서 제목을 뽑아 대화 타이틀로 사용 (서버 StartConversationRequest 요구)
    private String makeTitleFrom(String text) {
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() > 30) {
            t = t.substring(0, 30) + "…";
        }
        if (t.isEmpty()) t = "New Chat";
        return t;
    }

    // ✅ 대화 목록 호출
    private void fetchConversations(int page, int size) {
        String url = CONVERSATIONS_URL + "?page=" + page + "&size=" + size;

        Request.Builder reqBuilder = new Request.Builder().url(url).get();

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();
        if (accessToken != null) {
            reqBuilder.addHeader("Authorization", "Bearer " + accessToken);
        }

        http.newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("ChatActivity", "Fetch Conversations Failed", e);
                runOnUiThread(() ->
                        Toast.makeText(ChatActivity.this, "채팅 목록 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "채팅 목록 응답 오류: " + response.code(), Toast.LENGTH_SHORT).show()
                    );
                    return;
                }
                String resp = response.body().string();
                try {
                    // Spring Page JSON: { content:[{id,title,createdAt},...], totalPages,... }
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
                    Log.e("ChatActivity", "Parse Conversations Failed", e);
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "채팅 목록 파싱 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    /** 대화 전환 또는 새 대화 시작 시 페이징 상태 리셋 */
    private void resetMessagePaging() {
        msgPage = 0;
        msgHasMore = true;
        msgLoading = false;
        items.clear();
        adapter.notifyDataSetChanged();
    }

    /** 선택한 대화의 첫 페이지를 불러오고 화면에 채움 */
    private void openConversationAndLoadFirstPage(Long conversationId) {
        // 첫 페이지 로딩 (prepend=false → 그냥 채우기)
        loadMessagesPage(conversationId, 0, /*prepend*/ false);
    }

    /** 서버에서 메시지 페이지를 받아와서 어댑터에 추가
     * @param prepend true면 '과거 페이지'라서 위쪽에 끼워넣기(무한 스크롤), false면 그대로 뒤에 추가(첫 로딩)
     */
    private void loadMessagesPage(Long conversationId, int page, boolean prepend) {
        if (msgLoading) return;
        msgLoading = true;

        String url = BASE_URL + "/messages"
                + "?conversationId=" + conversationId
                + "&page=" + page
                + "&size=" + MSG_PAGE_SIZE;

        Request.Builder reqBuilder = new Request.Builder().url(url).get();
        // 토큰
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String accessToken = tokenManager.getAccessToken();
        if (accessToken != null) reqBuilder.addHeader("Authorization", "Bearer " + accessToken);

        http.newCall(reqBuilder.build()).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                msgLoading = false;
                Log.e("ChatActivity", "Fetch messages failed", e);
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
                    // Page JSON: content[], last, number(=page), totalPages ...
                    JSONArray content = root.getJSONArray("content");
                    boolean last = root.optBoolean("last", false);     // 마지막 페이지 여부
                    int number = root.optInt("number", page);          // 현재 페이지 번호

                    // 서버의 반환 순서(오래된→최근/최근→오래된)에 따라 보정할 수 있음.
                    // 여기서는 서버가 "오래된 → 최근" 순으로 준다고 가정하고 그대로 사용.
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
                            // 위에 끼워넣기(과거 페이지 추가): 스크롤 점프 방지
                            int firstPosBefore = lm.findFirstVisibleItemPosition();
                            View firstView = lm.findViewByPosition(firstPosBefore);
                            int offsetBefore = (firstView == null) ? 0 : firstView.getTop();

                            items.addAll(0, newItems);
                            adapter.notifyItemRangeInserted(0, newItems.size());

                            // 기존 첫 아이템이 같은 화면 위치에 머물도록 보정
                            lm.scrollToPositionWithOffset(firstPosBefore + newItems.size(), offsetBefore);
                        } else {
                            // 첫 로딩 또는 일반 추가
                            items.addAll(newItems);
                            adapter.notifyItemRangeInserted(items.size() - newItems.size(), newItems.size());
                            // 최신으로 스크롤
                            rvMessages.scrollToPosition(adapter.getItemCount() - 1);
                        }

                        // 페이징 상태 업데이트
                        msgPage = number;
                        msgHasMore = !last;
                    });

                } catch (JSONException e) {
                    Log.e("ChatActivity", "Parse messages failed", e);
                    runOnUiThread(() ->
                            Toast.makeText(ChatActivity.this, "메시지 파싱 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }
}
