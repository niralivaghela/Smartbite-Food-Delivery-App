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

## 📞 Support
Built with ❤️ by SmartBite Team
