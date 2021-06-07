package com.studyoshu.modules.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String title) {
        Optional<Tag> tag  = tagRepository.findByTitle(title);
        Tag newTag = tag.orElseGet(() -> tagRepository.save(Tag.builder()
                .title(title)
                .build()));
        return newTag;
    }

}
