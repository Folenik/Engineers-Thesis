package com.kamilmosz.projektinzynierski.Models;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.kamilmosz.projektinzynierski.R;

public class SurgeriesViewHolder extends RecyclerView.ViewHolder {

    public AppCompatTextView surgeryName, surgeryDate, surgeryInstrument, surgeryInstrumentCheck, surgeryInstrumentNotChecked;

    public SurgeriesViewHolder(@NonNull View itemView) {
        super(itemView);

        surgeryName = itemView.findViewById(R.id.surgery_name);
        surgeryDate = itemView.findViewById(R.id.surgery_date);
        surgeryInstrument = itemView.findViewById(R.id.surgery_instruments);
        surgeryInstrumentCheck = itemView.findViewById(R.id.surgery_instruments_check);
        surgeryInstrumentNotChecked = itemView.findViewById(R.id.surgery_instruments_not_checked);
    }
}
