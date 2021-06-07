package com.studyoshu.modules.account;

import com.studyoshu.infra.AbstractContainerBaseTest;
import com.studyoshu.infra.MockMvcTest;
import com.studyoshu.infra.mail.EmailMessage;
import com.studyoshu.infra.mail.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@MockMvcTest
class AccountControllerTest extends AbstractContainerBaseTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AccountRepository accountRepository;
    @MockBean
    JavaMailSender javaMailSender;
    @MockBean
    EmailService emailService;

    @Test
    @DisplayName("sign-up 화면으로 이동하는지 테스트")
    void signUpFormTest() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 오류 케이스")
    void signUpSubmit_with_wrong_inputTest() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "majorTom")
                .param("email", "email?")
                .param("password", "12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("회원 가입 처리 - 입력값 정상 케이스")
    void signUpSubmitTest() throws Exception {
        mockMvc.perform(post("/sign-up")
                .param("nickname", "majorTom")
                .param("email", "yourmail@mail.com")
                .param("password", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("majorTom"));

        Account account = accountRepository.findByEmail("yourmail@mail.com");
        assertNotNull(account);
        assertNotEquals(account.getPassword(),"12345678");//encoded
        assertNotNull(account.getEmailCheckToken());

//        then(javaMailSender).should().send(ArgumentMatchers.any(SimpleMailMessage.class));
        then(emailService).should().sendEmail(ArgumentMatchers.any(EmailMessage.class));

    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 오류")
    void checkEmailToken_with_wrong_inputTest() throws Exception{
        mockMvc.perform(get("/check-email-token")
        .param("token", "asdadasda")
        .param("email", "youmail2@mail.com"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("error"))
        .andExpect(view().name("account/checked-email"))
        .andExpect(unauthenticated());

    }

    @Test
    @DisplayName("인증 메일 확인 - 입력값 정상")
    void checkEmailTokenTest() throws Exception{
        //given
        Account testAccount = Account.builder()
                .email("yourmail3@mail.com")
                .password("12345678")
                .nickname("testUser")
                .build();
        testAccount.generateEmailCheckToken();
        Account newAccount = accountRepository.save(testAccount);

        //when

        //then
        mockMvc.perform(get("/check-email-token")
                .param("token", newAccount.getEmailCheckToken())
                .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("testUser"));


    }

}