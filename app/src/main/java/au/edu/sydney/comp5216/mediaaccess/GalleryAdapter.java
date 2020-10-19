package au.edu.sydney.comp5216.mediaaccess;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

/**
 * Adapts the image URI's received for processing by the RecyclerView.
 *
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private Context context;
    private List<String> images;
    protected PhotoListener photoListener;

    /**
     * Constructor to set appropriate values.
     * @param context
     * @param images
     * @param photoListener
     */
    public GalleryAdapter(Context context, List<String> images, PhotoListener photoListener) {
        this.context = context;
        this.images = images;
        this.photoListener = photoListener;
    }

    /**
     * Creates a ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false)
        );
    }

    /**
     * Binds the image to the ViewHolder, and giving details of the image when clicked.
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final String image = images.get(position);
        Glide.with(context).load(image).into(holder.image);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoListener.onPhotoClick(image);
            }
        });
    }

    /**
     * Returns the number of images.
     * @return
     */
    @Override
    public int getItemCount() {
        if(images != null) {
            return images.size();
        }
        return 0;
    }

    /**
     * ViewHolder class
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        /**
         * ViewHolder constructor
         * @param itemView
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }

    /**
     * PhotoListener interface.
     */
    public interface PhotoListener {
        void onPhotoClick(String path);
    }
}