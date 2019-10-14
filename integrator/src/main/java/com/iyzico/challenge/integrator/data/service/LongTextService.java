package com.iyzico.challenge.integrator.data.service;

import com.iyzico.challenge.integrator.data.entity.LongText;
import com.iyzico.challenge.integrator.data.repository.LongTextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LongTextService {
    private final LongTextRepository repository;

    public LongTextService(LongTextRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Throwable.class)
    public LongText create(String tableName, String columnName, String id, String content) {
        LongText longText = new LongText();
        longText.setTable(tableName);
        longText.setColumnName(columnName);
        longText.setRecordId(id);
        longText.setContent(content);
        return repository.save(longText);
    }

    public LongText update(String tableName, String column, String recordId, String content) {
        LongText longText = repository.findFirstByTableAndAndColumnNameAndRecordId(tableName, column, recordId);
        if (longText == null) {
            return create(tableName, column, recordId, content);
        }

        longText.setContent(content);
        return repository.save(longText);
    }
}
