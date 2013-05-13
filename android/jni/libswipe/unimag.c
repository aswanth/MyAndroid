/*
 * unimag.c
 *
 *  Created on: 13-03-2012
 *      Author: Kamil Krzywicki, Marek Krasnowski
 */

#include "unimag.h"
#include "square.h"
#include "types.h"

static int crossingPoint = 10;

int unimag_add_wave(int value) {
	int returnValue = 0;

	if (value - crossingPoint < 3 && value - crossingPoint > -3)
		returnValue = 16;
	else if (value - (crossingPoint + 10) < 3
			&& value - (crossingPoint + 10) > -3)
		returnValue = 32;
	else if (value - (crossingPoint + 20) < 3
			&& value - (crossingPoint + 20) > -3)
		returnValue = 64;
	else if (value - (crossingPoint + 30) < 3
			&& value - (crossingPoint + 30) > -3)
		returnValue = 128;
	else if (value - (crossingPoint + 50) < 3
			&& value - (crossingPoint + 50) > -3)
		returnValue = 1;
	else if (value - (crossingPoint + 60) < 3
			&& value - (crossingPoint + 60) > -3)
		returnValue = 2;
	else if (value - (crossingPoint + 70) < 3
			&& value - (crossingPoint + 70) > -3)
		returnValue = 4;
	else if (value - (crossingPoint + 80) < 3
			&& value - (crossingPoint + 80) > -3)
		returnValue = 8;
	else
		returnValue = 0;

	int diff = 0;
	int rA = value % 5;
	int rCP = crossingPoint % 5;

	// need adjustment
	if (rA != rCP) {
		// calculate difference between crossPoint
		diff = rA - rCP;

		if (diff >= 3) {
			diff = diff - 5;
		} else if (diff <= -3) {
			diff = diff + 5;
		}

		// adjust crossPoint
		if (diff < 0) {
			--crossingPoint;
		} else {
			++crossingPoint;
		}
	}

	return returnValue;
}

char* unimag_decode(signed char *samples, long length) {
	int sample, cardStringLength = 0;
	int baud2400 = 0; // set baud2400 = 0, it's probably a modulation rate
	int x4 = 1;
	boolean dataStart = false;

	if (baud2400) {
		x4 = 4;
	}

	char *cardString = (char *) malloc(MAX_CARD_CHARS * sizeof(char));

	samples[length - 1] = 0;

	int position = length - 65000; // 65000/48000 = 1.354167 second
	if (position < 0)
		position = 0;

	int baseline = 0;
	int maxSample = 0;
	int minSample = 0;
	int maxCounter = 0;
	int minCounter = 0;

	// get average high and average low of background noise
	if (length > position + 800) {
		for (sample = position; sample < position + 800; sample++) {
			if (samples[sample] > 0) {
				maxSample = maxSample + samples[sample];
				maxCounter = maxCounter + 1;
			}

			if (samples[sample] <= 0) {
				minSample = minSample + samples[sample];
				minCounter = minCounter + 1;
			}
		}

		maxSample = maxSample / maxCounter;
		minSample = minSample / minCounter;
	}

	if (maxSample < 10)
		maxSample = 10;
	if (minSample > -10)
		minSample = -10;
	maxSample = maxSample + 50;
	minSample = minSample - 50;
	if (maxSample > 100)
		maxSample = 80;
	if (minSample < -100)
		minSample = -80;

	// calculate baseline according to average high and average low
	baseline = maxSample + minSample;

	int myChar = 0;
	int bitFlag = 1;
	int newPosition = 0;
	int isLow = 0;
	int byteCounter = 0;

	//************************************************************
	while (position + 1 * x4 < length && cardStringLength < MAX_CARD_CHARS) {
		int z;

		if (samples[position] < minSample
				&& samples[position + 45 * x4] > maxSample
				&& samples[position + 50 * x4] < minSample) {
			bitFlag = -1;
			isLow = 1;
			myChar = 0;

			// reset bit range checker
			crossingPoint = 10;

			//lets get the crossover points for the next 95 samples
			for (z = 0; z < 101; z++) {
				if (bitFlag == -1) {
					if (samples[position + z * x4] > baseline) {
						int bla = unimag_add_wave(z);
						myChar = myChar + bla;
						bitFlag = 1;
					}
				} else {
					if (samples[position + z * x4] < baseline) {
						bitFlag = -1;
						if (z > 97 && isLow == 1) {
							newPosition = z;
						}
					}
				}
			}

			if (newPosition > 0) {
				if (((char) myChar == '%') || ((char) myChar == ';')) {
					dataStart = true;
				}
				if (dataStart) {
					cardString[cardStringLength++] = myChar;
				}
				byteCounter = byteCounter + 1;
				position = position + (newPosition * x4) - (3 * x4);
			}
		}

		position = position + x4; // increment by 1
	}
	cardString[cardStringLength] = '\0';

	return cardString;
}
