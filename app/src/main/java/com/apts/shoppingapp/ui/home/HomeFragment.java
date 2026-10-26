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
import com.apts.shoppingapp.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private DatabaseHelper databaseHelper;

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

        displayProducts();

        return binding.getRoot();
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

        for (Product product : products) {
            addProductCard(productsContainer, product);
        }
    }

    private void addProductCard(
            LinearLayout container,
            Product product) {

        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(16, 16, 16, 16);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = 20;
        card.setLayoutParams(cardParams);
        card.setBackgroundResource(
                android.R.drawable.dialog_holo_light_frame
        );

        // Load the image supplied by the project resources.
        int imageId = getResources().getIdentifier(
                product.getImageName(),
                "drawable",
                requireContext().getPackageName()
        );

        if (imageId != 0) {
            ImageView productImage = new ImageView(requireContext());
            productImage.setImageResource(imageId);
            productImage.setContentDescription(product.getName());
            productImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            productImage.setAdjustViewBounds(true);

            LinearLayout.LayoutParams imageParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            180
                    );

            imageParams.bottomMargin = 8;
            card.addView(productImage, imageParams);
        }

        TextView name = new TextView(requireContext());
        name.setText(product.getName());
        name.setTextSize(18);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(name);

        TextView description = new TextView(requireContext());
        description.setText(product.getDescription());
        description.setTextSize(14);
        card.addView(description);

        TextView price = new TextView(requireContext());
        price.setText(String.format(
                Locale.getDefault(),
                "₱%.2f",
                product.getPrice()
        ));
        price.setTextSize(16);
        price.setPadding(0, 8, 0, 8);
        card.addView(price);

        Button addButton = new Button(requireContext());
        addButton.setText("Add to Cart");

        addButton.setOnClickListener(view -> {
            boolean added = databaseHelper.addToCart(
                    product.getId(), 1
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

        card.addView(addButton);
        container.addView(card);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null && databaseHelper != null) {
            displayProducts();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}