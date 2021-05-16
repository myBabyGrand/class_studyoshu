package com.studyoshu.studyoshu.settings;

import com.studyoshu.studyoshu.ReadyForTest;
import com.studyoshu.studyoshu.account.AccountRepository;
import com.studyoshu.studyoshu.domain.Account;
import com.studyoshu.studyoshu.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingControllerTest {
    static String nickname = "majorTom";
    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    AccountService accountService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void signUpTestAccount(){
        accountService.processNewAccount(ReadyForTest.TestAccountSignUpForm(nickname));
    }

    @AfterEach
    void deleteAllAccount(){
        accountRepository.deleteAll();
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("프로필 수정폼")
    void updateProfileForm() throws Exception{

        mockMvc.perform(get(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("프로필 수정하기 - 정상케이스")
    void updateProfile() throws Exception{
        String modifiedBio = "소개수정내용";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio",modifiedBio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname(nickname);
        assertEquals(modifiedBio, account.getBio());
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("프로필 수정하기 - 에러케이스")
    void updateProfile_error() throws Exception{
        String modifiedBio = "소개수정내용길이초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과암튼초과";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio",modifiedBio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

        Account account = accountRepository.findByNickname(nickname);
        assertNull(account.getBio());
    }


    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("패스워드 수정폼")
    void updatePasswordForm() throws Exception{

        mockMvc.perform(get(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("패스워드 수정하기 - 정상케이스")
    void updatePassword() throws Exception{

        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("newPassword","12345678")
                .param("newPasswordConfirm","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname(nickname);
//        assertEquals(passwordEncoder.encode("12345678"), account.getPassword());//값비교가 아니다
        assertTrue(passwordEncoder.matches("12345678", account.getPassword()));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("패스워드 수정하기 - 에러케이스(불일치)")
    void updatePassword_error() throws Exception{
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("newPassword","12345678")
                .param("newPasswordConfirm","111111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }
}