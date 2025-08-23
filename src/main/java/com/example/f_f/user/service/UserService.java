package com.example.f_f.user.service;


import com.example.f_f.user.dto.UserJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.f_f.user.dto.UserLoginRequest;
import com.example.f_f.user.dto.UserLoginResponse;
import com.example.f_f.user.entity.User;
import com.example.f_f.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    // 회원가입
    public void register(UserJoinRequest request) {
        if (userRepository.existsByUserId(request.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = new User();
        user.setUserId(request.getUserId());
        user.setPassword(request.getPassword()); // 보안을 위해 나중에 해싱 필요

        userRepository.save(user);
    }

    //로그인 처리
    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 "));

        if (!request.getPassword().equals(user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return new UserLoginResponse(true,"로그인 성공");
    }




    }

