package com.studyoshu.modules.study.event;

import com.studyoshu.modules.study.Study;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class StudyCreateEvent {
    private final Study study;

//    public StudyCreateEvent(Study study){
//        this.study = study;
//    }
}
