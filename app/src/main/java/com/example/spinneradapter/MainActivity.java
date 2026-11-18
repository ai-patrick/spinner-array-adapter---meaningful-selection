package com.example.spinneradapter;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity demonstrating Spinner, ArrayAdapter, and meaningful selection.
 * 
 * KEY CONCEPTS IMPLEMENTED:
 * 1. Source collection (List<MushroomType>) is separate from adapter
 * 2. Adapter transforms domain objects into Views (row + dropdown layouts)
 * 3. Prompt item at position 0 is NOT a valid choice (returns NO_ID)
 * 4. Stable IDs via getItemId() using domain object's ID (scientific name)
 * 5. Selection stored by stable ID, not position - survives list reordering
 * 6. Custom dropdown layout with checkbox for independent preference
 * 6. State restoration after rotation via onSaveInstanceState/onRestoreInstanceState
 * 7. Empty list state handling
 */
public class MainActivity extends AppCompatActivity {

    // View references
    private Spinner spinnerMain;
    private TextView tvEmptyState;
    private TextView tvConfirmationTitle;
    private TextView tvSelectedItem;
    private TextView tvSelectedId;
    private TextView tvPosition;
    private TextView tvExtraInfo;
    private TextView tvExplanation;
    private Button btnSwitchMode;
    private androidx.cardview.widget.CardView cardConfirmation;

    // Data
    private List<MushroomType> mushroomList;
    private List<CollectionPoint> collectionList;
    private MushroomAdapter mushroomAdapter;
    private CollectionPointAdapter collectionAdapter;

    // State
    private boolean showingMushrooms = true;
    private String selectedMushroomId = null;
    private String selectedCollectionId = null;
    private int selectedMushroomPosition = 0;
    private int selectedCollectionPosition = 0;
    private boolean mushroomEdiblePreference = false;
    private boolean collectionCollectedPreference = false;

    // Keys for state restoration
    private static final String KEY_SHOWING_MUSHROOMS = "showing_mushrooms";
    private static final String KEY_SELECTED_MUSHROOM_ID = "selected_mushroom_id";
    private static final String KEY_SELECTED_COLLECTION_ID = "selected_collection_id";
    private static final String KEY_SELECTED_MUSHROOM_POS = "selected_mushroom_pos";
    private static final String KEY_SELECTED_COLLECTION_POS = "selected_collection_pos";
    private static final String KEY_MUSHROOM_EDIBLE = "mushroom_edible";
    private static final String KEY_COLLECTION_COLLECTED = "collection_collected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
        setupAdapters();
        setupListeners();

        // Restore state if rotating
        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        }

        // Initial UI update
        updateConfirmationView();
        updateExplanation();
    }

    private void initViews() {
        spinnerMain = findViewById(R.id.spinner_main);
        tvEmptyState = findViewById(R.id.tv_empty_state);
        tvConfirmationTitle = findViewById(R.id.tv_confirmation_title);
        tvSelectedItem = findViewById(R.id.tv_selected_item);
        tvSelectedId = findViewById(R.id.tv_selected_id);
        tvPosition = findViewById(R.id.tv_position);
        tvExtraInfo = findViewById(R.id.tv_extra_info);
        tvExplanation = findViewById(R.id.tv_explanation);
        btnSwitchMode = findViewById(R.id.btn_switch_mode);
        cardConfirmation = findViewById(R.id.card_confirmation);
    }

    private void initData() {
        // Mushroom types with stable IDs (scientific names)
        mushroomList = new ArrayList<>();
        mushroomList.add(new MushroomType("agaricus_bisporus", "Button Mushroom",
                "Agaricus bisporus", true, "Common cultivated mushroom"));
        mushroomList.add(new MushroomType("pleurotus_ostreatus", "Oyster Mushroom",
                "Pleurotus ostreatus", true, "Edible, fan-shaped caps"));
        mushroomList.add(new MushroomType("lentinula_edodes", "Shiitake",
                "Lentinula edodes", true, "Popular in Asian cuisine"));
        mushroomList.add(new MushroomType("amanita_muscaria", "Fly Agaric",
                "Amanita muscaria", false, "TOXIC - iconic red cap with white spots"));
        mushroomList.add(new MushroomType("cantharellus_cibarius", "Chanterelle",
                "Cantharellus cibarius", true, "Prized edible, fruity aroma"));
        mushroomList.add(new MushroomType("morchella_esculenta", "Morel",
                "Morchella esculenta", true, "Honeycomb cap, spring delicacy"));

        // Collection points with stable IDs
        collectionList = new ArrayList<>();
        collectionList.add(new CollectionPoint("cp_001", "Forest Trail North",
                "40.7128° N, 74.0060° W", true, "Mixed deciduous forest"));
        collectionList.add(new CollectionPoint("cp_002", "Riverbank East",
                "34.0522° N, 118.2437° W", true, "Riparian zone, damp soil"));
        collectionList.add(new CollectionPoint("cp_003", "Meadow Clearing",
                "41.8781° N, 87.6298° W", false, "Open grassland, seasonal"));
        collectionList.add(new CollectionPoint("cp_004", "Pine Plantation",
                "29.7604° N, 95.3698° W", true, "Coniferous, acidic soil"));
        collectionList.add(new CollectionPoint("cp_005", "Urban Park",
                "39.9526° N, 75.1652° W", false, "Disturbed habitat, edge effects"));
    }

    private void setupAdapters() {
        // MushroomAdapter with custom row and dropdown layouts
        mushroomAdapter = new MushroomAdapter(
                this,
                R.layout.spinner_row_mushroom,
                R.layout.spinner_dropdown_mushroom,
                new ArrayList<>() // Start empty, we'll add prompt + items
        );
        mushroomAdapter.setOnEdibleToggleListener((mushroom, isChecked) -> {
            mushroomEdiblePreference = isChecked;
            Toast.makeText(this, "Edible preference for " + mushroom.getCommonName() + ": " + isChecked,
                    Toast.LENGTH_SHORT).show();
            updateExplanation();
        });

        // CollectionPointAdapter
        collectionAdapter = new CollectionPointAdapter(
                this,
                R.layout.spinner_row_collection,
                R.layout.spinner_dropdown_collection,
                new ArrayList<>()
        );
        collectionAdapter.setOnCollectedToggleListener((point, isChecked) -> {
            collectionCollectedPreference = isChecked;
            Toast.makeText(this, "Collected status for " + point.getName() + ": " + isChecked,
                    Toast.LENGTH_SHORT).show();
            updateExplanation();
        });

        // Add prompt item (position 0) + data items
        rebuildSpinnerData();

        spinnerMain.setAdapter(showingMushrooms ? mushroomAdapter : collectionAdapter);
    }

    /**
     * Rebuilds spinner data with prompt at position 0.
     * This demonstrates: adapter manages prompt separately from valid choices.
     */
    private void rebuildSpinnerData() {
        if (showingMushrooms) {
            mushroomAdapter.clear();
            // Add prompt as first item (null = prompt)
            mushroomAdapter.add(null);
            // Add actual data
            for (MushroomType m : mushroomList) {
                mushroomAdapter.add(m);
            }
            // Restore selection by stable ID
            if (selectedMushroomId != null) {
                int pos = findPositionById(mushroomAdapter, selectedMushroomId);
                if (pos > 0) {
                    spinnerMain.setSelection(pos);
                    selectedMushroomPosition = pos;
                } else {
                    // Previously selected item no longer available
                    selectedMushroomId = null;
                    selectedMushroomPosition = 0;
                    spinnerMain.setSelection(0);
                }
            }
            updateEmptyState(mushroomList.isEmpty());
        } else {
            collectionAdapter.clear();
            collectionAdapter.add(null); // Prompt
            for (CollectionPoint c : collectionList) {
                collectionAdapter.add(c);
            }
            if (selectedCollectionId != null) {
                int pos = findPositionById(collectionAdapter, selectedCollectionId);
                if (pos > 0) {
                    spinnerMain.setSelection(pos);
                    selectedCollectionPosition = pos;
                } else {
                    selectedCollectionId = null;
                    selectedCollectionPosition = 0;
                    spinnerMain.setSelection(0);
                }
            }
            updateEmptyState(collectionList.isEmpty());
        }
        mushroomAdapter.notifyDataSetChanged();
        collectionAdapter.notifyDataSetChanged();
    }

    /**
     * Find position by stable ID - demonstrates why stable IDs matter.
     * If list order changes, position-based lookup would fail.
     */
    private <T> int findPositionById(ArrayAdapter<T> adapter, String targetId) {
        for (int i = 0; i < adapter.getCount(); i++) {
            T item = adapter.getItem(i);
            if (item instanceof MushroomType) {
                if (((MushroomType) item).getId().equals(targetId)) return i;
            } else if (item instanceof CollectionPoint) {
                if (((CollectionPoint) item).getId().equals(targetId)) return i;
            }
        }
        return -1;
    }

    private void setupListeners() {
        // Spinner selection listener
        spinnerMain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Ignore prompt selection (position 0 or NO_ID)
                if (position == 0 || id == AdapterView.NO_ID) {
                    clearSelection();
                    return;
                }

                if (showingMushrooms) {
                    MushroomType selected = mushroomAdapter.getItem(position);
                    if (selected != null) {
                        selectedMushroomId = selected.getId();
                        selectedMushroomPosition = position;
                        updateConfirmationView();
                    }
                } else {
                    CollectionPoint selected = collectionAdapter.getItem(position);
                    if (selected != null) {
                        selectedCollectionId = selected.getId();
                        selectedCollectionPosition = position;
                        updateConfirmationView();
                    }
                }
                updateExplanation();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                clearSelection();
            }
        });

        // Mode switch button
        btnSwitchMode.setOnClickListener(v -> {
            showingMushrooms = !showingMushrooms;
            btnSwitchMode.setText(showingMushrooms ?
                    R.string.btn_switch_mode : R.string.btn_switch_mode_mushroom);
            tvEmptyState.setText(showingMushrooms ?
                    R.string.empty_list_mushroom : R.string.empty_list_collection);

            // Swap adapter
            spinnerMain.setAdapter(showingMushrooms ? mushroomAdapter : collectionAdapter);
            rebuildSpinnerData();
            updateConfirmationView();
            updateExplanation();
        });
    }

    private void clearSelection() {
        if (showingMushrooms) {
            selectedMushroomId = null;
            selectedMushroomPosition = 0;
        } else {
            selectedCollectionId = null;
            selectedCollectionPosition = 0;
        }
        updateConfirmationView();
    }

    private void updateConfirmationView() {
        if (showingMushrooms) {
            tvConfirmationTitle.setText(R.string.confirmation_title);
            if (selectedMushroomId != null) {
                MushroomType selected = findMushroomById(selectedMushroomId);
                if (selected != null) {
                    tvSelectedItem.setText(getString(R.string.confirmation_mushroom, selected.getCommonName()));
                    tvSelectedId.setText(getString(R.string.label_selected_id, selected.getId()));
                    tvPosition.setText(getString(R.string.label_position, selectedMushroomPosition));
                    tvExtraInfo.setText(getString(R.string.confirmation_edible, selected.isEdible() ? "Yes" : "No") +
                            " | " + getString(R.string.confirmation_notes, selected.getNotes()));
                }
            } else {
                tvSelectedItem.setText(R.string.no_selection);
                tvSelectedId.setText("");
                tvPosition.setText("");
                tvExtraInfo.setText("");
            }
        } else {
            tvConfirmationTitle.setText(R.string.confirmation_title);
            if (selectedCollectionId != null) {
                CollectionPoint selected = findCollectionById(selectedCollectionId);
                if (selected != null) {
                    tvSelectedItem.setText(getString(R.string.confirmation_collection, selected.getName()));
                    tvSelectedId.setText(getString(R.string.label_selected_id, selected.getId()));
                    tvPosition.setText(getString(R.string.label_position, selectedCollectionPosition));
                    tvExtraInfo.setText("Active: " + (selected.isActive() ? "Yes" : "No") +
                            " | " + getString(R.string.confirmation_notes, selected.getNotes()));
                }
            } else {
                tvSelectedItem.setText(R.string.no_selection);
                tvSelectedId.setText("");
                tvPosition.setText("");
                tvExtraInfo.setText("");
            }
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        spinnerMain.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        cardConfirmation.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateExplanation() {
        StringBuilder sb = new StringBuilder();
        sb.append("EXPLANATION:\n\n");
        
        if (showingMushrooms) {
            sb.append("MODE: Mushroom Types\n");
            sb.append("• Source collection: List<MushroomType> (domain models)\n");
            sb.append("• Adapter: MushroomAdapter transforms models → Views\n");
            sb.append("• Prompt at position 0: \"").append(getString(R.string.prompt_select_mushroom)).append("\" (not selectable)\n");
            sb.append("• Stable ID: Scientific name (").append(selectedMushroomId != null ? selectedMushroomId : "none").append(")\n");
            sb.append("• Selection stored by ID, not position → survives reordering\n");
            sb.append("• Dropdown layout includes checkbox for \"edible\" preference\n");
            sb.append("• Checkbox is INDEPENDENT of Spinner selection:\n");
            sb.append("  - Spinner = single choice (radio behavior)\n");
            sb.append("  - Checkbox = per-item preference (can toggle multiple)\n");
            if (selectedMushroomId != null) {
                MushroomType m = findMushroomById(selectedMushroomId);
                if (m != null) {
                    sb.append("\nSELECTED: ").append(m.getCommonName())
                      .append(" (ID: ").append(m.getId()).append(")")
                      .append(", Position: ").append(selectedMushroomPosition)
                      .append(", Edible: ").append(m.isEdible());
                }
            }
        } else {
            sb.append("MODE: Collection Points\n");
            sb.append("• Source collection: List<CollectionPoint> (domain models)\n");
            sb.append("• Adapter: CollectionPointAdapter transforms models → Views\n");
            sb.append("• Prompt at position 0: \"").append(getString(R.string.prompt_select_collection)).append("\" (not selectable)\n");
            sb.append("• Stable ID: Custom ID (").append(selectedCollectionId != null ? selectedCollectionId : "none").append(")\n");
            sb.append("• Selection stored by ID, not position → survives reordering\n");
            sb.append("• Dropdown layout includes checkbox for \"collected\" status\n");
            if (selectedCollectionId != null) {
                CollectionPoint c = findCollectionById(selectedCollectionId);
                if (c != null) {
                    sb.append("\nSELECTED: ").append(c.getName())
                      .append(" (ID: ").append(c.getId()).append(")")
                      .append(", Position: ").append(selectedCollectionPosition)
                      .append(", Active: ").append(c.isActive());
                }
            }
        }
        
        sb.append("\n\nROTATION TEST: Rotate device → selection persists via stable ID");
        sb.append("\nEMPTY LIST TEST: Clear data → shows empty state message");
        sb.append("\nWHAT IF SELECTED OPTION REMOVED? → Falls back to prompt, ID cleared");
        
        tvExplanation.setText(sb.toString());
    }

    private MushroomType findMushroomById(String id) {
        for (MushroomType m : mushroomList) {
            if (m.getId().equals(id)) return m;
        }
        return null;
    }

    private CollectionPoint findCollectionById(String id) {
        for (CollectionPoint c : collectionList) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SHOWING_MUSHROOMS, showingMushrooms);
        outState.putString(KEY_SELECTED_MUSHROOM_ID, selectedMushroomId);
        outState.putString(KEY_SELECTED_COLLECTION_ID, selectedCollectionId);
        outState.putInt(KEY_SELECTED_MUSHROOM_POS, selectedMushroomPosition);
        outState.putInt(KEY_SELECTED_COLLECTION_POS, selectedCollectionPosition);
        outState.putBoolean(KEY_MUSHROOM_EDIBLE, mushroomEdiblePreference);
        outState.putBoolean(KEY_COLLECTION_COLLECTED, collectionCollectedPreference);
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {
        showingMushrooms = savedInstanceState.getBoolean(KEY_SHOWING_MUSHROOMS, true);
        selectedMushroomId = savedInstanceState.getString(KEY_SELECTED_MUSHROOM_ID);
        selectedCollectionId = savedInstanceState.getString(KEY_SELECTED_COLLECTION_ID);
        selectedMushroomPosition = savedInstanceState.getInt(KEY_SELECTED_MUSHROOM_POS, 0);
        selectedCollectionPosition = savedInstanceState.getInt(KEY_SELECTED_COLLECTION_POS, 0);
        mushroomEdiblePreference = savedInstanceState.getBoolean(KEY_MUSHROOM_EDIBLE, false);
        collectionCollectedPreference = savedInstanceState.getBoolean(KEY_COLLECTION_COLLECTED, false);

        btnSwitchMode.setText(showingMushrooms ?
                R.string.btn_switch_mode : R.string.btn_switch_mode_mushroom);
    }

    /**
     * Demo method to simulate empty list (for testing requirement #4)
     * Call this to demonstrate empty state handling
     */
    public void simulateEmptyList() {
        if (showingMushrooms) {
            mushroomList.clear();
        } else {
            collectionList.clear();
        }
        rebuildSpinnerData();
    }

    /**
     * Demo method to simulate list reordering (for testing requirement #2)
     * Demonstrates why stable IDs are essential
     */
    public void simulateReorderList() {
        if (showingMushrooms && mushroomList.size() > 1) {
            // Reverse the list to simulate reordering
            List<MushroomType> reversed = new ArrayList<>(mushroomList);
            java.util.Collections.reverse(reversed);
            mushroomList.clear();
            mushroomList.addAll(reversed);
        } else if (!showingMushrooms && collectionList.size() > 1) {
            List<CollectionPoint> reversed = new ArrayList<>(collectionList);
            java.util.Collections.reverse(reversed);
            collectionList.clear();
            collectionList.addAll(reversed);
        }
        rebuildSpinnerData();
        Toast.makeText(this, "List reordered - selection preserved via stable ID!", Toast.LENGTH_LONG).show();
    }
}