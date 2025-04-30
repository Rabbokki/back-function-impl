package com.backfunctionimpl.tag.controller;

import com.backfunctionimpl.tag.dto.TagDto;
import com.backfunctionimpl.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // 모든 태그 조회
    @GetMapping
    public ResponseEntity<List<TagDto>> getTags() {
        List<TagDto> tags = tagService.getTags();
        return ResponseEntity.ok(tags);
    }

    // 태그 추가
    @PostMapping
    public ResponseEntity<TagDto> addTag(@RequestBody TagDto tagDto) {
        TagDto savedTag = tagService.addTag(tagDto);
        return ResponseEntity.ok(savedTag);
    }

    // 태그 삭제
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.noContent().build();
    }
}
