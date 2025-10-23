package com.example.myapplication.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.common.ChatItem;
import com.example.myapplication.adapter.ConversationAdapter;
import com.example.myapplication.common.ConversationItem;
import com.example.myapplication.adapter.MessageAdapter;
import com.example.myapplication.R;
import com.example.myapplication.common.TokenManager;
import com.example.myapplication.viewmodel.ChatViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {
    private ChatViewModel vm;

    private MaterialButton btnLogoutDrawer;
    private DrawerLayout drawer;
    private RecyclerView rvMessages;
    private TextInputEditText etQuestion;
    private TextInputLayout btnSend;
    private MaterialButton btnNewChat;
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

    private TokenManager tokenManager;

    // 대화 상태
    private Long currentConversationId = null;

    // 메시지 페이지네이션 상태
    private int vmMsgPage = 0;
    private boolean vmMsgLoading = false;
    private boolean vmMsgHasMore = true;

    // 라이프사이클
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);
        tokenManager = new TokenManager(getApplicationContext());
        vm = new ViewModelProvider(this).get(ChatViewModel.class);

        drawer = findViewById(R.id.drawerLayout);
        drawer.setBackgroundColor(Color.parseColor("#0B0F19"));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        // 메인 채팅 뷰
        rvMessages = findViewById(R.id.rvMessages);
        etQuestion = findViewById(R.id.etQuestion);
        btnSend = findViewById(R.id.tilQuestion);
        btnNewChat = findViewById(R.id.btnNewChat);
        fabToBottom = findViewById(R.id.fabToBottom);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);

        adapter = new MessageAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        rvMessages.setAdapter(adapter);

        // 네비게이션 뷰 안의 rvChats
        navigationView = findViewById(R.id.navigationView);
        rvChats = navigationView.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        convAdapter = new ConversationAdapter(item -> {
            currentConversationId = item.getId();
            vm.setCurrentConversationId(currentConversationId);
            vm.resetMessagePaging();
            vm.openConversationAndLoadFirstPage(currentConversationId);
            drawer.closeDrawer(GravityCompat.START);
        });
        rvChats.setAdapter(convAdapter);

        // 로그아웃 버튼들
        btnLogoutDrawer = navigationView.findViewById(R.id.btnLogoutDrawer);
        if (btnLogoutDrawer != null) {
            btnLogoutDrawer.setOnClickListener(v -> vm.logout());
        }

        vm.getMessages().observe(this, list -> {
            adapter.submitList(new java.util.ArrayList<>(list));
            rvMessages.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
        });
        vm.getConversations().observe(this, list -> {
            convAdapter.submitList(new java.util.ArrayList<>(list));
        });
        vm.getMsgPage().observe(this, p -> vmMsgPage = p == null ? 0 : p);
        vm.getMsgHasMore().observe(this, h -> vmMsgHasMore = h != null && h);
        vm.getMsgLoading().observe(this, l -> vmMsgLoading = l != null && l);
        vm.getCurrentConversationId().observe(this, id -> currentConversationId = id);
        vm.getError().observe(this, msg -> { if (msg != null && !msg.isEmpty()) Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show(); });
        vm.getLoggedOut().observe(this, out -> { if (out != null && out) purgeSessionAndGoLogin(); });

        // 카테고리 Chip은 서버 전송 X (UI만)
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {});

        // 위로 스크롤 시 과거 페이지 로딩
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;
                int firstVisible = lm.findFirstVisibleItemPosition();
                if (!vmMsgLoading && vmMsgHasMore && firstVisible <= 2 && currentConversationId != null) {
                    vm.loadMessagesPage(currentConversationId, vmMsgPage + 1, /*prepend*/ true);
                }
            }
        });

        fabToBottom.setOnClickListener(v -> rvMessages.scrollToPosition(adapter.getItemCount() - 1));

        btnSend.setEndIconOnClickListener(v -> sendCurrentText());
        etQuestion.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendCurrentText();
                return true;
            }
            return false;
        });

        btnNewChat.setOnClickListener(v -> {
            adapter.submitList(new java.util.ArrayList<>());
            currentConversationId = null;
            vm.setCurrentConversationId(null);
            vm.resetMessagePaging();
        });

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (drawerView.getId() == R.id.navigationView) {
                    vm.fetchConversations(0, 50);
                }
                // 드로어 열렸을 때 → 회색 오버레이
                drawer.setScrimColor(Color.parseColor("#99000000"));
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (drawerView.getId() == R.id.navigationView) {
                    // 드로어 닫혔을 때 → 검정 배경 복원
                    drawer.setBackgroundColor(Color.parseColor("#0B0F19"));
                }
            }
        });
    }

    /** 토큰/세션 정리 후 로그인 화면으로 이동 */
    private void purgeSessionAndGoLogin() {
        // accessToken, refreshToken 제거
        tokenManager.clearAuthTokens();

        // UI 초기화 (선택)
        items.clear();
        adapter.notifyDataSetChanged();
        currentConversationId = null;

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

        etQuestion.setText("");
        vm.sendMessage(msg);
        rvMessages.scrollToPosition(Math.max(0, adapter.getItemCount() - 1));
    }
}