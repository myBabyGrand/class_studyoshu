package com.studyoshu.modules;

import com.studyoshu.modules.account.form.SignUpForm;

public class ReadyForTest {
    public static SignUpForm TestAccountSignUpForm(String nickname){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail("amuro_ray@kakao.com");
        signUpForm.setPassword("12345678");
        return signUpForm;
    }
}
