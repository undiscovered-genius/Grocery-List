package com.example.grocery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity {
    ImageButton btnLogout;
    ImageButton scan;
    RecyclerView mFirestoreList;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth mFirebaseAuth;
    String userid;
    FirestoreRecyclerAdapter adapter;
    FirebaseAuth mAuth;
    FirebaseFirestore fstore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirestoreList = findViewById(R.id.firestoreList);
        firebaseFirestore = FirebaseFirestore.getInstance();
        btnLogout = findViewById(R.id.logout);
        scan = findViewById(R.id.scan);

        userid = mFirebaseAuth.getCurrentUser().getUid();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("LOGOUT?");
                builder.setMessage("Product Details : ");
                builder.setMessage("Want to logout ?");
                builder.setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(HomeActivity.this, loginActivity.class));
                        finish();
                    }
                }).setNegativeButton("CONTINUE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Intent intSignUp = new Intent(loginActivity.this, MainActivity.class);
                        //startActivity(intSignUp);
                    }
                });
                builder.create().show();
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(HomeActivity.this, Scanner.class));
                final EditText addItem, itemName;
                addItem = new EditText(HomeActivity.this);
                itemName = new EditText(HomeActivity.this);
                addItem.getPaddingLeft();
                itemName.getPaddingLeft();
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("ADD ITEM");
                builder.setMessage("Product Details : ");
                builder.setView(itemName);
                builder.setView(addItem);
                Context context = getApplicationContext();
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

                    }
                });
                builder.setNeutralButton("ADD", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        userid = mFirebaseAuth.getCurrentUser().getUid();
                        final String item ;
                        item = addItem.getText().toString();
                        final String name ;
                        name = itemName.getText().toString();
                        final DocumentReference documentReference= firebaseFirestore.collection(userid).document(name);
                        Map<String,Object> user = new HashMap<>();
                        user.put("name",name);
                        user.put("quantity",item);
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(HomeActivity.this, "ADDED successfully, Thank You!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(HomeActivity.this," Unsuccessful, Please Try Again!" , Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        Query query = firebaseFirestore.collection(userid); //query
        //recycler option
        FirestoreRecyclerOptions<ProductsModel> options= new FirestoreRecyclerOptions.Builder<ProductsModel>().setQuery(query, ProductsModel.class).build();
         adapter = new FirestoreRecyclerAdapter<ProductsModel, ProductViewHolder>(options) {
            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single, parent, false);
                return new ProductViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull ProductsModel model) {
                holder.list_name.setText(model.getName());
                holder.list_quantity.setText(model.getQuantity());
            }
        };
        mFirestoreList.setHasFixedSize(true);
        mFirestoreList.setLayoutManager(new LinearLayoutManager(this));
        mFirestoreList.setAdapter(adapter);
//        onStart();
//        adapter.startListening();
    }
    @Override
    protected void onStart(){
        super.onStart();
        adapter.startListening();
    }
    //view holder
    public class ProductViewHolder extends RecyclerView.ViewHolder{
        private TextView list_name;
        private TextView list_quantity;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            list_name = itemView.findViewById(R.id.list_name);
            list_quantity = itemView.findViewById(R.id.list_quantity);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        adapter.stopListening();
    }
}
