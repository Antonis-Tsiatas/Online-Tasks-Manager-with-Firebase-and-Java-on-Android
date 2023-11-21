// DesiresAdapter.java
package com.example.test;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DesiresAdapter extends RecyclerView.Adapter<DesiresAdapter.ViewHolder> {
    private List<String> fullnames;
    String[] seperateName;
    CollectionReference userCollection = FirebaseFirestore.getInstance().collection("users");
    private LinearLayout taskRecycleId;
    private  String title;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final Button tickButton;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.itemNameTextView);
            tickButton = view.findViewById(R.id.tickButton);
        }

        public Button getTickButton() {
            return tickButton;
        }

        public TextView getTextView() {
            return textView;
        }
    }
    public void applyDesired(ViewHolder viewHolder){
        seperateName = viewHolder.getTextView().getText().toString().split(" ",2);
        getDesiredUserId();
        if (taskRecycleId != null) {
            changeColorRV(viewHolder);
        } else {
            Log.e(TAG, "RecyclerView is null in applyDesired()");
        }
    }
    public void setTaskLinear(LinearLayout taskRecycleId){
        this.taskRecycleId = taskRecycleId;
    }
    public void setFullnames(List<String> fullNames) {
        this.fullnames = fullNames;
        notifyDataSetChanged();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void changeColorRV(ViewHolder viewHolder) {
        if (taskRecycleId != null) {
            taskRecycleId.setBackgroundColor(Color.parseColor("#90EE90"));


                Log.d("onoma",viewHolder.getTextView().getText().toString());
            }
        }
    @SuppressLint("MissingInflatedId")
    private boolean isAdmin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.getEmail() != null && user.getEmail().equals("admin@admin.com")) {
                return true;

            }
        }
        return  false;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.show_desires, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getTextView().setText(fullnames.get(position));
        viewHolder.getTickButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyDesired(viewHolder);
            }
        });
    }
public void getDesiredUserId(){

    userCollection.whereEqualTo("firstName",seperateName[0])
            .whereEqualTo("lastName", seperateName[1])
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id= (String)  document.get("ID");
                            Log.d("doylevei " ,id);
                        }
                    } else {
                    }
                }
            });

                }

                public String getFirstNameFromFullName(String fullname){
                    String[] firstName = fullname.split(" ", 2);
                    return  firstName[0];
                }

    public String getLastNameFromFullName(String fullname){
        String[] lastName = fullname.split(" ", 2);
        return  lastName[1];
    }
    @Override
    public int getItemCount() {
        return fullnames != null ? fullnames.size() : 0;
    }
}

