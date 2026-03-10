package com.smartbite.models;
public class MenuItem {
    private String itemId,name,description,image,category,restaurantId;
    private double price,rating;
    private boolean isAvailable,isVeg;
    public MenuItem(){}
    public String getItemId(){return itemId;} public void setItemId(String v){itemId=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public String getImage(){return image;} public void setImage(String v){image=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String v){restaurantId=v;}
    public double getPrice(){return price;} public void setPrice(double v){price=v;}
    public double getRating(){return rating;} public void setRating(double v){rating=v;}
    public boolean isAvailable(){return isAvailable;} public void setAvailable(boolean v){isAvailable=v;}
    public boolean isVeg(){return isVeg;} public void setVeg(boolean v){isVeg=v;}
}