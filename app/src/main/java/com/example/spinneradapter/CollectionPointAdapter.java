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
 * Custom ArrayAdapter for CollectionPoint.
 * Mirrors MushroomAdapter structure for consistency.
 */
public class CollectionPointAdapter extends ArrayAdapter<CollectionPoint> {

    private final LayoutInflater inflater;
    private final int rowLayoutRes;
    private final int dropdownLayoutRes;
    private OnCollectedToggleListener collectedToggleListener;

    public interface OnCollectedToggleListener {
        void onCollectedToggled(CollectionPoint point, boolean isChecked);
    }

    public CollectionPointAdapter(@NonNull Context context, int rowLayoutRes, int dropdownLayoutRes,
                                  @NonNull List<CollectionPoint> objects) {
        super(context, rowLayoutRes, objects);
        this.inflater = LayoutInflater.from(context);
        this.rowLayoutRes = rowLayoutRes;
        this.dropdownLayoutRes = dropdownLayoutRes;
    }

    public void setOnCollectedToggleListener(OnCollectedToggleListener listener) {
        this.collectedToggleListener = listener;
    }

    @Override
    public long getItemId(int position) {
        if (position == 0) {
            return AdapterView.NO_ID;
        }
        CollectionPoint item = getItem(position);
        return item != null ? item.getId().hashCode() : AdapterView.NO_ID;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

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

        CollectionPoint point = getItem(position);
        if (point != null) {
            holder.nameText.setText(point.getName());
            holder.locationText.setText(point.getLocation());
            holder.collectedCheckBox.setVisibility(View.GONE);
        } else {
            holder.nameText.setText(getContext().getString(R.string.prompt_select_collection));
            holder.locationText.setText("");
            holder.collectedCheckBox.setVisibility(View.GONE);
        }
        return convertView;
    }

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

        CollectionPoint point = getItem(position);
        if (point != null) {
            holder.nameText.setText(point.getName());
            holder.locationText.setText(point.getLocation());
            holder.notesText.setText(point.getNotes());

            holder.collectedCheckBox.setText(R.string.checkbox_collected);
            holder.collectedCheckBox.setChecked(point.isActive());
            holder.collectedCheckBox.setOnCheckedChangeListener(null);
            holder.collectedCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (collectedToggleListener != null) {
                    collectedToggleListener.onCollectedToggled(point, isChecked);
                }
            });
            holder.collectedCheckBox.setVisibility(View.VISIBLE);
        } else {
            holder.nameText.setText(getContext().getString(R.string.prompt_select_collection));
            holder.locationText.setText("");
            holder.notesText.setText("");
            holder.collectedCheckBox.setVisibility(View.GONE);
            convertView.setEnabled(false);
            convertView.setAlpha(0.5f);
        }
        return convertView;
    }

    public boolean isPromptPosition(int position) {
        return position == 0;
    }

    static class ViewHolder {
        TextView nameText;
        TextView locationText;
        CheckBox collectedCheckBox;

        ViewHolder(View view) {
            nameText = view.findViewById(R.id.tv_point_name);
            locationText = view.findViewById(R.id.tv_point_location);
            collectedCheckBox = view.findViewById(R.id.cb_collected);
        }
    }

    static class DropDownViewHolder {
        TextView nameText;
        TextView locationText;
        TextView notesText;
        CheckBox collectedCheckBox;

        DropDownViewHolder(View view) {
            nameText = view.findViewById(R.id.tv_point_name);
            locationText = view.findViewById(R.id.tv_point_location);
            notesText = view.findViewById(R.id.tv_point_notes);
            collectedCheckBox = view.findViewById(R.id.cb_collected);
        }
    }
}