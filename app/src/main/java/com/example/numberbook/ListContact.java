package com.example.numberbook;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.numberbook.adapter.ContactAdapter;
import com.example.numberbook.api.ApiClient;
import com.example.numberbook.classes.ApiResponse;
import com.example.numberbook.classes.Contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListContact extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 101;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 102;

    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private ProgressBar progressBar;
    private EditText searchEditText;
    private ImageButton searchButton;
    private ImageButton clearButton;
    private TextView noResultsTextView;
    private Button btnSaveToServer;
    private List<Contact> contactList = new ArrayList<>();
    private int successCount = 0;
    private int failureCount = 0;
    private int totalContacts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contact);

        recyclerView = findViewById(R.id.contacts_recycler_view);
        progressBar = findViewById(R.id.loading_progress);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        clearButton = findViewById(R.id.clear_button);
        noResultsTextView = findViewById(R.id.no_results_text);
        btnSaveToServer = findViewById(R.id.btnSaveToServer);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, contactList, noResultsTextView);
        recyclerView.setAdapter(adapter);

        // Set up search functionality
        setupSearchFunctionality();

        // Request all necessary permissions
        requestRequiredPermissions();

        // Set up save to server button
        btnSaveToServer.setOnClickListener(v -> saveContactsToServer());
    }

    private void setupSearchFunctionality() {
        // Search button click listener
        searchButton.setOnClickListener(v -> {
            performSearch();
        });

        // Clear button click listener
        clearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            clearButton.setVisibility(View.GONE);
            adapter.getFilter().filter("");
        });

        // Search on enter key
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // Real-time search as user types and show/hide clear button
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter contacts as user types
                adapter.getFilter().filter(s);

                // Show/hide clear button
                if (s.length() > 0) {
                    clearButton.setVisibility(View.VISIBLE);
                } else {
                    clearButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();
        adapter.getFilter().filter(query);
    }

    private void requestRequiredPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        // Check for READ_CONTACTS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        // Check for CALL_PHONE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }

        // Check for SEND_SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsNeeded.toArray(new String[0]),
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            // All permissions are already granted
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            // Check if READ_CONTACTS permission was granted
            boolean contactsPermissionGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.READ_CONTACTS) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    contactsPermissionGranted = true;
                    break;
                }
            }

            if (contactsPermissionGranted) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadContacts() {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            List<Contact> contacts = getContactsFromDevice();

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                contactList.clear();
                contactList.addAll(contacts);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private List<Contact> getContactsFromDevice() {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI));
                int hasPhoneNumber = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhoneNumber > 0) {
                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null
                    );

                    if (phoneCursor != null && phoneCursor.moveToFirst()) {
                        String phoneNumber = phoneCursor.getString(
                                phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        // Utilise un constructeur selon ta classe Contact
                        Contact contact;
                        if (photoUri != null) {
                            contact = new Contact(name, phoneNumber);
                        } else {
                            contact = new Contact(name, phoneNumber);
                        }

                        contacts.add(contact);
                        phoneCursor.close(); // Fermer ici après utilisation
                    }
                }

            } while (cursor.moveToNext());

            cursor.close(); // Fermer le curseur principal
        }

        return contacts;
    }



    // Method to convert app Contact to API Contact
    private com.example.numberbook.classes.Contact convertToApiContact(Contact appContact) {
        return new com.example.numberbook.classes.Contact(
                appContact.getNom(),
                appContact.getNumero()
        );
    }

    private void saveContactsToServer() {
        // Reset counters
        successCount = 0;
        failureCount = 0;
        totalContacts = contactList.size();

        if (totalContacts == 0) {
            Toast.makeText(this, "No contacts to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress
        Toast.makeText(this, "Starting to save " + totalContacts + " contacts...", Toast.LENGTH_SHORT).show();

        // Disable button while processing
        btnSaveToServer.setEnabled(false);

        // For each contact in your list
        for (Contact appContact : contactList) {
            // Convert to API contact
            com.example.numberbook.classes.Contact apiContact = convertToApiContact(appContact);

            // Log the contact being sent
            Log.d("ContactAPI", "Sending contact: " + apiContact.getNom() + ", " + apiContact.getNumero());

            // Make API call
            ApiClient.getInstance()
                    .getContactApiService()
                    .createContact(apiContact)
                    .enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Log.d("ContactAPI", "Success: " + response.body().getMessage());
                                successCount++;
                                checkCompletion();
                            } else {
                                // Log the error response
                                String errorBody = "";
                                try {
                                    if (response.errorBody() != null) {
                                        errorBody = response.errorBody().string();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Log.e("ContactAPI", "Error: " + response.code() + " " + errorBody);
                                failureCount++;
                                checkCompletion();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Log.e("ContactAPI", "Failure: " + t.getMessage(), t);
                            failureCount++;
                            checkCompletion();
                        }
                    });
        }
    }

    private void checkCompletion() {
        // Check if all contacts have been processed
        if ((successCount + failureCount) == totalContacts) {
            // Re-enable button
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnSaveToServer.setEnabled(true);

                    // Show completion message
                    String message = "Completed: " + successCount + " contacts saved successfully, "
                            + failureCount + " failed.";
                    Toast.makeText(ListContact.this, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
