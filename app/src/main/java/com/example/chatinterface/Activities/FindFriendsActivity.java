package com.example.chatinterface.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.example.chatinterface.R;
import com.example.chatinterface.model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView recyclerList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerList=findViewById(R.id.find_friends_recyclerlist);
        recyclerList.setLayoutManager(new LinearLayoutManager(FindFriendsActivity.this));



        mToolbar=findViewById(R.id.find_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");



    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options= new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(usersRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(R.drawable.profile_image).into(holder.profileImage);


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();
                                Intent profileIntent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileIntent);

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        FindFriendViewHolder viewHolder= new FindFriendViewHolder(view);
                        return viewHolder;

                    }
                };

        recyclerList.setAdapter(adapter);
        adapter.startListening();



    }



    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {

        TextView userName,userStatus;
        CircleImageView profileImage;



        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName= itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profileImage=itemView.findViewById(R.id.users_profile_image);

        }
    }


}
