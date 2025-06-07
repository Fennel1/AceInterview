package com.fennel.common.to.member;

import lombok.Data;

@Data
public class GrowthChangeHistory {
    private Long memberId;
    private Integer changeCount;
    private String note;
    private Integer sourceType;
}
