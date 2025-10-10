package com.example.lab5_starter;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.Objects;

public class City implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String province;

    @Exclude
    private String docId; // Firestore document ID (not stored in the document)

    public City() { } // Required for Firestore deserialization

    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }

    @NonNull
    @Override
    public String toString() {
        return name + ", " + province;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof City)) return false;
        City other = (City) o;
        if (docId != null && other.docId != null) return docId.equals(other.docId);
        if (!Objects.equals(name, other.name)) return false;
        return Objects.equals(province, other.province);
    }

    @Override
    public int hashCode() {
        if (docId != null) return docId.hashCode();
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (province != null ? province.hashCode() : 0);
        return result;
    }
}


