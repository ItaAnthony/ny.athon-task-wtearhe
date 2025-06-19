package com.example.findinglogs.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.findinglogs.R;
import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.view.recyclerview.adapter.WeatherListAdapter;
import com.example.findinglogs.viewmodel.MainViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat; // Importar SimpleDateFormat
import java.util.ArrayList;
import java.util.Date; // Importar Date
import java.util.List;
import java.util.Locale; // Importar Locale

public class MainActivity extends AppCompatActivity {

    private WeatherListAdapter adapter;
    private final List<Weather> weathers = new ArrayList<>();
    private FloatingActionButton fetchButton;
    private MainViewModel mainViewModel; // Declarar mainViewModel aqui para ser acessível no OnClickListener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class); // Inicializar mainViewModel
        RecyclerView recyclerView = findViewById(R.id.recycler_view_weather);
        fetchButton = findViewById(R.id.fetchButton);
        adapter = new WeatherListAdapter(this, weathers);
        recyclerView.setAdapter(adapter);

        mainViewModel.getWeatherList().observe(this,
                weathers -> adapter.updateWeathers(weathers));

        // Mudar a ação do botão
        fetchButton.setOnClickListener(v -> {
            // Chamar o método para atualizar os dados no ViewModel
            // Supondo que você tem um método refreshData() ou fetchWeatherUpdates() no seu MainViewModel
            mainViewModel.fetchWeatherUpdates(); // ✅ Chamar método de atualização do ViewModel

            // Obter a hora atual formatada
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String currentTime = sdf.format(new Date());

            // Exibir o Toast com a hora atualizada
            Toast.makeText(MainActivity.this, "Dados do Clima atualizado às " + currentTime,
                    Toast.LENGTH_SHORT).show();
        });
    }
}