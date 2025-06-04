package com.fennel.aceinterview.vo;

import lombok.Data;

@Data
public class SearchParam {
    private String keyword; // 全文匹配的关键字
    private String id; // 题目 id
    private Integer pageNum;
}
