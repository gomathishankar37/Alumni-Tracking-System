package com.example.ats.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ats.Adapter.MypotoAdapter;
import com.example.ats.EdProfileActivity;
import com.example.ats.FollowersActivity;
import com.example.ats.Model.Comment;
import com.example.ats.Model.Post;
import com.example.ats.Model.User;
import com.example.ats.OptionActivity;
import com.example.ats.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileFragment extends Fragment {
    ImageView image_profile,options;
    TextView posts , followers, following, fullname, bio, username;
    Button edit_profile;
    RecyclerView myrecyclerview;
    MypotoAdapter mypotoAdapter;
    List<Post> postlists;

    private List<String> mysaves;
    RecyclerView myRecyclerview_save;
    MypotoAdapter mypotoAdapter_saves;
    List<Post>postList_saves;

    FirebaseUser firebaseUser;
    String profileid;

    ImageButton my_fotos,saved_fotos;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile,container,false);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        SharedPreferences preferences = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        profileid = preferences.getString("profileid","none");
        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        following = view.findViewById(R.id.following);
        followers = view.findViewById(R.id.followers);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        edit_profile = view.findViewById(R.id.edit_profile);
        my_fotos = view.findViewById(R.id.my_potos);
        saved_fotos = view.findViewById(R.id.saved_potos);
        myrecyclerview = view.findViewById(R.id.recycler_view);
        myrecyclerview.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(),3);
        myrecyclerview.setLayoutManager(linearLayoutManager);
        postlists = new ArrayList<>();
        mypotoAdapter = new MypotoAdapter(getContext(),postlists);
        myrecyclerview.setAdapter(mypotoAdapter);
        myRecyclerview_save = view.findViewById(R.id.recycler_view1);
        myRecyclerview_save.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new GridLayoutManager(getContext(),3);
        myRecyclerview_save.setLayoutManager(linearLayoutManager1);
        postList_saves = new ArrayList<>();
        mypotoAdapter_saves = new MypotoAdapter(getContext(),postList_saves);
        myRecyclerview_save.setAdapter(mypotoAdapter_saves);
        myrecyclerview.setVisibility(View.VISIBLE);
        myRecyclerview_save.setVisibility(View.GONE);

        userInfo();
        getfollowers();
        getnoposts();
        myPosts();
        mysaves();
        if (profileid.equals(firebaseUser.getUid())) {
            edit_profile.setText("Edit Profile");
        } else {
            checkfollow();
            saved_fotos.setVisibility(View.GONE);
        }


        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String btn = edit_profile.getText().toString();

                if(btn.equals("Edit Profile")){
                    startActivity(new Intent(getContext(), EdProfileActivity.class));

                }else if(btn.equals("follow")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                    addnotification();
                }else if(btn.equals("following")){
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }else {

                }
            }
        });
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OptionActivity.class);
                startActivity(intent);
            }
        });
        my_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myrecyclerview.setVisibility(View.VISIBLE);
                myRecyclerview_save.setVisibility(View.GONE);
            }
        });
        saved_fotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myrecyclerview.setVisibility(View.GONE);
                myRecyclerview_save.setVisibility(View.VISIBLE);

            }
        });
        followers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id",profileid);
                intent.putExtra("title","followers");
                startActivity(intent);
            }
        });
        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id",profileid);
                intent.putExtra("title","following");
                startActivity(intent);
            }
        });


        return view;
    }
    private void addnotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events").child(profileid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }
    private void userInfo(){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (getContext() == null) {
                        return;
                    }
                    User user = dataSnapshot.getValue(User.class);
                    try {
                        Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
                        username.setText(user.getUsername());
                        fullname.setText(user.getFullname());
                        bio.setText(user.getBio());
                }catch (NullPointerException e){}}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }
    private void checkfollow(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(profileid).exists()){
                    edit_profile.setText("following");
                }else{
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getfollowers(){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child("Follow").child(profileid).child("followers");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    followers.setText("" + dataSnapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                    .child("Follow").child(profileid).child("following");
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    following.setText("" + dataSnapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });}

    private void getnoposts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileid)){
                        i++;
                    }
                }
                    posts.setText(""+i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void myPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postlists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    if(post.getPublisher().equals(profileid)){
                        postlists.add(post);
                    }
                }
                Collections.reverse(postlists);
                mypotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void mysaves(){
        mysaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mysaves.add(snapshot.getKey());
                }
                readsaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        }
        private void readsaves(){
       DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               postList_saves.clear();
               for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                   Post post = snapshot.getValue(Post.class);
                   for (String id : mysaves){
                       if(post.getPostid().equals(id)){
                           postList_saves.add(post);
                       }
                   }
               }
               mypotoAdapter_saves.notifyDataSetChanged();
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }
}
