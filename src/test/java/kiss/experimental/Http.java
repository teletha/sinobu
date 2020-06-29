/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.experimental;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.XML;

public class Http {

    private static final HttpClient client = HttpClient.newHttpClient();

    public static Signal<XML> httpX(String uri) {
        return httpX(HttpRequest.newBuilder(URI.create(uri)));
    }

    public static Signal<XML> httpX(HttpRequest.Builder request) {
        return http(request, HttpResponse.BodyHandlers.ofInputStream()).map(I::xml);
    }

    public static Signal<JSON> httpJ(String uri) {
        return httpJ(HttpRequest.newBuilder(URI.create(uri)));
    }

    public static Signal<JSON> httpJ(HttpRequest.Builder request) {
        return http(request, HttpResponse.BodyHandlers.ofInputStream()).map(I::json);
    }

    public static <T> Signal<T> http(HttpRequest.Builder request, HttpResponse.BodyHandler<T> response) {
        return new Signal<>((observer, disposer) -> {
            return disposer.add(() -> client.sendAsync(request.build(), response).whenComplete((v, e) -> {
                if (e == null) {
                    observer.accept(v.body());
                    observer.complete();
                } else {
                    observer.error(e);
                }
            }).cancel(true));
        });
    }
}
