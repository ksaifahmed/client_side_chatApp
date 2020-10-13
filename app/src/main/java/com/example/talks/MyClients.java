package com.example.talks;


import android.content.Intent;
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
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyClients extends Fragment {

    private View fragmentview;
    private RecyclerView recyclerView;
    private DatabaseReference mycontactsref, usercontactsref;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUID = "";
    private ProgressBar progressBar;


    public MyClients() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) currentUID = currentUser.getUid();
        mycontactsref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUID);
        usercontactsref = FirebaseDatabase.getInstance().getReference().child("Users Assigned");

        fragmentview =  inflater.inflate(R.layout.fragment_my_clients, container, false);
        InitializeList();
        return fragmentview;
    }

    private void InitializeList()
    {
        recyclerView = (RecyclerView) fragmentview.findViewById(R.id.my_client_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = fragmentview.findViewById(R.id.progressMyClients);
        progressBar.setVisibility(View.VISIBLE);
    }




    private Map<String, String> mapper(String tmp, String listUserID)
    {
        Map<String, String> m = new HashMap<>();
        m.put("name", tmp);
        m.put("uid", listUserID);
        return m;
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
                .setQuery(mycontactsref.orderByChild("name"), User.class)
                .build();

        FirebaseRecyclerAdapter<User, MyClients.ClientViewHolder> adapter = new FirebaseRecyclerAdapter<User, MyClients.ClientViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyClients.ClientViewHolder holder, int position, @NonNull final User model)
            {
                holder.itemView.findViewById(R.id.messageButton).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.removeButton).setVisibility(View.VISIBLE);
                holder.name.setText(model.getName());
                final String listUserID = getRef(position).getKey();


                holder.itemView.findViewById(R.id.messageButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GoToMessageActivity(listUserID, model.getName());

                    }


                });

                holder.itemView.findViewById(R.id.removeButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setMessage("Remove this Client from your List?").setCancelable(false)
                                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //delete from Contacts node!
                                        mycontactsref.child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful())
                                                {
                                                    usercontactsref.child(listUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful())
                                                            {
                                                                Toast.makeText(getContext(), "Client Removed!", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
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
                        dialog.setTitle("Remove Contact: ");
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
            public MyClients.ClientViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myclients_layout, viewGroup, false);
                MyClients.ClientViewHolder viewHolder = new MyClients.ClientViewHolder(view);
                return viewHolder;
            }
        };



        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }


    public static class ClientViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        Button accept, remove;

        private ClientViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.myclient_username);
            accept = itemView.findViewById(R.id.messageButton);
            remove = itemView.findViewById(R.id.removeButton);
        }
    }


    public  void GoToMessageActivity(String listUserID, String name) {
        Intent messageIntent = new Intent(getContext(), MessageActivity.class);
        messageIntent.putExtra("receiverID", listUserID);
        messageIntent.putExtra("receiverName", name);
        startActivity(messageIntent);
    }

}
