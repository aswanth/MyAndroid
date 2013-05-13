#include <stdio.h>
#include <jni.h>
#include "libswipe/square.h"
#include "libswipe/pcm_helpers.h"
#include "libswipe/errors.h"
#include "libswipe/unimag.h"

jstring Java_com_ingogo_android_cardreader_helpers_Swipe_decodeSquareSwipe(JNIEnv *environment, jobject thiz, jshortArray jArray, int length) {
	jshort *jSamples;
	jstring returnString;
	short *samples;

	jSamples = (*environment)->GetShortArrayElements(environment, jArray, NULL);
	if (jSamples == NULL) {
		return (*environment)->NewStringUTF(environment, "");
	}
	samples = jSamples;

	PcmData *pcm = pcm_init(samples, length, false);

	(*environment)->ReleaseShortArrayElements(environment, jArray, jSamples, JNI_ABORT);

	SquareDecoder *decoder = square_init(pcm);
	square_decode_swipe(decoder);

	if (decoder->lastError == SwipeError_Success) {
		returnString = (*environment)->NewStringUTF(environment, decoder->cardString);
	} else {
		char errorCode[8];
		sprintf(errorCode, "%d", decoder->lastError);
		returnString = (*environment)->NewStringUTF(environment, errorCode);
	}

	pcm_free(pcm);
	square_free(decoder);

	return returnString;
}

jstring Java_com_ingogo_android_cardreader_helpers_Swipe_decodeUniMagSwipe(JNIEnv *environment, jobject thiz, jbyteArray jArray, int length) {
	jbyte *jSamples;
	jstring returnString;
	signed char *samples;

	jSamples = (*environment)->GetByteArrayElements(environment, jArray, NULL);
	if (jSamples == NULL) {
		return (*environment)->NewStringUTF(environment, "");
	}
	samples = jSamples;

	char *cardString = unimag_decode(samples, length);
	returnString = (*environment)->NewStringUTF(environment, cardString);
	if (cardString != NULL) {
		free(cardString);
	}

	(*environment)->ReleaseByteArrayElements(environment, jArray, jSamples, JNI_ABORT);

	return returnString;
}
