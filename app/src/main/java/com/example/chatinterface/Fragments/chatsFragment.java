package com.example.chatinterface.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.chatinterface.Activities.ChatActivity;
import com.example.chatinterface.Activities.ContactsActivity;
import com.example.chatinterface.Activities.ProfileActivity;
import com.example.chatinterface.model.Contacts;


import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatinterface.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class chatsFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView private_chat_list;
    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String CurrentUserId;
    private final String TAG = "chat_fragment";
    private long lastSeenInMilliseconds;
    private String userLastSeen;
    private FloatingActionButton fab;


    public chatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);


        mAuth = FirebaseAuth.getInstance();
        CurrentUserId = mAuth.getCurrentUser().getUid();
        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(CurrentUserId);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        private_chat_list = PrivateChatsView.findViewById(R.id.chats_list);
        private_chat_list.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);

        private_chat_list.addItemDecoration(dividerItemDecoration);

        fab = PrivateChatsView.findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ContactsIntent = new Intent(getContext(), ContactsActivity.class);
                startActivity(ContactsIntent);

            }
        });


        return PrivateChatsView;
    }

    @Override
    public void onStart() {

        super.onStart();

        //progressBar.setVisibility(View.VISIBLE);
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override


            protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String user_Ids = getRef(position).getKey();
                final String[] ret_Img = {"default_image"};


                usersRef.child(user_Ids).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            if (dataSnapshot.hasChild("image")) {
                                // progressBar.setVisibility(View.INVISIBLE);

                                ret_Img[0] = dataSnapshot.child("image").getValue().toString();
                                Log.d("img", ret_Img[0]);
                                Picasso.get().load(ret_Img[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);


                            }

                            final String retName = dataSnapshot.child("name").getValue().toString();
                            final String retStatus = dataSnapshot.child("status").getValue().toString();
                            holder.userName.setText(retName);


                            if (dataSnapshot.child("UserState").hasChild("state")) {
                                String state = dataSnapshot.child("UserState").child("state").getValue().toString();
                                String date = dataSnapshot.child("UserState").child("date").getValue().toString();
                                String time = dataSnapshot.child("UserState").child("time").getValue().toString();

                                try {
                                    Log.d("Last Seen Date: ", date);
                                    Log.d("Last Seen Time", time);

                                    userLastSeen = getFormattedLastSeen(date, time);


                                    Log.d("user", userLastSeen);
//
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                                if (state.equals("online")) {
                                    holder.userStatus.setText("online");
                                } else if (state.equals("offline")) {
                                    if (!TextUtils.isEmpty(userLastSeen)) {
                                        holder.userStatus.setText(userLastSeen);
                                    } else {
                                        holder.userStatus.setText(" Last seen: " + date + " " + time);
                                        Log.d(TAG, "User Formatted Last Seen Not Available!");
                                    }

                                }

                            } else {

                                holder.userStatus.setText("offline");


                            }


                            holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());


                                    LayoutInflater inflater = getLayoutInflater();
                                    final View dialogLayout = inflater.inflate(R.layout.custom_dialog_layout, null);
                                    builder.setView(dialogLayout);
                                    // FrameLayout frameLayout = dialogLayout.findViewById(R.id.frame_image);
                                    TextView textView = dialogLayout.findViewById(R.id.user_name);
                                    ImageView imageView = dialogLayout.findViewById(R.id.photo);
                                    ImageButton message = dialogLayout.findViewById(R.id.message);
                                    ImageButton info = dialogLayout.findViewById(R.id.info);

                                    textView.setText("  " + retName);
                                    Picasso.get().load(ret_Img[0]).placeholder(R.drawable.profile_image).into(imageView);
                                    message.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", user_Ids);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_user_image", ret_Img[0]);
                                            Log.d("img", ret_Img[0]);

                                            startActivity(chatIntent);

                                        }
                                    });

                                    info.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("visit_user_id", user_Ids);
                                            startActivity(profileIntent);

                                        }
                                    });


                                    builder.setCancelable(true);


                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                    dialog.getWindow().setLayout(800, 920);

                                }
                            });


                            holder.user_info_layout.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", user_Ids);
                                    chatIntent.putExtra("visit_user_name", retName);
                                    chatIntent.putExtra("visit_user_image", ret_Img[0]);
                                    Log.d("img", ret_Img[0]);

                                    startActivity(chatIntent);


                                }
                            });

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }


            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                return new ChatsViewHolder(view);
            }
        };


        private_chat_list.setAdapter(adapter);
        adapter.startListening();


    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userStatus, userName;
        LinearLayout user_info_layout;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            user_info_layout = itemView.findViewById(R.id.user_info_layout);


        }
    }


    private String getFormattedLastSeen(String date, String time){

        int dd,MM,yyyy;
        if(date.length() == 17)
        {
            MM = getMonth(date.substring(5, 8));
            dd = Integer.parseInt(date.substring(9,11));
            yyyy = Integer.parseInt(date.substring(13))-1900;
        }
        else
        {
            MM = getMonth(date.substring(5, 8));
            dd = Integer.parseInt(date.substring(9,10));
            yyyy = Integer.parseInt(date.substring(12));
        }
        Date date1 = new Date(yyyy,MM,dd);
        Log.d("date1", String.valueOf(date1));
        Calendar now = Calendar.getInstance();
        Date date2 = new Date(now.getTimeInMillis());
        Log.d("date2", String.valueOf(date2));


        if (date2.getDate()==date1.getDate()) {
            return "Today ";
        } else if (date2.getDate() - date1.getDate() == 1) {
            return "Yesterday ";
        } else {
            return  DateFormat.getDateInstance().format(date1);
        }
    }

    private int getMonth(String s) {
        int result = -1;
        switch (s)
        {
            case "Jan" : result = 0;
                break;
            case "Feb" : result = 1;
                break;
            case "Mar" : result = 2;
                break;
            case "Apr" : result = 3;
                break;
            case "May" : result = 4;
                break;
            case "Jun" : result = 5;
                break;
            case "Jul" : result = 6;
                break;
            case "Aug" : result = 7;
                break;
            case "Sep" : result = 8;
                break;
            case "Oct" : result = 9;
                break;
            case "Nov" : result =10;
                break;
            case "Dec" : result =11;
                break;
        }
        return result;
    }


}




   /* private String getFormattedLastSeen(String date, String time) throws ParseException {
        String pattern = "E, MMM dd, yyyy hh:mm a ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String temp = date + " " + time;

        Log.d("temp", temp);

        Date date1 = simpleDateFormat.parse(temp);
        Log.d("date1", String.valueOf(date1));
        Calendar now = Calendar.getInstance();
        Date date2 = now.getTime();
        Log.d("date2", String.valueOf(date2));


        if (date1.getDate() == date2.getDate()) {
            return "Today " +  DateFormat.getTimeInstance(DateFormat.SHORT).format(date1);
        } else if (date2.getDate() - date1.getDate() == 1) {
            return "Yesterday " + DateFormat.getTimeInstance(DateFormat.SHORT).format(date1);
        } else {
            return  DateFormat.getDateInstance().format(date1);
        }
    }*/




