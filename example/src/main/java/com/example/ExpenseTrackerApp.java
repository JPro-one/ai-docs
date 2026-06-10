package com.example;

import one.jpro.platform.routing.Route;
import one.jpro.platform.routing.RouteApp;
import one.jpro.platform.routing.Response;

public class ExpenseTrackerApp extends RouteApp {

    @Override
    public Route createRoute() {
        return Route.empty()
            .and(Route.redirect("/", "/expenses"))
            .and(Route.get("/expenses", (r) -> Response.node(new ExpenseTrackerPage())));
    }
}
