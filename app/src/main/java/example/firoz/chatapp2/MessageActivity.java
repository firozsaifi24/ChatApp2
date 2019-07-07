package example.firoz.chatapp2;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import example.firoz.chatapp2.Adapter.MediaAdapter;
import example.firoz.chatapp2.Adapter.MessageAdapter;
import example.firoz.chatapp2.Fragments.APIService;
import example.firoz.chatapp2.Interfaces.ImageCloseCallback;
import example.firoz.chatapp2.Model.Chat;
import example.firoz.chatapp2.Model.User;
import example.firoz.chatapp2.Notifications.Client;
import example.firoz.chatapp2.Notifications.Data;
import example.firoz.chatapp2.Notifications.MyResponse;
import example.firoz.chatapp2.Notifications.Sender;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements ImageCloseCallback {

    View rootView;
    TextView username;
    TextView status;
    CircleImageView profile_image;

    FirebaseUser fUser;
    DatabaseReference reference;

    ImageView btn_send;
    //EditText txt_send;
    EmojiconEditText txt_send;
    ImageView btn_attachment;
    ImageView btn_emoji;
    EmojIconActions emojIcon;
    RecyclerView recycler_mediaList;
    MediaAdapter mediaAdapter;
    int PICK_IMAGE_INTENT=1;
    ArrayList<String> mediaUriList;
    StorageReference storageReference;
    int totalMediaUpload=0;
    Boolean onlyImage=false;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;

    Intent intent;

    String userid;

    ValueEventListener seenListener;

    APIService apiService;

    boolean notify= false;

    DatabaseReference onlineRef;  //for device connectivity with firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar= findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                //change after adding status functionality
                Intent intent= new Intent(MessageActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //for sending notification
        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        //for device connectivity with firebase
        onlineRef= FirebaseDatabase.getInstance().getReference().child(".info/connected");

        username= findViewById(R.id.username);
        status= findViewById(R.id.status);
        profile_image= findViewById(R.id.profile_image);
        recyclerView= findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        rootView= findViewById(R.id.root_activity_message);
        btn_send= findViewById(R.id.btn_send);
        txt_send= findViewById(R.id.txt_send);
        btn_attachment= findViewById(R.id.btn_attachment);
        btn_emoji= findViewById(R.id.btn_emoji);

        emojIcon= new EmojIconActions(getApplicationContext(), rootView, txt_send, btn_emoji);
        emojIcon.ShowEmojIcon();

        initializeMediaRecycler();

        intent= getIntent();
        userid= intent.getStringExtra("userid");

        fUser= FirebaseAuth.getInstance().getCurrentUser();
        storageReference= FirebaseStorage.getInstance().getReference("chats");

        setupDeviceConnectivityWithFirebase();

        txt_send.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(charSequence.toString().trim().equals("") || TextUtils.isEmpty(charSequence.toString().trim()))
                {
                    status("online");
                }
                else
                {
                    status("Typing...");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify=true;
                String msg= txt_send.getText().toString().trim();

                sendMessage(fUser.getUid(), userid, msg);

            }
        });

        btn_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Button Clicked", Toast.LENGTH_LONG).show();
                openGallery();
            }
        });


        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user= dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getStatus().equalsIgnoreCase("online"))
                {
                    status.setText(user.getStatus());
                    status.setVisibility(View.VISIBLE);
                }
                else
                if(user.getStatus().equalsIgnoreCase("Typing..."))
                {
                    status.setText(user.getStatus());
                    status.setVisibility(View.VISIBLE);
                }
                else
                {
                    status.setText("");
                    status.setVisibility(View.GONE);
                }

                if(user.getImageURL().equals("default"))
                {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else
                {
                    //change this to get application context instead of this context
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }

                readMessages(fUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);
    }

    private void openGallery() {
        Intent intent2= new Intent();
        intent2.setType("image/*");
        //allow multiple selection;
        intent2.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent2.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent2, "Select Pictures"), PICK_IMAGE_INTENT);
    }

    //set status of current user to offline when disconnects after 2 minutes
    private void setupDeviceConnectivityWithFirebase() {

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean connected= dataSnapshot.getValue(Boolean.class);
                if(connected)
                {
                    /*DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                    Log.d("Connection Info", dataSnapshot.toString());
                    Log.d("Reference", presenceRef.toString());
                    HashMap<String, Object> hashMap= new HashMap<>();
                    hashMap.put("status", "offline");
                    // Write a string when this client loses connection
                    presenceRef.onDisconnect().updateChildren(hashMap);*/

                    // When I disconnect, update the last time I was seen online
                    //lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                }
                else
                {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void seenMessage(final String userid)
    {
        reference= FirebaseDatabase.getInstance().getReference("Chats");
        seenListener= reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Chat chat= snapshot.getValue(Chat.class);

                    String receiver1= chat.getReceiver();
                    String sender1=chat.getSender();
                    String currentUser1= fUser.getUid();
                    String userid1= userid;

                    Log.d("Receiver1", receiver1);
                    Log.d("Sender1", sender1);
                    Log.d("CurrentUser1", currentUser1);
                    Log.d("OtherUser1", userid1);

                    if(receiver1.equalsIgnoreCase(currentUser1) && sender1.equalsIgnoreCase(userid1))
                    {
                        HashMap<String, Object> hashMap= new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                        Log.d("IsSeen", "seen");
                    }

                    /*if(chat.getReceiver().equals(fUser.getUid()) && chat.getSender().equals(userid));
                    {
                        Log.d("Receiver", chat.getReceiver());
                        Log.d("Sender", chat.getSender());
                        Log.d("Current User", fUser.getUid());
                        Log.d("Other User", userid);
                        *//*HashMap<String, Object> hashMap= new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);*//*
                        Log.d("Seen", "seen");
                    }*/
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(final String sender, final String receiver, final String message)
    {
        //send message
        if(!onlyImage)
        {
            if(!message.equals(""))
            {
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference();
                HashMap<String, Object> hashMap= new HashMap<>();
                hashMap.put("sender", sender);
                hashMap.put("receiver", receiver);
                hashMap.put("message", message);
                hashMap.put("messageTime", new Date().getTime());
                hashMap.put("isseen", false);
                hashMap.put("ismedia", false);

                reference.child("Chats").push().setValue(hashMap);

                txt_send.setText("");

                initializeNotification(sender, receiver, message);

            }
            else
            {
                Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_LONG).show();
                Log.d("Returning", "returning");
                //using return to stop the executing of the rest code
                return;
            }

        }
        else
        {

            //media starts here
            if(!mediaUriList.isEmpty())
            {
                txt_send.setText("");
                final String messageForImage="Photo...";
                final DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
                final Map<String, Object> hashMap= new HashMap<>();
                hashMap.put("sender", sender);
                hashMap.put("receiver", receiver);
                hashMap.put("message", messageForImage);
                hashMap.put("messageTime", new Date().getTime());
                hashMap.put("isseen", false);
                hashMap.put("ismedia", true);

                //ref.child("Chats").push().child("media");  //
                //under testing
                //hashMap.put("/media/media", "hi");

                //create a unique id in chat and return that id
                //final String refid= ref.child("Chats").push().getKey();

                //ref.child("Chats").child(refid).setValue(hashMap);
                //ref.child("Chats").push().setValue(hashMap);

                //final HashMap<String, Object> hashMapMedia= new HashMap<>();



                Log.d("Total media", String.valueOf(mediaUriList.size()));
                for(int counter = 0; counter < mediaUriList.size(); counter++)
                {
                    //create a unique id in chat/media and return that id for storing image url
                    final String mediaId= FirebaseDatabase.getInstance().getReference("Chats").child("media").push().getKey();
                    Log.d("Media ID", mediaId);
                    final StorageReference filePath= storageReference.child(System.currentTimeMillis()
                            +"."+getFileExtension(Uri.parse(mediaUriList.get(counter))));

                    Log.d("Storage Path", filePath.toString());
                    Log.d("Current Index", String.valueOf(totalMediaUpload));

                    final ImageView img_close= recycler_mediaList.findViewHolderForAdapterPosition(counter).itemView.findViewById(R.id.img_close);
                    final CircularProgressBar progressBar= recycler_mediaList.findViewHolderForAdapterPosition(counter).itemView.findViewById(R.id.progress_bar);
                    progressBar.setProgress(0f);
                    img_close.setVisibility(View.GONE);


                    Log.d("Upload Task Bef", "Upload Task");
                    //upload file to the above path
                    UploadTask uploadTask= filePath.putFile(Uri.parse(mediaUriList.get(counter)));

                    Log.d("Upload Task Aft", "Upload Task");

                    //now save url link to database
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("Progress", taskSnapshot.toString());
                            Log.d("Upload Task Run", "Upload Task Run");
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            float progressInFloatType = (float)progress;  //progress value in float type
                            progressBar.setProgress(progressInFloatType);
                            Log.d("Progress Real ", String.valueOf(progress)+ "% done");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.d("Uploaded", "Uploaded");
                            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Log.d("Download Path", uri.toString());
                                    hashMap.put("/media/"+mediaId+"/", uri.toString());

                                    totalMediaUpload++;

                                    Log.d("Total Media up", String.valueOf(totalMediaUpload));
                                    if(totalMediaUpload==mediaUriList.size())
                                    {
                                        //updateDatabaseWithNewMessage(hashMap);
                                        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
                                        ref.child("Chats").push().updateChildren(hashMap);
                                        mediaUriList.clear();
                                        totalMediaUpload=0;
                                        onlyImage=false;
                                        //progressBar.setProgress(0f);
                                        mediaAdapter.notifyDataSetChanged();
                                        recycler_mediaList.removeAllViews();
                                        Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_LONG).show();

                                        initializeNotification(sender, receiver, messageForImage);
                                    }

                                }
                            });
                        }
                    });

                }
            }

            //media ends here
        }

        Log.d("Return going", "Return going forward");

    }

    private void initializeNotification(final String sender, final String receiver, final String message) {


        //current user who is sending
        //add user to chat fragment
        final DatabaseReference chatRef= FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fUser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //other user who is receiving
        //add user to chat fragment
        final DatabaseReference chatRef2= FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fUser.getUid());

        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    chatRef2.child("id").setValue(fUser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //firebase notification
        final String msg= message;
        reference= FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user= dataSnapshot.getValue(User.class);
                if(notify)
                {
                    Log.d("Sending Noti", "Sending notifications");
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify= false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateDatabaseWithNewMessage(Map<String, Object> hashMap) {

/*        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference();
        ref.child("Chats").push().updateChildren(hashMap);
        mediaUriList.clear();
        totalMediaUpload=0;
        onlyImage=false;
        //progressBar.setProgress(0f);
        mediaAdapter.notifyDataSetChanged();
        recycler_mediaList.removeAllViews();
        Toast.makeText(getApplicationContext(), "Image uploaded", Toast.LENGTH_LONG).show();*/

    }

    private void sendNotification(final String receiver, final String username, final String message)
    {
        DatabaseReference tokens= FirebaseDatabase.getInstance().getReference("Tokens");
        Query query= tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Log.d("DataSnapshot", snapshot.toString());
                    //Token token= snapshot.getValue(Token.class);
                    String tokenKey = snapshot.getKey();
                    String tokenValue= (String)snapshot.getValue();
                    Log.d("Token Key", tokenKey);
                    Log.d("Token Value", "" + tokenValue);
                    //Log.d("Token Value", "" + snapshot.getValue());
                    Data data= new Data(fUser.getUid(), R.mipmap.ic_launcher, username + ": "+ message, "New Message", userid);
                    //Sender sender= new Sender(data, token.getToken());
                    Sender sender= new Sender(data, tokenValue);
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    Log.d("Response", response.toString());
                                    Log.d("Res Code", String.valueOf(response.code()));
                                    if(response.code() == 200)
                                    {
                                        Log.d("Res Success", String.valueOf(response.body().success));
                                        if(response.body().success != 1)
                                        {
                                            Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessages(final String myid, final String userid, final String imageurl)
    {
        Log.d("Reading", "Reading msg");
        mChat= new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                //Loop 1 to go through all the child nodes of child
                for(DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Log.d("Snapshot", snapshot.toString());
                    Chat chat= snapshot.getValue(Chat.class);

                    //if media exists
                    if(snapshot.child("media").exists())
                    {
                        Log.d("Media Found", "Media found");
                        long a = snapshot.child("media").getChildrenCount();

                        if(a>0)
                        {
                            //loop 2 to go through all the child nodes of media node
                            ArrayList<String> mediaArrayList= new ArrayList<>();
                            for(DataSnapshot mediaList : snapshot.child("media").getChildren())
                            {
                                Log.d("Total", String.valueOf(a));
                                String mediaKey = mediaList.getKey();
                                String mediaValue = mediaList.getValue().toString();
                                Log.d("Key", mediaKey);
                                Log.d("Value", mediaValue);
                                mediaArrayList.add(mediaValue);
                            }

                            chat.getMedia().setMedia(mediaArrayList);

                        }

                    }



                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid))
                    {
                            Log.d("Condition", "Condition passed");
                            mChat.add(chat);
                            Log.d("MyChat", chat.toString());
                    }


                    messageAdapter= new MessageAdapter(MessageActivity.this, mChat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }

                Log.d("Chat", String.valueOf(mChat.size()));
                Log.d("Chats", mChat.toString());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== RESULT_OK)
        {
            if(requestCode==PICK_IMAGE_INTENT)
            {
                //mediaUriList= new ArrayList<>();
                ////if user selects single image
                onlyImage=true;
                Log.d("Only Image +", String.valueOf(onlyImage));
                if(data.getClipData()==null)
                {

                    mediaUriList.add(data.getData().toString());
                }
                else
                {
                    //if user selects multiple image
                    for(int i=0; i< data.getClipData().getItemCount(); i++)
                    {
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }

                }

                mediaAdapter.notifyDataSetChanged();

            }
        }
    }

    //save user id of with whom chatting, and stop notification from myfirebaseMessaging class
    private void currentUser(String userid)
    {
        SharedPreferences.Editor editor= getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    //update status of current uesr on onPause and onResume callback
    private void status(String status)
    {
        reference= FirebaseDatabase.getInstance().getReference("Users").child(fUser.getUid());
        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
    }

    public void initializeMediaRecycler()
    {
        mediaUriList= new ArrayList<>();
        recycler_mediaList= findViewById(R.id.recycler_mediaList);
        recycler_mediaList.setNestedScrollingEnabled(false);
        recycler_mediaList.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recycler_mediaList.setLayoutManager(linearLayoutManager);
        mediaAdapter= new MediaAdapter(getApplicationContext(), mediaUriList, this);
        recycler_mediaList.setAdapter(mediaAdapter);
    }

    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver= getApplicationContext().getContentResolver();
        MimeTypeMap mimeTypeMap= MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onImageClose(int position) {
        Log.d("Adapter position", String.valueOf(position));
        mediaUriList.remove(position);
        mediaAdapter.notifyDataSetChanged();
        if(mediaUriList.isEmpty())
        {
            onlyImage=false;
            Log.d("Only Image -", String.valueOf(onlyImage));
        }
    }
}
