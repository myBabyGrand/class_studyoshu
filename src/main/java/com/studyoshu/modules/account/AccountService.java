package com.studyoshu.modules.account;

import com.studyoshu.infra.config.AppProperties;
import com.studyoshu.infra.mail.EmailMessage;
import com.studyoshu.infra.mail.EmailService;
import com.studyoshu.modules.account.form.*;
import com.studyoshu.modules.tag.Tag;
import com.studyoshu.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
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
//    private final JavaMailSender javaMailSender;
    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AppProperties appProperties;
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
        Context context = new Context();
        context.setVariable("link", "/check-email-token?token="+ newAccount.getEmailCheckToken()
                +"&email="+ newAccount.getEmail());
        context.setVariable("nickname", newAccount.getNickname());
        context.setVariable("linkName", "????????? ????????????");
        context.setVariable("message", "??????????????? ???????????? ??????????????? ????????? ???????????????");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject("???????????????, ?????? ?????? ??????")
                .message(message).build();
        emailService.sendEmail(emailMessage);

//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//        simpleMailMessage.setTo(newAccount.getEmail());
//        simpleMailMessage.setSubject("???????????????, ?????? ?????? ??????");
//        simpleMailMessage.setText("/check-email-token?token="+ newAccount.getEmailCheckToken()
//                +"&email="+ newAccount.getEmail());
//        javaMailSender.send(simpleMailMessage);
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
        //navigation bar??? ????????????
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
        Context context = new Context();
        context.setVariable("link", "/login-by-email?token="+ account.getEmailCheckToken()+"&email="+ account.getEmail());
        context.setVariable("nickname", account.getNickname());
        context.setVariable("linkName", "???????????? ???????????????");
        context.setVariable("message", "????????? ????????? ?????? ????????? ???????????????");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);
        EmailMessage emailMessage = EmailMessage.builder()
                .to(account.getEmail())
                .subject("???????????????, ????????? ??????")
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
//        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
//        simpleMailMessage.setTo(account.getEmail());
//        simpleMailMessage.setSubject("???????????????, ????????? ??????");
//        simpleMailMessage.setText("/login-by-email?token="+ account.getEmailCheckToken()
//                +"&email="+ account.getEmail());
//        javaMailSender.send(simpleMailMessage);
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

    public Account getAccount(String nickname) {
        Account byNickName = accountRepository.findByNickname(nickname);
        if(nickname==null){
            throw  new IllegalArgumentException(nickname+"??? ???????????? ???????????? ????????????.");
        }
        return byNickName;
    }
}
