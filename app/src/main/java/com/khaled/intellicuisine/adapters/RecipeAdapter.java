package com.khaled.intellicuisine.adapters;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.models.Recipe;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.tvName.setText(recipe.getTitle());

        setPlaceholder(holder);

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            holder.imgRecipe.setTag(recipe.getImageUrl());
            new Thread(() -> {
                try {
                    java.net.URL url = new java.net.URL(recipe.getImageUrl());
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    holder.itemView.post(() -> {
                        if (recipe.getImageUrl().equals(holder.imgRecipe.getTag())) {
                            holder.imgRecipe.setImageBitmap(bmp);
                            holder.imgRecipe.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            holder.imgRecipe.setPadding(0, 0, 0, 0);
                            holder.imgRecipe.setImageTintList(null);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    private void setPlaceholder(ViewHolder holder) {
        holder.imgRecipe.setImageResource(R.drawable.ic_inventory);
        holder.imgRecipe.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int padding = (int) (16 * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        holder.imgRecipe.setPadding(padding, padding, padding, padding);
        holder.imgRecipe.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), R.color.primary_orange)));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView imgRecipe;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRecipeName);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
        }
    }
}
