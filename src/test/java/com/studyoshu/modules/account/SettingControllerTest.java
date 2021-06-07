package com.studyoshu.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyoshu.infra.AbstractContainerBaseTest;
import com.studyoshu.modules.ReadyForTest;
import com.studyoshu.modules.tag.TagForm;
import com.studyoshu.modules.zone.ZoneForm;
import com.studyoshu.modules.tag.Tag;
import com.studyoshu.modules.tag.TagRepository;
import com.studyoshu.modules.zone.Zone;
import com.studyoshu.modules.zone.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SettingControllerTest extends AbstractContainerBaseTest {
    static String nickname = "majorTom";
    @Autowired MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    ZoneRepository zoneRepository;
    @Autowired
    AccountService accountService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired ModelMapper modelMapper;

    Zone testZone = Zone.builder().city("testCity").localNameOfCity("테스트시").province("testProvince").build();

    @BeforeEach
    void signUpTestAccount(){
        accountService.processNewAccount(ReadyForTest.TestAccountSignUpForm(nickname));
        zoneRepository.save(testZone);
    }

    @AfterEach
    void deleteAllAccount(){
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("프로필 수정폼")
    void profileUpdateForm() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("프로필 수정하기 - 정상케이스")
    void updateProfile() throws Exception{
        String modifiedBio = "소개수정내용";
        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PROFILE)
                .param("bio",modifiedBio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname(nickname);
        assertEquals(modifiedBio, account.getBio());
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("프로필 수정하기 - 에러케이스")
    void updateProfile_error() throws Exception{
        String modifiedBio = "소개수정내용길이초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과초과암튼초과";
        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PROFILE)
                .param("bio",modifiedBio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS+ SettingController.PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));

        Account account = accountRepository.findByNickname(nickname);
        assertNull(account.getBio());
    }


    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("패스워드 수정폼")
    void passwordUpdateForm() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("패스워드 수정하기 - 정상케이스")
    void updatePassword() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PASSWORD)
                .param("newPassword","12345678")
                .param("newPasswordConfirm","12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname(nickname);
//        assertEquals(passwordEncoder.encode("12345678"), account.getPassword());//값비교가 아니다
        assertTrue(passwordEncoder.matches("12345678", account.getPassword()));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("패스워드 수정하기 - 에러케이스(불일치)")
    void updatePassword_error() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.PASSWORD)
                .param("newPassword","12345678")
                .param("newPasswordConfirm","111111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS+ SettingController.PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("태그 수정폼")
    void tagsUpdateForm() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.TAGS))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS+ SettingController.TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("tags"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("계정에 태그 추가하기 - 정상케이스")
    void addTags() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("testTitle");
        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.TAGS+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        //DB 확인
        Tag newTag = tagRepository.findByTitle("testTitle").orElseThrow();
        assertNotNull(newTag);
        Account majorTom = accountRepository.findByNickname(nickname);
        assertTrue(majorTom.getTags().contains(newTag));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("계정에 태그 삭제하기 - 에러케이스")
    void removeTags() throws Exception{
        //테스트 태그 입력
        Tag newTag = tagRepository.save(Tag.builder().title("testTitle").build());
        Account majorTom = accountRepository.findByNickname(nickname);
        accountService.addTag(majorTom, newTag);
        //확인
        assertTrue(majorTom.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("testTitle");
        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.TAGS+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        //DB 확인
        assertFalse(majorTom.getTags().contains(newTag));
    }


    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("활동지역 수정폼")
    void zonesUpdateForm() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.ZONES))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS+ SettingController.ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("zones"))
                .andExpect(model().attributeExists("whitelist"));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("계정에 활동지역 추가하기 - 정상케이스")
    void addZones() throws Exception{
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());
        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.ZONES+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        //DB 확인
        Zone newZone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertNotNull(newZone);
        Account majorTom = accountRepository.findByNickname(nickname);
        assertTrue(majorTom.getZones().contains(newZone));
    }

    @WithUserDetails(value = "majorTom", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Test
    @DisplayName("계정에 활동지역 삭제하기 - 에러케이스")
    void removeZones() throws Exception{
        //테스트 태그 입력
        Zone zone = zoneRepository.save(testZone);
        Account majorTom = accountRepository.findByNickname(nickname);
        accountService.addZone(majorTom, zone);
        //확인
        assertTrue(majorTom.getZones().contains(zone));

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(MockMvcRequestBuilders.post(SettingController.ROOT+ SettingController.SETTINGS+ SettingController.ZONES+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        //DB 확인
        assertFalse(majorTom.getZones().contains(zone));
    }
}
