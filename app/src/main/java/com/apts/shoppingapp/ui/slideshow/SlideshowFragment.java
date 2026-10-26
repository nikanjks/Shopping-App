package com.apts.shoppingapp.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apts.shoppingapp.CartItem;
import com.apts.shoppingapp.DatabaseHelper;
import com.apts.shoppingapp.OrderSummary;
import com.apts.shoppingapp.databinding.FragmentSlideshowBinding;

import java.util.ArrayList;
import java.util.Locale;

public class SlideshowFragment extends Fragment {

    private FragmentSlideshowBinding binding;
    private DatabaseHelper databaseHelper;

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

        displayOrders();

        return binding.getRoot();
    }

    private void displayOrders() {
        if (binding == null) {
            return;
        }

        LinearLayout container = binding.ordersContainer;
        container.removeAllViews();

        ArrayList<OrderSummary> orders = databaseHelper.getOrders();

        if (orders.isEmpty()) {
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("You haven't placed any orders yet.");
            emptyMessage.setTextSize(16);
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

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);
        card.setBackgroundResource(
                android.R.drawable.dialog_holo_light_frame
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = 16;
        card.setLayoutParams(cardParams);

        TextView orderId = new TextView(requireContext());
        orderId.setText("Order #" + order.getId());
        orderId.setTextSize(18);
        orderId.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(orderId);

        TextView orderDate = new TextView(requireContext());
        orderDate.setText("Date: " + order.getOrderDate());
        orderDate.setTextSize(14);
        card.addView(orderDate);

        TextView orderTotal = new TextView(requireContext());
        orderTotal.setText(String.format(
                Locale.getDefault(),
                "Total: ₱%.2f",
                order.getTotal()
        ));
        orderTotal.setTextSize(16);
        orderTotal.setPadding(0, 8, 0, 8);
        card.addView(orderTotal);

        TextView details = new TextView(requireContext());
        details.setTextSize(14);
        details.setVisibility(View.GONE);
        card.addView(details);

        Button detailsButton = new Button(requireContext());
        detailsButton.setText("View Order Details");

        detailsButton.setOnClickListener(view -> {
            if (details.getVisibility() == View.VISIBLE) {
                details.setVisibility(View.GONE);
                detailsButton.setText("View Order Details");
                return;
            }

            ArrayList<CartItem> items =
                    databaseHelper.getOrderItems(order.getId());

            if (items.isEmpty()) {
                details.setText("No item details found for this order.");
            } else {
                StringBuilder text = new StringBuilder();

                for (CartItem item : items) {
                    text.append(item.getName())
                            .append("\nQuantity: ")
                            .append(item.getQuantity())
                            .append("\nPrice: ")
                            .append(String.format(
                                    Locale.getDefault(),
                                    "₱%.2f",
                                    item.getPrice()
                            ))
                            .append("\nSubtotal: ")
                            .append(String.format(
                                    Locale.getDefault(),
                                    "₱%.2f",
                                    item.getSubtotal()
                            ))
                            .append("\n\n");
                }

                details.setText(text.toString().trim());
            }

            details.setVisibility(View.VISIBLE);
            detailsButton.setText("Hide Order Details");
        });

        card.addView(detailsButton);
        container.addView(card);
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