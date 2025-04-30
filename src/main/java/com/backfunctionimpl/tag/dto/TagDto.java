package com.backfunctionimpl.tag.dto;

import com.backfunctionimpl.tag.enums.City;
import com.backfunctionimpl.tag.enums.Country;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDto {
    private Long id;             // 태그 ID (for response)
    private Country country;     // 나라 정보
    private City city;           // 도시 정보
    private String name;         // 태그 이름 (optional)
}
