package com.lionico.mbaremusika;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommodityAdapter adapter;
    private EditText searchBox;
    private ProgressBar progressBar;
    private TextView emptyView;

    private List<Commodity> commodityList = new ArrayList<>();
    private List<Commodity> filteredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        searchBox = findViewById(R.id.search_box);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommodityAdapter(filteredList, commodity -> {
            // Handle click event here
            // Example: open detail screen
        });
        recyclerView.setAdapter(adapter);

        loadData();
        setupSearch();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        // TODO: Replace with actual data fetching
        commodityList.clear();
        commodityList.add(new Commodity("Tomatoes", "10kg", "$5"));
        commodityList.add(new Commodity("Onions", "20kg", "$8"));
        commodityList.add(new Commodity("Cabbages", "per head", "$1"));

        filteredList.clear();
        filteredList.addAll(commodityList);
        adapter.notifyDataSetChanged();

        progressBar.setVisibility(View.GONE);
        toggleEmptyView();
    }

    private void setupSearch() {
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filterList(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(commodityList);
        } else {
            for (Commodity item : commodityList) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
        toggleEmptyView();
    }

    private void toggleEmptyView() {
        if (filteredList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}
