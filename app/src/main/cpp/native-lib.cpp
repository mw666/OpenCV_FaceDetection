#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <android/native_window_jni.h>

#define LOG_TAG "C_TAG"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

using namespace cv;
using namespace  std;
CascadeClassifier * classifier=0;
ANativeWindow *window=0;
DetectionBasedTracker *tracker=0;

//samples/face-detection/jni/DetectionBasedTracker_jni.cpp
class CascadeDetectorAdapter: public DetectionBasedTracker::IDetector
{
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector):
            IDetector(),
            Detector(detector)
    {
        LOGD("CascadeDetectorAdapter::Detect::Detect");
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects)
    {
        LOGD("CascadeDetectorAdapter::Detect: begin");
        LOGD("CascadeDetectorAdapter::Detect: scaleFactor=%.2f, minNeighbours=%d, minObjSize=(%dx%d), maxObjSize=(%dx%d)", scaleFactor, minNeighbours, minObjSize.width, minObjSize.height, maxObjSize.width, maxObjSize.height);
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
        LOGD("CascadeDetectorAdapter::Detect: end");
    }

    virtual ~CascadeDetectorAdapter()
    {
        LOGD("CascadeDetectorAdapter::Detect::~Detect");
    }

private:
    CascadeDetectorAdapter();
    cv::Ptr<cv::CascadeClassifier> Detector;
};



extern "C" JNIEXPORT jstring JNICALL
Java_com_thyb_face_netease_1opencv_1facedetection_CameraActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_thyb_face_netease_1opencv_1facedetection_CameraActivity_init(JNIEnv *env, jobject instance, jstring model_) {
    const char *path = env->GetStringUTFChars(model_, 0);
    LOGD("CameraActivity=%s","init");
    if (tracker){
        tracker->stop();
        delete tracker;
        tracker=0;
    }
    //初始化分拣器模型
    //第一张方案
    //classifier=new CascadeClassifier(path);
    //第二种方案 创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(path));

    //创建一个跟踪适配器
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(path));

    DetectionBasedTracker::Parameters parameters;
    //创建一个跟踪器
    tracker=new DetectionBasedTracker(mainDetector,trackingDetector,parameters);

    //开始跟踪
     tracker->run();

    env->ReleaseStringUTFChars(model_, path);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_thyb_face_netease_1opencv_1facedetection_CameraActivity_setSurface(JNIEnv *env, jobject instance, jobject surface) {
    LOGD("CameraActivity=%s","setSurface");
   if(window){
      ANativeWindow_release(window);
      window=0;
   }
   window=ANativeWindow_fromSurface(env,surface);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_thyb_face_netease_1opencv_1facedetection_CameraActivity_faceDetected(JNIEnv *env, jobject instance, jbyteArray data_, jint width,
                                                                              jint height, jint mCameraId) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);

    LOGD("CameraActivity=%s","faceDetected");
    //数据存储的格式
    Mat src(height+height/2,width,CV_8UC1,data);

    cvtColor(src,src,COLOR_YUV2BGRA_NV21);

    if (mCameraId==1){
        //前置摄像头逆时针旋转90度
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);
        //水平翻转
        flip(src,src,1);
    } else{
        //后置摄像头顺时针旋转90度
        rotate(src,src,ROTATE_90_CLOCKWISE);
    }

    //写入存储卡
    imwrite("/sdcard/face.jpg",src);

    //灰度化处理
    Mat grayMat;
    cvtColor(src,grayMat,COLOR_BGRA2GRAY);

    //均衡化处理，增强图像对比度
    equalizeHist(grayMat,grayMat);

    //追踪定位
    std::vector<Rect> faces;
    //classifier->detectMultiScale(grayMat, faces);
    //第二种方案
    tracker->process(grayMat);
    tracker->getObjects(faces);

    //一张照片多个人脸
    for (int i = 0; i < faces.size(); ++i) {
        Rect face=faces[i];
        rectangle(src,face,Scalar(255,0,255));
    }
      // imshow("detection",src);

    if (window){
        //设置缓存区的形状
        ANativeWindow_setBuffersGeometry(window,src.cols,src.rows,WINDOW_FORMAT_RGBA_8888);

        ANativeWindow_Buffer buffer;
        do{
            if ( ANativeWindow_lock(window,&buffer,0)){
                //lock失败,直接break
                ANativeWindow_release(window);
                window=0;
                break;
            }
            //开始数据拷贝，src.data -->buffer.bits,一行一行的拷贝
            uint8_t *dst_data= static_cast<uint8_t *>(buffer.bits);
            //一行数据有多少个像素
             int dst_line_data=buffer.stride * 4;
             //进行拷贝
              for(int i=0;i<buffer.height;++i){
                 memcpy(dst_data+i * dst_line_data,src.data+ i*src.cols*4,dst_line_data);
              }
              //提交刷新
              ANativeWindow_unlockAndPost(window);
        }while (0);

    }
    //释放资源
    grayMat.release();
    src.release();
    env->ReleaseByteArrayElements(data_, data, 0);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_thyb_face_netease_1opencv_1facedetection_CameraActivity_release__(JNIEnv *env, jobject instance) {

    // TODO

}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_thyb_face_netease_1opencv_1facedetection_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}