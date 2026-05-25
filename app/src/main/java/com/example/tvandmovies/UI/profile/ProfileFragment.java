package com.example.tvandmovies.UI.profile;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Objects;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.tvandmovies.R;
import com.example.tvandmovies.UI.activities.LoadingActivity;
import com.example.tvandmovies.databinding.FragmentProfileBinding;
import com.example.tvandmovies.utilities.SharedViewModel;
import com.example.tvandmovies.worker.SeriesNotificationWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private SharedPreferences sharedPreferences;
    private ProfileViewModel viewModel;
    private SharedViewModel sharedViewModel;
    private Uri selectedImageUri;
    private android.widget.ImageView dialogImagePreview;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (dialogImagePreview != null) {
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(dialogImagePreview);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(requireContext());
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        loadUserData();
        setupPhotoObserver();
        setupThemeSwitch();
        setupListeners();
        setupStats();

        // Adatok betöltése, ha még nem történt meg
        sharedViewModel.loadUsernameIfNeeded();
    }

    private void setupPhotoObserver() {
        sharedViewModel.getPhotoUrl().observe(getViewLifecycleOwner(), url -> {
            // Ha ugyanaz az URL, ne töltsük be újra
            if (Objects.equals(url, binding.imageProfile.getTag(R.id.imageProfile))) return;
            binding.imageProfile.setTag(R.id.imageProfile, url);

            String finalUrl = url;
            Object model = R.drawable.user_icon;

            if (url != null && !url.isEmpty()) {
                binding.imageProfile.setImageTintList(null);
                if (url.startsWith("http")) {
                    if (url.contains("googleusercontent.com")) {
                        finalUrl = url.replace("s96-c", "s400-c");
                    }
                    model = finalUrl;
                } else {
                    try {
                        byte[] decodedString = android.util.Base64.decode(url, android.util.Base64.DEFAULT);
                        model = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    } catch (Exception e) {
                        model = R.drawable.user_icon;
                    }
                }
            } else {
                binding.imageProfile.setImageTintList(android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.user_icon_background)));
            }

            Glide.with(this)
                    .load(model)
                    .circleCrop()
                    .dontAnimate()
                    .placeholder(binding.imageProfile.getDrawable())
                    .error(R.drawable.user_icon)
                    .into(binding.imageProfile);
        });
    }

    private void setupStats() {
        viewModel.getWatchedMoviesCount().observe(getViewLifecycleOwner(), count -> {
            binding.textWatchedMovies.setText(String.valueOf(count != null ? count : 0));
        });
        viewModel.getWatchedSeriesCount().observe(getViewLifecycleOwner(), count -> {
            binding.textWatchedSeries.setText(String.valueOf(count != null ? count : 0));
        });
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            binding.textUserName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Vendég");
            binding.textUserEmail.setText(user.getEmail());
            binding.btnEditProfile.setVisibility(View.VISIBLE);

            // Jelszó módosítás elrejtése Google bejelentkezés esetén
            boolean isGoogleUser = false;
            for (UserInfo profile : user.getProviderData()) {
                if ("google.com".equals(profile.getProviderId())) {
                    isGoogleUser = true;
                    break;
                }
            }
            binding.btnChangePassword.setVisibility(isGoogleUser ? View.GONE : View.VISIBLE);
        }
    }

    private void setupThemeSwitch() {
        // Alapállapot beállítása a mentett adatok alapján
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", true);
        binding.switchTheme.setChecked(isDarkMode);

        binding.switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("isDarkMode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupListeners() {
        binding.btnLogout.setOnClickListener(view -> signOut());

        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        binding.btnWatchedMoviesContainer.setOnClickListener(v -> openSeeAllActivity("WATCHED_MOVIES", "Megnézett filmek"));

        binding.btnWatchedSeriesContainer.setOnClickListener(v -> openSeeAllActivity("WATCHED_SERIES", "Megnézett sorozatok"));

        binding.btnNotificationList.setOnClickListener(v -> openSeeAllActivity("NOTIFIED_SERIES", "Emlékeztetők"));

        binding.btnSetNotifTime.setOnClickListener(v -> showTimePickerDialog());

        loadNotificationTime();
    }

    private void loadNotificationTime() {
        String savedTime = sharedPreferences.getString("notificationTime", "09:00");
        binding.textNotifTime.setText(savedTime);
    }

    private void showTimePickerDialog() {
        String currentTime = binding.textNotifTime.getText().toString();
        int hour = 9, minute = 0;
        try {
            String[] parts = currentTime.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        } catch (Exception ignored) {}

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(),
                (view, hourOfDay, minuteOfHour) -> {
                    String newTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    binding.textNotifTime.setText(newTime);
                    sharedPreferences.edit().putString("notificationTime", newTime).apply();
                    rescheduleWorker(hourOfDay, minuteOfHour);
                    Toast.makeText(requireContext(), "Értesítés ideje frissítve: " + newTime, Toast.LENGTH_SHORT).show();
                }, hour, minute, true);

        timePickerDialog.show();
    }

    private void rescheduleWorker(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= now) calendar.add(Calendar.DAY_OF_YEAR, 1);
        long delay = calendar.getTimeInMillis() - now;

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SeriesNotificationWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                "SeriesDailyCheck",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }

    private void showEditProfileDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(com.example.tvandmovies.R.layout.dialog_edit_profile, null);
        com.google.android.material.textfield.TextInputEditText editName = dialogView.findViewById(com.example.tvandmovies.R.id.editName);
        dialogImagePreview = dialogView.findViewById(com.example.tvandmovies.R.id.imagePreview);
        com.google.android.material.button.MaterialButton btnChangePhoto = dialogView.findViewById(com.example.tvandmovies.R.id.btnChangePhoto);

        editName.setText(user.getDisplayName());
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).circleCrop().into(dialogImagePreview);
        }

        selectedImageUri = null;
        btnChangePhoto.setOnClickListener(v -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Profil szerkesztése")
                .setView(dialogView)
                .setPositiveButton("Mentés", (dialog, which) -> updateProfile(editName.getText().toString().trim(), selectedImageUri))
                .setNegativeButton("Mégse", (dialog, which) -> dialogImagePreview = null)
                .setOnDismissListener(dialog -> dialogImagePreview = null)
                .show();
    }

    private void updateProfile(String newName, Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        if (imageUri != null) {
            // 1. Kép lekicsinyítése és konvertálása Base64-re (Storage helyett)
            try {
                android.graphics.Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), imageUri);
                
                // Átméretezés max 200x200-ra (hogy beleférjen a Firestore-ba és gyors maradjon)
                android.graphics.Bitmap scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] bytes = baos.toByteArray();
                String base64Image = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
                
                // 2. Mentés Firestore-ba és Auth-ba
                finalizeUpdate(newName, base64Image);
            } catch (java.io.IOException e) {
                Toast.makeText(requireContext(), "Hiba a kép feldolgozásakor", Toast.LENGTH_SHORT).show();
            }
        } else {
            finalizeUpdate(newName, null);
        }
    }

    private void finalizeUpdate(String newName, String photoData) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        com.google.firebase.auth.UserProfileChangeRequest.Builder profileUpdatesBuilder = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(newName);

        // Az Auth-ba nem mentünk Base64-et (mert túl hosszú), oda csak a nevet
        user.updateProfile(profileUpdatesBuilder.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 3. Firestore szinkronizálás (Itt mentjük a Base64-et)
                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                        updates.put("username", newName);
                        if (photoData != null) updates.put("photoUrl", photoData);

                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(user.getUid())
                                .update(updates)
                                .addOnCompleteListener(dbTask -> {
                                    Toast.makeText(requireContext(), "Profil sikeresen frissítve", Toast.LENGTH_SHORT).show();
                                    sharedViewModel.setUsername(newName);
                                    if (photoData != null) sharedViewModel.setPhotoUrl(photoData);
                                    loadUserData();
                                });
                    }
                });
    }

    private void openSeeAllActivity(String type, String title) {
        Intent intent = new Intent(requireActivity(), com.example.tvandmovies.UI.activities.SeeAllActivity.class);
        intent.putExtra("CATEGORY_TYPE", type);
        intent.putExtra("CATEGORY_TITLE", title);
        startActivity(intent);
    }

    private void signOut() {
        mAuth.signOut();
        ClearCredentialStateRequest clearCredentialStateRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(clearCredentialStateRequest, new CancellationSignal(), Executors.newSingleThreadExecutor(), new CredentialManagerCallback<Void, ClearCredentialException>() {
            @Override
            public void onResult(Void result) { navigateToLogin(); }
            @Override
            public void onError(@NonNull ClearCredentialException e) { navigateToLogin(); }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoadingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
