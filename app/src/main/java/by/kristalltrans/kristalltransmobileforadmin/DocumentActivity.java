package by.kristalltrans.kristalltransmobileforadmin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DocumentActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 0;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    final ArrayList<Bitmap> bitmap = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                    startActivity(new Intent(DocumentActivity.this, EmailPasswordActivity.class));
            }
        };

        final TouchImageView touchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        progressBar.setVisibility(View.VISIBLE);
        findViewById(R.id.progressBar2).setVisibility(View.GONE);
        findViewById(R.id.save).setVisibility(View.INVISIBLE);
        findViewById(R.id.save).setOnClickListener(this);

        Glide
                .with(DocumentActivity.this) // could be an issue!
                .load(getIntent().getExtras().getString("photoUrl"))
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bm, GlideAnimation glideAnimation) {
                        progressBar.setVisibility(View.GONE);
                        findViewById(R.id.save).setVisibility(View.VISIBLE);
                        touchImageView.setImageBitmap(bm);
                        bitmap.clear();
                        bitmap.add(bm);
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


    public boolean saveBitmapToFile(File dir, String fileName, Bitmap bm,
                                    Bitmap.CompressFormat format, int quality) {

        File imageFile = new File(dir, fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(imageFile);

            bm.compress(format, quality, fos);

            fos.close();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progressBar2).setVisibility(View.GONE);
                    findViewById(R.id.save).setClickable(true);
                    Toast.makeText(getApplicationContext(), "Документ сохранен: KistallTrans Mobile/Documents",
                            Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } catch (IOException e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.progressBar2).setVisibility(View.GONE);
                    findViewById(R.id.save).setClickable(true);
                    Toast.makeText(getApplicationContext(), "Ошибка!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        return false;
    }

    class SaveInBackground extends AsyncTask<Boolean, String, String> {
        @Override
        protected String doInBackground(Boolean... booleen) {
            File dir = new File(Environment.getExternalStorageDirectory() + File.separator +
                    "KristallTrans Mobile" + File.separator +
                    "Documents" + File.separator +
                    getIntent().getExtras().getString("name") + File.separator +
                    getIntent().getExtras().getString("date").substring(0, 10)
            );

            boolean doSave = true;
            if (!dir.exists())
                doSave = dir.mkdirs();
            if (doSave && bitmap.size() > 0) {
                saveBitmapToFile(dir, getIntent().getExtras().getString("date") + '_' + getIntent().getExtras().getString("name") + ".jpg"
                        , bitmap.get(0), Bitmap.CompressFormat.JPEG, 100);
            }
            return null;
        }
    }

    @Override
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(DocumentActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DocumentActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
            return;
        }
        findViewById(R.id.progressBar2).setVisibility(View.VISIBLE);
        findViewById(R.id.save).setClickable(false);
        new SaveInBackground().execute(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION && grantResults.length == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onClick(findViewById(R.id.save));
            } else {
                Toast.makeText(DocumentActivity.this, "Для сохранения документа требуется разрешение на запись файлов на Вашем устройстве.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
