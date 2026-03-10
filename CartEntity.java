package com.smartbite.database;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
@Entity(tableName="cart")
public class CartEntity {
    @PrimaryKey @NonNull private String itemId;
    private String name,image,restaurantId,restaurantName;
    private double price;
    private int quantity;
    public CartEntity(@NonNull String itemId,String name,String image,String restaurantId,String restaurantName,double price,int quantity){
        this.itemId=itemId;this.name=name;this.image=image;this.restaurantId=restaurantId;
        this.restaurantName=restaurantName;this.price=price;this.quantity=quantity;
    }
    @NonNull public String getItemId(){return itemId;} public void setItemId(@NonNull String v){itemId=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getImage(){return image;} public void setImage(String v){image=v;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String v){restaurantId=v;}
    public String getRestaurantName(){return restaurantName;} public void setRestaurantName(String v){restaurantName=v;}
    public double getPrice(){return price;} public void setPrice(double v){price=v;}
    public int getQuantity(){return quantity;} public void setQuantity(int v){quantity=v;}
    public double getTotalPrice(){return price*quantity;}
}