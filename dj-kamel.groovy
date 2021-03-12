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