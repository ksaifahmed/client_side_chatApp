package com.example.talks;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UnregClientsActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private RecyclerView recyclerView;
    private DatabaseReference unregRef, userRef;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unreg_clients);
        unregRef = FirebaseDatabase.getInstance().getReference().child("Unregistered");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        InitializeList();

    }


    @Override
    protected void onStart() {
        super.onStart();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                DatabaseReference connectionRef = FirebaseDatabase.getInstance().getReference(".info/connected");
                connectionRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean connected = dataSnapshot.getValue(Boolean.class);
                        if(!connected)
                        {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(UnregClientsActivity.this, "Cannot connect to Server", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }, 2000);


        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(unregRef, User.class)
                        .build();

        FirebaseRecyclerAdapter<User, UnregViewHolder> adapter = new FirebaseRecyclerAdapter<User, UnregViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UnregViewHolder holder, int position, @NonNull User model)
            {
                holder.name.setText(model.getName());
                String tmp = model.getName() + getString(R.string.gmail);
                holder.email.setText(tmp);
                holder.itemView.findViewById(R.id.acceptButton).setVisibility(View.VISIBLE);
                final String listUserID = getRef(position).getKey();

                holder.itemView.findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {

                        AlertDialog.Builder builder = new AlertDialog.Builder(UnregClientsActivity.this);
                        builder.setMessage("Approve this Client?").setCancelable(false)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        unregRef.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.child(listUserID).child("name").exists())
                                                {
                                                    String tmp = dataSnapshot.child(listUserID).child("name").getValue().toString();
                                                    userRef.child(listUserID).setValue(mapper(tmp, listUserID)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                unregRef.child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful())
                                                                        {
                                                                            Toast.makeText(UnregClientsActivity.this, "Client Approved!", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                        onStart();
                                    }
                                })
                                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("Client Approval:");
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    }
                });
            }


            @Override
            public void onDataChanged() {
                super.onDataChanged();

                if(progressBar!=null)
                {
                    progressBar.setVisibility(View.GONE);
                }
            }




            @NonNull
            @Override
            public UnregViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.unreg_user_disp_layout, viewGroup, false);
                UnregViewHolder viewholder = new UnregViewHolder(view);
                return viewholder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();




    }

    private Map<String, String> mapper(String tmp, String listUserID)
    {
        Map<String, String> m = new HashMap<>();
        m.put("name", tmp);
        m.put("uid", listUserID);
        return m;
    }

    public static class UnregViewHolder extends RecyclerView.ViewHolder
    {
        TextView name, email;
        Button accept;

        private UnregViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.unreg_username);
            email = itemView.findViewById(R.id.unreg_user_email);
            accept = itemView.findViewById(R.id.acceptButton);


        }
    }

    private void InitializeList()
    {
        recyclerView = findViewById(R.id.unreg_rec_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mtoolbar = findViewById(R.id.unreg_toolbar);
        progressBar = findViewById(R.id.progressBarUnreg);
        progressBar.setVisibility(View.VISIBLE);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Pending Clients");
        getSupportActionBar().setSubtitle(null);
    }
}
