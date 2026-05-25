package com.hubdelivery.company.company.domain.entity;

import com.hubdelivery.common.entity.BaseEntity;
import com.hubdelivery.company.company.domain.type.CompanyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "p_company")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_type", nullable = false)
    private CompanyType companyType;

    @Column(name = "hub_id", nullable = false)
    private UUID hubId;

    @Column(nullable = false, length = 255)
    private String address;

    @Builder
    public Company(String companyName, CompanyType companyType, UUID hubId, String address) {
        this.companyName = companyName;
        this.companyType = companyType;
        this.hubId = hubId;
        this.address = address;
    }

    public void update(String companyName, CompanyType companyType, UUID hubId, String address) {
        this.companyName = companyName;
        this.companyType = companyType;
        this.hubId = hubId;
        this.address = address;
    }
}
