package com.example.tvandmovies.UI.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // FONTOS ÚJ IMPORT!
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.activities.MovieDetailActivity;
import com.example.tvandmovies.databinding.FragmentHomeBinding;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.model.MediaItem;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements ContentAdapter.ContentClickListener{
    private FragmentHomeBinding binding;
    private HomeViewModel viewModel; // Controller helyett ViewModel
    private ContentAdapter popContentAdapter;
    private ContentAdapter newContentAdapter;

    // Állapotmentéshez a kiválasztott gomb ID-ja
    private int currentSelectedId = R.id.btnMovies;
    private static final String KEY_SELECTED_ID = "selected_id_key";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstance){
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Adapterek és RecyclerView inicializálása
        popContentAdapter = new ContentAdapter(this, false);
        newContentAdapter = new ContentAdapter(this, false);

        initRecyclerView(binding.recyclerViewPopularMovie, popContentAdapter);
        initRecyclerView(binding.recyclerViewNewMovie, newContentAdapter);

        // 2. ViewModel PÉLDÁNYOSÍTÁSA (Controller helyett)
        // Ez a sor köti össze a Fragmentet a memóriában élő ViewModellel.
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // 3. FELIRATKOZÁS AZ ADATOKRA (OBSERVE)
        subscribeToViewModel();

        // 4. ÁLLAPOT VISSZATÖLTÉS (Ha elforgattad a telefont)
        if (savedInstanceState != null) {
            currentSelectedId = savedInstanceState.getInt(KEY_SELECTED_ID, R.id.btnMovies);
        }

        // 5. GOMB FIGYELŐ (LISTENER)
        binding.toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                currentSelectedId = checkedId; // Elmentjük, mi van kiválasztva

                if (checkedId == R.id.btnMovies) {
                    setupUIMovies(); // Szövegek átírása
                    viewModel.loadMovies(); // Adat kérése
                } else if (checkedId == R.id.btnSeries) {
                    setupUISeries(); // Szövegek átírása
                    viewModel.loadSeries(); // Adat kérése
                }
            }
        });

        // 6. INDÍTÁS
        // Csak akkor töltünk adatot, ha a ViewModel még üres (pl. első indítás).
        // Ha forgatás történt, a ViewModelben már ott az adat, az Observerek (3-as pont)
        // automatikusan kirajzolják, így nem kell újratölteni!
        if (viewModel.getPopularContent().getValue() == null) {
            // Beállítjuk a gombot, ami elsüti a Listenert -> ami betölti az adatot
            binding.toggleGroup.check(currentSelectedId);
        } else {
            // Ha már van adat, csak a gombot állítjuk be vizuálisan (hogy jó helyen álljon),
            // de nem indítunk új hálózati kérést.
            // Trükk: Ideiglenesen levesszük a listenert, beállítjuk a gombot, majd visszarakjuk,
            // de egyszerűbb, ha hagyjuk lefutni, a Retrofit/ViewModel gyorsítótárazhat.
            // A legbiztosabb most a sima check:
            binding.toggleGroup.check(currentSelectedId);
        }
    }

    // ContentClcikListener által észlelt események kezelése
    // a kártyákra kattintás esetén indítja az activity-t
    @Override
    public void onItemClick(MediaItem item) {
        // Részletek megnyitása
        Intent intent = new Intent(requireContext(), MovieDetailActivity.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(MediaItem item){
        // amikor a kártyán lévő mentésre kattintunk -> TODO: viewModel.saveItem (majd)
        Toast.makeText(requireContext(), "Mentve" + item.getTitle(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Itt figyeljük a ViewModel változásait.
     * Amint adat érkezik a háttérből, ezek a blokkok lefutnak.
     */
    private void subscribeToViewModel() {
        // Népszerű lista figyelése
        viewModel.getPopularContent().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                popContentAdapter.setContentList(movies);
            }
        });

        // Új lista figyelése
        viewModel.getNewContent().observe(getViewLifecycleOwner(), movies -> {
            if (movies != null) {
                newContentAdapter.setContentList(movies);
            }
        });

        // Töltés állapot figyelése
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            setLoading(isLoading);
        });

        // Hibaüzenetek figyelése
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                setLoading(false); // Hiba esetén levesszük a homokórát
            }
        });
    }


    // --- UI SEGÉDFÜGGVÉNYEK ---

    private void setupUIMovies() {
        binding.popTitle.setText("Népszerű");
        binding.popularText.setText("filmek");
        binding.newTitle.setText("Új");
        binding.newText.setText("filmek");
        // Opcionális: listák törlése, hogy látszódjon a váltás
        popContentAdapter.setContentList(new ArrayList<>());
        newContentAdapter.setContentList(new ArrayList<>());
    }

    private void setupUISeries() {
        binding.popTitle.setText("Népszerű");
        binding.popularText.setText("sorozatok");
        binding.newTitle.setText("Új");
        binding.newText.setText("epizódok a mai napon");
        popContentAdapter.setContentList(new ArrayList<>());
        newContentAdapter.setContentList(new ArrayList<>());
    }

    private void setLoading(boolean isLoading) {
        int visibility = isLoading ? View.VISIBLE : View.GONE;
        binding.popProgressBar.setVisibility(visibility);
        binding.newProgressBar.setVisibility(visibility);
    }

    private void initRecyclerView(RecyclerView recyclerView, ContentAdapter adapter){
        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_ID, currentSelectedId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}