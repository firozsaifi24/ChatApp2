package example.firoz.chatapp2.Adapter;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import example.firoz.chatapp2.Interfaces.ImageCloseCallback;
import example.firoz.chatapp2.R;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.MediaViewHolder> {

    Context context;
    ArrayList<String> mediaList;
    private ImageCloseCallback imageCloseCallback;

    public MediaAdapter(Context context, ArrayList<String> mediaList, ImageCloseCallback imageCloseCallback) {
        this.context = context;
        this.mediaList = mediaList;
        this.imageCloseCallback= imageCloseCallback;
    }

    @NonNull
    @Override
    public MediaAdapter.MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media,null, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaAdapter.MediaViewHolder mediaViewHolder, int position) {
        Glide.with(context).load(Uri.parse(mediaList.get(position))).into(mediaViewHolder.mMedia);
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mMedia;
        ImageView mClose;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);

            mMedia= itemView.findViewById(R.id.img_media);
            mClose= itemView.findViewById(R.id.img_close);

            mClose.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            imageCloseCallback.onImageClose(getAdapterPosition());
        }
    }
}
