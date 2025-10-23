package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.common.ChatItem;
import com.example.myapplication.common.ConversationItem;
import com.example.myapplication.network.repository.ChatRepository;
import com.example.myapplication.network.repository.AuthRepository;
import com.example.myapplication.common.TokenManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository repository;
    private final AuthRepository authRepository;
    private final TokenManager tokenManager;

    private final MutableLiveData<List<ChatItem>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ConversationItem>> conversations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> msgLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> msgHasMore = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> msgPage = new MutableLiveData<>(0);
    private final MutableLiveData<Long> currentConversationId = new MutableLiveData<>(null);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> loggedOut = new MutableLiveData<>(false);

    @Inject
    public ChatViewModel(@NonNull Application application,
                         ChatRepository repository,
                         AuthRepository authRepository,
                         TokenManager tokenManager) {
        super(application);
        this.repository = repository;
        this.authRepository = authRepository;
        this.tokenManager = tokenManager;
    }

    public LiveData<List<ChatItem>> getMessages() { return messages; }
    public LiveData<List<ConversationItem>> getConversations() { return conversations; }
    public LiveData<Boolean> getMsgLoading() { return msgLoading; }
    public LiveData<Boolean> getMsgHasMore() { return msgHasMore; }
    public LiveData<Integer> getMsgPage() { return msgPage; }
    public LiveData<Long> getCurrentConversationId() { return currentConversationId; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getLoggedOut() { return loggedOut; }

    public void setCurrentConversationId(Long id) { currentConversationId.postValue(id); }

    public void resetMessagePaging() {
        msgPage.postValue(0);
        msgHasMore.postValue(true);
        msgLoading.postValue(false);
        List<ChatItem> empty = new ArrayList<>();
        messages.postValue(empty);
    }

    public void openConversationAndLoadFirstPage(Long conversationId) {
        loadMessagesPage(conversationId, 0, false);
    }

    public void loadMessagesPage(Long conversationId, int page, boolean prepend) {
        Boolean loading = msgLoading.getValue();
        if (loading != null && loading) return;
        msgLoading.postValue(true);
        repository.fetchMessages(conversationId, page, 10, new ChatRepository.MessagesCallback() {
            @Override public void onSuccess(List<ChatItem> items, int number, boolean hasMore) {
                msgLoading.postValue(false);
                List<ChatItem> current = messages.getValue();
                if (current == null) current = new ArrayList<>();
                if (prepend) {
                    List<ChatItem> merged = new ArrayList<ChatItem>(items);
                    merged.addAll(current);
                    messages.postValue(merged);
                } else {
                    List<ChatItem> merged = new ArrayList<>(current);
                    merged.addAll(items);
                    messages.postValue(merged);
                }
                msgPage.postValue(number);
                msgHasMore.postValue(hasMore);
            }
            @Override public void onError(String msg) {
                msgLoading.postValue(false);
                error.postValue(msg);
            }
        });
    }

    public void fetchConversations(int page, int size) {
        repository.fetchConversations(page, size, new ChatRepository.ConversationsCallback() {
            @Override public void onSuccess(List<ConversationItem> list) {
                conversations.postValue(list);
            }
            @Override public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    public void sendMessage(String text) {
        List<ChatItem> current = messages.getValue();
        if (current == null) current = new ArrayList<>();
        List<ChatItem> updated = new ArrayList<>(current);
        updated.add(new ChatItem(text, true));
        messages.postValue(updated);

        Long cid = currentConversationId.getValue();
        if (cid == null) {
            startConversationThenAsk(text);
        } else {
            askQuestion(cid, text);
        }
    }

    private void startConversationThenAsk(String firstMessage) {
        repository.createConversation(makeTitleFrom(firstMessage), new ChatRepository.ConversationCreateCallback() {
            @Override public void onSuccess(long id) {
                currentConversationId.postValue(id);
                askQuestion(id, firstMessage);
            }
            @Override public void onError(String message) { error.postValue(message); }
        });
    }

    public void askQuestion(Long conversationId, String question) {
        repository.ask(conversationId, question, new ChatRepository.AskCallback() {
            @Override public void onSuccess(String answer) {
                List<ChatItem> current = messages.getValue();
                if (current == null) current = new ArrayList<>();
                List<ChatItem> updated = new ArrayList<>(current);
                updated.add(new ChatItem(answer, false));
                messages.postValue(updated);
            }
            @Override public void onError(String message) {
                error.postValue(message);
            }
        });
    }

    private String makeTitleFrom(String text) {
        String t = text.replaceAll("\\s+", " ").trim();
        if (t.length() > 30) t = t.substring(0, 30) + "…";
        if (t.isEmpty()) t = "New Chat";
        return t;
    }

    public void logout() {
        String refresh = tokenManager.getRefreshToken();
        authRepository.logout(refresh, new AuthRepository.LogoutCallback() {
            @Override public void onSuccess() {
                tokenManager.clearAuthTokens();
                loggedOut.postValue(true);
            }
            @Override public void onError(String message) {
                loggedOut.postValue(true);
            }
        });
    }
}
