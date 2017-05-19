package ch.zweifel.services;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import ch.zweifel.services.clickable.ClickableRecyclerView;
import ch.zweifel.services.model.Service;
import ch.zweifel.services.networking.NetworkFragment;
import ch.zweifel.services.networking.RequestCallback;
import ch.zweifel.services.networking.ServiceRequest;

public class MainActivity extends AppCompatActivity implements RequestCallback, AddServiceDialogFragment.AddServiceListener {
    private static final String JSON_ROOT = "services";

    private ClickableRecyclerView serviceList;
    private SwipeRefreshLayout swipeContainer;
    private ServiceAdapter adapter;
    private NetworkFragment networkFragment;
    private boolean downloading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addServiceDialog();
            }
        });
        networkFragment = NetworkFragment.getInstance(getSupportFragmentManager());
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!downloading) {
                    fetchServices();
                }
            }
        });
        adapter = new ServiceAdapter();
        serviceList = (ClickableRecyclerView) findViewById(R.id.service_list);
        serviceList.setLayoutManager(new LinearLayoutManager(this));
        serviceList.setAdapter(adapter);
        serviceList.setOnItemLongClickListener(new ClickableRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(final int position) {
                if (adapter != null) {
                    final Service service = adapter.getItem(position);
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    dialogBuilder.setTitle(service.getName())
                            .setMessage(R.string.delete_service_message);
                    dialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            removeService(service, position);
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing
                        }
                    });
                    dialogBuilder.create().show();
                }
            }
        });
        fetchServices();
    }

    private void fetchServices() {
        if (!downloading && networkFragment != null) {
            // Execute the async download.
            networkFragment.startRequest(NetworkFragment.METHOD_GET);
            downloading = true;
            swipeContainer.setRefreshing(true);
        }
    }

    private void addServiceDialog() {
        AddServiceDialogFragment dialog = new AddServiceDialogFragment();
        dialog.show(getFragmentManager(), "AddServiceDialogFragment");
    }

    @Override
    public void addService(String name, String url) {
        Service service = new Service();
        service.setName(name);
        service.setUrl(url);
        networkFragment.startRequest(service, NetworkFragment.METHOD_POST);
        if (adapter != null) {
            adapter.addService(service);
        }
    }

    public void removeService(Service service, int position) {
        if (adapter != null) {
            adapter.removeAt(position);
        }
        networkFragment.startRequest(service, NetworkFragment.METHOD_DELETE);
    }

    @Override
    public void onSuccess(ServiceRequest request, String result) {
        if (request.getMethod().equals(NetworkFragment.METHOD_GET)) {
            // Update your UI here based on result of download.
            swipeContainer.setRefreshing(false);
            downloading = false;
            try {
                ObjectMapper mapper = new ObjectMapper();
                Service[] services = mapper.readerFor(Service[].class).withRootName(JSON_ROOT).readValue(result);
                if (adapter != null) {
                    adapter.addAll(services);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFail(ServiceRequest request, String message) {
        switch (request.getMethod()) {
            case NetworkFragment.METHOD_GET:
                swipeContainer.setRefreshing(false);
                downloading = false;
                break;
            case NetworkFragment.METHOD_POST:
                if (adapter != null) {
                    adapter.removeAt(request.getService().getPosition());
                }
                break;
            case NetworkFragment.METHOD_DELETE:
                if (adapter != null) {
                    adapter.insertAt(request.getService().getPosition(), request.getService());
                }
                break;
        }
        Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch (progressCode) {
            // not used
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }


    @Override
    public void finishRequest() {
        downloading = false;
        if (networkFragment != null) {
            networkFragment.cancelRequest();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
