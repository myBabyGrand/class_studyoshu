package com.studyoshu.modules.study.validator;

import com.studyoshu.modules.study.form.StudyForm;
import com.studyoshu.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class StudyFormValidator implements Validator {

    private final StudyRepository studyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return StudyForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudyForm studyForm = (StudyForm)target;
        if(studyRepository.existsByPath(studyForm.getPath())){
            errors.rejectValue("path", "dup.path", "이미 사용중인 스터디 경로입니다");
        }

    }
}
