package com.smartbite.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.smartbite.adapters.ChatAdapter;
import com.smartbite.databinding.ActivityChatbotBinding;
import com.smartbite.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatbotActivity extends AppCompatActivity {

    private ActivityChatbotBinding binding;
    private ChatAdapter chatAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // ── Predefined AI responses ───────────────────────────────────────────────
    // Each even index = keyword array, each odd index = response string
    private static final String[] KEYWORDS_0  = {"hello", "hi", "hey"};
    private static final String   RESPONSE_0  =
            "👋 Hey there! I'm Bitey, your SmartBite assistant! 🍕\n" +
                    "Ask me about:\n• Menu recommendations 🥘\n• Order tracking 🛵\n• Deals & offers 🏷️\n• Restaurant info 🏪";

    private static final String[] KEYWORDS_1  = {"menu", "recommend", "suggest", "what should"};
    private static final String   RESPONSE_1  =
            "Here are today's top picks! 🌟\n\n" +
                    "🥘 Butter Chicken at Spice Garden — ₹299\n" +
                    "🍕 Margherita Pizza at Pizza Palace — ₹249\n" +
                    "🍔 Classic Burger at Burger Barn — ₹189\n" +
                    "🍜 Hakka Noodles at Dragon Wok — ₹179\n\n" +
                    "Want details on any of these?";

    private static final String[] KEYWORDS_2  = {"order", "track", "status", "where"};
    private static final String   RESPONSE_2  =
            "I can help you track your order! 🛵\n\n" +
                    "You can see all your orders in the Orders tab, or tap 'Track' on any active order for live updates.\n\n" +
                    "Typically: Placed → Confirmed → Preparing → Out for Delivery → Delivered 🎉";

    // Promo codes are intentional non-dictionary words — not typos
    @SuppressWarnings("SpellCheckingInspection")
    private static final String PROMO_FIRST   = "FIRST50";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String PROMO_FREEDEL = "FREEDEL";

    private static final String[] KEYWORDS_3  = {"deal", "offer", "discount", "coupon", "promo"};
    private static final String   RESPONSE_3  =
            "Hot deals just for you! 🔥\n\n" +
                    "🎟️ " + PROMO_FIRST   + " — 50% off your first order (up to ₹100)\n" +
                    "🚚 " + PROMO_FREEDEL + " — Free delivery on any order\n" +
                    "💎 Check the cart screen to apply coupons!";

    private static final String[] KEYWORDS_4  = {"delivery", "time", "how long", "eta"};
    private static final String   RESPONSE_4  =
            "⏱️ Estimated delivery times:\n\n" +
                    "• Fast Food: 15–20 mins\n• Indian cuisine: 25–35 mins\n• Chinese: 20–30 mins\n\n" +
                    "Live tracking updates every 30 seconds once your rider picks up! 🛵";

    private static final String[] KEYWORDS_5  = {"payment", "pay", "wallet"};
    private static final String   RESPONSE_5  =
            "💳 We accept:\n\n" +
                    "• Cash on Delivery (COD)\n• Online payment via Razorpay\n• SmartBite Wallet balance\n\n" +
                    "Your wallet balance earns 2x loyalty points! 🌟";

    private static final String[] KEYWORDS_6  = {"loyalty", "points", "reward", "achievement"};
    private static final String   RESPONSE_6  =
            "🏆 SmartBite Rewards:\n\n" +
                    "• Every ₹10 spent = 1 point\n• 100 points = ₹10 wallet cashback\n" +
                    "• Unlock badges for ordering milestones\n• Check your achievements in Profile! 🎖️";

    private static final String[] KEYWORDS_7  = {"cancel", "refund"};
    private static final String   RESPONSE_7  =
            "❗ Cancellation policy:\n\n" +
                    "• Cancel within 2 minutes of placing ✅\n• After confirmation: contact support\n" +
                    "• Refunds go to wallet within 24 hours 💰";

    private static final String[] KEYWORDS_8  = {"restaurant", "food", "cuisine"};
    private static final String   RESPONSE_8  =
            "🏪 We have 6 amazing restaurants:\n\n" +
                    "• 🥘 Spice Garden — North Indian\n• 🍕 Pizza Palace — Italian\n" +
                    "• 🍔 Burger Barn — American\n• 🍜 Dragon Wok — Chinese\n" +
                    "• 🥘 South Tadka — South Indian\n• 🍦 Sweet Tooth — Desserts";

    private static final String[] KEYWORDS_9  = {"thank", "thanks", "bye", "goodbye"};
    private static final String   RESPONSE_9  =
            "You're welcome! 😊 Happy eating!\nFeel free to ask anything anytime. 🍕\n\n— Bitey 🤖";

    /** Parallel arrays: KEYWORD_GROUPS[i] maps to RESPONSES[i]. */
    private static final String[][] KEYWORD_GROUPS = {
            KEYWORDS_0, KEYWORDS_1, KEYWORDS_2, KEYWORDS_3, KEYWORDS_4,
            KEYWORDS_5, KEYWORDS_6, KEYWORDS_7, KEYWORDS_8, KEYWORDS_9
    };
    private static final String[] RESPONSES = {
            RESPONSE_0, RESPONSE_1, RESPONSE_2, RESPONSE_3, RESPONSE_4,
            RESPONSE_5, RESPONSE_6, RESPONSE_7, RESPONSE_8, RESPONSE_9
    };

    private static final String DEFAULT_RESPONSE =
            "Hmm, I'm still learning! 🤔\n\n" +
                    "You can ask me about:\n• Menu & recommendations 🥘\n• Order tracking 🛵\n" +
                    "• Deals & coupons 🏷️\n• Delivery time ⏱️\n• Payments 💳\n• Loyalty points 🏆";

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatbotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupRecyclerView();
        setupInput();
        playEntranceAnimation();
        sendWelcomeMessage();
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Bitey AI Assistant 🤖");
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(messages);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(llm);
        binding.rvChat.setAdapter(chatAdapter);
    }

    private void setupInput() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(text)) {
                animateSendButton();
                sendUserMessage(text);
                binding.etMessage.setText("");
            }
        });

        binding.chipMenu.setOnClickListener(v  -> sendUserMessage("What's on the menu?"));
        binding.chipTrack.setOnClickListener(v -> sendUserMessage("Track my order"));
        binding.chipDeals.setOnClickListener(v -> sendUserMessage("Show me deals"));
        binding.chipHelp.setOnClickListener(v  -> sendUserMessage("I need help"));
    }

    // ── Messaging ─────────────────────────────────────────────────────────────

    private void sendWelcomeMessage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null
                && user.getDisplayName() != null
                && !user.getDisplayName().isEmpty())
                ? user.getDisplayName().split(" ")[0]
                : "there";

        handler.postDelayed(() -> addBotMessage(
                "👋 Hi " + name + "! I'm Bitey, your SmartBite assistant! 🍕\n\n"
                        + "I can help you with menu recommendations, order tracking, deals & more. "
                        + "What can I do for you today?"), 400);
    }

    private void sendUserMessage(String text) {
        addUserMessage(text);
        showTypingIndicator();
        long delay = 800L + (long) (Math.random() * 600);
        handler.postDelayed(() -> {
            removeTypingIndicator();
            addBotMessage(generateResponse(text));
        }, delay);
    }

    private String generateResponse(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        for (int i = 0; i < KEYWORD_GROUPS.length; i++) {
            for (String keyword : KEYWORD_GROUPS[i]) {
                if (lower.contains(keyword)) return RESPONSES[i];
            }
        }
        return DEFAULT_RESPONSE;
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true, System.currentTimeMillis()));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, false, System.currentTimeMillis()));
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void showTypingIndicator() {
        ChatMessage typing = new ChatMessage("...", false, System.currentTimeMillis(), true);
        messages.add(typing);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).isTyping()) {
                messages.remove(i);
                chatAdapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    private void scrollToBottom() {
        binding.rvChat.post(() ->
                binding.rvChat.smoothScrollToPosition(Math.max(0, messages.size() - 1)));
    }

    // ── Animations ────────────────────────────────────────────────────────────

    private void playEntranceAnimation() {
        binding.cardInput.setTranslationY(100f);
        binding.cardInput.setAlpha(0f);
        binding.cardInput.animate()
                .translationY(0f).alpha(1f)
                .setDuration(400).setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .start();
    }

    private void animateSendButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.btnSend, "scaleX", 1f, 0.85f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.btnSend, "scaleY", 1f, 0.85f, 1.1f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }
}