package com.backfunctionimpl.tag.service;

import com.backfunctionimpl.tag.dto.TagDto;
import com.backfunctionimpl.tag.entity.Tag;
import com.backfunctionimpl.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<TagDto> getTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TagDto addTag(TagDto tagDto) {
        Tag tag = Tag.builder()
                .country(tagDto.getCountry())
                .city(tagDto.getCity())
                .name(tagDto.getName())
                .build();
        Tag savedTag = tagRepository.save(tag);
        return convertToDto(savedTag);
    }

    public void deleteTag(Long tagId) {
        tagRepository.deleteById(tagId);
    }

    private TagDto convertToDto(Tag tag) {
        return TagDto.builder()
                .id(tag.getId())
                .country(tag.getCountry())
                .city(tag.getCity())
                .name(tag.getName())
                .build();
    }
}