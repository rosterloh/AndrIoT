package com.rosterloh.andriot.db;

import android.os.Build;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class FirebaseAdapter {

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    @Inject
    public FirebaseAdapter() {
        mAuth = FirebaseAuth.getInstance();
        initialiseAuth();
    }

    public void initialiseAuth() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Timber.d("onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                // User is signed out
                Timber.d("onAuthStateChanged:signed_out");
            }
        };
    }

    public void onStart() {
        mAuth.addAuthStateListener(mAuthListener);
        if (!isUserSignedIn()) {
            mAuth.signInAnonymously().addOnCompleteListener(task -> {
                Timber.d("signInAnonymously:onComplete:" + task.isSuccessful());

                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Timber.w("signInAnonymously: Task failed. ", task.getException());
                } else {
                    Timber.w("signInAnonymously: Task succeeded. ", task.getException());
                }
            });
        } else {
            Timber.d("User already signed in.");
        }
    }

    public void onStop() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void uploadSensorData(SensorData data) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("sensor")
                .child(Build.SERIAL);

        myRef.push().setValue(data)
            .addOnFailureListener(e -> {
                Timber.w("Sensor data upload failed.", e);
            });
    }
}
