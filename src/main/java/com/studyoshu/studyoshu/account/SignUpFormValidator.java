package com.studyoshu.studyoshu.account;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {
    
    private final AccountRepository accountRepository;//RequiredArgsConstructor로 만들어짐
//    public SignUpFormValidator(AccountRepository accountRepository) {
//        this.accountRepository = accountRepository;
//    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom((SignUpForm.class));
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm =(SignUpForm)target;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email", "invalid.email", new Object[]{signUpForm.getEmail()}, "이미 사용중인 이메일입니다.");
        }
        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname", "invalid.nickname", new Object[]{signUpForm.getNickname()}, "이미 사용중인 이메일입니다.");
        }
    }
}
