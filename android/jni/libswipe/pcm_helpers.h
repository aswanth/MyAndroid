/*
 * pcm_helpers.h
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#ifndef PCM_HELPERS_H_
#define PCM_HELPERS_H_

#include "types.h"

#define SILENCE_SAMPLES 500
#define SILENCE_MULTIPLIER 5
#define CHUNK_SIZE 2048
#define CHECK_POINTS 32

typedef struct pcm_data {
	short* samples;
	int length;
	int silenceSamples;
	int silenceMultiplier;
	int chunkSize;
	int checkPoints;
} PcmData;

PcmData* pcm_init(short* samples, int length, boolean bigEndian);
PcmData* pcm_free(PcmData *pcm);
short* array_copy(short* source, int sourceFrom, short* destination, int destinationFrom, int length);
void pcm_normalize(short *toNormalize, int length);
void pcm_median_filter(short* inputData, int length);
int pcm_compare_samples(const void *sample1, const void *sample2);
int pcm_find_abs_mean(short *buffer, int start, int end);
int pcm_find_standard_deviation(short *buffer, int start, int end, int mean);
void pcm_remove_start_silence(PcmData *pcm);
void pcm_remove_end_silence(PcmData *pcm);
boolean pcm_check_for_silence(int checkPoints, int silenceThreshold, short *buffer, int startIndex, int endIndex);
void pcm_remove_dc_offset(PcmData *pcm);

#endif /* PCM_HELPERS_H_ */
