package com.apts.shoppingapp.ui.gallery;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apts.shoppingapp.CartItem;
import com.apts.shoppingapp.DatabaseHelper;
import com.apts.shoppingapp.R;
import com.apts.shoppingapp.SessionManager;
import com.apts.shoppingapp.databinding.FragmentGalleryBinding;
import com.apts.shoppingapp.databinding.ItemCartBinding;

import java.util.ArrayList;
import java.util.Locale;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

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
        sessionManager = new SessionManager(requireContext());

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

        String username = sessionManager.getUsername();
        ArrayList<CartItem> items = databaseHelper.getCartItems(username);

        if (items.isEmpty()) {
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("Your cart is empty.");
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
        }

        for (CartItem item : items) {
            addCartItemView(container, item);
        }

        binding.txtCartTotal.setText(
                String.format(
                        Locale.getDefault(),
                        "Total: ₱%.2f",
                        databaseHelper.getCartTotal(username)
                )
        );

        binding.btnCheckout.setEnabled(!items.isEmpty());
    }

    private void addCartItemView(
            LinearLayout container,
            CartItem item) {

        ItemCartBinding itemBinding =
                ItemCartBinding.inflate(
                        getLayoutInflater(),
                        container,
                        false
                );

        itemBinding.txtCartProductName.setText(item.getName());
        itemBinding.txtCartProductPrice.setText(String.format(
                Locale.getDefault(),
                "Price: ₱%.2f each",
                item.getPrice()
        ));
        itemBinding.txtCartProductSubtotal.setText(String.format(
                Locale.getDefault(),
                "Subtotal: ₱%.2f",
                item.getSubtotal()
        ));
        itemBinding.txtCartQuantity.setText(String.valueOf(item.getQuantity()));

        // Load product image
        int imageId = getResources().getIdentifier(
                item.getImageName(),
                "drawable",
                requireContext().getPackageName()
        );

        if (imageId != 0) {
            itemBinding.imgCartProduct.setImageResource(imageId);
            itemBinding.imgCartProduct.setVisibility(View.VISIBLE);
        } else {
            itemBinding.imgCartProduct.setVisibility(View.GONE);
        }

        itemBinding.btnDecreaseQty.setOnClickListener(view -> {
            databaseHelper.updateCartQuantity(
                    sessionManager.getUsername(),
                    item.getProductId(),
                    item.getQuantity() - 1
            );
            displayCart();
        });

        itemBinding.btnIncreaseQty.setOnClickListener(view -> {
            databaseHelper.updateCartQuantity(
                    sessionManager.getUsername(),
                    item.getProductId(),
                    item.getQuantity() + 1
            );
            displayCart();
        });

        itemBinding.btnRemoveCartItem.setOnClickListener(view -> {
            databaseHelper.removeFromCart(
                    sessionManager.getUsername(),
                    item.getProductId()
            );
            Toast.makeText(
                    requireContext(),
                    item.getName() + " removed from cart.",
                    Toast.LENGTH_SHORT
            ).show();
            displayCart();
        });

        container.addView(itemBinding.getRoot());
    }

    private void checkout() {
        String username = sessionManager.getUsername();
        if (databaseHelper.getCartItems(username).isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Your cart is empty. Please add an item first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        double total = databaseHelper.getCartTotal(username);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
                .setTitle("Confirm Checkout")
                .setMessage(String.format(
                        Locale.getDefault(),
                        "Are you sure you want to place this order?\n\nTotal Amount: ₱%.2f",
                        total
                ))
                .setPositiveButton("Place Order", (dialog, which) -> performCheckout(username))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performCheckout(String username) {
        long orderId = databaseHelper.checkout(username);

        if (orderId > 0) {
            Toast.makeText(
                    requireContext(),
                    "Successfully placed an order",
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