package com.elenakuropatkina.my.cloud.common;

import java.util.List;

public class ListMessage extends AbstractMessage {
    private List<String> list;

    public List<String> getList() {

        return list;
    }

    public void setList(List<String> list) {
        this.list = list;
    }
}
