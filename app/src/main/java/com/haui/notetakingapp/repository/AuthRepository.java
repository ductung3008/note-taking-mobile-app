package com.haui.notetakingapp.repository;

import android.app.Application;
import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.haui.notetakingapp.data.remote.firebase.SyncManager;
import com.haui.notetakingapp.data.remote.model.FirestoreUser;
import com.haui.notetakingapp.utils.FirebaseErrorUtils;

import java.util.Date;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private Context applicationContext;

    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.setLanguageCode("vi");
        firestore = FirebaseFirestore.getInstance();
    }

    public AuthRepository(Context context) {
        this();
        this.applicationContext = context.getApplicationContext();
    }

    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<Void> updateUserProfile(FirebaseUser firebaseUser, String displayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        return firebaseUser.updateProfile(profileUpdates);
    }

    public Task<Void> saveUserToFirestore(FirebaseUser firebaseUser, String email, String displayName) {
        FirestoreUser user = new FirestoreUser(
                firebaseUser.getUid(),
                email,
                displayName,
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                new Date()
        );

        return firestore.collection("users")
                .document(firebaseUser.getUid())
                .set(user);
    }

    public Task<Void> resetPassword(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    public void logout() {
        if (applicationContext != null) {
            try {
                NoteRepository noteRepository = new NoteRepository((Application) applicationContext);
                noteRepository.clearAllLocalNotes();
                SyncManager.getInstance().reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public String getLocalizedErrorMessage(Exception exception) {
        return FirebaseErrorUtils.getLocalizedErrorMessage(exception);
    }
}
