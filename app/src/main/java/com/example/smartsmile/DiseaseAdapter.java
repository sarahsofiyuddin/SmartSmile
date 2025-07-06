package com.example.smartsmile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {

    private List<DentalDisease> diseaseList;
    private Fragment parentFragment;

    public DiseaseAdapter(List<DentalDisease> diseaseList, Fragment parentFragment) {
        this.diseaseList = diseaseList;
        this.parentFragment = parentFragment;
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disease, parent, false);
        return new DiseaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        DentalDisease disease = diseaseList.get(position);
        holder.textViewDisease.setText(disease.getName());

        // Set up ViewPager2 with ImageSliderAdapter
        ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(disease.getImageList());
        holder.viewPager.setAdapter(sliderAdapter);

        // Auto-slide images every 2 seconds
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                holder.viewPager.post(() -> {
                    int nextItem = (holder.viewPager.getCurrentItem() + 1) % disease.getImageList().size();
                    holder.viewPager.setCurrentItem(nextItem, true);
                });
            }
        }, 2000, 2000);

        // Click event to navigate to the respective fragment
        holder.itemView.setOnClickListener(v -> {
            Fragment targetFragment;
            switch (disease.getName()) {
                case "Dental Caries":
                    targetFragment = new DentalCariesFragment();
                    break;
                case "Dental Calculus":
                    targetFragment = new DentalCalculusFragment();
                    break;
                case "Gingivitis":
                    targetFragment = new GingivitisFragment();
                    break;
                case "Hypodontia":
                    targetFragment = new HypodontiaFragment();
                    break;
                default:
                    targetFragment = new DentalCariesFragment();
            }

            FragmentTransaction transaction = parentFragment.getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout_info, targetFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    static class DiseaseViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDisease;
        ViewPager2 viewPager;

        public DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDisease = itemView.findViewById(R.id.textViewDiseaseTitle);
            viewPager = itemView.findViewById(R.id.viewPager);
        }
    }
}
