package com.studyoshu.modules.study;

import com.studyoshu.modules.study.event.StudyCreateEvent;
import com.studyoshu.modules.study.event.StudyUpdateEvent;
import com.studyoshu.modules.study.form.StudyDescriptionForm;
import com.studyoshu.modules.study.form.StudyForm;
import com.studyoshu.modules.tag.Tag;
import com.studyoshu.modules.account.Account;
import com.studyoshu.modules.tag.TagRepository;
import com.studyoshu.modules.zone.Zone;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

    public Study createNewStudy(Study study, Account account) {
        study.addManager(account);
        Study newStudy = studyRepository.save(study);
//        eventPublisher.publishEvent(new StudyCreateEvent(newStudy)); //스터디가 공개되었을때 알림이 가겠금 수정
        return newStudy;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = getStudy(path);
        checkIfManager(account, study);
        return study;
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }
    private void checkIfManager(Account account, Study study) {
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm, study);
//        studyRepository.save(study); //pesistance 상태므로 불필요
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디 소개를 수정하였습니다."));
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public Study getStudyToUpdateTag(Account account, String path) {
        Study study = studyRepository.findStudyWithTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void addTag(Study study, Tag tag) {
//        Optional<Study> byId = studyRepository.findById(study.getId());
//        byId.ifPresent(a -> a.getTags().add(tag));
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
//        Optional<Study> byId = studyRepository.findById(study.getId());
//        byId.ifPresent(a -> a.getTags().remove(tag));
        study.getTags().remove(tag);
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findStudyWithZonesByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }


    public void addZone(Study study, Zone zone) {
//        Optional<Study> byId = studyRepository.findById(study.getId());
//        byId.ifPresent(a -> a.getZones().add(zone));
        study.getZones().add(zone);
    }

    public void removeZone(Study study, Zone zone) {
//        Optional<Study> byId = studyRepository.findById(study.getId());
//        byId.ifPresent(a -> a.getZones().remove(zone));
        study.getZones().remove(zone);
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStudyWithManagersByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account, study);
        return study;
    }

    public void publish(Study study) {
        study.publish();
        this.eventPublisher.publishEvent(new StudyCreateEvent(study));
    }

    public void close(Study study) {
        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디를 종료하였습니다."));
    }

    public void startRecruit(Study study) {
        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디의 팀원 모집을 시작하였습니다."));
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study, "스터디의 팀원 모집을 중지하였습니다."));
    }

    public boolean isValidPath(String newPath) {
        if(!newPath.matches(StudyForm.VALID_PATH_PATTERN)){
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public void remove(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study);
        } else {
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    public void removeMember(Study study, Account account) {
        study.removeMember(account);
    }

    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findStudyOnlyByPath(path);
        checkIfExistingStudy(path, study);
        return study;
    }

    public void generateTestStudyData(Account account) {
        for (int i = 0; i < 30; i++) {
             String randomValue = RandomString.make(5);
             Study study = Study.builder()
                     .title("테스트스터디" + randomValue)
                     .path("test-"+randomValue)
                     .shortDescription("테스트스터디 : "+randomValue)
                     .fullDescription("테스트스터디 : "+randomValue)
                     .tags(new HashSet<>())
                     .managers(new HashSet<>())
                     .build();
             study.publish();
             Study newStudy = this.createNewStudy(study, account);

             Optional<Tag> tag= tagRepository.findByTitle("JPA");
            if (tag.isEmpty()) {
                continue;
            }
            newStudy.getTags().add(tag.get());
        }
    }
}
