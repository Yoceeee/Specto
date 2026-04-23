package com.example.tvandmovies.UI.home;

import android.content.Intent;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.activities.ActivityContentDetail;
import com.example.tvandmovies.UI.activities.SeeAllActivity;
import com.example.tvandmovies.UI.adapter.ContinueWatchingAdapter;
import com.example.tvandmovies.UI.saved.BookmarkViewModel;
import com.example.tvandmovies.databinding.FragmentHomeBinding;
import com.example.tvandmovies.UI.adapter.ContentAdapter;
import com.example.tvandmovies.model.entities.MediaItem;
import com.example.tvandmovies.utilities.SharedViewModel;

public class HomeFragment extends Fragment implements ContentAdapter.ContentClickListener {
    private FragmentHomeBinding binding;
    private SharedViewModel sharedViewModel;
    private HomeViewModel viewModel;
    private BookmarkViewModel viewModelSavedContent;

    private ContentAdapter popContentAdapter;
    private ContentAdapter newContentAdapter;
    private ContentAdapter allTimeBestAdapter;
    private ContinueWatchingAdapter continueWatchingAdapter;

    private int currentSelectedId = R.id.btnMovies;
    private static final String KEY_SELECTED_ID = "selected_id_key";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstance) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModelSavedContent = new ViewModelProvider(requireActivity()).get(BookmarkViewModel.class);

        if (savedInstanceState != null) {
            currentSelectedId = savedInstanceState.getInt(KEY_SELECTED_ID, R.id.btnMovies);
        }

        setupAdaptersAndRecyclers();
        observeGlobalData();

        view.post(() -> {
            if (!isAdded()) return;

            setupListeners();
            subscribeToViewModel();

            binding.radioGroupToggle.check(currentSelectedId);
            refreshUIForSelection(currentSelectedId);

            sharedViewModel.loadUsernameIfNeeded();
        });
    }

    private void setupAdaptersAndRecyclers() {
        popContentAdapter = new ContentAdapter(this, 0);
        newContentAdapter = new ContentAdapter(this, 0);
        allTimeBestAdapter = new ContentAdapter(this, 0);

        initRecyclerView(binding.recyclerViewPopularMovie, popContentAdapter);
        initRecyclerView(binding.recyclerViewNewMovie, newContentAdapter);
        initRecyclerView(binding.recyclerViewAllTimeBestMovie, allTimeBestAdapter);

        continueWatchingAdapter = new ContinueWatchingAdapter(item -> {
            Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
            intent.putExtra("object", item);
            startActivity(intent);
        });
        binding.rvHomeContinueWatching.setAdapter(continueWatchingAdapter);
    }

    private void observeGlobalData() {
        sharedViewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            binding.userName.setText(username != null ? username : "Felhasználó");
        });

        viewModel.getAllSaved().observe(getViewLifecycleOwner(), saved -> {
            if (saved != null) {
                popContentAdapter.setSavedItems(saved);
                newContentAdapter.setSavedItems(saved);
                allTimeBestAdapter.setSavedItems(saved);
            }
        });

        viewModelSavedContent.getContinueWatching().observe(getViewLifecycleOwner(), displayList -> {
            continueWatchingAdapter.setSeries(displayList != null ? displayList : null);
            updateContinueWatchingVisibility();
        });
    }

    private void setupListeners() {
        binding.radioGroupToggle.setOnCheckedChangeListener((group, checkedId) -> {
            TransitionManager.beginDelayedTransition((ViewGroup) binding.getRoot());
            refreshUIForSelection(checkedId);
        });

        binding.swipeRefreshLayout.setOnRefreshListener(() -> viewModel.refreshData());

        binding.btnSeeAllPopular.setOnClickListener(v -> {
            boolean isMovie = (currentSelectedId == R.id.btnMovies);
            openSeeAllActivity(isMovie ? "POPULAR_MOVIES" : "POPULAR_SERIES", isMovie ? "Népszerű filmek" : "Népszerű sorozatok");
        });

        binding.btnSeeAllNew.setOnClickListener(v -> {
            boolean isMovie = (currentSelectedId == R.id.btnMovies);
            openSeeAllActivity(isMovie ? "NEW_MOVIES" : "NEW_SERIES", isMovie ? "Új filmek" : "Új sorozatok");
        });

        binding.btnSeeAllBest.setOnClickListener(v -> openSeeAllActivity("TOP_RATED_MOVIES", "Legjobb filmek"));
    }

    private void refreshUIForSelection(int checkedId) {
        currentSelectedId = checkedId;
        boolean isMovies = (checkedId == R.id.btnMovies);

        if (isMovies) {
            setupUIMovies();
            viewModel.setContentType("movies");
        } else {
            setupUISeries();
            viewModel.setContentType("series");
        }
        updateContinueWatchingVisibility();
    }

    private void updateContinueWatchingVisibility() {
        boolean shouldShow = (currentSelectedId == R.id.btnSeries) && (continueWatchingAdapter.getItemCount() > 0);
        int visibility = shouldShow ? View.VISIBLE : View.GONE;
        binding.tvHomeContinueWatchingTitle.setVisibility(visibility);
        binding.rvHomeContinueWatching.setVisibility(visibility);
    }

    private void subscribeToViewModel() {
        viewModel.getPopularContent().observe(getViewLifecycleOwner(), content -> {
            popContentAdapter.submitList(content);
        });

        viewModel.getHeroState().observe(getViewLifecycleOwner(), state -> {
            if (state != null) setupHeroHeader(state);
        });

        viewModel.getNewContent().observe(getViewLifecycleOwner(), content -> {
            newContentAdapter.submitList(content);
        });

        viewModel.getAllTimeBestContent().observe(getViewLifecycleOwner(), content -> {
            allTimeBestAdapter.submitList(content);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            setLoading(isLoading);
            if (!isLoading) binding.swipeRefreshLayout.setRefreshing(false);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null) Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupUIMovies() {
        binding.popTitle.setText("Népszerű");
        binding.popularText.setText("filmek");
        binding.newTitle.setText("Új");
        binding.newText.setText("filmek");

        binding.allTimeBestTitle.setVisibility(View.VISIBLE);
        binding.allTimeBestText.setVisibility(View.VISIBLE);
        binding.recyclerViewAllTimeBestMovie.setVisibility(View.VISIBLE);
        binding.btnSeeAllBest.setVisibility(View.VISIBLE);
        binding.allTimeBestTitle.setText("Minden idők legjobb");
        binding.allTimeBestText.setText("filmjei");
    }

    private void setupUISeries() {
        binding.popTitle.setText("Népszerű");
        binding.popularText.setText("sorozatok");
        binding.newTitle.setText("Új");
        binding.newText.setText("epizódok a mai napon");

        binding.allTimeBestTitle.setVisibility(View.GONE);
        binding.allTimeBestText.setVisibility(View.GONE);
        binding.btnSeeAllBest.setVisibility(View.GONE);
        binding.recyclerViewAllTimeBestMovie.setVisibility(View.GONE);
    }

    private void setLoading(boolean isLoading) {
        int visibility = isLoading ? View.VISIBLE : View.GONE;
        binding.popProgressBar.setVisibility(visibility);
        binding.newProgressBar.setVisibility(visibility);
        if (currentSelectedId == R.id.btnMovies) {
            binding.allTimeBestProgressBar.setVisibility(visibility);
        } else {
            binding.allTimeBestProgressBar.setVisibility(View.GONE);
        }
    }

    private void setupHeroHeader(HomeViewModel.HeroUiState state) {
        binding.heroContainer.setVisibility(View.VISIBLE);
        binding.heroTitle.setText(state.title);
        binding.heroTags.setText(state.genreText);

        Glide.with(this)
                .load(state.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .thumbnail(0.2f)
                .placeholder(R.drawable.gradient_transparent_to_color)
                .centerCrop()
                .into(binding.heroImage);

        binding.heroContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
            intent.putExtra("object", state.originalItem);
            startActivity(intent);
        });
    }

    private void openSeeAllActivity(String categoryType, String title) {
        Intent intent = new Intent(requireContext(), SeeAllActivity.class);
        intent.putExtra("CATEGORY_TYPE", categoryType);
        intent.putExtra("CATEGORY_TITLE", title);
        startActivity(intent);
    }

    private void initRecyclerView(RecyclerView recyclerView, ContentAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(MediaItem item) {
        Intent intent = new Intent(requireContext(), ActivityContentDetail.class);
        intent.putExtra("object", item);
        startActivity(intent);
    }

    @Override
    public void onBookmarkClick(MediaItem item, boolean isCurrentlySaved) {
        viewModel.toggleSavedStatus(item, isCurrentlySaved);
        Toast.makeText(requireContext(), isCurrentlySaved ? "Eltávolítva" : "Mentve", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onResume() {
        super.onResume();
        if (viewModelSavedContent != null) viewModelSavedContent.forceRefreshEpisodes();
    }
}