package ch.zweifel.services.clickable;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by samuel on 18.05.17.
 */

public abstract class ClickableAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> implements View.OnClickListener {

    private View.OnClickListener longClickListener;

    void setOnItemClickListener(View.OnClickListener listener) {
        longClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        longClickListener.onClick(view);
    }
}
