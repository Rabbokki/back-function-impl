package com.backfunctionimpl.account.service;

import com.backfunctionimpl.account.dto.AccountRegisterRequest;
import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.TravelLevel;
import com.backfunctionimpl.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public void register(AccountRegisterRequest request) {
        // 이메일 중복 검사
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // Account 객체 생성
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword());
        account.setNickname(request.getNickname());
        account.setBirthday(request.getBirthday());

        // TravelLevel 기본값 세팅
        account.setLevel(TravelLevel.BEGINNER);
        account.setLevelExp(0);

        // DB에 저장
        accountRepository.save(account);
    }
}
