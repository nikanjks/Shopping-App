package com.apts.shoppingapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.apts.shoppingapp.CartItem;
import com.apts.shoppingapp.DatabaseHelper;
import com.apts.shoppingapp.Product;
import com.apts.shoppingapp.R;
import com.apts.shoppingapp.SessionManager;
import com.apts.shoppingapp.databinding.FragmentHomeBinding;
import com.apts.shoppingapp.databinding.ItemProductBinding;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(
                inflater, container, false
        );

        databaseHelper = new DatabaseHelper(requireContext());
        sessionManager = new SessionManager(requireContext());

        binding.btnViewCart.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_gallery)
        );

        binding.btnOrderHistory.setOnClickListener(v ->
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_slideshow)
        );

        updateTitle();
        displayProducts();

        return binding.getRoot();
    }

    private void updateTitle() {
        if (binding == null || sessionManager == null) {
            return;
        }

        String fullName = sessionManager.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            binding.txtDashboardTitle.setText("Hi, " + fullName.trim() + "!");
        } else {
            binding.txtDashboardTitle.setText("Hi, Customer!");
        }
    }

    private void displayProducts() {
        if (binding == null) {
            return;
        }

        LinearLayout productsContainer = binding.productsContainer;
        productsContainer.removeAllViews();

        ArrayList<Product> products = databaseHelper.getAllProducts();

        if (products.isEmpty()) {
            TextView emptyMessage = new TextView(requireContext());
            emptyMessage.setText("No products are available yet.");
            emptyMessage.setTextSize(16);
            productsContainer.addView(emptyMessage);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        int marginPx = (int) (6 * density);

        LinearLayout currentRow = null;
        for (int i = 0; i < products.size(); i++) {
            boolean isLeft = (i % 2 == 0);

            if (isLeft) {
                currentRow = new LinearLayout(requireContext());
                currentRow.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                currentRow.setLayoutParams(rowParams);
                productsContainer.addView(currentRow);
            }

            View card = createProductCard(products.get(i), isLeft, currentRow);
            currentRow.addView(card);
        }

        if (products.size() % 2 != 0 && currentRow != null) {
            View placeholder = new View(requireContext());
            LinearLayout.LayoutParams placeholderParams = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            placeholderParams.leftMargin = marginPx;
            placeholderParams.bottomMargin = (int) (12 * density);
            placeholder.setLayoutParams(placeholderParams);
            placeholder.setVisibility(View.INVISIBLE);
            currentRow.addView(placeholder);
        }
    }

    private View createProductCard(Product product, boolean isLeft, ViewGroup parent) {
        ItemProductBinding itemBinding = ItemProductBinding.inflate(
                getLayoutInflater(),
                parent,
                false
        );

        float density = getResources().getDisplayMetrics().density;
        int marginPx = (int) (6 * density);

        LinearLayout.LayoutParams cardParams = (LinearLayout.LayoutParams) itemBinding.getRoot().getLayoutParams();
        if (cardParams == null) {
            cardParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        } else {
            cardParams.width = 0;
            cardParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            cardParams.weight = 1.0f;
        }
        cardParams.bottomMargin = (int) (12 * density);
        if (isLeft) {
            cardParams.rightMargin = marginPx;
        } else {
            cardParams.leftMargin = marginPx;
        }
        itemBinding.getRoot().setLayoutParams(cardParams);

        // Load image
        int imageId = getResources().getIdentifier(
                product.getImageName(),
                "drawable",
                requireContext().getPackageName()
        );

        if (imageId != 0) {
            itemBinding.imgProduct.setImageResource(imageId);
            itemBinding.imgProduct.setContentDescription(product.getName());
            itemBinding.imgProduct.setVisibility(View.VISIBLE);
        } else {
            itemBinding.imgProduct.setVisibility(View.GONE);
        }

        itemBinding.txtProductName.setText(product.getName());
        itemBinding.txtProductDescription.setText(product.getDescription());
        itemBinding.txtProductPrice.setText(String.format(
                Locale.getDefault(),
                "₱%.2f",
                product.getPrice()
        ));

        itemBinding.btnAddToCart.setOnClickListener(view -> {
            boolean added = databaseHelper.addToCart(
                    sessionManager.getUsername(),
                    product.getId(),
                    1
            );

            if (added) {
                Toast.makeText(
                        requireContext(),
                        product.getName() + " added to cart.",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        requireContext(),
                        "Could not add product to cart.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        return itemBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null && databaseHelper != null) {
            updateTitle();
            displayProducts();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}