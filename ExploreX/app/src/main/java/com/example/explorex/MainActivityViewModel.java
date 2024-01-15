package com.example.explorex;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private MutableLiveData<Boolean> isNightModeEnabled = new MutableLiveData<>();
    private int currentColor;

    public MainActivityViewModel() {
        isNightModeEnabled.setValue(false);
    }

    public LiveData<Boolean> isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled.setValue(isNightModeEnabled);
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(int color) {
        this.currentColor = color;
    }
}
