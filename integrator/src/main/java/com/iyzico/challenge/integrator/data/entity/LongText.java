package com.iyzico.challenge.integrator.data.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "long_text")
public class LongText {
    private long id;
    private String table;
    private String type;
    private String content;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "table_name", length = 128)
    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Basic
    @Column(name = "type", length = 128)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Lob
    @Column(name = "content", length = 128)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
