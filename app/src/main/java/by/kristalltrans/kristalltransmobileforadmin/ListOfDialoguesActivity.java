package by.kristalltrans.kristalltransmobileforadmin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ListOfDialoguesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_dialogues);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                    startActivity(new Intent(ListOfDialoguesActivity.this, EmailPasswordActivity.class));
            }
        };

        ListView listView = (ListView) findViewById(R.id.listView);

        final ArrayList<HashMap<String, String>> users = new ArrayList<>();
        final ArrayList<String> keys = new ArrayList<>();

        final SimpleAdapter adapter = new SimpleAdapter(this, users, R.layout.dialogs_user, new String[]{"name", "email"}, new int[]{R.id.nameUserTextView, R.id.emailUserTextView});

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position,
                                    long id) {
                Intent intent = new Intent(ListOfDialoguesActivity.this, DialogActivity.class);
                TextView textView = (TextView) itemClicked.findViewById(R.id.emailUserTextView);
                intent.putExtra("user", textView.getText().toString());
                startActivity(intent);
            }
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("listOfUsers");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                HashMap<String, HashMap<String, String>> value = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                keys.clear();
                for (int i = 0; i < value.size(); i++)
                    keys.add(value.keySet().toArray()[i].toString());
                users.clear();
                for (int i = 0; i < keys.size(); i++)
                    users.add(value.get(keys.get(i)));
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("LOL", "Failed to read value.", error.toException());
            }
        });


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
