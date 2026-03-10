package com.smartbite.models;
public class CartItem {
    private String itemId,name,image,restaurantId;
    private double price;
    private int quantity;
    public CartItem(){}
    public CartItem(String itemId,String name,String image,String restaurantId,double price,int quantity){
        this.itemId=itemId;this.name=name;this.image=image;this.restaurantId=restaurantId;this.price=price;this.quantity=quantity;
    }
    public String getItemId(){return itemId;} public void setItemId(String v){itemId=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getImage(){return image;} public void setImage(String v){image=v;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String v){restaurantId=v;}
    public double getPrice(){return price;} public void setPrice(double v){price=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public double getTotalPrice(){return price*quantity;}
}