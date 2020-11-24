package patryk.tasks.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "notes_table")
public class Note {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String note;

    public Date date;

    public int priority;

    public double latitude;

    private double longitude;

    public Note(String note, Date date, int priority,double latitude,double longitude) {
        this.note = note;
        this.date = date;
        this.priority = priority;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNote() {
        return note;
    }

    public Date getDate() {
        return date;
    }

    public int getPriority() {
        return priority;
    }
    public double getLatitude(){
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
}
