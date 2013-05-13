/*
 * pcm_helpers.c
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdint.h>
#include <math.h>

#include "pcm_helpers.h"

PcmData* pcm_init(short* samples, int length, boolean endianSwap) {
	PcmData *pcm = (PcmData*) malloc(sizeof(PcmData));

	if (pcm == NULL) {
		return NULL;
	}

	int i;
	pcm->samples = malloc(length * sizeof(short));
	if (pcm->samples == NULL) {
		return NULL;
	}

	for (i = 0; i < length; ++i) {
		if (endianSwap) {
			pcm->samples[i] = samples[i] << 8 | samples[i] >> 8;
		} else {
			pcm->samples[i] = samples[i];
		}
	}

	pcm->length = length;
	pcm->checkPoints = CHECK_POINTS;
	pcm->chunkSize = CHUNK_SIZE;
	pcm->silenceMultiplier = SILENCE_MULTIPLIER;
	pcm->silenceSamples = SILENCE_SAMPLES;

	return pcm;
}

PcmData* pcm_free(PcmData *pcm) {
	if (pcm == NULL) {
		return NULL;
	}

	free(pcm->samples);
	free(pcm);

	return NULL;
}

short* array_copy(short* source, int sourceFrom, short* destination, int destinationFrom, int length) {
	if (source == NULL || destination == NULL) {
		return NULL;
	}

	int i;
	for (i = 0; i < length; ++i) {
		destination[i + destinationFrom] = source[i + sourceFrom];
	}

	return destination;
}

void pcm_normalize(short *toNormalize, int length) {
	if (length <= 0) {
		return;
	}

	int i, max = INT16_MAX;
	for (i = 0; i < length; ++i) {
		max = abs(toNormalize[i]) > max ? abs(toNormalize[i]) : max;
	}

	if (max == 0) {
		return;
	}

	double coefficient = (double) INT16_MAX / (double) max;
	for (i = 0; i < length; ++i) {
		toNormalize[i] = (short) (toNormalize[i] * coefficient);
	}
}

void pcm_median_filter(short* inputData, int length) {
	short window[5];

	int i;
	for (i = 2; i < length - 2; ++i) {
		array_copy(inputData, i - 2, window, 0, 5);
		qsort(window, 5, sizeof(short), pcm_compare_samples);
		inputData[i] = window[2];
	}
}

int pcm_compare_samples(const void *sample1, const void *sample2) {
	return (*(short*) sample1 - *(short*) sample1);
}

int pcm_find_abs_mean(short *buffer, int start, int end) {
	double total = 0;
	int length = end - start + 1;

	int i;
	for (i = start; i < start + length; ++i) {
		total += buffer[i];
	}

	return abs((int) (total / length));
}

int pcm_find_standard_deviation(short *buffer, int start, int end, int mean) {
	double squareSum = 0;
	int length = end - start + 1;

	int i;
	for (i = start; i < start + length; ++i) {
		squareSum += buffer[i] * buffer[i];
	}

	return (int) (sqrt(squareSum / length - mean * mean));
}

void pcm_remove_start_silence(PcmData *pcm) {
	int silenceAvg = pcm_find_abs_mean(pcm->samples, 0, pcm->silenceSamples - 1) + 1;
	int silenceDeviation = pcm_find_standard_deviation(pcm->samples, 0, pcm->silenceSamples - 1, silenceAvg) * pcm->silenceMultiplier;

	int i;
	for (i = 0; i < pcm->length; i += pcm->chunkSize) {
		if (i > pcm->length - 1) {
			i = pcm->length - 1;
		}
		if (!pcm_check_for_silence(pcm->checkPoints, silenceAvg + silenceDeviation, pcm->samples, i, i + pcm->chunkSize - 1)) {
			short *copy = malloc((pcm->length - i - 1) * sizeof(short));
			array_copy(pcm->samples, i + 1, copy, 0, pcm->length - i - 1);
			free(pcm->samples);
			pcm->samples = copy;
			pcm->length = pcm->length - i - 1;
			return;
		}
	}
}

void pcm_remove_end_silence(PcmData *pcm) {
	int silenceAvg = pcm_find_abs_mean(pcm->samples, pcm->length - pcm->silenceSamples, pcm->length - 1) + 1;
	int silenceDeviation = pcm_find_standard_deviation(pcm->samples, pcm->length - pcm->silenceSamples, pcm->length - 1, silenceAvg) * pcm->silenceMultiplier;

	int i;
	for (i = pcm->length - 1; i >= 0; i -= pcm->chunkSize) {
		if (i < 0) {
			i = 0;
		}
		if (!pcm_check_for_silence(pcm->checkPoints, silenceAvg + silenceDeviation, pcm->samples, i - pcm->chunkSize + 1, i)) {
			short *copy = malloc((i + 1) * sizeof(short));
			array_copy(pcm->samples, 0, copy, 0, i + 1);
			free(pcm->samples);
			pcm->samples = copy;
			pcm->length = i + 1;
			return;
		}
	}
}

/* Returns true if whole buffer contains silence */
boolean pcm_check_for_silence(int checkPoints, int silenceThreshold, short *buffer, int startIndex, int endIndex) {
	int length = abs(endIndex - startIndex + 1);
	int checkPeriod = length / checkPoints;
	short* temp = malloc(length * sizeof(short));

	if (startIndex < 0 || endIndex < 0 || startIndex > endIndex) {
		return true;
	}

	array_copy(buffer, startIndex, temp, 0, length);
	pcm_median_filter(temp, length);

	int j;
	for (j = 0; j < length; j += checkPeriod) {
		if (abs(temp[j]) > silenceThreshold) {
			free(temp);
			return false;
		}
	}
	if (abs(temp[length - 1]) > silenceThreshold) {
		free(temp);
		return false;
	}
	free(temp);

	return true;
}

void pcm_remove_dc_offset(PcmData *pcm) {
	int mean = 0;

	int i;
	for (i = 0; i < pcm->length; ++i) {
		mean += pcm->samples[i];
	}
	mean /= pcm->length;
	for (i = 0; i < pcm->length; ++i) {
		pcm->samples[i] -= mean;
	}
}

