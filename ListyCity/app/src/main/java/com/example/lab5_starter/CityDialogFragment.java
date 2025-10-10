package com.example.lab5_starter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class CityDialogFragment extends DialogFragment {

    interface CityDialogListener {
        void updateCity(City city, String title, String province);
        void addCity(City city);
        void deleteCity(City city);
    }

    private CityDialogListener listener;

    public static CityDialogFragment newInstance(City city){
        Bundle args = new Bundle();
        args.putSerializable("City", city);
        CityDialogFragment fragment = new CityDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof CityDialogListener){
            listener = (CityDialogListener) context;
        } else {
            throw new RuntimeException("Host activity must implement CityDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.fragment_city_details, null);
        EditText editCityName = view.findViewById(R.id.edit_city_name);
        EditText editProvince = view.findViewById(R.id.edit_province);

        String tag = getTag();
        Bundle bundle = getArguments();
        City city;

        boolean isEdit = Objects.equals(tag, "City Details") && bundle != null && bundle.getSerializable("City") != null;
        if (isEdit) {
            city = (City) bundle.getSerializable("City");
            editCityName.setText(city.getName());
            editProvince.setText(city.getProvince());
        } else {
            city = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle(isEdit ? "Edit City" : "Add City")
                .setNegativeButton("Cancel", null);

        if (isEdit) {
            builder.setPositiveButton("Save", (dialog, which) -> {
                String title = editCityName.getText().toString().trim();
                String province = editProvince.getText().toString().trim();
                if (!title.isEmpty() && !province.isEmpty()) {
                    listener.updateCity(city, title, province);
                }
            });
            builder.setNeutralButton("Delete", (dialog, which) -> {
                listener.deleteCity(city);
            });
        } else {
            builder.setPositiveButton("Add", (dialog, which) -> {
                String title = editCityName.getText().toString().trim();
                String province = editProvince.getText().toString().trim();
                if (!title.isEmpty() && !province.isEmpty()) {
                    listener.addCity(new City(title, province));
                }
            });
        }

        return builder.create();
    }
}
