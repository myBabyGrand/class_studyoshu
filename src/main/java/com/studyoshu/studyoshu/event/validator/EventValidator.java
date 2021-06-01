package com.studyoshu.studyoshu.event.validator;

import com.studyoshu.studyoshu.domain.Event;
import com.studyoshu.studyoshu.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;
        if(eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now())){
            errors.rejectValue("endEnrollmentDateTime", "wrong.dateTime", "모임 접수 종료 일시를 정확하게 입력하세요");
        }
        if(eventForm.getStartDateTime().isBefore(LocalDateTime.now())){
            errors.rejectValue("startDateTime", "wrong.dateTime", "모임 시작 일시를 정확하게 입력하세요");
        }
        if(eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime())){
            errors.rejectValue("startDateTime", "wrong.dateTime", "모임 시작 일시는 모임 접수 종료 일시 보다 빠를 수 없습니다.");
        }
        if(eventForm.getEndDateTime().isBefore(eventForm.getStartDateTime())){
            errors.rejectValue("endDateTime", "wrong.dateTime", "모임 종료 일시는 모임 시작 일시 보다 빠를 수 없습니다.");
        }

    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments", "wrong.value", "확인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }


}
