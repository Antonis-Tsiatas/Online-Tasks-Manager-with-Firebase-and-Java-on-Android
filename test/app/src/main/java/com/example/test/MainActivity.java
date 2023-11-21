package com.example.test;

import static android.content.ContentValues.TAG;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button button, addButton, crossButton,tickbutton,trashButton;


    Adapter adapter;
    RecyclerView recyclerView;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<Map<String, Object>> dataset = new ArrayList<>();
    CollectionReference tasksCollection = db.collection("tasks");
    String title;
    String description;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddItemDialog();
            }
        });



        adapter = new Adapter(this,dataset);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));


        tasksCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }


                int firstSize = dataset.size();
                dataset.clear();

                for (QueryDocumentSnapshot document : value) {

                    Map<String, Object> dataMap = document.getData();
                    dataset.add(dataMap);



                }
                int secondSize = dataset.size();

                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                if(secondSize>firstSize){

                    if (!dataset.isEmpty()) {
                        Map<String, Object> lastDataMap = dataset.get(dataset.size() - 1);
                        title = (String) lastDataMap.get("title");
                        description = (String) lastDataMap.get("description");

                    }

                }


            }
        });


        button = findViewById(R.id.buttonSignOut);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });


    }



    private void showAddItemDialog() {

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setView(dialogView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText titleEditText = dialogView.findViewById(R.id.titleEditText);
                        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

                        title = titleEditText.getText().toString();
                        description = descriptionEditText.getText().toString();

                        adapter.saveDataToFirebase(title, description);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();

    }







}