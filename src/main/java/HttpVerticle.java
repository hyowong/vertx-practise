import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.client.WebClient;
import io.vertx.rxjava.ext.web.codec.BodyCodec;

/**
 * Created by yuq3 on 8/18/2017.
 */
public class HttpVerticle extends AbstractVerticle {

    Vertx vertx;

    public HttpVerticle() {
        vertx = Vertx.vertx();
        Router router = Router.router(vertx);

        WebClient webClient = WebClient.create(vertx,
                new WebClientOptions().setDefaultPort(8084).setDefaultHost("localhost"));

        router.route("/").handler(routingContext ->
                routingContext.response().end("server1")
        );

        router.get("/file1").handler(ctx -> {
            HttpServerResponse response = ctx.response();
            response.setChunked(true);
            response.putHeader("Content-Type", "application/octet-stream");
            response.putHeader("Content-Disposition", "attachment; filename=test.tar");

            webClient.get("/file2").as(BodyCodec.pipe(response))
                    .followRedirects(false)
                    .timeout(6000000)
                    .rxSend()
                    .subscribe(ctx1 -> {
                        System.out.println("subscribed");
                    });
        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8083);
        System.out.println("http server1 bootstrap successful, listening on port: " + 8083);
    }


}
