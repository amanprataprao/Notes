package patryk.tasks.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;
import patryk.tasks.R;
import patryk.tasks.adapters.NoteAdapter;
import patryk.tasks.models.Note;
import patryk.tasks.viewmodels.NoteViewModel;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnItemClickListener {

    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    public static final String SORT_ALPHABETICALLY = "note";
    public static final String SORT_BY_DATE = "date";
    public static final String SORT_BY_PRIORITY = "priority";
    public static final String SORT_BY_DISTANCE = "distance";
    public static File fileDir;
    private NoteViewModel noteViewModel;
    private NoteAdapter adapter;

    public FusedLocationProviderClient mFusedLocationClient;


    public Location mLastLocation;  //stores location
    double latitude=0;
    double longitude=0;
    private Geocoder geocoder;
    public String streetAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        geocoder = new Geocoder(this);

        // For reading old notes from previous storage
        // which was text file
        fileDir = getFilesDir();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
            startActivityForResult(intent, ADD_NOTE_REQUEST);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        ImageView emptyViewImage = findViewById(R.id.emptyViewImage);

        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getNotes().observe(this, notes -> {
                    if (notes.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyViewImage.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyViewImage.setVisibility(View.GONE);
                    }
                    adapter.submitList(notes);
                }
        );

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

                /*
                int position_from = viewHolder.getAdapterPosition();
                int position_to = target.getAdapterPosition();

                Note draggedNote = adapter.getNoteAt(position_from);
                Note targetNote = adapter.getNoteAt(position_to);

                //TODO: Zamiana notatek w VievModel

                adapter.notifyItemMoved(position_from, position_to);

                 */
                return false;
            }

            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
                AlertDialog alertDialog = getBuilder()
                        .setMessage(getString(R.string.delete_note_prompt_message))
                        .setPositiveButton(getString(R.string.delete_note_delete_button_text), (dialog, whichButton) -> {
                            noteViewModel.delete(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                            Toasty.info(MainActivity.this, getString(R.string.delete_note_confirmation_text), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.delete_note_cancel_button_text), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.notifyItemChanged(viewHolder.getAdapterPosition());
                                dialog.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(this);
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();
                            latitude = 2;

                        } else {

                            //showSnackbar("No location detected. Make sure location is enabled on the device.");
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            String text = data.getStringExtra(AddEditActivity.EXTRA_TEXT);
            int priority = data.getIntExtra(AddEditActivity.EXTRA_PRIORITY, 1);
            Date pickedDate = (Date) data.getSerializableExtra(AddEditActivity.EXTRA_DATE);
            double lat = data.getDoubleExtra(AddEditActivity.EXTRA_LAT, -500);
            double lng = data.getDoubleExtra(AddEditActivity.EXTRA_LNG, -500);
            if (mLastLocation == null) {

                latitude = -1;
                longitude = -1;
            } else {
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }
            if (lat != -500 && lng != -500)
            {
                latitude = lat;
                longitude = lng;
            }
            try {
                LatLng latLng = new LatLng(latitude, longitude);
                Log.d("Location", String.valueOf(latitude));
                Log.d("Location", String.valueOf(longitude));
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    streetAddress = address.getAddressLine(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Log.d("Hello", streetAddress);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            Note note = new Note(text, pickedDate, priority,latitude,longitude, 0, streetAddress);

            noteViewModel.insert(note);
            //Toasty.success(this, streetAddress, Toast.LENGTH_SHORT).show();
            //Toasty.success(this, String.valueOf(latitude), Toast.LENGTH_SHORT).show();
            Toasty.success(this, getString(R.string.activity_note_successfully_saved), Toast.LENGTH_SHORT).show();
        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {

            int id = data.getIntExtra(AddEditActivity.EXTRA_ID, -1);
            if (id == -1) {
                Toasty.error(this, getString(R.string.activity_sth_went_wrong), Toast.LENGTH_SHORT).show();
            }

            String text = data.getStringExtra(AddEditActivity.EXTRA_TEXT);
            int priority = data.getIntExtra(AddEditActivity.EXTRA_PRIORITY, 1);
            Date pickedDate = (Date) data.getSerializableExtra(AddEditActivity.EXTRA_DATE);

            Note note = new Note(text, pickedDate, priority,latitude,longitude, 0, streetAddress);
            note.setId(id);
            noteViewModel.update(note);


            Toasty.info(this, getString(R.string.activity_update_message), Toast.LENGTH_SHORT).show();
        } else {
            // Note not saved
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main_activity, menu);

        // Override menu behaviour
        // to show icons next to the text
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete_all:
                AlertDialog alertDialog = getBuilder()
                        .setMessage(getString(R.string.alert_delete_all_message))
                        .setPositiveButton(getString(R.string.delete_note_delete_button_text), (dialog, whichButton) -> {
                            noteViewModel.deleteAll();
                            Toasty.info(MainActivity.this, getString(R.string.alert_delete_all_confirmation_text), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .setNegativeButton(getString(R.string.delete_note_cancel_button_text), (dialog, which) -> dialog.dismiss())
                        .create();
                alertDialog.show();
                return true;
            case R.id.menu_sort_alphabetically:
                noteViewModel.sortNotes(SORT_ALPHABETICALLY);
                return true;
            case R.id.menu_sort_by_date:
                noteViewModel.sortNotes(SORT_BY_DATE);
                return true;
            case R.id.menu_sort_by_priority:
                noteViewModel.sortNotes(SORT_BY_PRIORITY);
                return true;
            case R.id.menu_sort_by_distance:
                Log.d("HELLO", "Sort by distance clicked!");
                noteViewModel.updateDistance();
                Log.d("Hello","Distance updated");
                noteViewModel.sortNotes(SORT_BY_DISTANCE);
                Log.d("Hello","Sorted by distance");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private AlertDialog.Builder getBuilder() {
        return new AlertDialog.Builder(this)
                .setTitle(R.string.delete_note_delete_button_text)
                .setIcon(R.drawable.ic_delete_forever)
                .setCancelable(false);
    }

    @Override
    public void onItemClick(Note note) {
        Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
        intent.putExtra(AddEditActivity.EXTRA_ID, note.getId());
        intent.putExtra(AddEditActivity.EXTRA_TEXT, note.getNote());
        intent.putExtra(AddEditActivity.EXTRA_DATE, note.getDate());
        intent.putExtra(AddEditActivity.EXTRA_PRIORITY, note.getPriority());
        intent.putExtra(AddEditActivity.EXTRA_LAT, note.getLatitude());
        intent.putExtra(AddEditActivity.EXTRA_LNG, note.getLongitude());
        startActivityForResult(intent, EDIT_NOTE_REQUEST);
    }
}
