package com.backfunctionimpl.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Data
public class BaseEntity {
    //입력시 자동으로 날짜 시각 자동저장
    @CreatedDate
    @Column(updatable = false) // 저장 이후 수정 불가
    private LocalDateTime createdAt;
    @LastModifiedDate // 수정 발생시 수정된 날짜 시각 자동저장
    @Column(insertable = false)
    private LocalDateTime updatedAt;
}
