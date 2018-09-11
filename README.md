# Alibaba Cloud OSS SDK for Android

## [README of Chinese](https://github.com/aliyun/aliyun-oss-android-sdk/blob/master/README-CN.md)

## Introduction

This document mainly describes how to install and use the OSS Android SDK. This document assumes that you have already activated the Alibaba Cloud OSS service and created an *AccessKeyID* and an *AccessKeySecret*. In the document, *ID* refers to the *AccessKeyID* and *KEY* indicates the *AccessKeySecret*. If you have not yet activated or do not know about the OSS service, log on to the [OSS Product Homepage](http://www.aliyun.com/product/oss) for more help.

## Environment requirements

- Android ***2.3*** or above
- You must have registered an Alibaba Cloud account with the OSS activated.

## Installation

OSS Android SDK is dependent on [OkHttp](https://github.com/square/okhttp). 

You can introduce the downloaded JAR package into the project, or you can use it through the Maven dependency. 

### Introduce the JAR package directly 

After you download the OSS Android SDK ZIP package, perform the following steps (applicable to Android Studio and Eclipse):

* On the official website, [click to view details](https://help.aliyun.com/document_detail/oss/sdk/sdk-download/android.html) to download the SDK package
* Unzip the SDK package in the libs directory to obtain the following JAR packages: aliyun-oss-sdk-android-2.3.0.jar, okhttp-3.x.x.jar and okio-1.x.x.jar
* Import the three JAR packages to the *libs* directory of the project

### Gradle via JCenter

```
compile 'com.aliyun.dpa:oss-android-sdk:+'

```

### Compile the JAR package from the source code

You can run the gradle command for packaging after cloning the project source code: 

```bash
# Clone the project
$ git clone https://github.com/aliyun/aliyun-oss-android-sdk.git

# Enter the directory
$ cd aliyun-oss-android-sdk/

# Run the packaging script. JDK 1.7 is required
$ ./gradlew releaseJar

# Enter the directory generated after packaging and the JAR package will be generated in this directory
$ cd build/libs && ls
```

### Javadoc

[Click to view details](http://aliyun.github.io/aliyun-oss-android-sdk/).

### Configure permissions

The following are the Android permissions needed by the OSS Android SDK. Please make sure these permissions are already set in your `AndroidManifest.xml` file. Otherwise, the SDK will not work normally.

```
<uses-permission android:name="android.permission.INTERNET"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"></uses-permission>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"></uses-permission>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"></uses-permission>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"></uses-permission>
```

### Descriptions of synchronous interfaces and asynchronous interfaces in the SDK

Because network requests cannot be processed on the UI thread in mobile development scenarios, many interfaces of the SDK support synchronous and asynchronous calls to handle requests. After being called, the synchronous interface block other requests while waiting for the returned results, whereas the asynchronous interface imports a callback function in the request to return the request processing results.

The UI thread does not support synchronous interface calls. *ClientException* or *ServiceException* will be thrown in the event of an exception. *ClientException* indicates a local exception, such as a network exception or invalid parameters. *ServiceException* indicates a service exception returned by the OSS, such as an authentication failure or a server error.

When an exception occurs during asynchronous request processing, the exception will be handled by a callback function.

When an asynchronous interface is called, the function will return a task directly. You can cancel the task, wait till the task is finished, or obtain results directly. For example, you can

```java
OSSAsyncTask task = oss.asyncGetObject(...);

task.cancel(); // Cancel the task

task.waitUntilFinished(); // Wait till the task is finished

GetObjectResult result = task.getResult(); // Block other requests and wait for the returned results
```

## Quick start

The basic object upload and download processes are demonstrated below. For details, you can refer to the following directories of this project:

The test directory: [click to view details](https://github.com/aliyun/aliyun-oss-android-sdk/tree/master/oss-android-sdk/src/androidTest/java/com/alibaba/sdk/android)

or

the sample directory: [click to view details](https://github.com/aliyun/aliyun-oss-android-sdk/tree/master/app).

### Step-1. Initialize the OSSClient

We recommend STS authentication mode to initialize the OSSClient on mobile. For details about authentication, refer to the *Access Control* section.

```java
String endpoint = "http://oss-cn-hangzhou.aliyuncs.com";

//if null , default will be init
ClientConfiguration conf = new ClientConfiguration();
conf.setConnectionTimeout(15 * 1000); // connction time out default 15s
conf.setSocketTimeout(15 * 1000); // socket timeout，default 15s
conf.setMaxConcurrentRequest(5); // synchronous request number，default 5 
conf.setMaxErrorRetry(2); // retry，default 2
OSSLog.enableLog(); //write local log file ,path is SDCard_path\OSSLog\logs.csv

OSSCredentialProvider credentialProvider = new OSSStsTokenCredentialProvider("<StsToken.AccessKeyId>", "<StsToken.SecretKeyId>", "<StsToken.SecurityToken>");

OSS oss = new OSSClient(getApplicationContext(), endpoint, credentialProvider, conf);
```

### Step-2. Upload a file

Suppose you already have a bucket in the OSS console. You can use the following code to upload a local file to OSS:

```java
// Construct an upload request
PutObjectRequest put = new PutObjectRequest("<bucketName>", "<objectKey>", "<uploadFilePath>");

// You can set progress callback during asynchronous upload
put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
	@Override
	public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
		Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
	}
});

OSSAsyncTask task = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
	@Override
	public void onSuccess(PutObjectRequest request, PutObjectResult result) {
		Log.d("PutObject", "UploadSuccess");
	}

	@Override
	public void onFailure(PutObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
		// Request exception
		if (clientExcepion != null) {
			// Local exception, such as a network exception
			clientExcepion.printStackTrace();
		}
		if (serviceException != null) {
			// Service exception
			Log.e("ErrorCode", serviceException.getErrorCode());
			Log.e("RequestId", serviceException.getRequestId());
			Log.e("HostId", serviceException.getHostId());
			Log.e("RawMessage", serviceException.getRawMessage());
		}
	}
});

// task.cancel(); // Cancel the task

// task.waitUntilFinished(); // Wait till the task is finished
```

### Step-3. Download a specified object

The following code downloads the specified object (you need to handle the input stream of the returned data):

```java
// Construct an object download request
GetObjectRequest get = new GetObjectRequest("<bucketName>", "<objectKey>");

OSSAsyncTask task = oss.asyncGetObject(get, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
	@Override
	public void onSuccess(GetObjectRequest request, GetObjectResult result) {
		// Request succeeds
		Log.d("Content-Length", "" + getResult.getContentLength());

		InputStream inputStream = result.getObjectContent();

		byte[] buffer = new byte[2048];
		int len;

		try {
			while ((len = inputStream.read(buffer)) != -1) {
				// Process the downloaded data
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onFailure(GetObjectRequest request, ClientException clientExcepion, ServiceException serviceException) {
		// Request exception
		if (clientExcepion != null) {
			// Local exception, such as a network exception
			clientExcepion.printStackTrace();
		}
		if (serviceException != null) {
			// Service exception
			Log.e("ErrorCode", serviceException.getErrorCode());
			Log.e("RequestId", serviceException.getRequestId());
			Log.e("HostId", serviceException.getHostId());
			Log.e("RawMessage", serviceException.getRawMessage());
		}
	}
});

// task.cancel(); // Cancel the task

// task.waitUntilFinished(); // Wait till the task is finished as needed

```

## Complete documentation

The SDK provides advanced upload, download, resumable upload/download, object management and bucket management features. For details, see the complete official documentation: [click to view details](https://help.aliyun.com/document_detail/oss/sdk/android-sdk/preface.html)

## License

* Apache License 2.0.

## Contact us

* [Alibaba Cloud OSS official website](http://oss.aliyun.com).
* [Alibaba Cloud OSS official forum](http://bbs.aliyun.com).
* [Alibaba Cloud OSS official documentation center](http://www.aliyun.com/product/oss#Docs).
* Alibaba Cloud official technical support: [Submit a ticket](https://workorder.console.aliyun.com/#/ticket/createIndex).
