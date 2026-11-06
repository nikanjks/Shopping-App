package com.apts.shoppingapp.ui.slideshow;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apts.shoppingapp.CartItem;
import com.apts.shoppingapp.DatabaseHelper;
import com.apts.shoppingapp.OrderSummary;
import com.apts.shoppingapp.SessionManager;
import com.apts.shoppingapp.databinding.FragmentSlideshowBinding;
import com.apts.shoppingapp.databinding.ItemOrderBinding;

import java.util.ArrayList;
import java.util.Locale;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentSlideshowBinding.inflate(
                inflater, container, false
        );

        databaseHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        displayOrders();

        return binding.getRoot();
    }

    private void displayOrders() {
        if (binding == null) {
            return;
        }

        LinearLayout container = binding.ordersContainer;
        container.removeAllViews();

        ArrayList<OrderSummary> orders = databaseHelper.getOrders(sessionManager.getUsername());

        if (orders.isEmpty()) {
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("You haven't placed any orders yet.");
            emptyMessage.setTextSize(16);
            emptyMessage.setTextColor(Color.parseColor("#4fc0e8"));
            emptyMessage.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            emptyMessage.setLayoutParams(params);

            container.addView(emptyMessage);
            return;
        }

        for (OrderSummary order : orders) {
            addOrderCard(container, order);
        }
    }

    private void addOrderCard(
            LinearLayout container,
            OrderSummary order) {

        ItemOrderBinding itemBinding = ItemOrderBinding.inflate(
                getLayoutInflater(),
                container,
                false
        );

        itemBinding.txtOrderId.setText("Order #" + order.getOrderNumber());
        itemBinding.txtOrderDate.setText("Date: " + order.getOrderDate());
        itemBinding.txtOrderTotal.setText(String.format(
                Locale.getDefault(),
                "₱%.2f",
                order.getTotal()
        ));

        itemBinding.txtOrderDetails.setVisibility(View.GONE);
        itemBinding.btnToggleDetails.setText("View Order Details");

        itemBinding.btnToggleDetails.setOnClickListener(view -> {
            if (itemBinding.txtOrderDetails.getVisibility() == View.VISIBLE) {
                itemBinding.txtOrderDetails.setVisibility(View.GONE);
                itemBinding.btnToggleDetails.setText("View Order Details");
                return;
            }

            ArrayList<CartItem> items =
                    databaseHelper.getOrderItems(order.getId());

            if (items.isEmpty()) {
                itemBinding.txtOrderDetails.setText("No item details found for this order.");
            } else {
                StringBuilder text = new StringBuilder();

                for (CartItem item : items) {
                    text.append("• ")
                            .append(item.getName())
                            .append(" (Qty: ")
                            .append(item.getQuantity())
                            .append(")\n  Subtotal: ")
                            .append(String.format(
                                    Locale.getDefault(),
                                    "₱%.2f",
                                    item.getSubtotal()
                            ))
                            .append("\n\n");
                }

                itemBinding.txtOrderDetails.setText(text.toString().trim());
            }

            itemBinding.txtOrderDetails.setVisibility(View.VISIBLE);
            itemBinding.btnToggleDetails.setText("Hide Order Details");
        });

        container.addView(itemBinding.getRoot());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null && databaseHelper != null) {
            displayOrders();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}