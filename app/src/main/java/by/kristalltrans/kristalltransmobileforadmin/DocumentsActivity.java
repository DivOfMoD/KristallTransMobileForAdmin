package by.kristalltrans.kristalltransmobileforadmin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DocumentsActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Document, FirechatMsgViewHolder>
            mFirebaseAdapter;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public static class FirechatMsgViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView nameUserTextView;
        ImageView docImageView;

        public FirechatMsgViewHolder(View v) {
            super(v);
            dateTextView = (TextView) itemView.findViewById(R.id.dateUserTextView);
            nameUserTextView = (TextView) itemView.findViewById(R.id.nameUserTextView);
            docImageView = (ImageView) itemView.findViewById(R.id.docImageView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documents);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                    startActivity(new Intent(DocumentsActivity.this, EmailPasswordActivity.class));
            }
        };

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        DatabaseReference mSimpleFirechatDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Document,
                FirechatMsgViewHolder>(
                Document.class,
                R.layout.document,
                FirechatMsgViewHolder.class,
                mSimpleFirechatDatabaseReference.child("documents")) {

            @Override
            protected void populateViewHolder(FirechatMsgViewHolder viewHolder, final Document document, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.dateTextView.setText(document.getDate());
                viewHolder.nameUserTextView.setText(document.getName());
                viewHolder.docImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(DocumentsActivity.this, DocumentActivity.class)
                                .putExtra("photoUrl", document.getPhotoUrl())
                                .putExtra("date", document.getDate())
                                .putExtra("name", document.getName()));
                    }
                });
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int chatMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (chatMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
