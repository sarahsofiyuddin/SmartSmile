package com.example.smartsmile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DiseaseSliderAdapter extends RecyclerView.Adapter<DiseaseSliderAdapter.ViewHolder> {

    private List<DentalDisease> diseaseList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(DentalDisease disease);
    }

    public DiseaseSliderAdapter(List<DentalDisease> diseaseList, OnItemClickListener onItemClickListener) {
        this.diseaseList = diseaseList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disease_slide, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DentalDisease disease = diseaseList.get(position);
        holder.imageView.setImageResource(disease.getImageResId());

        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(disease));
    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewDisease);
        }
    }
}
