package com.studyoshu.studyoshu.event;

import com.studyoshu.studyoshu.domain.Account;
import com.studyoshu.studyoshu.domain.Enrollment;
import com.studyoshu.studyoshu.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByEventAndAccount(Event event, Account account);

    Enrollment findByEventAndAccount(Event event, Account account);
}
