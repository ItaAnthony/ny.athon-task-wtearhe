package com.example.findinglogs.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.findinglogs.model.model.Weather;
import com.example.findinglogs.model.repo.Repository;
import com.example.findinglogs.model.repo.remote.api.WeatherCallback;
import com.example.findinglogs.model.util.Logger; // Certifique-se que esta classe Logger está definida e funcional.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private static final int FETCH_INTERVAL = 120_000; // 2 minutos
    private final Repository mRepository;
    private final MutableLiveData<List<Weather>> _weatherList = new MutableLiveData<>(new ArrayList<>());
    private final LiveData<List<Weather>> weatherList = _weatherList;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable fetchRunnable = this::fetchAllForecasts;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new Repository(application);
        startFetching(); // Inicia a busca inicial e o agendamento periódico
    }

    public LiveData<List<Weather>> getWeatherList() {
        return weatherList;
    }

    private void startFetching() {
        // Garante que o fetchRunnable não seja postado múltiplas vezes se o startFetching for chamado novamente
        handler.removeCallbacks(fetchRunnable);
        fetchAllForecasts(); // Faz a busca imediatamente
        handler.postDelayed(fetchRunnable, FETCH_INTERVAL); // Agenda a próxima busca
    }

    private void fetchAllForecasts() {
        // Log para depuração
        if (Logger.ISLOGABLE) Logger.d(TAG, "fetchAllForecasts() - Iniciando busca de previsões.");

        // Para evitar problemas com a atualização assíncrona, crie uma cópia ou gerencie o estado.
        // O trecho original adiciona à updatedList e só atualiza se o tamanho for igual.
        // Isso pode falhar se alguma requisição demorar muito ou falhar.

        HashMap<String, String> localizations = mRepository.getLocalizations();
        // Se não houver localizações, evite o loop e o callback:
        if (localizations.isEmpty()) {
            if (Logger.ISLOGABLE) Logger.d(TAG, "Nenhuma localização encontrada para buscar.");
            _weatherList.setValue(new ArrayList<>()); // Limpa a lista se não houver locais
            // Re-agenda a busca mesmo que não haja localizações, para tentar de novo mais tarde
            handler.postDelayed(fetchRunnable, FETCH_INTERVAL);
            return;
        }

        // Variáveis para rastrear o progresso e garantir que o setValue seja chamado apenas uma vez
        final int[] completedRequests = {0}; // Usado para contar callbacks concluídos
        final List<Weather> resultsFromBatch = new ArrayList<>(); // Armazena resultados para esta leva

        for (String latlon : localizations.values()) {
            mRepository.retrieveForecast(latlon, new WeatherCallback() {
                @Override
                public void onSuccess(Weather result) {
                    synchronized (this) { // Sincroniza o acesso a shared variables em um contexto multi-thread
                        resultsFromBatch.add(result);
                        completedRequests[0]++;
                        if (Logger.ISLOGABLE) Logger.d(TAG, "Sucesso para " + result.getName() + ". Concluído: " + completedRequests[0] + "/" + localizations.size());
                        if (completedRequests[0] == localizations.size()) {
                            // Todas as requisições concluídas (sucesso ou falha) para esta leva
                            _weatherList.postValue(resultsFromBatch); // Usa postValue para threads secundárias
                            if (Logger.ISLOGABLE) Logger.d(TAG, "Todas as previsões atualizadas. Agendando próxima busca.");
                            handler.postDelayed(fetchRunnable, FETCH_INTERVAL); // Re-agenda a próxima busca
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    synchronized (this) { // Sincroniza o acesso
                        completedRequests[0]++;
                        if (Logger.ISLOGABLE) Logger.e(TAG, "Falha na busca para latlon: " + latlon + " Erro: " + error + ". Concluído: " + completedRequests[0] + "/" + localizations.size());
                        if (completedRequests[0] == localizations.size()) {
                            // Todas as requisições concluídas (sucesso ou falha) para esta leva
                            // Não atualizamos _weatherList em caso de falha TOTAL, mas reagendamos
                            if (Logger.ISLOGABLE) Logger.d(TAG, "Busca de previsões finalizada com falhas. Agendando próxima busca.");
                            handler.postDelayed(fetchRunnable, FETCH_INTERVAL); // Re-agenda a próxima busca
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacks(fetchRunnable); // Remove qualquer agendamento pendente ao ViewModel ser limpo
        super.onCleared();
    }

    // Este método permite que a MainActivity dispare uma atualização manual
    public void fetchWeatherUpdates() {
        if (Logger.ISLOGABLE) Logger.d(TAG, "fetchWeatherUpdates() - Disparado manualmente.");
        handler.removeCallbacks(fetchRunnable); // Remove o agendamento atual
        fetchAllForecasts(); // Inicia uma busca imediata
        // O reagendamento para a próxima busca periódica é feito dentro de fetchAllForecasts()
    }

    // Este método (retrieveForecast) não estava sendo usado na lógica de atualização periódica,
    // mas está mantido caso seja usado para uma busca de localização específica.
    public void retrieveForecast(String latLon, WeatherCallback callback) {
        mRepository.retrieveForecast(latLon, callback);
    }
}