package com.example.tvandmovies.utilities;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import org.chromium.base.Log;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Olyan LiveData, ami csak egyetlen egyszer küldi el a frissítést az Observernek.
 * navigációs eseményekhez vagy hibaüzenetek (Toast/Snackbar) megjelenítéséhez hasznos
 */
public class SingleLiveEvent<T> extends MutableLiveData<T> {
    private static final String TAG = "SingleLiveEvent";
    private final AtomicBoolean mPending = new AtomicBoolean(false);

    @MainThread
    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {
        if (hasActiveObservers()) {
            Log.w(TAG, "Több observer is feliratkozott, de csak egy fogja megkapni az értesítést.");
        }

        // Feliratkozás az eredeti LiveData-ra
        super.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(@Nullable T t) {
                // Csak akkor jelez az observernek, ha az mPending true volt (és egyből false-ra is állítja)
                if (mPending.compareAndSet(true, false)) {
                    observer.onChanged(t);
                }
            }
        });
    }

    @MainThread
    @Override
    public void setValue(@Nullable T t) {
        mPending.set(true);
        super.setValue(t);
    }

    // ha háttérszálról lenne hívva
    @Override
    public void postValue(T value) {
        mPending.set(true);
        super.postValue(value);
    }

    /**
     * Akkor hasznos, ha nem akarunk adatot küldeni, csak magát a triggert (pl. Void).
     */
    @MainThread
    public void call() {
        setValue(null);
    }
}