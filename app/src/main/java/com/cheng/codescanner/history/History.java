package com.cheng.codescanner.history;

public class History {
    private String history_text;
    private String history_time;

    public History(String history_time, String history_text) {
        this.history_text = history_text;
        this.history_time = history_time;
        System.out.println("History：对象创建成功！");
    }

    public String getHistory_text() {
        return history_text;
    }

    public void setHistory_text(String history_text) {
        this.history_text = history_text;
    }

    public String getHistory_time() {
        return history_time;
    }

    public void setHistory_time(String history_time) {
        this.history_time = history_time;
    }

    @Override
    public String toString() {
        return " 👉： " + history_text + "\n —— " + history_time + "\n";
    }
}
