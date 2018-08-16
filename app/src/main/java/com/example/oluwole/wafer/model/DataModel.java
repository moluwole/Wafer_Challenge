package com.example.oluwole.wafer.model;

public class DataModel {

    private String name;
    private String currencies;
    private String languages;

    public DataModel(String name, String currencies, String languages){
        this.name = name;
        this.currencies = currencies;
        this.languages = languages;
    }

    public String getName(){
        return name;
    }

    public String getCurrencies() {
        return currencies;
    }

    public String getLanguages() {
        return languages;
    }
}
