package com.apts.shoppingapp.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apts.shoppingapp.CartItem;
import com.apts.shoppingapp.DatabaseHelper;
import com.apts.shoppingapp.databinding.FragmentGalleryBinding;

import java.util.ArrayList;
import java.util.Locale;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentGalleryBinding.inflate(
                inflater, container, false
        );

        databaseHelper = new DatabaseHelper(requireContext());

        binding.btnCheckout.setOnClickListener(view -> checkout());

        displayCart();

        return binding.getRoot();
    }

    private void displayCart() {
        if (binding == null) {
            return;
        }

        LinearLayout container = binding.cartItemsContainer;
        container.removeAllViews();

        ArrayList<CartItem> items = databaseHelper.getCartItems();

        if (items.isEmpty()) {
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("Your cart is empty.");
            emptyMessage.setTextSize(16);
            container.addView(emptyMessage);
        }

        for (CartItem item : items) {
            addCartItemView(container, item);
        }

        binding.txtCartTotal.setText(
                String.format(
                        Locale.getDefault(),
                        "Total: ₱%.2f",
                        databaseHelper.getCartTotal()
                )
        );

        binding.btnCheckout.setEnabled(!items.isEmpty());
    }

    private void addCartItemView(
            LinearLayout container,
            CartItem item) {

        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(12, 12, 12, 12);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rowParams.bottomMargin = 12;
        row.setLayoutParams(rowParams);
        row.setBackgroundResource(
                android.R.drawable.dialog_holo_light_frame
        );

        TextView name = new TextView(requireContext());
        name.setText(item.getName());
        name.setTextSize(18);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        row.addView(name);

        TextView price = new TextView(requireContext());
        price.setText(String.format(
                Locale.getDefault(),
                "Price: ₱%.2f each",
                item.getPrice()
        ));
        row.addView(price);

        TextView subtotal = new TextView(requireContext());
        subtotal.setText(String.format(
                Locale.getDefault(),
                "Subtotal: ₱%.2f",
                item.getSubtotal()
        ));
        row.addView(subtotal);

        LinearLayout controls = new LinearLayout(requireContext());
        controls.setOrientation(LinearLayout.HORIZONTAL);

        Button decrease = new Button(requireContext());
        decrease.setText("−");

        TextView quantity = new TextView(requireContext());
        quantity.setText(String.valueOf(item.getQuantity()));
        quantity.setTextSize(18);
        quantity.setGravity(android.view.Gravity.CENTER);
        quantity.setPadding(12, 0, 12, 0);

        Button increase = new Button(requireContext());
        increase.setText("+");

        Button remove = new Button(requireContext());
        remove.setText("Remove");

        decrease.setOnClickListener(view -> {
            databaseHelper.updateCartQuantity(
                    item.getProductId(),
                    item.getQuantity() - 1
            );
            displayCart();
        });

        increase.setOnClickListener(view -> {
            databaseHelper.updateCartQuantity(
                    item.getProductId(),
                    item.getQuantity() + 1
            );
            displayCart();
        });

        remove.setOnClickListener(view -> {
            databaseHelper.removeFromCart(item.getProductId());
            displayCart();
        });

        controls.addView(decrease);
        controls.addView(quantity);
        controls.addView(increase);
        controls.addView(remove);

        row.addView(controls);
        container.addView(row);
    }

    private void checkout() {
        if (databaseHelper.getCartItems().isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Your cart is empty.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        long orderId = databaseHelper.checkout();

        if (orderId > 0) {
            Toast.makeText(
                    requireContext(),
                    "Order placed successfully! Order #" + orderId,
                    Toast.LENGTH_LONG
            ).show();

            displayCart();
        } else {
            Toast.makeText(
                    requireContext(),
                    "Checkout failed. Please try again.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null && databaseHelper != null) {
            displayCart();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}