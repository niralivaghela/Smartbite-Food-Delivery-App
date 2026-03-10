package com.smartbite.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.smartbite.models.Order;
import com.smartbite.utils.Constants;

import java.util.List;

public class OrderViewModel extends ViewModel {

    private static final String TAG = "OrderViewModel";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private final MutableLiveData<Order> currentOrder = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private ListenerRegistration listener;

    public LiveData<List<Order>> getOrders() { return orders; }
    public LiveData<Order> getCurrentOrder() { return currentOrder; }

    // FIX: Expose errors so UI can react (e.g. show a Toast or retry button)
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadUserOrders(String userId) {
        if (userId == null || userId.isEmpty()) {
            Log.w(TAG, "loadUserOrders called with empty userId");
            errorMessage.setValue("User not logged in");
            return;
        }

        Log.d(TAG, "Loading orders for userId: " + userId);

        // FIX: Removed .orderBy("timestamp") because it requires a Firestore
        // composite index that may not exist — this was causing silent failures.
        // We sort the results in-memory instead (see below).
        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("customerId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    Log.d(TAG, "Orders loaded: " + snap.size());
                    List<Order> result = snap.toObjects(Order.class);

                    // FIX: Set orderId manually since Firestore doesn't auto-map document ID
                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).getOrderId() == null || result.get(i).getOrderId().isEmpty()) {
                            result.get(i).setOrderId(snap.getDocuments().get(i).getId());
                        }
                    }

                    // FIX: Sort in-memory by timestamp descending (no index needed)
                    result.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                    orders.setValue(result);
                })
                .addOnFailureListener(e -> {
                    // FIX: Log the real error and try fallback with "userId" field
                    Log.e(TAG, "❌ customerId query failed: " + e.getMessage(), e);
                    loadUserOrdersFallback(userId);
                });
    }

    /**
     * FIX: Fallback — some orders may have been saved with "userId" field
     * instead of "customerId". This covers both cases.
     */
    private void loadUserOrdersFallback(String userId) {
        Log.d(TAG, "Trying fallback with userId field...");
        db.collection(Constants.COLLECTION_ORDERS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    Log.d(TAG, "Fallback orders loaded: " + snap.size());
                    List<Order> result = snap.toObjects(Order.class);

                    for (int i = 0; i < result.size(); i++) {
                        if (result.get(i).getOrderId() == null || result.get(i).getOrderId().isEmpty()) {
                            result.get(i).setOrderId(snap.getDocuments().get(i).getId());
                        }
                    }

                    result.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    orders.setValue(result);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Fallback userId query also failed: " + e.getMessage(), e);
                    errorMessage.setValue("Could not load orders: " + e.getMessage());
                });
    }

    public void listenToOrder(String orderId) {
        if (listener != null) listener.remove(); // FIX: prevent duplicate listeners
        listener = db.collection(Constants.COLLECTION_ORDERS).document(orderId)
                .addSnapshotListener((doc, e) -> {
                    if (e != null) {
                        Log.e(TAG, "❌ listenToOrder error: " + e.getMessage());
                        errorMessage.setValue(e.getMessage());
                        return;
                    }
                    if (doc != null && doc.exists()) {
                        Order order = doc.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(doc.getId()); // FIX: set ID manually
                            currentOrder.setValue(order);
                        }
                    }
                });
    }

    public void updateOrderStatus(String orderId, String status) {
        db.collection(Constants.COLLECTION_ORDERS)
                .document(orderId)
                .update("status", status)
                .addOnFailureListener(e ->
                        Log.e(TAG, "❌ Failed to update order status: " + e.getMessage()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listener != null) listener.remove();
    }
}