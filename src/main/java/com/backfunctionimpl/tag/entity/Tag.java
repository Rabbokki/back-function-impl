package com.backfunctionimpl.tag.entity;

import com.backfunctionimpl.post.entity.PostTag;
import com.backfunctionimpl.tag.enums.City;
import com.backfunctionimpl.tag.enums.Country;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Country country; // 나라 정보

    @Enumerated(EnumType.STRING)
    private City city; // 도시 정보

    private String name; // 태그 이름 (optional: e.g. "여행", "자연", "일본-도쿄")

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> postTags = new ArrayList<>();

    public void addPostTag(PostTag postTag) {
        this.postTags.add(postTag);
        if (postTag.getTag() != this) {
            postTag.setTag(this);
        }
    }
}
