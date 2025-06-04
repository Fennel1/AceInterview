package com.fennel.aceinterview.service.impl;

import com.alibaba.fastjson.JSON;
import com.fennel.aceinterview.config.EsConstant;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import com.fennel.aceinterview.service.QuestionService;
import com.fennel.common.to.es.QuestionEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private RestHighLevelClient elasticsearchClient;

    @Override
    public boolean save(QuestionEsModel questionEsModel) throws IOException {
        IndexRequest indexRequest = new IndexRequest(EsConstant.QUESTION_INDEX);
        indexRequest.id(questionEsModel.getId().toString());
        String s = JSON.toJSONString(questionEsModel);
        indexRequest.source(s, XContentType.JSON);
        IndexResponse response = elasticsearchClient.index(indexRequest, RequestOptions.DEFAULT);
        log.info(response.toString());
        return true;
    }
}
