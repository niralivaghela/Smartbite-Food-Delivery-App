package com.smartbite.utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.models.MenuItem;
import com.smartbite.models.Order;
import java.util.*;
public class AIRecommendationEngine {
    private FirebaseFirestore db;
    private String userId;
    public interface RecommendationCallback{void onRecommendations(List<MenuItem> items,String reason);}
    public AIRecommendationEngine(String userId){
        this.db=FirebaseFirestore.getInstance(); this.userId=userId;
    }
    public void getPersonalizedRecommendations(RecommendationCallback callback){
        db.collection(Constants.COLLECTION_ORDERS).whereEqualTo("customerId",userId).limit(20).get()
            .addOnSuccessListener(snapshots->{
                List<Order> orders=snapshots.toObjects(Order.class);
                Map<String,Integer> freq=new HashMap<>();
                for(Order o:orders) if(o.getItems()!=null)
                    for(com.smartbite.models.CartItem i:o.getItems())
                        freq.put(i.getItemId(),freq.getOrDefault(i.getItemId(),0)+1);
                String ctx=getTimeContext();
                fetchRecommendedItems(freq,ctx,callback);
            });
    }
    private void fetchRecommendedItems(Map<String,Integer> history,String ctx,RecommendationCallback cb){
        db.collection(Constants.COLLECTION_MENU_ITEMS).whereEqualTo("available",true).limit(20).get()
            .addOnSuccessListener(snapshots->{
                List<MenuItem> all=snapshots.toObjects(MenuItem.class);
                List<ScoredItem> scored=new ArrayList<>();
                for(MenuItem m:all) scored.add(new ScoredItem(m,calcScore(m,history,ctx)));
                scored.sort((a,b)->Double.compare(b.score,a.score));
                List<MenuItem> recs=new ArrayList<>();
                for(int i=0;i<Math.min(10,scored.size());i++) recs.add(scored.get(i).item);
                cb.onRecommendations(recs,getReason(ctx));
            });
    }
    private double calcScore(MenuItem m,Map<String,Integer> h,String ctx){
        double s=h.getOrDefault(m.getItemId(),0)*10+m.getRating()*5;
        if(ctx.equals("breakfast")&&m.getCategory()!=null&&m.getCategory().toLowerCase().contains("breakfast")) s+=20;
        else if(ctx.equals("lunch")&&m.getCategory()!=null&&m.getCategory().toLowerCase().contains("main")) s+=20;
        else if(ctx.equals("dinner")&&m.getPrice()>200) s+=15;
        s+=(500-m.getPrice())/100;
        return s;
    }
    private String getTimeContext(){
        int h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(h>=6&&h<11) return "breakfast";
        if(h>=11&&h<16) return "lunch";
        if(h>=16&&h<19) return "snack";
        return "dinner";
    }
    private String getReason(String ctx){
        switch(ctx){
            case "breakfast": return "🌅 Perfect for breakfast!";
            case "lunch": return "☀️ Top lunch picks for you!";
            case "snack": return "🕓 Evening snack time!";
            default: return "🌙 Dinner recommendations!";
        }
    }
    static class ScoredItem{MenuItem item;double score;ScoredItem(MenuItem i,double s){item=i;score=s;}}
}