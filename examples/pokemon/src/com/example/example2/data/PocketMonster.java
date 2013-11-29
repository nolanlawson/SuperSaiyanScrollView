package com.example.example2.data;

/**
 * POJO describing a Pocket Monster.
 * 
 * @author nolan
 *
 */
public class PocketMonster {

    private String uniqueId;
    private int nationalDexNumber;
    private String type1;
    private String type2;
    private String name;
    
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
    public int getNationalDexNumber() {
        return nationalDexNumber;
    }
    public void setNationalDexNumber(int nationalDexNumber) {
        this.nationalDexNumber = nationalDexNumber;
    }
    public String getType1() {
        return type1;
    }
    public void setType1(String type1) {
        this.type1 = type1;
    }
    public String getType2() {
        return type2;
    }
    public void setType2(String type2) {
        this.type2 = type2;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
