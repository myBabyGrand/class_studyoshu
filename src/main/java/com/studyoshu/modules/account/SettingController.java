package com.studyoshu.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyoshu.modules.account.form.*;
import com.studyoshu.modules.account.validator.NicknameValidator;
import com.studyoshu.modules.account.validator.PasswordFormValidator;
import com.studyoshu.modules.tag.Tag;
import com.studyoshu.modules.tag.TagForm;
import com.studyoshu.modules.tag.TagRepository;
import com.studyoshu.modules.tag.TagService;
import com.studyoshu.modules.zone.Zone;
import com.studyoshu.modules.zone.ZoneForm;
import com.studyoshu.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studyoshu.modules.account.SettingController.ROOT;
import static com.studyoshu.modules.account.SettingController.SETTINGS;

@Controller
@Slf4j
@RequestMapping(ROOT + SETTINGS)
@RequiredArgsConstructor
public class SettingController {
    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATIONS = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";

/*    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/" + SETTINGS_PROFILE_VIEW_NAME;
    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSWORD_URL = "/"+SETTINGS_PASSWORD_VIEW_NAME;
    static final String SETTINGS_NOTIFICATIONS_VIEW_NAME = "settings/notifications";
    static final String SETTINGS_NOTIFICATIONS_URL = "/"+SETTINGS_NOTIFICATIONS_VIEW_NAME;
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";
    static final String SETTINGS_ACCOUNT_URL = "/"+SETTINGS_ACCOUNT_VIEW_NAME;
    static final String SETTINGS_TAG_VIEW_NAME = "settings/tags";
    static final String SETTINGS_TAG_URL = "/"+SETTINGS_TAG_VIEW_NAME;
    static final String SETTINGS_ZONE_VIEW_NAME = "settings/zone";
    static final String SETTINGS_ZONE_URL = "/"+SETTINGS_ZONE_VIEW_NAME;*/

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final TagService tagService;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;



    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }
    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(PROFILE)
    public String profileUpdateForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
//        model.addAttribute(new Profile(account));
        model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS+PROFILE;
    }

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS+PROFILE;
        }
        accountService.updateProfile(account, profile);
        redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다");
        return "redirect:/"+SETTINGS+PROFILE;
    }

    @GetMapping(PASSWORD)
    public String passwordUpdateForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS+PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid @ModelAttribute PasswordForm passwordForm, Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS+PASSWORD;
        }
        accountService.updatePassword(account, passwordForm);
        redirectAttributes.addFlashAttribute("message", "패스워드가 수정되었습니다.");
        return "redirect:/"+SETTINGS+PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String notificationUpdateForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
//        model.addAttribute(new Notifications(account));
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS+NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updateNotification(@CurrentAccount Account account, @Valid @ModelAttribute Notifications notifications, Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS+NOTIFICATIONS;
        }

        accountService.updateNotification(account, notifications);
        redirectAttributes.addFlashAttribute("message", "알림설정이 변경 되었습니다.");
        return "redirect:/"+SETTINGS+NOTIFICATIONS;
    }

    @GetMapping(ACCOUNT)
    public String accountUpdateForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
//        model.addAttribute(new Notifications(account));
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS+ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid @ModelAttribute NicknameForm nicknameForm, Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS+ACCOUNT;
        }

        accountService.updateNickName(account, nicknameForm);
        redirectAttributes.addFlashAttribute("message", "알림설정이 변경 되었습니다.");
        return "redirect:/"+SETTINGS+ACCOUNT;
    }

    @GetMapping(TAGS)
    public String tagUpdateForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        log.info(accountService.getTags(account).stream().map(Tag::getTitle).collect(Collectors.toList()).toString());
        model.addAttribute("tags", accountService.getTags(account).stream().map(Tag::getTitle).collect(Collectors.toList()));

        //tag white list 가져오기
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS+TAGS;
    }

    @PostMapping(TAGS+"/add")
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        /*
        optional 사용하지 않고 구현
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder()
                .title(title)
                .build()));
        }
         */
//        Optional<Tag> tag = tagRepository.findByTitle(title);
//        Tag newTag = tag.orElseGet(() -> tagRepository.save(Tag.builder()
//                .title(title)
//                .build()));

        Tag newTag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, newTag);
        return ResponseEntity.ok().build();
    }
    @PostMapping(TAGS+"/remove")
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        /*
        optional 사용하지 않고 구현
        Tag tag = tagRepository.findByTitle(title);
        if(tag == null){
            tag = tagRepository.save(Tag.builder()
                .title(title)
                .build()));
        }
         */
        Optional<Tag> tag = tagRepository.findByTitle(title);
        if(!tag.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account,tag.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String zoneUpdateForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        //tag white list 가져오기
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));
        return SETTINGS+ZONES;
    }

    @PostMapping(ZONES+"/add")
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone==null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES+"/remove")
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if(zone==null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }
}
