package com.studyoshu.studyoshu.service;

import com.studyoshu.studyoshu.account.AccountRepository;
import com.studyoshu.studyoshu.account.SignUpForm;
import com.studyoshu.studyoshu.account.UserAccount;
import com.studyoshu.studyoshu.domain.Account;
import com.studyoshu.studyoshu.domain.Tag;
import com.studyoshu.studyoshu.domain.Zone;
import com.studyoshu.studyoshu.settings.form.NicknameForm;
import com.studyoshu.studyoshu.settings.form.Notifications;
import com.studyoshu.studyoshu.settings.form.PasswordForm;
import com.studyoshu.studyoshu.settings.form.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;


import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
//    private final AuthenticationManager authenticationManager;


    @NotNull
    private Account saveNewAccount(SignUpForm signUpForm) {
//        Account account = Account.builder()
//                .email(signUpForm.getEmail())
//                .nickname(signUpForm.getNickname())
//                .password(passwordEncoder.encode(signUpForm.getPassword()))
////                .studyCreatedByWeb(true)
////                .studyUpdatedByWeb(true)
////                .studyEnrollmentResultByWeb(true)
//                .build();
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
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

    public void updateProfile(Account account, Profile profile) {
        modelMapper.map(profile, account); //profile -> account
//        account.setUrl(profile.getUrl());
//        account.setBio(profile.getBio());
//        account.setLocation(profile.getLocation());
//        account.setOccupation(profile.getOccupation());
//        account.setProfileImage(profile.getProfileImage());
        accountRepository.save(account);
        //navigation bar에 반영하기
    }

    public void updatePassword(Account account, PasswordForm passwordForm) {
        account.setPassword(passwordEncoder.encode(passwordForm.getNewPassword()));
        accountRepository.save(account);
    }

    public void updateNotification(Account account, Notifications notifications) {
        modelMapper.map(notifications, account);
//        account.setStudyCreatedByEmail(notifications.isStudyCreatedByEmail());
//        account.setStudyCreatedByWeb(notifications.isStudyCreatedByWeb());
//        account.setStudyEnrollmentResultByEmail(notifications.isStudyEnrollmentResultByEmail());
//        account.setStudyEnrollmentResultByWeb(notifications.isStudyEnrollmentResultByWeb());
//        account.setStudyUpdatedByEmail(notifications.isStudyUpdatedByEmail());
//        account.setStudyUpdatedByWeb(notifications.isStudyUpdatedByWeb());
        accountRepository.save(account);
    }

    public void updateNickName(Account account, NicknameForm nicknameForm) {
        modelMapper.map(nicknameForm, account);
        accountRepository.save(account);
        login(account);
    }

    public void sendLoginLink(Account account) {
        account.generateEmailCheckToken();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(account.getEmail());
        simpleMailMessage.setSubject("스터디 오슈, 로그인 링크");
        simpleMailMessage.setText("/login-by-email?token="+ account.getEmailCheckToken()
                +"&email="+ account.getEmail());
        javaMailSender.send(simpleMailMessage);
    }

    public Set<Tag> getTags(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getTags();
    }

    public void addTag(Account account,Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().add(tag));
    }

    public void removeTag(Account account, Tag tag) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getTags().remove(tag));
    }

    public Set<Zone> getZones(Account account) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        return byId.orElseThrow().getZones();
    }

    public void addZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().add(zone));
    }

    public void removeZone(Account account, Zone zone) {
        Optional<Account> byId = accountRepository.findById(account.getId());
        byId.ifPresent(a -> a.getZones().remove(zone));
    }
}
