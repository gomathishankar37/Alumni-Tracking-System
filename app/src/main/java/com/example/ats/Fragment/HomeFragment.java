package com.example.ats.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.ats.Adapter.MessageActivity;
import com.example.ats.Adapter.PostAdapter;
import com.example.ats.Beta;
import com.example.ats.Model.Post;
import com.example.ats.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> pubLists;
    ProgressBar progressBar;
    private  ImageView message;


    private List<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        message = view.findViewById(R.id.send);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), Beta.class));
            }
        });

        recyclerView = view.findViewById(R.id.bbb);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        pubLists = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(),pubLists);
        recyclerView.setAdapter(postAdapter);
        progressBar = view.findViewById(R.id.progress_circular);

        checkfollowing();
        return view;
}
private void checkfollowing(){
        followingList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }
                readposts();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

}
private  void readposts(){
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
    reference.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            pubLists.clear();
            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                Post post = snapshot.getValue(Post.class);
                for(String id : followingList){
                    if(post.getPublisher().equals(id)){
                        pubLists.add(post);
                    }
                }
            }
            postAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });
}
}

