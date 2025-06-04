package com.fennel.aceinterview.acesearch;


import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@ToString
@Data
class User {
    private String userName;
    private String age;
    private String gender;
}

@ToString
@Data
class BankMember {
    private int account_number;
    private int balance;
    private String firstname;
    private String lastname;
    private int age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;
}

@SpringBootTest
class AceSearchApplicationTests {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Test
    public void testIndexDataOldClient() throws IOException {
        // 1. 构造 User 对象
        User user = new User();
        user.setUserName("ace");
        user.setAge("18");
        user.setGender("Man");

        // 2. 创建 IndexRequest（注意：索引名必须为小写）
        IndexRequest request = new IndexRequest("users");
        request.id("1"); // 设置文档 ID
        request.source(JSON.toJSONString(user), XContentType.JSON);

        // 3. 执行请求
        IndexResponse response = elasticsearchClient.index(request, RequestOptions.DEFAULT);

        // 4. 打印响应
        System.out.println("Document indexed, ID: " + response.getId());
        System.out.println("Result: " + response.getResult()); // CREATED 或 UPDATED
        System.out.println("Index: " + response.getIndex());
        System.out.println("Version: " + response.getVersion());
    }

//    @Test
//    public void testSearchData() throws IOException {
//        SearchRequest request = new SearchRequest("bank");
//
//        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
//        sourceBuilder.query(QueryBuilders.matchQuery("address", "road"));
//
//        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
//        sourceBuilder.aggregation(ageAgg);
//
//        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
//        sourceBuilder.aggregation(balanceAvg);
//
//        request.source(sourceBuilder);
//
//        SearchResponse response = elasticsearchClient.search(request, RequestOptions.DEFAULT);
//
//        SearchHits hits = response.getHits();
//        for (SearchHit hit : hits.getHits()) {
//            String json = hit.getSourceAsString();
//            BankMember member = JSON.parseObject(json, BankMember.class);
//            System.out.println(member);
//        }
//
//        Aggregations aggregations = response.getAggregations();
//        Terms ageAggResult = aggregations.get("ageAgg");
//        for (Terms.Bucket bucket : ageAggResult.getBuckets()) {
//            System.out.println("年龄：" + bucket.getKeyAsString() + " 人数：" + bucket.getDocCount());
//        }
//
//        Avg balanceAvgResult = aggregations.get("balanceAvg");
//        System.out.println("平均薪资：" + balanceAvgResult.getValue());
//    }
}
