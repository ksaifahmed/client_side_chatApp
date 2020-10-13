package com.example.talks;


import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chat extends Fragment {


    private View fragmentview;
    private RecyclerView recyclerView;
    private DatabaseReference mycontactsref, mymessagesref;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUID = "";
    private ProgressBar progressBar;



    public Chat() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) currentUID = currentUser.getUid();
        mycontactsref = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUID);
        mymessagesref = FirebaseDatabase.getInstance().getReference().child("Messages");



        // Inflate the layout for this fragment
        fragmentview =  inflater.inflate(R.layout.fragment_chat, container, false);
        InitializeList();
        return fragmentview;
    }


    private void InitializeList()
    {
        recyclerView = (RecyclerView) fragmentview.findViewById(R.id.chat_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        progressBar = fragmentview.findViewById(R.id.progressBarx);
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
                            if(getContext()!=null)
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
                .setQuery(mycontactsref.orderByChild("time"), User.class)
                .build();

        FirebaseRecyclerAdapter<User, Chat.ClientViewHolder> adapter = new FirebaseRecyclerAdapter<User, Chat.ClientViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final Chat.ClientViewHolder holder, int position, @NonNull User model)
            {
                final String tmp = model.getName();
                holder.name.setText(tmp);
                final String listUserID = getRef(position).getKey();

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GoToMessageActivity(listUserID, tmp);

                    }
                });


                mymessagesref.child(listUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            holder.itemView.setVisibility(View.VISIBLE);
                            mycontactsref.child(listUserID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.child("from").exists())
                                    {
                                        ContactClass chatHead = dataSnapshot.getValue(ContactClass.class);


                                        if(chatHead.getFrom().equals(listUserID))
                                        {
                                            String lastmes = tmp+": "+chatHead.getMessage();
                                            holder.lastMessage.setText(lastmes);
                                        }
                                        else
                                        {
                                            String lastmes = "You: "+chatHead.getMessage();
                                            holder.lastMessage.setText(lastmes);
                                        }
                                        if(chatHead.getSeen().equals("false"))
                                        {
                                            holder.lastMessage.setTypeface(null, Typeface.BOLD);
                                            holder.lastMessage.setTextColor(Color.BLACK);
                                        }

                                        else
                                        {
                                            holder.lastMessage.setTypeface(null, Typeface.NORMAL);
                                            holder.lastMessage.setTextColor(Color.GRAY);
                                        }

                                        StringBuilder sb = new StringBuilder(dataSnapshot.child("time").getValue().toString());
                                        sb.deleteCharAt(0);
                                        String timeString = sb.toString();
                                        Long time = Long.parseLong(timeString);
                                        Date date = new Date(time);
                                        SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                                        String messagetime = df.format(date);
                                        holder.lastTime.setText(messagetime);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else
                        {
                            holder.itemView.setVisibility(View.GONE);
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
            public Chat.ClientViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chat_disp_layout, viewGroup, false);
                Chat.ClientViewHolder viewHolder = new Chat.ClientViewHolder(view);
                return viewHolder;
            }
        };



        recyclerView.setAdapter(adapter);
        adapter.startListening();


    }


    public static class ClientViewHolder extends RecyclerView.ViewHolder
    {
        TextView name, lastMessage, lastTime;


        private ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.chat_username);
            lastMessage = itemView.findViewById(R.id.last_message_of_user);
            lastTime = itemView.findViewById(R.id.time_of_last_message);
        }
    }


    public  void GoToMessageActivity(String listUserID, String name) {
        Intent messageIntent = new Intent(getContext(), MessageActivity.class);
        messageIntent.putExtra("receiverID", listUserID);
        messageIntent.putExtra("receiverName", name);
        startActivity(messageIntent);
    }

}
