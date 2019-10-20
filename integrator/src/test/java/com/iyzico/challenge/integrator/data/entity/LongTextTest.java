package com.iyzico.challenge.integrator.data.entity;

import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class LongTextTest {
    @Tested
    private LongText tested;

    @Test
    public void mapping_test() {
        long id = 1L;
        String table = "table";
        String columnName = "columnName";
        String recordId = "recordId";
        String content = "content";

        new StrictExpectations() {{
        }};

        tested.setId(id);
        tested.setColumnName(columnName);
        tested.setTable(table);
        tested.setRecordId(recordId);
        tested.setContent(content);

        Assert.assertEquals(id, tested.getId());
        Assert.assertEquals(columnName, tested.getColumnName());
        Assert.assertEquals(table, tested.getTable());
        Assert.assertEquals(recordId, tested.getRecordId());
        Assert.assertEquals(content, tested.getContent());
    }
}