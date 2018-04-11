import io.vertx.core.file.OpenOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.core.streams.Pump;
import io.vertx.rxjava.ext.web.Router;

public class HttpVerticle2 extends AbstractVerticle {

    Vertx vertx;

    public HttpVerticle2() {
        vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        router.route("/").handler(routingContext ->
                routingContext.response().end("server2")
        );

        router.get("/file2").handler(ctx -> {
            HttpServerResponse response = ctx.response();

            String path = "test.tar";
            System.out.println("Requested");

            vertx.fileSystem()
                    .rxOpen(path, new OpenOptions().setRead(true))
                    .doOnSuccess(file -> {
                        response.setChunked(true);
                        Pump pump = Pump.pump(file, response);
                        pump.start();

                        file.endHandler(h -> response.end());
                        file.exceptionHandler(h -> response.end());
                    })
                    .doOnError(err -> {
                        System.out.println("Failed to process DataCollection download" + err.getLocalizedMessage());
                        response.setStatusCode(500);
                        response.end(err.getLocalizedMessage());
                    })

                    .subscribe(v -> {
                        System.out.println("Completed");
                    });
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8084);
        System.out.println("http server2 bootstrap successful, listening on port: " + 8084);
    }

}
