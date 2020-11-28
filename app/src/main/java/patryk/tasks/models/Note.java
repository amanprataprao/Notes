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

    public double distance;

    public String streetAddress;

    public Note(String note, Date date, int priority,double latitude,double longitude, double distance, String streetAddress) {
        this.note = note;
        this.date = date;
        this.priority = priority;
        this.latitude = latitude;
        //this.latitude = Math.random()*200-100;
        this.longitude = longitude;
        //this.longitude = Math.random()*200-100;
        //this.distance = distance;
        this.distance = Math.sqrt((this.longitude-81.86)*(this.longitude-81.86) + (this.latitude-25.45)*(this.latitude-25.45));
        this.streetAddress = streetAddress;
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

    public double getDistance()
    {
        return distance;
    }

    public void setDistance(double distance)
    {
        this.distance = distance;
    }

    public String getStreetAddress()
    {
        return streetAddress;
    }
}
