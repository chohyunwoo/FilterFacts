package com.example.f_f.user.service;

import com.example.f_f.user.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username 파라미터 = 우리 시스템의 userId 로 사용
        var user = users.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 권한 없이(빈 리스트) 반환. 비밀번호는 DB에 저장된 해시 그대로.
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPassword())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)            // 필요하면 엔티티 필드로 제어
                .build();
    }
}