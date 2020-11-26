package patryk.tasks.repository;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import patryk.tasks.database.NoteDB;
import patryk.tasks.interfaces.NoteDao;
import patryk.tasks.models.Note;

public class NoteRepository {

    private NoteDao noteDao;
    private MediatorLiveData<List<Note>> mNotes;
    private LiveData<List<Note>> sortedNotes;
    private MutableLiveData<String> order;

    public NoteRepository(Application application) {
        NoteDB db = NoteDB.getInstance(application);
        noteDao = db.noteDao();

        order = new MutableLiveData<>();
        sortedNotes = new MediatorLiveData<>();
        mNotes = new MediatorLiveData<>();

        // Setting default sorting order
        order.setValue("priority");

        // LiveData with List<Note> that reacts to sorting changes
        // Observes order changes and sorts in that specific order,
        // whenever user choose to switch to the other sorting type
        sortedNotes = Transformations.switchMap(order, this::getSortedNotes);

        // LiveData with List<Note> that reacts to sorting changes
        // Observes sortedNotes and gets updates whenever source gets changed
        mNotes.addSource(sortedNotes, notes -> mNotes.postValue(notes));
    }

    public void insert(Note note) {
        new InsertAsyncTask(noteDao).execute(note);
    }

    public void update(Note note) {
        new UpdateAsyncTask(noteDao).execute(note);
    }

    public void delete(Note note) {
        new DeleteAsyncTask(noteDao).execute(note);
    }

    public void deleteAll() {
        new DeleteAllAsyncTask(noteDao).execute();
    }

    public LiveData<List<Note>> getNotes() {
        return mNotes;
    }

    public void updateDistance() {
        Log.d("Hello", "In updateDistance() in NoteRepository.java");
        new UpdateDistanceAsyncTask(noteDao).execute();
        Log.d("Hello", "UpdateDistanceAsync finished");
    }

    public void setSortingOrder(String order) {
        this.order.setValue(order);
    }

    // Returns LiveData with list of sorted notes in order selected by the user
    // Sorting order types: Alphabetically, by date, by priority
    private LiveData<List<Note>> getSortedNotes(String orderBy) {
        switch (orderBy) {
            case "note":
                sortedNotes = noteDao.getNotesByName();
                break;
            case "date":
                sortedNotes = noteDao.getNotesByDate();
                break;
            case "priority":
                sortedNotes = noteDao.getNotesByPriority();
                break;
            case "distance":
                sortedNotes = noteDao.getNotesByDistance();
                break;
            default:
                // Wrong sorting parameter
                Log.w("SORTING", "Wrong sorting parameter in getSortedNotes(String orderBy). Available sorting order types: note, date, priority.");
                break;
        }
        return sortedNotes;
    }

    private static class InsertAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;

        private InsertAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.insert(notes[0]);
            Log.d("Hello", String.valueOf(notes[0].distance));
            return null;
        }
    }

    private static class UpdateAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;

        private UpdateAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.update(notes[0]);
            return null;
        }
    }

    private static class UpdateDistanceAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;

        private UpdateDistanceAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }
        @Override
        protected Void doInBackground(Note... notes) {
            //double lat1 = 37;
            //double lng1 = 50;
            //Log.d("Hello", String.valueOf(notes[0]));
            //double lat2 = notes[0].getLatitude();
            //double lng2 = notes[0].getLongitude();
            //notes[0].distance = Math.sqrt((lng2-lng1)*(lng2-lng1) + (lat2-lat1)*(lat2-lat1));
            Log.d("Hello", String.valueOf(notes.length));
            for (int i = 0; i<notes.length; i++)
            {
                double lat1 = 37, lng1 = 50;
                //double lat2 = notes[i].getLatitude(), lng2 = notes[i].getLongitude();
                double lat2 = Math.random(), lng2 = Math.random();
                notes[i].setDistance(Math.sqrt((lng2-lng1)*(lng2-lng1) + (lat2-lat1)*(lat2-lat1)));
                Log.d("Hello", String.valueOf(notes[i].distance));
                System.out.println(notes[i].distance);
                noteDao.update(notes[i]);
            }
            //noteDao.update(notes[0]);
            return null;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Note, Void, Void> {

        private NoteDao noteDao;

        private DeleteAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Note... notes) {
            noteDao.delete(notes[0]);
            return null;
        }
    }

    private static class DeleteAllAsyncTask extends AsyncTask<Void, Void, Void> {

        private NoteDao noteDao;

        private DeleteAllAsyncTask(NoteDao noteDao) {
            this.noteDao = noteDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            noteDao.deleteAll();
            return null;
        }
    }
}
