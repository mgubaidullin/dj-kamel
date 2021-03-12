## Serverless Image classification inference with DJL and Camel-K

### How to run
Requires OpenShift or CRC, Camel-K CLI and trained model 

```
kamel run -d camel-djl -d camel-jackson -d camel-vertx-http \
-d mvn:ai.djl:api:0.9.0 \
-d mvn:ai.djl.mxnet:mxnet-engine:0.9.0 \
--env=ENGINE_CACHE_DIR=/tmp \
dj-kamel.groovy --dev
```

### How to infer
Requires OpenShift or CRC, Camel-K CLI

```
curl -i -X GET "http://APP_URL/image?protocol=http&url=IMAGE_URL"
```
Example
```
curl -i -X GET "http://dj-kamel-dj-kamel.apps-crc.testing/image?protocol=http&url=github.com/mgubaidullin/dj-kamel/raw/master/negative.jpg"
```