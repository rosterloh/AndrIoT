package com.rosterloh.andriot.cloud;

import android.arch.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseLiveData extends LiveData<String> {

    private ValueEventListener dataListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                setValue(dataSnapshot.toString());
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onActive() {
        super.onActive();

        FirebaseDatabase.getInstance()
                .getReference("data/info")
                .addListenerForSingleValueEvent(dataListener);
    }
}
