package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class Scanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView zXingScannerView ;
    private static final int REQUEST_CAMERA = 1;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;
    String userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        mAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        userid = mAuth.getCurrentUser().getUid();

        zXingScannerView = new ZXingScannerView(this);
        if(checkPermission())
        {
            Toast.makeText(Scanner.this, "PERMISSION is granted!", Toast.LENGTH_LONG).show();
        }
        else
        {
            requestPermission();
        }
        zXingScannerView  =new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
    private boolean checkPermission()
    {
        return (ContextCompat.checkSelfPermission(Scanner.this, CAMERA) == PackageManager.PERMISSION_GRANTED);
    }
    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }
//    public void scan(View view){
//
//    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        final EditText addItem, itemName;
        addItem = new EditText(zXingScannerView.getContext());
        itemName = new EditText(zXingScannerView.getContext());
        addItem.getPaddingLeft();
        itemName.getPaddingLeft();

        final String qrcopy = scanResult;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ADD ITEM");
        builder.setMessage("Product ID : "+scanResult);
        builder.setView(itemName);
        builder.setView(addItem);

        Context context = zXingScannerView.getContext();
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

// Add a TextView here for the "Title" label, as noted in the comments
        itemName.setHint(" Product Name");
        layout.addView(itemName); // Notice this is an add method

// Add another TextView here for the "Description" label
        addItem.setHint(" Enter Quantity");
        layout.addView(addItem); // Another add method

        builder.setView(layout);

        builder.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                zXingScannerView.resumeCameraPreview(Scanner.this);
            }
        });
        builder.setNeutralButton("ADD", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userid = mAuth.getCurrentUser().getUid();
                final String item ;
                item = addItem.getText().toString();
                final String name ;
                name = itemName.getText().toString();
                final DocumentReference documentReference= fstore.collection(userid).document(qrcopy);
                Map<String,Object> user = new HashMap<>();
                user.put("Product Name  ",name);
                user.put("Quantity  ",item);
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Scanner.this, "ADDED successfully, Thank You!", Toast.LENGTH_SHORT).show();
                        zXingScannerView.resumeCameraPreview(Scanner.this);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Scanner.this," Unsuccessful, Please Try Again!" , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}
