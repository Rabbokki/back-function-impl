package com.backfunctionimpl.post.entity;

import com.backfunctionimpl.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tagName; // 태그 이름

    @ManyToOne
//    @JoinColumn(name = "post_id")
    private Post post; // 어떤 게시글에 연결된 태그인지

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag; // 어떤 태그인지
}