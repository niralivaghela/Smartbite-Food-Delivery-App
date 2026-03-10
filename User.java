package com.smartbite.models;

public class User {
    private String userId, name, email, phone, role, profilePic, referralCode, address, fcmToken;
    private double walletBalance;
    private int loyaltyPoints;
    private String activePlan;
    private Boolean banned;

    public User() {}

    public User(String userId, String name, String email, String phone, String role) {
        this.userId = userId; this.name = name; this.email = email;
        this.phone = phone; this.role = role;
        this.walletBalance = 0.0; this.loyaltyPoints = 0;
    }

    public String getUserId()  { return userId; }  public void setUserId(String v)  { userId = v; }
    /** Alias for getUserId() — used by fragments via getUid() */
    public String getUid()     { return userId; }  public void setUid(String v)     { userId = v; }
    public String getName()    { return name; }    public void setName(String v)    { name = v; }
    public String getEmail()   { return email; }   public void setEmail(String v)   { email = v; }
    public String getPhone()   { return phone; }   public void setPhone(String v)   { phone = v; }
    public String getRole()    { return role; }    public void setRole(String v)    { role = v; }
    public String getProfilePic()   { return profilePic; }   public void setProfilePic(String v)   { profilePic = v; }
    public String getReferralCode() { return referralCode; } public void setReferralCode(String v) { referralCode = v; }
    public String getAddress()      { return address; }      public void setAddress(String v)      { address = v; }
    public String getFcmToken()     { return fcmToken; }     public void setFcmToken(String v)     { fcmToken = v; }
    public double getWalletBalance() { return walletBalance; } public void setWalletBalance(double v) { walletBalance = v; }
    public int getLoyaltyPoints()    { return loyaltyPoints; } public void setLoyaltyPoints(int v)    { loyaltyPoints = v; }
    public String getActivePlan()    { return activePlan; }    public void setActivePlan(String v)    { activePlan = v; }
    public Boolean getBanned()       { return banned; }        public void setBanned(Boolean v)       { banned = v; }
}