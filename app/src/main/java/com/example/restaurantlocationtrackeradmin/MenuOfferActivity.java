package com.example.restaurantlocationtrackeradmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class MenuOfferActivity extends AppCompatActivity {

    private static final int GALLERY_INTENT = 358;
    private String placeID;
    private FirebaseDatabase database;
    private DatabaseReference offerDatabase;
    private DatabaseReference menuDatabase;
    FirebaseStorage storage;
    StorageReference menuStorage;
    EditText menuNameET;
    EditText portionET;
    EditText priceET;
    EditText offerET;
    TextView errorTV;
    ProgressBar progressBar;
    private String menuID;
    Button photoBT;
    Button menuBT;
    Button offerBT;
    private String image;
    private String menuItemName;
    private String portion;
    private String price;
    private String offerText;
    private String offerID;
    private Menu inMenu;
    private Offer inOffer;
    private boolean offBool;
    private boolean menuBool;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_offer);
        offBool=false;
        menuBool=false;
        progressDialog=new ProgressDialog(this);
        menuNameET=findViewById(R.id.menuNameET);
        portionET=findViewById(R.id.portionET);
        priceET=findViewById(R.id.priceET);
        offerET=findViewById(R.id.offerET);
        errorTV=findViewById(R.id.errorTV);
        progressBar=findViewById(R.id.progressBar);
        photoBT=findViewById(R.id.menuPhotoBT);
        menuBT=findViewById(R.id.addMenuBT);
        Intent intent=getIntent();
        placeID=intent.getStringExtra("placeID");
        inMenu= (Menu) intent.getSerializableExtra("menu");
        inOffer= (Offer) intent.getSerializableExtra("off");
        database=FirebaseDatabase.getInstance();
        offerDatabase=database.getReference().child("offers").child(placeID);
        menuDatabase=database.getReference().child("menu").child(placeID);
        storage=FirebaseStorage.getInstance();
        menuStorage=storage.getReference().child("menu").child(placeID);
        offerBT=findViewById(R.id.offerBT);
        if(inMenu!=null){
            offerET.setVisibility(View.GONE);
            offerBT.setVisibility(View.GONE);
            menuID=inMenu.getMenuID();
            image=inMenu.getPhoto();
            menuItemName=inMenu.getName();
            portion=inMenu.getPortion();
            price=inMenu.getPrice();
            menuNameET.setText(inMenu.getName());
            portionET.setText(inMenu.getPortion());
            priceET.setText(inMenu.getPrice());
            menuBT.setText("Update Menu");
        }
        if(inOffer!=null){
            offerID=inOffer.getOfferId();
            photoBT.setVisibility(View.GONE);
            menuBT.setVisibility(View.GONE);
            menuNameET.setVisibility(View.GONE);
            portionET.setVisibility(View.GONE);
            priceET.setVisibility(View.GONE);
            offerText=inOffer.getOfferText();
            offerET.setText(inOffer.getOfferText());
            offerBT.setText("Update Offer");
        }
    }

    public void addMenuPhoto(View view) {
        UIUtil.hideKeyboard(MenuOfferActivity.this);
        selectImage();
    }

    private void selectImage() {
        Intent intent =new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_INTENT);
    }
    public void addMenuItem(View view) {
        UIUtil.hideKeyboard(MenuOfferActivity.this);
        errorTV.setVisibility(View.GONE);
        menuItemName=menuNameET.getText().toString();
        portion=portionET.getText().toString();
        price=priceET.getText().toString();
        if(TextUtils.isEmpty(menuItemName)||TextUtils.isEmpty(portion)||TextUtils.isEmpty(price)){
            errorTV.setText("Please Complete All The Fields");
            errorTV.setVisibility(View.VISIBLE);
        } else {
            if(image==null&&inMenu==null)
                menuID=menuDatabase.push().getKey();
            else if(inMenu!=null) {
                inMenu = null;
                menuBool=true;
            }
            progressBar.setVisibility(View.VISIBLE);

            Menu menu=new Menu(menuID,image,menuItemName,portion,price);
            menuDatabase.child(menuID).setValue(menu).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    progressBar.setVisibility(View.GONE);

                    if(task.isSuccessful()){
                        menuID=null;
                        image=null;
                        menuNameET.setText("");
                        portionET.setText("");
                        priceET.setText("");
                        if(menuBool){
                            menuBT.setText("Add Menu");
                            offerET.setVisibility(View.VISIBLE);
                            offerBT.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(MenuOfferActivity.this,"Menu Added",Toast.LENGTH_SHORT).show();
                    } else {
                        errorTV.setText(task.getException().getLocalizedMessage());
                        errorTV.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

    }

    public void addOffer(View view) {
        UIUtil.hideKeyboard(MenuOfferActivity.this);
        errorTV.setVisibility(View.GONE);
        offerText=offerET.getText().toString();
        if(TextUtils.isEmpty(offerText)){
            errorTV.setText("Enter Offer");
            errorTV.setVisibility(View.VISIBLE);
        } else {

            progressBar.setVisibility(View.VISIBLE);
            if(inOffer==null)
                offerID=offerDatabase.push().getKey();
            else {
                inOffer=null;
                offBool=true;

            }
            Offer offer=new Offer(offerID,offerText);
            offerDatabase.child(offerID).setValue(offer).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        offerET.setText("");
                        if(offBool){
                            offerBT.setText("Add Offer");
                            menuNameET.setVisibility(View.VISIBLE);
                            portionET.setVisibility(View.VISIBLE);
                            priceET.setVisibility(View.VISIBLE);
                            photoBT.setVisibility(View.VISIBLE);
                            menuBT.setVisibility(View.VISIBLE);
                        }
                        Toast.makeText(MenuOfferActivity.this,"Offer Added",Toast.LENGTH_SHORT).show();
                    } else {
                        errorTV.setText(task.getException().getLocalizedMessage());
                        errorTV.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_INTENT && resultCode== RESULT_OK){
            progressDialog.setMessage("Uploading..");
            progressDialog.show();
            progressDialog.setCancelable(false);
            photoBT.setEnabled(false);
            menuBT.setEnabled(false);
            final Uri uri=data.getData();
            Date date=new Date();
            Timestamp timestamp=new Timestamp(date.getTime());
            if(inMenu==null)
                menuID=menuDatabase.push().getKey();
            final StorageReference filePath=menuStorage.child(menuID);
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            //Do what you want with the url
                            image= String.valueOf(downloadUrl);
                            progressDialog.dismiss();
                            photoBT.setEnabled(true);
                            menuBT.setEnabled(true);
                            Toast.makeText(MenuOfferActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();

                        }

                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    errorTV.setText(e.getLocalizedMessage());
                    errorTV.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        UIUtil.hideKeyboard(MenuOfferActivity.this);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent=new Intent(MenuOfferActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
