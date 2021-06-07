package com.studyoshu.modules.main;

import com.studyoshu.infra.AbstractContainerBaseTest;
import com.studyoshu.infra.MockMvcTest;
import com.studyoshu.modules.account.form.SignUpForm;
import com.studyoshu.modules.account.AccountRepository;
import com.studyoshu.modules.account.AccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@MockMvcTest
class MainControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    AccountService accountService;
    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    void makeTestAccount(){
        accountService.processNewAccount(TestAccountSignUpForm());
    }
    @AfterEach
    void deleteAllAccount(){
        accountRepository.deleteAll();
    }

    SignUpForm TestAccountSignUpForm(){
        SignUpForm signUpForm = new SignUpForm();
        signUpForm.setNickname("majorTom");
        signUpForm.setEmail("amuro_ray@kakao.com");
        signUpForm.setPassword("12345678");
        return signUpForm;
    }

    @Test
    @DisplayName("이메일로 로그인 테스트")
    void login_with_mail() throws Exception{
        mockMvc.perform(post("/login")
                .param("username", "amuro_ray@kakao.com")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("majorTom"));
    }

    @Test
    @DisplayName("닉네임으로 로그인 테스트")
    void login_with_nickname() throws Exception{
        mockMvc.perform(post("/login")
                .param("username", "majorTom")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated().withUsername("majorTom"));
    }

    @Test
    @DisplayName("로그인 실패")
    void login_fail() throws Exception{
        mockMvc.perform(post("/login")
                .param("username", "majorTom2222")
                .param("password","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("로그아웃 테스트")
    void logout() throws Exception{
        mockMvc.perform(post("/logout")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(unauthenticated());
    }
}