package com.example.spinneradapter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Domain model for a mushroom type.
 * Uses a stable ID (scientific name) rather than position for reliable selection.
 */
public class MushroomType implements Parcelable {

    @NonNull
    private final String id;          // Stable identifier (scientific name)
    @NonNull
    private final String commonName;  // Display label
    @NonNull
    private final String scientificName;
    private final boolean isEdible;   // Domain property
    @NonNull
    private final String notes;

    public MushroomType(@NonNull String id, @NonNull String commonName,
                        @NonNull String scientificName, boolean isEdible, @NonNull String notes) {
        this.id = id;
        this.commonName = commonName;
        this.scientificName = scientificName;
        this.isEdible = isEdible;
        this.notes = notes;
    }

    // Parcelable implementation
    protected MushroomType(@NonNull Parcel in) {
        id = in.readString();
        commonName = in.readString();
        scientificName = in.readString();
        isEdible = in.readByte() != 0;
        notes = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(commonName);
        dest.writeString(scientificName);
        dest.writeByte((byte) (isEdible ? 1 : 0));
        dest.writeString(notes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MushroomType> CREATOR = new Creator<MushroomType>() {
        @Override
        public MushroomType createFromParcel(@NonNull Parcel in) {
            return new MushroomType(in);
        }

        @Override
        public MushroomType[] newArray(int size) {
            return new MushroomType[size];
        }
    };

    // Getters
    @NonNull public String getId() { return id; }
    @NonNull public String getCommonName() { return commonName; }
    @NonNull public String getScientificName() { return scientificName; }
    public boolean isEdible() { return isEdible; }
    @NonNull public String getNotes() { return notes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MushroomType)) return false;
        MushroomType that = (MushroomType) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return commonName; // Default display for ArrayAdapter
    }
}