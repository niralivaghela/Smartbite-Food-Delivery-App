package com.smartbite.models;
public class Achievement {
    private String id,title,description,icon,type;
    private int requiredCount,currentCount,rewardPoints;
    private boolean unlocked;
    public Achievement(){}
    public Achievement(String id,String title,String description,String icon,int requiredCount,String type,int rewardPoints){
        this.id=id;this.title=title;this.description=description;this.icon=icon;
        this.requiredCount=requiredCount;this.type=type;this.rewardPoints=rewardPoints;this.unlocked=false;this.currentCount=0;
    }
    public String getId(){return id;} public void setId(String v){id=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getIcon(){return icon;} public void setIcon(String v){icon=v;}
    public String getType(){return type;} public void setType(String v){type=v;}
    public int getRequiredCount(){return requiredCount;} public void setRequiredCount(int v){requiredCount=v;}
    public int getCurrentCount(){return currentCount;} public void setCurrentCount(int v){currentCount=v;}
    public int getRewardPoints(){return rewardPoints;} public void setRewardPoints(int v){rewardPoints=v;}
    public boolean isUnlocked(){return unlocked;} public void setUnlocked(boolean v){unlocked=v;}
    public float getProgress(){return Math.min(1f,(float)currentCount/requiredCount);}
}