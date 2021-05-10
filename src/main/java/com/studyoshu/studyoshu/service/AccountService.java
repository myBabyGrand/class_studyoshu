package com.studyoshu.studyoshu.service;

import com.studyoshu.studyoshu.account.AccountRepository;
import com.studyoshu.studyoshu.account.SignUpForm;
import com.studyoshu.studyoshu.account.UserAccount;
import com.studyoshu.studyoshu.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
//    private final AuthenticationManager authenticationManager;


    @NotNull
    private Account saveNewAccount(SignUpForm signUpForm) {
        Account account = Account.builder()
                .email(signUpForm.getEmail())
                .nickname(signUpForm.getNickname())
                .password(passwordEncoder.encode(signUpForm.getPassword()))
                .studyCreatedByWeb(true)
                .studyUpdatedByWeb(true)
                .build();
        account.generateEmailCheckToken();
        return accountRepository.save(account);
    }

    private void sendEmailUsingAccount(Account newAccount) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(newAccount.getEmail());
        simpleMailMessage.setSubject("스터디 오슈, 회원 가입 인증");
        simpleMailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()
                +"&email="+ newAccount.getEmail());
        javaMailSender.send(simpleMailMessage);
    }

    public Account processNewAccount(SignUpForm signUpForm) {
        Account newAccount = saveNewAccount(signUpForm);
        sendEmailUsingAccount(newAccount);
        return newAccount;
    }


    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                 account.getNickname()
                 new UserAccount(account)
                ,account.getPassword() //encoded
                , List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
        SecurityContextHolder.getContext().setAuthentication(token);

//FM
//        UsernamePasswordAuthenticationToken token1 = new UsernamePasswordAuthenticationToken(
//                 account.getNickname()
//                ,account.getPassword());
//        Authentication authentication = authenticationManager.authenticate(token1);
//        SecurityContext context = SecurityContextHolder.getContext();
//        context.setAuthentication(authentication);
    }

    public void sendSignUpConfirmEmail(Account account) {
        this.sendEmailUsingAccount(account);
    }

    @Transactional( readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String emailOrNickName) throws UsernameNotFoundException {
        Account account = null;
        account = accountRepository.findByEmail(emailOrNickName);
        if(account == null){
            account = accountRepository.findByNickname(emailOrNickName);
            if(account==null){
                throw new UsernameNotFoundException(emailOrNickName);
            }
        }

        return new UserAccount(account);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }
}
