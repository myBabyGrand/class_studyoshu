package com.studyoshu.modules.event.event;


import com.studyoshu.modules.event.Enrollment;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnrollmentAcceptedEvent extends EnrollmentEvent {

    public EnrollmentAcceptedEvent(Enrollment enrollment) {
        super(enrollment, "모임 참가 신청을 확인했습니다. 모임에 참석하세요.");
    }

}
