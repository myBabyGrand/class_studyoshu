package com.studyoshu.studyoshu;

import com.studyoshu.studyoshu.account.SignUpForm;

public class ReadyForTest {
    public static SignUpForm TestAccountSignUpForm(String nickname){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname(nickname);
        signUpForm.setEmail("amuro_ray@kakao.com");
        signUpForm.setPassword("12345678");
        return signUpForm;
    }
}
