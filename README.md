## Serverless Image classification inference with DJL and Camel-K
Requires OpenShift or CRC, Camel-K CLI and trained model 

### What is Camel-K

Read [Introducing Camel K](https://www.nicolaferraro.me/2018/10/15/introducing-camel-k/) to learn more 

![camel-k](https://github.com/mgubaidullin/dj-kamel/raw/master/camel-k.png)

### How to run

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

### Integration code
dj-kamel.groovy
``` groovy
import ai.djl.Model
import ai.djl.basicmodelzoo.cv.classification.ResNetV1
import ai.djl.modality.cv.transform.Resize
import ai.djl.modality.cv.transform.ToTensor
import ai.djl.modality.cv.translator.ImageClassificationTranslator
import ai.djl.ndarray.types.Shape
import ai.djl.training.util.DownloadUtils
import org.apache.camel.Exchange

import java.nio.file.Paths

camel {
    DownloadUtils.download('https://github.com/mgubaidullin/dj-kamel/raw/master/defects-0001.params', '/tmp/model-0001.params')

    def resNet = ResNetV1.builder().setImageShape(new Shape(3, 23, 23)).setNumLayers(20).setOutSize(2).build()
    def model = Model.newInstance('model')
    model.setBlock(resNet)
    model.load(Paths.get('/tmp'), 'model')

    def translator = ImageClassificationTranslator.builder()
            .addTransform(new Resize(23, 23))
            .addTransform(new ToTensor())
            .optApplySoftmax(true)
            .optSynset(List.of('0', '1'))
            .build()

    registry.bind('Model', model)
    registry.bind('Translator', translator)
}

rest('/image')
        .get()
        .param().name('protocol').endParam()
        .param().name('url').endParam()
        .route()
        .setHeader(Exchange.HTTP_METHOD, constant('GET'))
        .setHeader(Exchange.HTTP_URI, constant(''))
        .setHeader('imageUrl', simple('vertx-http:${header.protocol}://${header.url}'))
        .toD('${header.imageUrl}?bridgeEndpoint=true&throwExceptionOnFailure=false')
        .to('djl:cv/image_classification??model=Model&translator=Translator')
        .marshal().json(true)
```        

### Data
Dataset used for model training is ["Concrete Crack Images for Classification"](https://data.mendeley.com/datasets/5y9wdsg2zt/2)
by Çağlar Fırat Özgenel is licensed under CC BY 4.0