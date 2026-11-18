package com.example.spinneradapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Custom ArrayAdapter for MushroomType.
 * Demonstrates:
 * - Source collection (List<MushroomType>) is separate from the adapter
 * - Adapter transforms domain objects into Views for Spinner rows
 * - Custom row layout for selected item display
 * - Custom dropdown layout with checkbox for non-sensitive preference
 * - Stable IDs via getItemId() using domain object's ID
 */
public class MushroomAdapter extends ArrayAdapter<MushroomType> {

    private final LayoutInflater inflater;
    private final int rowLayoutRes;
    private final int dropdownLayoutRes;
    private OnEdibleToggleListener edibleToggleListener;

    public interface OnEdibleToggleListener {
        void onEdibleToggled(MushroomType mushroom, boolean isChecked);
    }

    public MushroomAdapter(@NonNull Context context, int rowLayoutRes, int dropdownLayoutRes,
                           @NonNull List<MushroomType> objects) {
        super(context, rowLayoutRes, objects);
        this.inflater = LayoutInflater.from(context);
        this.rowLayoutRes = rowLayoutRes;
        this.dropdownLayoutRes = dropdownLayoutRes;
    }

    public void setOnEdibleToggleListener(OnEdibleToggleListener listener) {
        this.edibleToggleListener = listener;
    }

    /**
     * The adapter is MORE than a copy of the data:
     * - It manages view recycling (convertView pattern)
     * - It binds domain data to specific view layouts
     * - It handles the prompt item (position 0) separately from valid choices
     * - It provides stable IDs via getItemId() for reliable selection tracking
     */
    @Override
    public long getItemId(int position) {
        // Position 0 is the prompt - return NO_ID
        if (position == 0) {
            return AdapterView.NO_ID;
        }
        // Return stable ID from domain object (scientific name hash)
        MushroomType item = getItem(position);
        return item != null ? item.getId().hashCode() : AdapterView.NO_ID;
    }

    @Override
    public boolean hasStableIds() {
        return true; // Critical: enables reliable selection restoration
    }

    /**
     * View for the selected item shown in the Spinner button (closed state).
     * Uses a simpler layout without the checkbox.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(rowLayoutRes, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MushroomType mushroom = getItem(position);
        if (mushroom != null) {
            holder.nameText.setText(mushroom.getCommonName());
            holder.scientificText.setText(mushroom.getScientificName());
            // Hide checkbox in closed state - it's only in dropdown
            holder.edibleCheckBox.setVisibility(View.GONE);
        } else {
            // Prompt item
            holder.nameText.setText(getContext().getString(R.string.prompt_select_mushroom));
            holder.scientificText.setText("");
            holder.edibleCheckBox.setVisibility(View.GONE);
        }
        return convertView;
    }

    /**
     * View for dropdown items (expanded state).
     * Includes checkbox for optional non-sensitive preference.
     * The checkbox is INDEPENDENT of the single-choice Spinner selection:
     * - Spinner selection = which mushroom type (mutually exclusive)
     * - Checkbox = user preference about edibility (independent, can be toggled per item)
     */
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DropDownViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(dropdownLayoutRes, parent, false);
            holder = new DropDownViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (DropDownViewHolder) convertView.getTag();
        }

        MushroomType mushroom = getItem(position);
        if (mushroom != null) {
            holder.nameText.setText(mushroom.getCommonName());
            holder.scientificText.setText(mushroom.getScientificName());
            holder.notesText.setText(mushroom.getNotes());

            // Checkbox for non-sensitive preference (edible marking)
            // This is INDEPENDENT of Spinner selection:
            // - Selection chooses ONE mushroom (radio-like behavior)
            // - Checkbox is a per-item preference that doesn't affect selection
            holder.edibleCheckBox.setText(R.string.checkbox_edible);
            holder.edibleCheckBox.setChecked(mushroom.isEdible());
            holder.edibleCheckBox.setOnCheckedChangeListener(null); // Clear old listener
            holder.edibleCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (edibleToggleListener != null) {
                    edibleToggleListener.onEdibleToggled(mushroom, isChecked);
                }
            });
            holder.edibleCheckBox.setVisibility(View.VISIBLE);
        } else {
            // Prompt item - disable and style differently
            holder.nameText.setText(getContext().getString(R.string.prompt_select_mushroom));
            holder.scientificText.setText("");
            holder.notesText.setText("");
            holder.edibleCheckBox.setVisibility(View.GONE);
            convertView.setEnabled(false);
            convertView.setAlpha(0.5f);
        }
        return convertView;
    }

    /**
     * Distinguish prompt from actual choice:
     * - Position 0 is always the prompt (added by Activity)
     * - Prompt returns NO_ID from getItemId()
     * - Prompt is disabled in dropdown (not clickable)
     * - Validation checks position > 0 before accepting selection
     */
    public boolean isPromptPosition(int position) {
        return position == 0;
    }

    // ViewHolder for closed spinner view
    static class ViewHolder {
        TextView nameText;
        TextView scientificText;
        CheckBox edibleCheckBox;

        ViewHolder(View view) {
            nameText = view.findViewById(R.id.tv_mushroom_name);
            scientificText = view.findViewById(R.id.tv_mushroom_scientific);
            edibleCheckBox = view.findViewById(R.id.cb_edible);
        }
    }

    // ViewHolder for dropdown view
    static class DropDownViewHolder {
        TextView nameText;
        TextView scientificText;
        TextView notesText;
        CheckBox edibleCheckBox;

        DropDownViewHolder(View view) {
            nameText = view.findViewById(R.id.tv_mushroom_name);
            scientificText = view.findViewById(R.id.tv_mushroom_scientific);
            notesText = view.findViewById(R.id.tv_mushroom_notes);
            edibleCheckBox = view.findViewById(R.id.cb_edible);
        }
    }
}