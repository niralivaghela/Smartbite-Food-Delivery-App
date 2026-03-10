# 🍕 SmartBite - Advanced Food Delivery App

## 📱 Complete Android Food Delivery Ecosystem

### 🚀 Project Overview
SmartBite is a **next-level multi-vendor food delivery app** built with Java for Android.

---

## 📁 Project Structure
```
SmartBite/
├── app/
│   ├── src/main/
│   │   ├── java/com/smartbite/
│   │   │   ├── activities/          (21 Activity files)
│   │   │   │   ├── SplashActivity.java
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   ├── HomeActivity.java
│   │   │   │   ├── CartActivity.java
│   │   │   │   ├── CheckoutActivity.java
│   │   │   │   ├── OrderTrackingActivity.java
│   │   │   │   ├── ChatbotActivity.java
│   │   │   │   ├── GroupOrderActivity.java
│   │   │   │   ├── MealPlanActivity.java
│   │   │   │   ├── AchievementsActivity.java
│   │   │   │   ├── WalletActivity.java
│   │   │   │   ├── ProfileActivity.java
│   │   │   │   ├── RestaurantDetailActivity.java
│   │   │   │   ├── RestaurantPanelActivity.java
│   │   │   │   ├── DeliveryAgentActivity.java
│   │   │   │   ├── AdminActivity.java
│   │   │   │   ├── SearchActivity.java
│   │   │   │   ├── OrderSuccessActivity.java
│   │   │   │   ├── OrderHistoryActivity.java
│   │   │   │   └── OTPActivity.java
│   │   │   ├── fragments/
│   │   │   │   ├── customer/
│   │   │   │   │   ├── HomeFragment.java
│   │   │   │   │   ├── SearchFragment.java
│   │   │   │   │   ├── OrdersFragment.java
│   │   │   │   │   └── ProfileFragment.java
│   │   │   │   ├── restaurant/
│   │   │   │   │   ├── OrderManageFragment.java
│   │   │   │   │   ├── MenuManageFragment.java
│   │   │   │   │   └── RevenueFragment.java
│   │   │   │   └── admin/
│   │   │   │       ├── AdminDashboardFragment.java
│   │   │   │       ├── AdminRestaurantsFragment.java
│   │   │   │       ├── AdminUsersFragment.java
│   │   │   │       └── AdminOrdersFragment.java
│   │   │   ├── adapters/            (10 Adapter files)
│   │   │   ├── models/              (9 Model files)
│   │   │   ├── database/            (Room DB: 3 files)
│   │   │   ├── viewmodels/          (4 ViewModel files)
│   │   │   ├── services/            (FCM + Location)
│   │   │   └── utils/               (8 Utility files)
│   │   ├── res/
│   │   │   ├── layout/              (40+ XML layouts)
│   │   │   ├── values/              (colors, themes, strings, dimens)
│   │   │   ├── values-night/        (Dark Mode colors)
│   │   │   ├── drawable/            (22 drawable XMLs)
│   │   │   ├── anim/                (4 animations)
│   │   │   ├── menu/                (navigation menus)
│   │   │   └── color/               (selectors)
│   │   └── AndroidManifest.xml
│   ├── build.gradle
│   └── google-services.json        ⚠️ REPLACE WITH YOUR OWN
├── build.gradle
└── settings.gradle
```

---

## ⚙️ SETUP STEPS (MUST DO BEFORE RUNNING)

### Step 1: Firebase Setup
1. Go to https://console.firebase.google.com
2. Create new project: **SmartBite**
3. Add Android app with package: `com.smartbite`
4. Download **google-services.json** → place in `/app/` folder
5. Enable: Authentication, Firestore, Storage, Cloud Messaging

### Step 2: Google Maps API
1. Go to https://console.cloud.google.com
2. Enable: Maps SDK, Directions API, Places API
3. Create API Key
4. Replace in **AndroidManifest.xml**:
   `android:value="YOUR_GOOGLE_MAPS_API_KEY"`
5. Also replace in **Constants.java**: `MAPS_API_KEY`

### Step 3: Razorpay
1. Go to https://razorpay.com → Sign Up → Dashboard
2. Get Test API Key
3. Replace in **Constants.java**: `RAZORPAY_KEY`

### Step 4: Google Sign-In
1. In Firebase Console → Authentication → Sign-in method → Google
2. Copy Web Client ID
3. Replace in **strings.xml**: `default_web_client_id`

### Step 5: Run
```
1. Open in Android Studio
2. Sync Gradle
3. Run on emulator or device (API 24+)
```

---

## 🔑 FEATURES IMPLEMENTED

### Core Features (Steps 1-7)
- ✅ Multi-role Auth (Customer/Restaurant/Agent/Admin)
- ✅ Google Sign-In + Email/Password
- ✅ Restaurant Listing with Shimmer Loading
- ✅ Cart System (Room SQLite)
- ✅ Checkout (3 Payment Methods)
- ✅ Razorpay Payment Gateway
- ✅ Wallet System
- ✅ Live Order Tracking (Google Maps)
- ✅ Restaurant Panel (Orders/Menu/Revenue)
- ✅ Admin Panel (Dashboard/Restaurants/Users)

### Advanced Features
- ✅ AI Food Recommendation Engine
- ✅ Voice Search & Voice Order
- ✅ Smart Chatbot Assistant
- ✅ Gamification & Achievements
- ✅ Group Order with Split Bill
- ✅ Subscription Meal Plans
- ✅ Animated Splash with Particles
- ✅ Dark Mode Support
- ✅ Skeleton Loading Screens
- ✅ Micro-animations & Transitions
- ✅ FCM Push Notifications
- ✅ Background Location Service

---

## 📦 Key Dependencies
| Library | Purpose |
|---------|---------|
| Firebase Auth/Firestore/Storage | Backend |
| Google Maps SDK | Live Tracking |
| Room Database | Local Cart |
| Razorpay | Payments |
| Glide | Image Loading |
| Lottie | Animations |
| MPAndroidChart | Revenue Charts |
| Shimmer | Loading Effects |
| CircleImageView | Profile Pictures |

---
<img width="431" height="840" alt="Image" src="https://github.com/user-attachments/assets/be876fd9-a296-4558-8792-07466d9a5c1d" />
<img width="435" height="861" alt="Image" src="https://github.com/user-attachments/assets/23c00a2c-772b-4bd8-94b4-52a4eb44fbae" />
<img width="415" height="866" alt="Image" src="https://github.com/user-attachments/assets/d9978dda-9a24-4d1f-87e0-9b73d1b4e3aa" />
<img width="501" height="834" alt="Image" src="https://github.com/user-attachments/assets/a8b2cf07-aa21-4d55-bbc1-4bef2980bd6c" />
<img width="469" height="854" alt="Image" src="https://github.com/user-attachments/assets/7e8c1cca-70d6-49e2-831d-2e0ded34584f" />
<img width="449" height="855" alt="Image" src="https://github.com/user-attachments/assets/369f8621-9299-4b5e-8e0b-80e388ac38d8" />
<img width="460" height="857" alt="Image" src="https://github.com/user-attachments/assets/3aff6ddf-64a5-41ed-81a4-31625b3c7197" />
<img width="473" height="859" alt="Image" src="https://github.com/user-attachments/assets/434876bc-4bf0-46f7-9c01-f7a545b3c1af" />
<img width="426" height="858" alt="Image" src="https://github.com/user-attachments/assets/7ea4df25-ff0d-4268-878a-abfb2a7ed91d" />
<img width="440" height="852" alt="Image" src="https://github.com/user-attachments/assets/6254eda6-8c39-477f-bc3d-02324e29f3ea" />
<img width="433" height="862" alt="Image" src="https://github.com/user-attachments/assets/238e249c-1e37-4677-a1cc-53b9e87d7a77" />
<img width="427" height="785" alt="Image" src="https://github.com/user-attachments/assets/34155325-f13d-45ee-85e3-419f01230022" />
<img width="426" height="837" alt="Image" src="https://github.com/user-attachments/assets/10821895-2158-4d79-b166-df338e274572" />
<img width="436" height="851" alt="Image" src="https://github.com/user-attachments/assets/c17cd99c-f65d-4a87-8fc2-c41f245c7996" />
<img width="444" height="848" alt="Image" src="https://github.com/user-attachments/assets/310f159f-a7ba-48d1-bc9a-6bf10477dd61" />
