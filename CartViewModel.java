package com.smartbite.viewmodels;
import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.smartbite.database.*;
import java.util.List;
import java.util.concurrent.*;
public class CartViewModel extends AndroidViewModel {
    private final CartDao cartDao;
    private final ExecutorService executor=Executors.newSingleThreadExecutor();
    public CartViewModel(@NonNull Application app){
        super(app); cartDao=AppDatabase.getInstance(app).cartDao();
    }
    public LiveData<List<CartEntity>> getAllCartItems(){return cartDao.getAllCartItems();}
    public LiveData<Integer> getTotalItemCount(){return cartDao.getTotalItemCount();}
    public LiveData<Double> getTotalPrice(){return cartDao.getTotalPrice();}
    public void addToCart(CartEntity item){
        executor.execute(()->{
            CartEntity ex=cartDao.getItemById(item.getItemId());
            if(ex!=null){ex.setQuantity(ex.getQuantity()+1);cartDao.updateItem(ex);}
            else cartDao.insertItem(item);
        });
    }
    public void increaseQuantity(CartEntity item){
        executor.execute(()->{item.setQuantity(item.getQuantity()+1);cartDao.updateItem(item);});
    }
    public void decreaseQuantity(CartEntity item){
        executor.execute(()->{
            if(item.getQuantity()<=1) cartDao.deleteItem(item);
            else{item.setQuantity(item.getQuantity()-1);cartDao.updateItem(item);}
        });
    }
    public void removeItem(CartEntity item){executor.execute(()->cartDao.deleteItem(item));}
    public void clearCart(){executor.execute(cartDao::clearCart);}
    public String getCartRestaurantId(){
        try{return Executors.newSingleThreadExecutor().submit(cartDao::getCartRestaurantId).get();}
        catch(Exception e){return null;}
    }
}