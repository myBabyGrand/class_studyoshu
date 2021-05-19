package com.studyoshu.studyoshu.zone;

import com.studyoshu.studyoshu.domain.Tag;
import com.studyoshu.studyoshu.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Zone findByCityAndProvince(String cityName, String provinceName);
}
