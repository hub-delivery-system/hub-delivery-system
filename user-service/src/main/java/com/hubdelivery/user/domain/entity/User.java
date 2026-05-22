package com.hubdelivery.user.domain.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.common.security.UserRole;
import com.hubdelivery.user.domain.type.AffiliationType;
import com.hubdelivery.user.domain.type.UserStatus;

@Entity
@Table(name="p_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class User extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 10)
    private String username;

    @Column(name = "slack_id", nullable = false, length = 20)
    private String slackId;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'PENDING'")
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(name = "affiliation_type")
    @Enumerated(EnumType.STRING)
    private AffiliationType affiliationType;

    @Column(name = "affiliation_name", length = 100)
    private String affiliationName;

    @Column(name = "hub_id")
    private UUID hubId;

    @Column(name = "company_id")
    private UUID companyId;

    @PrePersist
    private void applyDefaultStatus() {
        if (this.status == null) {
            this.status = UserStatus.PENDING;
        }
    }



}
