package com.smartbite.database;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;
@Dao
public interface CartDao {
    @Insert(onConflict=OnConflictStrategy.REPLACE) void insertItem(CartEntity item);
    @Update void updateItem(CartEntity item);
    @Delete void deleteItem(CartEntity item);
    @Query("DELETE FROM cart") void clearCart();
    @Query("DELETE FROM cart WHERE restaurantId=:restaurantId") void clearCartByRestaurant(String restaurantId);
    @Query("SELECT * FROM cart") LiveData<List<CartEntity>> getAllCartItems();
    @Query("SELECT * FROM cart WHERE itemId=:itemId") CartEntity getItemById(String itemId);
    @Query("SELECT SUM(quantity) FROM cart") LiveData<Integer> getTotalItemCount();
    @Query("SELECT SUM(price*quantity) FROM cart") LiveData<Double> getTotalPrice();
    @Query("SELECT restaurantId FROM cart LIMIT 1") String getCartRestaurantId();
    @Query("SELECT COUNT(*) FROM cart") int getCartCount();
}