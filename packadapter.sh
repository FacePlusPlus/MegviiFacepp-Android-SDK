#sh
rm -rf sdk/src/main/jniLibs/*
pushd .
cd ../megfacev2adapter/android/jni
echo "build began"
ndk-build
echo "build finish"
cp -R ../libs/* ../../../facepp/sdk/src/main/jniLibs
echo `pwd`

echo "finish"
popd
if [ $1="i" ]; then
    ./gradlew installDebug
fi