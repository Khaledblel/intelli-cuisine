package com.khaled.intellicuisine.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.khaled.intellicuisine.R;
import com.khaled.intellicuisine.models.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.ViewHolder> {

    private List<Ingredient> ingredientList = new ArrayList<>();
    private OnIngredientActionListener actionListener;

    public interface OnIngredientActionListener {
        void onDelete(Ingredient ingredient);
        void onEdit(Ingredient ingredient);
    }

    public void setOnIngredientActionListener(OnIngredientActionListener listener) {
        this.actionListener = listener;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredientList = ingredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);

        String name = ingredient.getName();
        if (name != null && !name.isEmpty()) {
            if (name.length() > 1) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            } else {
                name = name.toUpperCase();
            }
        }

        holder.tvName.setText(name);
        holder.tvQuantity.setText("Qté : " + ingredient.getQuantity());

        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEdit(ingredient);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDelete(ingredient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}