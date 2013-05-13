/*
 * square.c
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <stdint.h>

#include "square.h"
#include "pcm_helpers.h"
#include "errors.h"

SquareDecoder* square_init(PcmData *pcm) {
	SquareDecoder *decoder = (SquareDecoder*) malloc(sizeof(SquareDecoder));
	if (decoder == NULL) {
		return NULL;
	}

	decoder->pcm = pcm;
	decoder->bitStream = NULL;
	decoder->peaks = NULL;

	decoder->bitCount = 0;
	decoder->preFetchCharacters = PREFETCH_CHARACTERS;
	decoder->skipInitialPeaks = SKIP_INITIAL_PEAKS;
	decoder->deltaChange = DELTA_CHANGE;
	decoder->deltaChanges = DELTA_CHANGES;
	decoder->startDelta = START_DELTA;
	decoder->deltaVariation = DELTA_VARIATION;
	decoder->startSentinelSearchLimit = START_SENTINEL_SEARCH_LIMIT;
	decoder->trackNum = TRACK_UNKNOWN;

	if ((decoder->cardString = calloc(MAX_CARD_CHARS, sizeof(char))) == NULL) {
		free(decoder);
		return NULL;
	}

	decoder->lastError = SwipeError_Success;

	return decoder;
}

SquareDecoder* square_free(SquareDecoder *decoder) {
	if (decoder == NULL) {
		return NULL;
	}

	free(decoder->cardString);
	free(decoder->bitStream);
	list_free(decoder->peaks);
	free(decoder);

	return NULL;
}

SwipeError square_decode_swipe(SquareDecoder *decoder) {
	int startIndex = 0, loop = 2;

	do {
		boolean up = true;
		int i, delta = decoder->startDelta, upDelta = decoder->startDelta, downDelta = decoder->startDelta;
		for (i = 0; i < decoder->deltaChanges; ++i) {
			square_find_peaks(decoder, delta);
			square_decode_peaks(decoder);
			if (decoder->lastError == SwipeError_Success) {
				startIndex = square_determine_track_type(decoder);
				if (decoder->lastError == SwipeError_Success) {
					square_decode_bit_stream(decoder, startIndex);
				}
			}
			if (decoder->lastError != SwipeError_Success) {
				if (up) {
					upDelta += decoder->deltaChange;
					delta = upDelta;
					up = false;
				} else {
					downDelta -= decoder->deltaChange;
					delta = downDelta;
					up = true;
				}
			} else {
				return decoder->lastError;
			}
		}
		pcm_remove_dc_offset(decoder->pcm);
		pcm_normalize(decoder->pcm->samples, decoder->pcm->length);
		pcm_median_filter(decoder->pcm->samples, decoder->pcm->length);
		--loop;
	} while (loop > 0);

	return decoder->lastError;
}

void square_find_peaks(SquareDecoder *decoder, int delta) {
	short min = INT16_MAX, max = INT16_MIN;
	int i, minPos = 0, maxPos = 0;
	double firstDistance = 0.0, lastDistance = 0.0;
	boolean lookForMax = true, first = true;

	decoder->peaks = list_free(decoder->peaks);
	if ((decoder->peaks = list_init()) == NULL) {
		decoder->lastError = SwipeError_List;
		return;
	}

	for (i = 0; i < decoder->pcm->length; ++i) {
		short sample = decoder->pcm->samples[i];
		if (sample > max) {
			max = sample;
			maxPos = i;
		}
		if (sample < min) {
			min = sample;
			minPos = i;
		}

		if (lookForMax) {
			if (sample < max - delta) {
				list_add(decoder->peaks, maxPos);
				min = sample;
				minPos = i;
				lookForMax = false;
			}
		} else {
			if (sample > min + delta) {
				if (first) {
					firstDistance = max + abs(min);
					first = false;
				} else {
					double distance = max + abs(min);
					delta += ((int) (delta * (((distance + lastDistance) / 2.0) / firstDistance - 1.0)) * decoder->deltaVariation);
					lastDistance = distance;
				}
				list_add(decoder->peaks, minPos);
				max = sample;
				maxPos = i;
				lookForMax = true;
			}
		}
	}

	decoder->lastError = SwipeError_Success;
}

void square_decode_peaks(SquareDecoder *decoder) {
	if (decoder->peaks->size < decoder->skipInitialPeaks + 2) {
		decoder->lastError = SwipeError_NoPeaks;
		return;
	}

	int i, oneClock, lastPeakIndex, lastBit = -1, currentBit;
	List *bitStream = list_init();

	lastPeakIndex = list_get(decoder->peaks, decoder->skipInitialPeaks);
	oneClock = (list_get(decoder->peaks, decoder->skipInitialPeaks + 1) - lastPeakIndex) / 2;

	for (i = decoder->skipInitialPeaks + 1; i < decoder->peaks->size; ++i) {
		int diff = list_get(decoder->peaks, i) - lastPeakIndex;
		int oneDif = abs(oneClock - diff);
		int zeroDif = abs((oneClock * 2) - diff);

		if (oneDif < zeroDif) {
			oneClock = diff;
			currentBit = 1;
		} else {
			oneClock = diff / 2;
			currentBit = 0;
		}

		if (currentBit == 0) {
			list_add(bitStream, currentBit);
		} else if (currentBit == lastBit) {
			list_add(bitStream, currentBit);
			currentBit = -1;
		}

		lastBit = currentBit;
		lastPeakIndex = list_get(decoder->peaks, i);
	}

	decoder->bitCount = bitStream->size;
	if (decoder->bitStream) {
		free(decoder->bitStream);
	}
	decoder->bitStream = calloc(bitStream->size, sizeof(int));
	for (i = 0; i < bitStream->size; ++i) {
		decoder->bitStream[i] = list_get(bitStream, i);
	}
	list_free(bitStream);

	decoder->lastError = SwipeError_Success;
}

int square_determine_track_type(SquareDecoder *decoder) {
	int loop = 2;
	SwipeError swipeError = SwipeError_Success;
	decoder->trackNum = TRACK_UNKNOWN;

	do {
		int i = 0, j, stop;

		while (i < decoder->bitCount && decoder->bitStream[i] == 0) {
			++i;
		}
		stop = (int) ((decoder->bitCount - i) * decoder->startSentinelSearchLimit + i);

		while (i <= stop) {
			if (square_decode_character(i, decoder->bitStream, decoder->bitCount, TRACK_2, &swipeError) == TRACK2_START_SENTINEL
					&& square_decode_character(i + BCD_CHAR_LENGTH, decoder->bitStream, decoder->bitCount, TRACK_2, &swipeError) != TRACK_END_SENTINEL) {
				for (j = 1; j <= decoder->preFetchCharacters; ++j) {
					square_decode_character(i + BCD_CHAR_LENGTH + j * BCD_CHAR_LENGTH, decoder->bitStream, decoder->bitCount, TRACK_2, &swipeError);
					if (swipeError != SwipeError_Success) {
						break;
					}
				}
				if (swipeError != SwipeError_Success) {
					++i;
					continue;
				}
				decoder->trackNum = TRACK_2;
				decoder->lastError = SwipeError_Success;
				return i;
			}

			if (square_decode_character(i, decoder->bitStream, decoder->bitCount, TRACK_1, &swipeError) == TRACK1_START_SENTINEL
					&& square_decode_character(i + ALPHA_CHAR_LENGTH, decoder->bitStream, decoder->bitCount, TRACK_1, &swipeError) != TRACK_END_SENTINEL) {
				for (j = 1; j <= decoder->preFetchCharacters; ++i) {
					square_decode_character(i + ALPHA_CHAR_LENGTH + j * ALPHA_CHAR_LENGTH, decoder->bitStream, decoder->bitCount, TRACK_1, &swipeError);
					if (swipeError != SwipeError_Success) {
						break;
					}
				}
				if (swipeError != SwipeError_Success) {
					++i;
					continue;
				}
				decoder->trackNum = TRACK_1;
				decoder->lastError = SwipeError_Success;
				return i;
			}

			++i;
		}

		int *temp = decoder->bitStream;
		decoder->bitStream = square_reverse_bit_stream(decoder->bitStream, decoder->bitCount);
		free(temp);
		--loop;
	} while (loop > 0);

	decoder->lastError = SwipeError_IndeterminableTrackType;

	return TRACK_UNKNOWN;
}

int* square_reverse_bit_stream(int *bitStream, int bitCount) {
	int *reversed = (int*) calloc(bitCount, sizeof(int));
	int i;

	for (i = 0; i < bitCount; ++i) {
		reversed[i] = bitStream[bitCount - i - 1];
	}

	return reversed;
}

boolean square_decode_bit_stream(SquareDecoder *decoder, int startIndex) {
	boolean skipLeadingZeros = true;
	int charLength = 0, i = startIndex, streamStart = 0, bit, numChars = 0;

	decoder->lastError = SwipeError_Success;
	decoder->cardString[0] = '\0';

	if (decoder->trackNum == TRACK_1) {
		charLength = ALPHA_CHAR_LENGTH;
	} else if (decoder->trackNum == TRACK_2) {
		charLength = BCD_CHAR_LENGTH;
	} else {
		decoder->lastError = SwipeError_IndeterminableTrackType;
		return false;
	}

	while (i < decoder->bitCount) {
		bit = decoder->bitStream[i];
		if (skipLeadingZeros && bit == 0) {
			++i;
			continue;
		} else if (skipLeadingZeros) {
			skipLeadingZeros = false;
			streamStart = i;
		}

		char character = square_decode_character(i, decoder->bitStream, decoder->bitCount, decoder->trackNum, &decoder->lastError);
		if (decoder->lastError != SwipeError_Success) {
			return false;
		}

		if (numChars + 2 >= MAX_CARD_CHARS)
		{
			decoder->cardString[numChars] = '\0';
			decoder->lastError = SwipeError_TooManyChars;
			return false;
		}

		decoder->cardString[numChars++] = character;
		i += charLength;
		if (character == TRACK_END_SENTINEL) {
			decoder->cardString[numChars] = '\0';
			break;
		}
	}

	if (square_check_lrc(streamStart, i, decoder->bitStream, decoder->bitCount, decoder->trackNum, &decoder->lastError)) {
		decoder->lastError = SwipeError_Success;
		return true;
	} else {
		return false;
	}
}

boolean square_check_lrc(int startIndex, int endIndex, int *bitStream, int bitCount, TrackNum trackNum, SwipeError *swipeError) {
	int charLength = 0, streamLrcCode = 0, calculatedLrcCode = 0, j;

	*swipeError = SwipeError_Success;

	streamLrcCode = square_decode_character(endIndex, bitStream, bitCount, trackNum, swipeError);
	if (*swipeError != SwipeError_Success) {
		return false;
	}

	if (trackNum == TRACK_1) {
		charLength = ALPHA_CHAR_LENGTH;
	} else if (trackNum == TRACK_2) {
		charLength = BCD_CHAR_LENGTH;
	} else {
		*swipeError = SwipeError_IndeterminableTrackType;
		return false;
	}

	int *bitSum = calloc(charLength - 1, sizeof(int));
	int i;
	for (i = startIndex; i < endIndex; i += charLength) {
		for (j = 0; j < charLength - 1; ++j) {
			bitSum[j] += bitStream[i + j];
		}
	}

	int *lrcBitStream = calloc(charLength, sizeof(int));
	int lrcBitSum = 0;
	for (i = 0; i < charLength - 1; ++i) {
		int bit = bitSum[i] % 2 == 0 ? 0 : 1;
		lrcBitStream[i] = bit;
		lrcBitSum += bit;
	}
	lrcBitStream[i] = lrcBitSum % 2 == 0 ? 1 : 0;

	free(bitSum);

	calculatedLrcCode = square_decode_character(0, lrcBitStream, charLength, trackNum, swipeError);
	free(lrcBitStream);
	if (*swipeError != SwipeError_Success) {
		return false;
	}

	if (streamLrcCode == calculatedLrcCode) {
		return true;
	} else {
		*swipeError = SwipeError_Lrc;
		return false;
	}
}

char square_decode_character(int bitStreamIndex, int *bitStream, int bitCount, TrackNum trackNum, SwipeError *swipeError) {
	int bitSum = 0, bitNumber = 0, charLength = 0, asciiOffset = 0, i;

	*swipeError = SwipeError_Success;

	if (trackNum == TRACK_1) {
		charLength = ALPHA_CHAR_LENGTH;
		asciiOffset = ALPHA_ASCII_OFFSET;
	} else if (trackNum == TRACK_2) {
		charLength = BCD_CHAR_LENGTH;
		asciiOffset = BCD_ASCII_OFFSET;
	} else {
		*swipeError = SwipeError_IndeterminableTrackType;
		return -1;
	}

	if (!(bitStreamIndex + charLength <= bitCount)) {
		*swipeError = SwipeError_NotEnoughBits;
		return -1;
	}

	for (i = 0; i < charLength; ++i) {
		int bit = bitStream[bitStreamIndex + i];
		if (bit == 1) {
			++bitNumber;
		}
		if (i < charLength - 1) {
			bitSum += (1 << i) * bit;
		}
	}
	if (bitNumber % 2 == 0) {
		*swipeError = SwipeError_Parity;
		return -1;
	}

	return (char) (asciiOffset + bitSum);
}

