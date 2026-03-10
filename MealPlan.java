package com.smartbite.models;
public class MealPlan {
    private String planId,name,description,badge,color;
    private double weeklyPrice,monthlyPrice;
    private int mealsPerWeek;
    public MealPlan(){}
    public MealPlan(String planId,String name,String description,double weeklyPrice,double monthlyPrice,int mealsPerWeek,String badge,String color){
        this.planId=planId;this.name=name;this.description=description;this.weeklyPrice=weeklyPrice;
        this.monthlyPrice=monthlyPrice;this.mealsPerWeek=mealsPerWeek;this.badge=badge;this.color=color;
    }
    public String getPlanId(){return planId;} public void setPlanId(String v){planId=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getBadge(){return badge;} public void setBadge(String v){badge=v;}
    public String getColor(){return color;} public void setColor(String v){color=v;}
    public double getWeeklyPrice(){return weeklyPrice;} public void setWeeklyPrice(double v){weeklyPrice=v;}
    public double getMonthlyPrice(){return monthlyPrice;} public void setMonthlyPrice(double v){monthlyPrice=v;}
    public int getMealsPerWeek(){return mealsPerWeek;} public void setMealsPerWeek(int v){mealsPerWeek=v;}
}