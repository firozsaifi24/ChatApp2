package example.firoz.chatapp2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

import example.firoz.chatapp2.MessageActivity;
import example.firoz.chatapp2.Model.Chat;
import example.firoz.chatapp2.Model.Media;
import example.firoz.chatapp2.Model.User;
import example.firoz.chatapp2.R;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    private Context mContext;
    private List<Chat> mChat;
    private String imageurl;

    ArrayList<String> mediaArrayList;


    FirebaseUser fUser;

    public MessageAdapter(Context mContext, List<Chat> mChat, String imageurl) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageurl= imageurl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if(viewType == MSG_TYPE_RIGHT)
        {
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        }
        else
        {
            View view= LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, viewGroup, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder viewHolder, int position) {
        final Chat chat= mChat.get(position);

        viewHolder.show_message.setText(chat.getMessage());
        viewHolder.txt_message_time.setText(DateFormat.format("h:mm a", chat.getMessageTime()));
        //viewHolder.txt_message_time.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", chat.getMessageTime()));


        if(chat.isIsmedia())
        {
            if(chat.getMedia().getMedia().size()>0)
            {
                mediaArrayList=new ArrayList<>();
                Log.d("Adapter position ", position + ": "+ chat.getMedia().getMedia().size());

                for(String media: chat.getMedia().getMedia())
                {
                    mediaArrayList.add(media);
                }
                mediaArrayList.size();
                Log.d("Media Array", String.valueOf(mediaArrayList.size()));
                Glide.with(mContext).load(mediaArrayList.get(0)).into(viewHolder.show_image);
                if(mediaArrayList.size()>1)
                {
                    viewHolder.txt_image_count.setText(String.valueOf(mediaArrayList.size()));
                    viewHolder.txt_image_count.setVisibility(View.VISIBLE);
                }
                else
                {
                    viewHolder.txt_image_count.setText("");
                    viewHolder.txt_image_count.setVisibility(View.GONE);
                }

                viewHolder.show_message.setVisibility(View.GONE);
                viewHolder.image_layout.setVisibility(View.VISIBLE);
                //viewHolder.show_image.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            viewHolder.show_message.setText(chat.getMessage());
            viewHolder.show_message.setVisibility(View.VISIBLE);
            viewHolder.image_layout.setVisibility(View.GONE);
            //viewHolder.show_image.setVisibility(View.GONE);
        }

        if(imageurl.equals("default"))
        {
            viewHolder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else
        {
            Glide.with(mContext).load(imageurl).into(viewHolder.profile_image);
        }

        //check for last message
        if(position==mChat.size()-1)
        {
            if(chat.isIsseen())
            {
                viewHolder.txt_seen.setText("Seen");
            }
            else
            {
                viewHolder.txt_seen.setText("Delivered");
            }
        }
        else
        {
            viewHolder.txt_seen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public TextView txt_message_time;
        public ImageView show_image;
        public TextView txt_image_count;
        public RelativeLayout image_layout;
        public LinearLayout content_layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message= itemView.findViewById(R.id.show_message);
            profile_image= itemView.findViewById(R.id.profile_image);
            txt_seen= itemView.findViewById(R.id.txt_seen);
            show_image= itemView.findViewById(R.id.show_image);
            txt_image_count= itemView.findViewById(R.id.txt_image_count);
            txt_message_time= itemView.findViewById(R.id.txt_message_time);
            image_layout= itemView.findViewById(R.id.image_layout);
            content_layout= itemView.findViewById(R.id.show_content);

            show_image.setOnClickListener(this);
            content_layout.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            new ImageViewer.Builder(mContext, mChat.get(getAdapterPosition()).getMedia().getMedia())
                    .setStartPosition(0)
                    .show();
        }

        @Override
        public boolean onLongClick(View view) {
            String datetime= (String)DateFormat.format("dd-MMMM-yyyy (h:mm a)", mChat.get(getAdapterPosition()).getMessageTime());
            Toast.makeText(mContext, datetime, Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fUser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }
}
