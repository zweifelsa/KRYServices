package ch.zweifel.services.clickable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Samuel Zweifel on 18.05.17.
 */

public class ClickableRecyclerView extends RecyclerView {

    private OnItemClickListener itemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public ClickableRecyclerView(Context context) {
        super(context);
    }

    public ClickableRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnItemLongClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
        setAdapterOnItemClickListener();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        setAdapterOnItemClickListener();
    }

    private void setAdapterOnItemClickListener() {
        Adapter adapter = getAdapter();
        if(adapter instanceof ClickableAdapter) {
            ((ClickableAdapter) adapter).setOnItemClickListener(itemClickListener);
        }
    }
}
