package ch.zweifel.services.clickable;

import android.support.v7.widget.RecyclerView;

/**
 * Created by samuel on 18.05.17.
 */

public abstract class ClickableAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    private ClickableRecyclerView.OnItemClickListener longClickListener;

    void setOnItemClickListener(ClickableRecyclerView.OnItemClickListener listener) {
        longClickListener = listener;
    }

    public void onItemClick(int position) {
        longClickListener.onItemClick(position);
    }
}
