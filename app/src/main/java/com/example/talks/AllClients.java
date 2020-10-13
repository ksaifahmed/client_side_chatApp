package com.example.talks;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllClients extends Fragment {

    private View fragmentview;
    private RecyclerView recyclerView;
    private DatabaseReference mycontactsref,usercontactref, userRef, counselorref;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUID = "";
    private ProgressBar progressBar;


    public AllClients() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) currentUID = currentUser.getUid();

        mycontactsref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUID);
        usercontactref = FirebaseDatabase.getInstance().getReference().child("Users Assigned");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        counselorref = FirebaseDatabase.getInstance().getReference().child("Counselors");

        fragmentview =  inflater.inflate(R.layout.fragment_all_clients, container, false);

        InitializeList();


        return fragmentview;
    }

    private void InitializeList()
    {
        recyclerView = (RecyclerView) fragmentview.findViewById(R.id.all_client_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = fragmentview.findViewById(R.id.progressBarAllclients);
        progressBar.setVisibility(View.VISIBLE);
    }






    @Override
    public void onStart() {
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
                            Toast.makeText(getContext(), "Cannot connect to Server", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }, 2000);


        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(userRef.orderByChild("name"), User.class)
                .build();

        FirebaseRecyclerAdapter<User, ClientViewHolder> adapter = new FirebaseRecyclerAdapter<User, ClientViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ClientViewHolder holder, int position, @NonNull User model)
            {

                holder.name.setText(model.getName());
                final String listUserID = getRef(position).getKey();


                usercontactref.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child("peer").exists())
                        {
                            String peerID = dataSnapshot.child("peer").getValue().toString();
                            holder.itemView.findViewById(R.id.addButton).setVisibility(View.GONE);

                            counselorref.child(peerID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child("name").exists())
                                    {
                                        String peername = dataSnapshot.child("name").getValue().toString();
                                        holder.email.setText("Peer: "+peername);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else
                        {
                            holder.itemView.findViewById(R.id.addButton).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setMessage("Assign this user as your Client?").setCancelable(false)
                                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    userRef.child(listUserID).addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.child("name").exists())
                                                            {
                                                                String naam = dataSnapshot.child("name").getValue().toString();
                                                                mycontactsref.child(listUserID).setValue(mapper(naam,listUserID))
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if(task.isSuccessful())
                                                                                {
                                                                                    usercontactref.child(listUserID).setValue(mapper2("peer", currentUID))
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if(task.isSuccessful())
                                                                                                        Toast.makeText(getContext(), "Client Added to MyClients List", Toast.LENGTH_SHORT).show();
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
                                    dialog.setTitle("Assign Client:");
                                    dialog.show();
                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);

                                }


                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

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
            public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.client_disp_layout, viewGroup, false);
                ClientViewHolder viewHolder = new ClientViewHolder(view);
                return viewHolder;
            }
        };



        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }




    public static class ClientViewHolder extends RecyclerView.ViewHolder
    {
        TextView name, email;
        Button accept;

        private ClientViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.client_username);
            email = itemView.findViewById(R.id.client_user_email);
            accept = itemView.findViewById(R.id.addButton);
        }
    }


    private Map<String, String> mapper(String tmp, String listUserID)
    {
        Map<String, String> m = new HashMap<>();
        m.put("name", tmp);
        m.put("uid", listUserID);
        return m;
    }

    private Map<String, String> mapper2(String peer, String ID)
    {
        Map<String, String> m = new HashMap<>();
        m.put(peer, ID);
        return m;
    }



}


