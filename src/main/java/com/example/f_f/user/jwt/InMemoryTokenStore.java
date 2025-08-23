//package com.example.f_f.user.jwt;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//@ConditionalOnMissingBean(RedisTokenStore.class)
//public class InMemoryTokenStore implements TokenStore {
//    private static class Entry { long exp; }
//    private final Map<String, Entry> map = new ConcurrentHashMap<>();
//    private String key(String u, String t){ return "refresh:"+u+":"+t; }
//
//    @Override public void saveRefresh(String u, String t, long ttlMs) {
//        var e = new Entry(); e.exp = Instant.now().toEpochMilli()+ttlMs; map.put(key(u,t), e);
//    }
//    @Override public boolean isRefreshValid(String u, String t) {
//        var e = map.get(key(u,t));
//        if (e==null) return false;
//        if (e.exp < Instant.now().toEpochMilli()) { map.remove(key(u,t)); return false; }
//        return true;
//    }
//    @Override public void revokeRefresh(String u, String t) { map.remove(key(u,t)); }
//
//
//}
