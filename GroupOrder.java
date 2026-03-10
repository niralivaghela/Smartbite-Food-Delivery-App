package com.smartbite.models;
import java.util.List;
import java.util.Map;
public class GroupOrder {
    private String groupOrderId,hostUserId,restaurantId,status,shareCode;
    private Map<String,List<CartItem>> memberItems;
    private Map<String,Double> memberAmounts;
    private List<String> memberIds;
    private long expiresAt;
    public GroupOrder(){}
    public String getGroupOrderId(){return groupOrderId;} public void setGroupOrderId(String v){groupOrderId=v;}
    public String getHostUserId(){return hostUserId;} public void setHostUserId(String v){hostUserId=v;}
    public String getRestaurantId(){return restaurantId;} public void setRestaurantId(String v){restaurantId=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getShareCode(){return shareCode;} public void setShareCode(String v){shareCode=v;}
    public Map<String,List<CartItem>> getMemberItems(){return memberItems;} public void setMemberItems(Map<String,List<CartItem>> v){memberItems=v;}
    public Map<String,Double> getMemberAmounts(){return memberAmounts;} public void setMemberAmounts(Map<String,Double> v){memberAmounts=v;}
    public List<String> getMemberIds(){return memberIds;} public void setMemberIds(List<String> v){memberIds=v;}
    public long getExpiresAt(){return expiresAt;} public void setExpiresAt(long v){expiresAt=v;}
}