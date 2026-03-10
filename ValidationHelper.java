package com.smartbite.utils;
import android.text.TextUtils;
public class ValidationHelper {
    public static boolean isValidEmail(String email){
        return !TextUtils.isEmpty(email)&&android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    public static boolean isValidPhone(String phone){
        return !TextUtils.isEmpty(phone)&&phone.length()>=10;
    }
    public static boolean isValidPassword(String password){
        return !TextUtils.isEmpty(password)&&password.length()>=6;
    }
    public static boolean isValidName(String name){
        return !TextUtils.isEmpty(name)&&name.length()>=2;
    }
    public static boolean passwordsMatch(String p1,String p2){
        return p1!=null&&p1.equals(p2);
    }
}