package ch.zweifel.services;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.rx.java.RxHelper;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Created by Samuel Zweifel on 18.05.17.
 */
class ServiceChecker {

    private Vertx vertx;
    private Map<String,Subscription > subscriptions = new HashMap<>();
    ServiceChecker(Vertx vertx) {
        this.vertx = vertx;
    }

    synchronized void scheduleServiceCheck(Service service) {
        Scheduler scheduler = RxHelper.scheduler(vertx);
        Observable<Long> interval = Observable.interval(60, TimeUnit.SECONDS, scheduler);
        Subscription subscription = interval.subscribe(count -> {
            try {
                HttpClientOptions clientOptions = new HttpClientOptions().setSsl(true).setTrustAll(true);
                HttpClient client = vertx.createHttpClient(clientOptions);
                client.getAbs(service.getUrl(), response -> {
                    service.setLastCheck(new Date());
                    if (response.statusCode() < 300) {
                        service.setStatus("OK");
                    } else if (response.statusCode() < 400) {
                        service.setStatus("OK (3XX)");
                    } else {
                        service.setStatus("DOWN");
                    }
                    service.notifyChange();
                }).end();
            } catch (Exception e) {
                service.setLastCheck(new Date());
                service.setStatus("INVALID");
                service.notifyChange();
            }

        });
        subscriptions.put(service.getId(), subscription);
    }

    synchronized void removeServiceCheck(String id) {
        subscriptions.remove(id).unsubscribe();
    }
    synchronized void clearSubscriptions() {
        subscriptions.values().forEach(Subscription::unsubscribe);
        subscriptions.clear();
    }
}
