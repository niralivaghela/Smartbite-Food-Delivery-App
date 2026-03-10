package com.smartbite.utils;
import android.app.Activity;
import android.os.Handler;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.models.Achievement;
import java.util.*;
public class AchievementManager {
    private static final List<Achievement> ALL=new ArrayList<Achievement>(){{
        add(new Achievement("first_order","🎉 First Bite!","Place your first order","🎉",1,"orders",50));
        add(new Achievement("food_explorer","🌍 Food Explorer","Order from 5 restaurants","🌍",5,"restaurants",100));
        add(new Achievement("loyal_customer","💎 Loyal Customer","Place 10 orders","💎",10,"orders",200));
        add(new Achievement("big_spender","💸 Big Spender","Spend ₹5000 total","💸",5000,"spending",300));
        add(new Achievement("review_master","⭐ Review Master","Write 5 reviews","⭐",5,"reviews",150));
        add(new Achievement("referral_king","👑 Referral King","Refer 3 friends","👑",3,"referrals",250));
        add(new Achievement("night_owl","🦉 Night Owl","Order after midnight 3 times","🦉",3,"night_orders",100));
        add(new Achievement("speed_orderer","⚡ Speed Orderer","Place 3 orders in one day","⚡",3,"daily_orders",150));
    }};
    private FirebaseFirestore db;
    private String userId;
    private Activity activity;
    public AchievementManager(Activity activity,String userId){
        this.activity=activity;this.userId=userId;this.db=FirebaseFirestore.getInstance();
    }
    public void checkAndUpdateAchievements(String eventType,int value){
        for(Achievement a:ALL) if(a.getType().equals(eventType)) checkAchievement(a,value);
    }
    private void checkAchievement(Achievement a,int val){
        String docPath=Constants.COLLECTION_USERS+"/"+userId+"/achievements/"+a.getId();
        db.document(docPath).get().addOnSuccessListener(doc->{
            boolean unlocked=doc.exists()&&Boolean.TRUE.equals(doc.getBoolean("unlocked"));
            if(!unlocked&&val>=a.getRequiredCount()){
                Map<String,Object> data=new HashMap<>();
                data.put("unlocked",true);
                data.put("unlockedAt",System.currentTimeMillis());
                data.put("rewardPoints",a.getRewardPoints());
                db.document(docPath).set(data).addOnSuccessListener(u->{
                    showAchievementUnlocked(a);addRewardPoints(a.getRewardPoints());
                });
            } else {
                Map<String,Object> data=new HashMap<>();
                data.put("currentCount",val);data.put("unlocked",false);
                db.document(docPath).set(data);
            }
        });
    }
    private void showAchievementUnlocked(Achievement a){
        if(activity==null) return;
        activity.runOnUiThread(()->{
            android.widget.Toast.makeText(activity,
                "🏆 Achievement Unlocked: "+a.getTitle(),android.widget.Toast.LENGTH_LONG).show();
        });
    }
    private void addRewardPoints(int points){
        db.collection(Constants.COLLECTION_USERS).document(userId).get()
            .addOnSuccessListener(doc->{
                int cur=doc.getLong("loyaltyPoints")!=null?doc.getLong("loyaltyPoints").intValue():0;
                db.collection(Constants.COLLECTION_USERS).document(userId).update("loyaltyPoints",cur+points);
            });
    }
    public void loadUserAchievements(AchievementLoadCallback cb){
        db.collection(Constants.COLLECTION_USERS).document(userId).collection("achievements").get()
            .addOnSuccessListener(snapshots->{
                List<Achievement> list=new ArrayList<>();
                for(Achievement a:ALL){
                    for(com.google.firebase.firestore.DocumentSnapshot doc:snapshots.getDocuments()){
                        if(doc.getId().equals(a.getId())){
                            a.setUnlocked(Boolean.TRUE.equals(doc.getBoolean("unlocked")));
                            Long c=doc.getLong("currentCount");
                            a.setCurrentCount(c!=null?c.intValue():0);
                        }
                    }
                    list.add(a);
                }
                cb.onLoaded(list);
            });
    }
    public interface AchievementLoadCallback{void onLoaded(List<Achievement> achievements);}
}