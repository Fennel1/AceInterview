package com.fennel.aceinterview.service;

import com.fennel.common.to.es.QuestionEsModel;

import java.io.IOException;

public interface QuestionService {

    boolean save(QuestionEsModel questionEsModel) throws IOException;
}
