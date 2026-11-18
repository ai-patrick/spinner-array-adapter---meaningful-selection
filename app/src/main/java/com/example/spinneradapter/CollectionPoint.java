package com.example.spinneradapter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Domain model for a collection point.
 * Uses a stable ID rather than position for reliable selection.
 */
public class CollectionPoint implements Parcelable {

    @NonNull
    private final String id;          // Stable identifier
    @NonNull
    private final String name;        // Display label
    @NonNull
    private final String location;    // GPS or address
    private final boolean isActive;   // Domain property
    @NonNull
    private final String notes;

    public CollectionPoint(@NonNull String id, @NonNull String name,
                           @NonNull String location, boolean isActive, @NonNull String notes) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.isActive = isActive;
        this.notes = notes;
    }

    // Parcelable implementation
    protected CollectionPoint(@NonNull Parcel in) {
        id = in.readString();
        name = in.readString();
        location = in.readString();
        isActive = in.readByte() != 0;
        notes = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(location);
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(notes);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollectionPoint> CREATOR = new Creator<CollectionPoint>() {
        @Override
        public CollectionPoint createFromParcel(@NonNull Parcel in) {
            return new CollectionPoint(in);
        }

        @Override
        public CollectionPoint[] newArray(int size) {
            return new CollectionPoint[size];
        }
    };

    // Getters
    @NonNull public String getId() { return id; }
    @NonNull public String getName() { return name; }
    @NonNull public String getLocation() { return location; }
    public boolean isActive() { return isActive; }
    @NonNull public String getNotes() { return notes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CollectionPoint)) return false;
        CollectionPoint that = (CollectionPoint) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name; // Default display for ArrayAdapter
    }
}