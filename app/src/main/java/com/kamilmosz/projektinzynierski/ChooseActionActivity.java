package com.kamilmosz.projektinzynierski;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.google.firebase.auth.FirebaseAuth;

public class ChooseActionActivity extends AppCompatActivity implements View.OnClickListener {

    AppCompatTextView newSurgeryTextView, browseSurgeriesTextView, editInstrumentsTextview;
    AppCompatButton logoutButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_action);

        init();
    }

    public void init() {
        newSurgeryTextView = (AppCompatTextView) findViewById(R.id.newSurgeryLabel);
        browseSurgeriesTextView = (AppCompatTextView) findViewById(R.id.browseSurgeriesLabel);
        editInstrumentsTextview = (AppCompatTextView) findViewById(R.id.editInstrumentsLabel);
        logoutButton = (AppCompatButton) findViewById(R.id.logoutButton);

        newSurgeryTextView.setOnClickListener(this);
        browseSurgeriesTextView.setOnClickListener(this);
        editInstrumentsTextview.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
    }

    public void logOut() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_action_logging_out))
                .setMessage(getString(R.string.choose_action_are_u_sure))
                .setPositiveButton(getString(R.string.global_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ChooseActionActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.global_no), null)
                .show();
    }

    public void newSurgery() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setTitle("Insert surgery name");
        builder.setView(input);


        builder.setPositiveButton(getString(R.string.choose_action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String surgeryNameText = input.getText().toString();

                Intent intent = new Intent(ChooseActionActivity.this, FragmentsHandlerActivity.class);
                intent.putExtra("my_key", surgeryNameText);
                startActivity(intent);

            }
        });
        builder.setNegativeButton(getString(R.string.choose_action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        builder.show();
    }

    public void browseSurgeries() {
        Intent browseIntent = new Intent(ChooseActionActivity.this, FragmentsHandlerActivity.class);
        browseIntent.putExtra("my_key", "browse");
        startActivity(browseIntent);
    }

    public void editInstruments() {
        Intent editIntent = new Intent(ChooseActionActivity.this, FragmentsHandlerActivity.class);
        editIntent.putExtra("my_key", "edit");
        startActivity(editIntent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.newSurgeryLabel:
                newSurgery();
                break;
            case R.id.browseSurgeriesLabel:
                browseSurgeries();
                break;
            case R.id.editInstrumentsLabel:
                editInstruments();
                break;
            case R.id.logoutButton:
                logOut();
                break;
        }
    }
}



