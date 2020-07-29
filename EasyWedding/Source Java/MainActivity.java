package com.example.easywedding;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;


import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


import com.example.easywedding.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //fields for view pager and tabs
    private ViewPager2 mViewPager2;
    private ViewPagerFragmentAdapter mAdapter;
    private TabLayout mTabLayout;
    private boolean isDataCleared;


   // public static final String[] fragmentsTags = {ChatFragment.TAG, FeaturesFragment.T}



    private static final String TAG = MainActivity.class.getSimpleName();

    /*fragments constants*/
    public static final int FRAGMENTS_COUNT = 3;
    public static final int FEATURES_POSITION = 0;
    public static final int GUESTS_POSITION = 1;
    public static final int CHAT_POSITION = 2;
    /*firebase constants*/
    //sign-in flow request code
    private static final int RC_SIGN_IN = 1;

    private DatabaseReference mRootReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleViewPagerWithTabs();
        isDataCleared = false;

        mRootReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    // user is signed in
                    // Apply behavior here
                }
                else{
                    // user is signed out, use FirebaseUI sign-in flow
                    createSignInIntent();
                }
            }
        };

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // IdpResponse is a container that encapsulates the result of authenticating with an Identity Provider.
                    IdpResponse response = IdpResponse.fromResultIntent(data);
                    // If this is the first time the user did sign in
                    // then create a user record for this user in the users node in the db
                    if (response != null) {
                        if (response.isNewUser()) {
                            // The user's chat is initially closed, so no chat id
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();

                            User newUser = new User(user.getDisplayName(),// TODO change invitesAllowed to false
                                    user.getEmail(), true);

                            mRootReference.child(Constants.PATH_USERS)
                                    .child(user.getUid())
                                    .setValue(newUser);

                        }

                        if (isDataCleared) {
                            handleViewPagerWithTabs();
                            isDataCleared = false;
                        }
                    }
                    // Sign in failed. If response is null the user canceled the
                    // sign-in flow using the back button. Otherwise check
                    // response.getError().getErrorCode() and handle the error.
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                }
        }

    }

    @Override
    protected void onResume() {

        super.onResume();
        if (mFirebaseAuth != null)
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {

        super.onPause();
        if (mFirebaseAuth != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);

    }


    public void createSignInIntent() {

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    /**
     * signs out the current user
     */
    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, R.string.signed_out_success
                                ,Toast.LENGTH_SHORT).show();
                    }
                });

    }

    /**
     * set the toolbar and link the TabsLayout with the ViewPager2
     */
    private void handleViewPagerWithTabs() {

        mViewPager2 = findViewById(R.id.main_viewpager);
        mAdapter = new ViewPagerFragmentAdapter(getSupportFragmentManager(), getLifecycle());
        mViewPager2.setAdapter(mAdapter);
        mTabLayout = findViewById(R.id.main_tabs);
        // link the TabLayout with the ViewPager
        new TabLayoutMediator(mTabLayout, mViewPager2,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        // set tab title according to its position
                        switch (position){
                            case FEATURES_POSITION:
                                tab.setText(R.string.tab_features_title);
                                tab.setIcon(R.drawable.vector_features_24);
                                break;
                            case GUESTS_POSITION:
                                tab.setText(R.string.tab_guests_title);
                                tab.setIcon(R.drawable.vector_guests_24);
                                break;
                            case CHAT_POSITION:
                                tab.setText(R.string.tab_chat_title);
                                tab.setIcon(R.drawable.vector_chat_24);
                                break;
                        }
                    }
                }
        ).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                signOut();
                removeFragments();
                // Next time the user logs in we'll create new pages for the fragments.
                isDataCleared = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Removing the fragments. This method gets called after a user has signed out.
     */
    private void removeFragments() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (Fragment fragment : getSupportFragmentManager().getFragments())
            ft.remove(fragment);

        ft.commitAllowingStateLoss();
    }


}


























