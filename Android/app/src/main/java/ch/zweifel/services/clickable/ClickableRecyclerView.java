package ch.zweifel.services.clickable;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by samuel on 18.05.17.
 */

public class ClickableRecyclerView extends RecyclerView implements View.OnClickListener {

    private OnItemClickListener longClickListener;

    @Override
    public void onClick(View v) {
        longClickListener.onItemClick(getChildLayoutPosition(v));
    }

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
        longClickListener = listener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter instanceof ClickableAdapter) {
            ((ClickableAdapter) adapter).setOnItemClickListener(this);
        }

    }
}
