package com.kamilmosz.projektinzynierski.Models;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kamilmosz.projektinzynierski.R;

public class ScannedElementsViewHolder extends RecyclerView.ViewHolder {

    public AppCompatTextView elementId, elementName;
    public AppCompatImageView elementImage, elementButtonImage;
    public AppCompatButton elementButton;
    public CardView elementCardView;

    public ScannedElementsViewHolder(@NonNull View itemView) {
        super(itemView);

        elementId = itemView.findViewById(R.id.element_id);
        elementImage = itemView.findViewById(R.id.element_image);
        elementName = itemView.findViewById(R.id.element_name);
        elementButton = itemView.findViewById(R.id.edit_button);
        elementButtonImage = itemView.findViewById(R.id.edit_button_image);
        elementCardView = itemView.findViewById(R.id.element_cardview);
    }
}
