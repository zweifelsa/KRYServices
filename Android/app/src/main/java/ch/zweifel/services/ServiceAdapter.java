package ch.zweifel.services;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.zweifel.services.clickable.ClickableAdapter;

/**
 * Created by samuel on 18.05.17.
 */

public class ServiceAdapter extends ClickableAdapter<ServiceAdapter.ViewHolder> {

    private List<Service> services = new ArrayList<>();

    class ViewHolder extends RecyclerView.ViewHolder{
        View view;
        View marker;
        TextView name;
        TextView url;
        TextView status;
        TextView lastCheck;
        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            name = (TextView) itemView.findViewById(R.id.service_name);
            url = (TextView) itemView.findViewById(R.id.service_url);
            status = (TextView) itemView.findViewById(R.id.service_status);
            marker = itemView.findViewById(R.id.service_marker);
            lastCheck = (TextView) itemView.findViewById(R.id.service_lastcheck);
        }
    }

    @Override
    public ServiceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ServiceAdapter.ViewHolder holder, final int position) {
        holder.view.setOnClickListener(this);
        holder.name.setText(services.get(position).getName());
        holder.url.setText(services.get(position).getUrl());
        holder.status.setText(services.get(position).getStatus());
        holder.lastCheck.setText(services.get(position).getLastCheckString());
        if(services.get(position).getStatus().equals("OK")) {
            holder.marker.setBackgroundResource(R.color.colorStatusOK);
        } else if (services.get(position).getStatus().startsWith("OK")) {
            holder.marker.setBackgroundResource(R.color.colorStatus);
        } else {
            holder.marker.setBackgroundResource(R.color.colorStatusDownInvalid);
        }
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public Service removeAt(int position) {
        Service removed = services.remove(position);
        removed.setPosition(position);
        notifyItemRemoved(position);
        return removed;
    }

    public void addService(Service service) {
        services.add(service);
        service.setPosition(services.size()-1);
        notifyItemInserted(services.size()-1);
    }

    public void insertAt(int position, Service service) {
        services.add(position, service);
        service.setPosition(position);
        notifyItemInserted(position);
    }

    public void addAll(Service[] services) {
        this.services.clear();
        this.services.addAll(Arrays.asList(services));
        notifyDataSetChanged();
    }

    public Service getItem(int position) {
        return services.get(position);
    }
}
