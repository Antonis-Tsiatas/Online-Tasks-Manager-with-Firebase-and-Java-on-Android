// Adapter.java
package com.example.test;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<Map<String, Object>> localDataSet = new ArrayList<>();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid = user != null ? user.getUid() : "";
    private Context context;
    private LayoutInflater inflater;
    private DesiresAdapter desiresAdapter;
    private RecyclerView recyclerView;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final Button buttonTrash, buttonTick, buttonCross, buttonAlldesires;
        private LinearLayout taskRecycleId;

        HashMap<String, String> desiresId = new HashMap<String, String>();

        public ViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.titleTextView);
            descriptionTextView = view.findViewById(R.id.descriptionTextView);
            buttonTrash = view.findViewById(R.id.buttonTrash);
            buttonTick = view.findViewById(R.id.buttonTick);
            buttonAlldesires = view.findViewById(R.id.buttonDesires);
            buttonCross = view.findViewById(R.id.buttonCross);
            taskRecycleId = view.findViewById(R.id.taskRecycleId);
        }

        public LinearLayout getTaskRecycleId() {
            return taskRecycleId;
        }

        public TextView getTitleTextView() {
            return titleTextView;
        }

        public TextView getDescriptionTextView() {
            return descriptionTextView;
        }

        public Button getButtonTrash() {
            return buttonTrash;
        }

        public Button getButtonTick() {
            return buttonTick;
        }

        public Button getButtonCross() {
            return buttonCross;
        }

        public Button getButtonAlldesires() {
            return buttonAlldesires;
        }
    }

    public Adapter(Context context, List<Map<String, Object>> dataSet) {
        localDataSet = dataSet;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.desiresAdapter = new DesiresAdapter();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int position) {
        Map<String, Object> data = localDataSet.get(position);
        setItemData(viewHolder, data);
        setDeleteButtonListener(viewHolder, position);
        setDesiredButtonListener(viewHolder, position);
        turnButtonsToInvinsible(viewHolder);
        setAllDesiresButtonListener(viewHolder, position);
    }

    private void setDesiredButtonListener(ViewHolder viewHolder, final int position) {
        viewHolder.getButtonTick().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addDesiredUser(viewHolder, position);
            }
        });
    }

    private void setDeleteButtonListener(ViewHolder viewHolder, final int position) {
        viewHolder.getButtonTrash().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteItemFromFirebase(position);
            }
        });
    }

    private void setAllDesiresButtonListener(ViewHolder viewHolder, final int position) {
        viewHolder.getButtonAlldesires().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemDialog(position,viewHolder);
            }
        });
    }

    private void setItemData(ViewHolder viewHolder, Map<String, Object> data) {
        String title = (String) data.get("title");
        String description = (String) data.get("description");
        viewHolder.getTitleTextView().setText(title);
        viewHolder.getDescriptionTextView().setText(description);
    }

    public void getAllDesires(int position) {
        Map<String, Object> data = localDataSet.get(position);
        String title = (String) data.get("title");
        String description = (String) data.get("description");
        CollectionReference tasksCollection = FirebaseFirestore.getInstance().collection("tasks");

        tasksCollection.whereEqualTo("title", title)
                .whereEqualTo("description", description)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Task<?>> tasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            List<String> desiredBy = (List<String>) document.get("desiredBy");

                            if (desiredBy != null) {
                                tasks.add(convertIdToFullName(desiredBy));
                            } else {
                                Log.w(TAG, "no desires yet");
                            }
                        }

                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                                    @Override
                                    public void onSuccess(List<Object> results) {
                                        List<String> fullNames = new ArrayList<>();
                                        for (Object result : results) {
                                            if (result instanceof List) {
                                                fullNames = (List<String>) result;
                                            }
                                        }
                                        desiresAdapter.setFullnames(fullNames);
                                    }
                                });
                    }
                });
    }

    public Task<List<String>> convertIdToFullName(List<String> idList) {
        List<Task<String>> tasks = new ArrayList<>();

        CollectionReference userCollection = FirebaseFirestore.getInstance().collection("users");

        for (String i : idList) {
            Task<String> task = userCollection.whereEqualTo("ID", i)
                    .get()
                    .continueWith(new Continuation<QuerySnapshot, String>() {
                        @Override
                        public String then(@NonNull Task<QuerySnapshot> task) throws Exception {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String firstName = (String) document.get("firstName");
                                    String lastName = (String) document.get("lastName");
                                    return firstName + " " + lastName;
                                }
                            }
                            return null;
                        }
                    });

            tasks.add(task);
        }

        return Tasks.whenAllSuccess(tasks);
    }

    private void addDesiredUser(ViewHolder viewHolder, int position) {
        Map<String, Object> data = localDataSet.get(position);
        String title = (String) data.get("title");
        String description = (String) data.get("description");

        CollectionReference tasksCollection = FirebaseFirestore.getInstance().collection("tasks");

        tasksCollection.whereEqualTo("title", title)
                .whereEqualTo("description", description)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        String id;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            List<String> desiredBy = (List<String>) document.get("desiredBy");
                            id= document.getId();
                            if (desiredBy == null) {
                                desiredBy = new ArrayList<>();
                            }

                            desiredBy.add(uid);
                            Log.d("to id einai",id)
;                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("desiredBy", desiredBy);

                            document.getReference().set(updateData, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(viewHolder.itemView.getContext(), "Desire successfully added",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
    }

    @SuppressLint("MissingInflatedId")
    private void showAddItemDialog(int position,ViewHolder viewHolder) {

        recyclerView = new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(desiresAdapter);
        desiresAdapter.setTaskLinear(viewHolder.getTaskRecycleId());
        desiresAdapter.setTitle(viewHolder.getTitleTextView().toString());
        getAllDesires(position);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Desired By")
                .setView(recyclerView)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Add desired functionality if needed
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }

    private void deleteItemFromFirebase(int position) {
        Map<String, Object> data = localDataSet.get(position);
        String title = (String) data.get("title");
        String description = (String) data.get("description");

        CollectionReference tasksCollection = FirebaseFirestore.getInstance().collection("tasks");
        tasksCollection.whereEqualTo("title", title).whereEqualTo("description", description).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    document.getReference().delete();
                }

                localDataSet.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, localDataSet.size());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error deleting document: " + e);
            }
        });
    }

    public void saveDataToFirebase(String title, String description) {
        CollectionReference tasksCollection = db.collection("tasks");

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("description", description);

        tasksCollection.add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Handle success if needed
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure if needed
                    }
                });
    }

    @SuppressLint("MissingInflatedId")
    private boolean isAdmin() {
        if (user != null) {
            if (user.getEmail() != null && user.getEmail().equals("admin@admin.com")) {
                return true;

            }
        }
        return  false;
    }


    private void turnButtonsToInvinsible(ViewHolder viewHolder){
        if(isAdmin()){
            viewHolder.getButtonTick().setVisibility(View.GONE);
            viewHolder.getButtonCross().setVisibility(View.GONE);

        }
        else{
            viewHolder.getButtonTrash().setVisibility(View.GONE);
            viewHolder.getButtonAlldesires().setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
