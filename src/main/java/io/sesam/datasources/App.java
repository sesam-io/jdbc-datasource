package io.sesam.datasources;

import java.io.OutputStreamWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

import spark.Spark;

public class App {

    static Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        String configuration = System.getenv("CONFIGURATION");
        if (configuration == null) {
            log.error("Environment variable 'CONFIGURATION' not set. Using empty configuration.");
            configuration = "{}";
        } else {
            log.info("Loading configuration from the 'CONFIGURATION' environment variable.");
        }
        log.info("Configuration: " + configuration);

        Mapper mapper = Mapper.loadString(configuration);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Spark.stop();
                mapper.close();
            }
        });

        Spark.get("/:system/:source", (req, res) -> {
            res.type("application/json; charset=utf-8");
            String systemId = req.params("system");
            String sourceId = req.params("source");
            String since = req.queryParams("since");

            if (!mapper.isValidSource(systemId, sourceId)) {
                Spark.halt(404, "Unknown system/source pair.\n");
            }
            try {
                Writer writer = new OutputStreamWriter(res.raw().getOutputStream(), "utf-8");
                JsonWriter jsonWriter = new JsonWriter(writer);
                mapper.writeEntities(jsonWriter, systemId, sourceId, since);
                jsonWriter.flush();
            } catch (Exception e) {
                log.error("Got exception", e);
                Spark.halt(500);
            }
            return "";
        });
    }

}