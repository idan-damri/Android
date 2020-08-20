package com.example.easywedding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.easywedding.Utils.Utils;
import com.example.easywedding.model.Contact;
import com.example.easywedding.model.Guest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<Contact> mContacts;
    private ContactsAdapter mAdapter;
    HashSet<Contact> mContactHashSet;

    private ProgressBar mProgressBar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mDataId;

    private ArrayList<String> mPhones;

    private static final String PROHIBITED_PHONE_CHARACTERS = "[()\\s+]";
    private static final String AREA_CODE_PREFIX_ISR = "972";
    private static final Uri CONTACTS_BASE_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final String ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private static final String PHOTO = ContactsContract.CommonDataKinds.Photo.PHOTO_URI;
    private static final String NAME = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String EMAIL = ContactsContract.CommonDataKinds.Email.ADDRESS;

    public static final String TAG = ContactsActivity.class.getSimpleName();

    private static final String[] PROJECTION = new String[]{
            ID,
            PHOTO,
            NAME,
            PHONE_NUMBER
    };

    private static final String[] EMAIL_PROJECTION = new String[]{EMAIL};

    private static final String SORT_ORDER = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.contacts_progress_bar);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();

        if (intent.hasExtra(Constants.FRAGMENT_DATA_ID_ARG))
            mDataId = intent.getStringExtra(Constants.FRAGMENT_DATA_ID_ARG);

        if (intent.hasExtra(Constants.EXTRA_PHONE_LIST))
            mPhones = intent.getStringArrayListExtra(Constants.EXTRA_PHONE_LIST);

        mRecyclerView = findViewById(R.id.contacts_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        mContacts = new ArrayList<>();
        mAdapter = new ContactsAdapter(this, mContacts);
        mRecyclerView.setAdapter(mAdapter);
        // For getting a unique user display name, without duplicates.
        mContactHashSet = new HashSet<>();

        getContacts();


    }

    private void getContacts() {
        boolean isLayoutDirectionRTL =  false;
        // We need to know this for later. Change the phone number string pattern.
        // In Israel, replace "...972..." variations with "".
        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL)
            isLayoutDirectionRTL = true;

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                CONTACTS_BASE_URI,
                PROJECTION,
                null,
                null,
                null);

        if (cursor != null && 0 < cursor.getCount()) {
            while (cursor.moveToNext()) {
                String uid = cursor.getString(cursor.getColumnIndex(ID));
                String name = cursor.getString(cursor.getColumnIndex(NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndex(PHONE_NUMBER));
                String photoUri = cursor.getString(cursor.getColumnIndex(PHOTO));
                String email = "";

                if (isLayoutDirectionRTL) {
                    phoneNumber = phoneNumber.replaceAll(PROHIBITED_PHONE_CHARACTERS, "");
                    if (phoneNumber.startsWith(AREA_CODE_PREFIX_ISR))
                        phoneNumber = phoneNumber.replaceFirst(AREA_CODE_PREFIX_ISR, "0");



                }

       /*
                Cursor emailCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        EMAIL_PROJECTION,
                        ID + " = ?",
                        new String[]{uid},
                        null);

                if (emailCursor != null && 0 < emailCursor.getCount()
                        && emailCursor.moveToNext()) {
                    email = emailCursor.getString(0);
                    emailCursor.close();
                }
        */

                mContactHashSet.add(new Contact(name, phoneNumber, photoUri, false, uid));


            }
            mContacts.addAll(mContactHashSet);
            // Sort by the contact name in ascending order.
            Collections.sort(mContacts);
            mAdapter.notifyDataSetChanged();
            // Now that the data is all set up, create a copy of the contacts list
            // for filtering purposes
            mAdapter.initializeContactsCopyList();
        }
        if (cursor != null)
            cursor.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search_contact).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_contacts:
                writeGuestsToDatabase();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * write to te db the contacts that the user wants to invite
     */
    private void writeGuestsToDatabase() {
        DatabaseReference userGuestsRef = mRootRef.child(Constants.PATH_GUESTS).child(mDataId);
        // Get all the checked contacts and for each contact make a corresponding guest object
        // return the result as a list of guests.
        List<Guest> guests = mAdapter.getGuestsFromContacts();

        if (guests == null || guests.size() == 0) {
            Toast.makeText(ContactsActivity.this, R.string.empty_guest_choice, Toast.LENGTH_SHORT).show();
        }
        else{
            Map<String, Object> map = new HashMap<>();
            // For each guest make a unique key for this guest in the db
            // and set the value of this key to be the guest object.
            // this one line in the for loop sets our map and also generates the keys
            // in the db.
            for ( Guest guest : guests) {
                // If this guest not already in the guest list then add him.
                if (!Utils.isDuplicateGuest(guest.getPhoneNumber()
                        .trim()
                        .replaceAll("-", ""), mPhones)) {

                    map.put(userGuestsRef.push().getKey(), guest);
                }
            }
            // If there're no duplicates
            if (0 < map.size()) {
                mProgressBar.setVisibility(View.VISIBLE);

                // Now insert al the guests to the db. (their keys are all set up from before)
                userGuestsRef.updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(ContactsActivity.this, R.string.guest_saved_success, Toast.LENGTH_SHORT).show();
                        // Let AddGuestActivity know that it should return to GuestsFragment
                        setResult(RESULT_OK);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressBar.setVisibility(View.GONE);
                        Toast.makeText(ContactsActivity.this, R.string.error_generic_msg, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
            else{
                Toast.makeText(this, R.string.dup_guests, Toast.LENGTH_SHORT).show();
            }

        }
    }
}