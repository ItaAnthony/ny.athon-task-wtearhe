package com.example.findinglogs.model.repo;


import android.app.Application;

import com.example.findinglogs.model.repo.local.SharedPrefManager;
import com.example.findinglogs.model.repo.remote.WeatherManager;
import com.example.findinglogs.model.repo.remote.api.WeatherCallback;
import com.example.findinglogs.model.util.Logger;

import java.util.HashMap;

public class Repository {
    private static final String TAG = Repository.class.getSimpleName();

    private final WeatherManager weatherManager;
    private final SharedPrefManager sharedPrefManagerManager;

    public Repository(Application application) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "Repository()");
        weatherManager = new WeatherManager();
        sharedPrefManagerManager = SharedPrefManager.getInstance(application);
    }

    public void retrieveForecast(String latLon, WeatherCallback callback) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "retrieveForecast for:" + latLon);
        weatherManager.retrieveForecast(latLon, callback);
    }

    public void saveString(String key, String value) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "saveString()");
        sharedPrefManagerManager.writeString(key, value);
    }

    public String readString(String key) {
        if (Logger.ISLOGABLE) Logger.d(TAG, "readString()");
        return sharedPrefManagerManager.readString(key);
    }

    public HashMap<String, String> getLocalizations() {
        HashMap<String, String> localizations = new HashMap<>();
        localizations.put("1", "-8.05428,-34.8813");     // Recife
        localizations.put("2", "-9.39416,-40.5096");     // Petrolina
        localizations.put("3", "-8.284547,-35.969863");  // Caruaru, Pernambuco
        localizations.put("4", "-3.101944,-60.025"); // Manaus
        localizations.put("5", "-25.4284,-49.2733");     // Curitiba
        localizations.put("6", "-23.561414,-46.655881"); // Consolação



        return localizations;
    }
}