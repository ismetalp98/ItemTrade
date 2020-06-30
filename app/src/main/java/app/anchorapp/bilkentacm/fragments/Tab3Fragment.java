package app.anchorapp.bilkentacm.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executor;

import app.anchorapp.bilkentacm.R;
import app.anchorapp.bilkentacm.models.User;
import io.grpc.Context;

public class Tab3Fragment extends Fragment {

    public static boolean control;
    ImageView profileimage;
    TextView name,mail,phonenumber;
    TextView warning,notification;
    String nametoedit,lastnametoedit;
    Button change,edit;
    FirebaseAuth fauth;
    ImageButton btnaddphonenumber;
    Button btnverifiy;
    FirebaseFirestore fStore;
    ImageButton profilepicbtn;
    StorageReference storageReference;
    FirebaseUser fuser;
    public static final int GALLERY_REQUEST_CODE = 105;


    public Tab3Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tab3, container, false);

        ////// Indicators //////////
        profileimage = view.findViewById(R.id.profileimage);
        name = view.findViewById(R.id.tw_name);
        mail = view.findViewById(R.id.tw_mail);
        btnaddphonenumber = view.findViewById(R.id.btn_add_phonenumber);
        phonenumber = view.findViewById(R.id.tw_phone);
        change = view.findViewById(R.id.btnchangepss);
        edit = view.findViewById(R.id.btneditprofile);
        profilepicbtn = view.findViewById(R.id.propic);
        warning = view.findViewById(R.id.tv_warning);
        notification = view.findViewById(R.id.tv_notification);
        btnverifiy = view.findViewById(R.id.btn_verify);
        fauth = FirebaseAuth.getInstance();
        fuser = fauth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        ////// Fletch data from database//////////
        final DocumentReference documentReference = fStore.collection("Users").document(fuser.getUid());
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                nametoedit = documentSnapshot.getString("name");
                lastnametoedit = documentSnapshot.getString("lastname");
                name.setText(nametoedit + " " + lastnametoedit);
                mail.setText(fuser.getEmail());
                btnaddphonenumber.setVisibility(View.VISIBLE);
                btnaddphonenumber.setClickable(true);
                phonenumber.setText(documentSnapshot.getString("phonenumber"));
            }
        });


            StorageReference profileRef = storageReference.child("users/" + fuser.getUid() + "/profile.jpg");

            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profileimage);
                }
            });
            System.out.println(control);

        /*if (control)
        {
            warning.setVisibility(View.INVISIBLE);
            notification.setVisibility(View.INVISIBLE);
            btnverifiy.setVisibility(View.INVISIBLE);
            btnverifiy.setClickable(false);
        }*/


        btnverifiy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fuser.sendEmailVerification();
            }
        });


        ////// Button Listeners//////////
        profilepicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openfromgallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openfromgallery, GALLERY_REQUEST_CODE);
            }
        });

        btnaddphonenumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText newphonenumber = new EditText(view.getContext());
                final AlertDialog.Builder phonenumberdialog = new AlertDialog.Builder(view.getContext());
                phonenumberdialog.setTitle("Phone Number");
                phonenumberdialog.setView(newphonenumber);
                phonenumberdialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String phone_number = newphonenumber.getText().toString();
                        DocumentReference documentReference1 = fStore.collection("Users").document(fuser.getUid());
                        HashMap<String, Object> edited = new HashMap<>();
                        edited.put("name", nametoedit);
                        edited.put("lastname", lastnametoedit);
                        edited.put("email", mail.getText().toString());
                        edited.put("phonenumber", phone_number);
                        documentReference.update(edited);
                    }
                });
                phonenumberdialog.show();
            }
        });

        return view;
    }


    @SuppressLint("MissingSuperCall")
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final Uri contentUri = data.getData();
                profileimage.setImageURI((contentUri));
                final StorageReference str = storageReference.child("users/"+fuser.getUid()+"/profile.jpg");
                str.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        str.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(profileimage);
                            }
                        });
                    }
                });
            }
        }
    }
}
