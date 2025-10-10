package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private ListenerRegistration citiesReg;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // Adapter
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        // Firestore
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities"); // fixed collection name

        // Realtime listener: populate and refresh list
        citiesReg = citiesRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", "listen error", error);
                return;
            }
            cityArrayList.clear();
            if (value != null) {
                for (QueryDocumentSnapshot snapshot : value) {
                    City c = snapshot.toObject(City.class);
                    if (c != null) {
                        // Ensure City has setDocId(String) and a no-arg constructor
                        c.setDocId(snapshot.getId());
                        cityArrayList.add(c);
                    }
                }
            }
            cityArrayAdapter.notifyDataSetChanged();
        });

        // Add flow: open dialog in "Add" mode
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment dialog = new CityDialogFragment();
            dialog.show(getSupportFragmentManager(), "Add City");
        });

        // Edit/Delete flow: open dialog in "Edit" mode
        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment dialog = CityDialogFragment.newInstance(city);
            dialog.show(getSupportFragmentManager(), "City Details");
        });
    }

    @Override
    protected void onDestroy() {
        if (citiesReg != null) {
            citiesReg.remove();
            citiesReg = null;
        }
        super.onDestroy();
    }

    // Dialog callbacks

    @Override
    public void updateCity(City city, String title, String province) {
        city.setName(title);
        city.setProvince(province);
        if (city.getDocId() != null && !city.getDocId().isEmpty()) {
            citiesRef.document(city.getDocId())
                    .set(city, SetOptions.merge())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Fallback: treat as add if no docId
            addCity(city);
        }
        // Do not mutate local list; listener will refresh UI
    }

    @Override
    public void addCity(City city) {
        citiesRef.add(city)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Add failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        // Do not mutate local list; listener will refresh UI
    }

    // Add this method since the dialog now supports deletion
    public void deleteCity(City city) {
        if (city.getDocId() != null && !city.getDocId().isEmpty()) {
            citiesRef.document(city.getDocId()).delete()
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Fallback: delete by fields if no docId present
            citiesRef.whereEqualTo("name", city.getName())
                    .whereEqualTo("province", city.getProvince())
                    .get()
                    .addOnSuccessListener(q -> {
                        for (DocumentSnapshot ds : q.getDocuments()) {
                            ds.getReference().delete();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}

