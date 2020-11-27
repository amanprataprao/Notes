package patryk.tasks.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

import patryk.tasks.R;

public class SearchActivity extends AppCompatActivity {

    PlacesClient placesClient;
    private final int MY_MAP_ACTIVITY = 200;
    private static final String TAG = "SearchActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if(isServicesOK()){
            init();
        }

    }

    private void init(){
        String apiKey = getString(R.string.google_maps_key);

        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(SearchActivity.this, apiKey);
        }
        // Retrieve a PlacesClient (previously initialized - see MainActivity)
        placesClient = Places.createClient(this);

        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment)
                        getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS));

        autocompleteSupportFragment.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        final LatLng latLng = place.getLatLng();
                        Toast.makeText(SearchActivity.this, ""+latLng.latitude, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SearchActivity.this, MapsActivity.class);
                        intent.putExtra("Latlng", latLng);
                        startActivityForResult(intent, MY_MAP_ACTIVITY);

                    }

                    @Override
                    public void onError(Status status) {
                        Toast.makeText(SearchActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
//        Button btnMap = (Button) findViewById(R.id.btnMap);
//        btnMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, MapActivity.class);
//                startActivity(intent);
//            }
//        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (MY_MAP_ACTIVITY) : {
                if (resultCode == Activity.RESULT_OK) {
                    // TODO Extract the data returned from the child Activity.
                    Bundle bundle = data.getExtras();
                    LatLng selectedLocation = bundle.getParcelable("latLng");
                    String streetAddress = bundle.getString("streetAddress");

                    Intent resultIntent = new Intent();
                    // TODO Add extras or a data URI to this intent as appropriate.
                    resultIntent.putExtra("latLng", selectedLocation);
                    resultIntent.putExtra("streetAddress", streetAddress);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();

                }
                break;
            }
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SearchActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(SearchActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}