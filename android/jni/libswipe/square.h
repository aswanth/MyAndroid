/*
 * square.h
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#ifndef SQUARE_H_
#define SQUARE_H_

#include "list.h"
#include "errors.h"
#include "pcm_helpers.h"
#include "types.h"

#define TRACK1_START_SENTINEL '%'
#define TRACK2_START_SENTINEL ';'
#define TRACK_END_SENTINEL '?'
#define BCD_CHAR_LENGTH 5
#define ALPHA_CHAR_LENGTH 7
#define BCD_ASCII_OFFSET 48
#define ALPHA_ASCII_OFFSET 32
#define MAX_CARD_CHARS 512

#define PREFETCH_CHARACTERS 10
#define SKIP_INITIAL_PEAKS 3
#define DELTA_CHANGE 250
#define DELTA_CHANGES 48
#define START_DELTA 6000
#define DELTA_VARIATION 0.002
#define START_SENTINEL_SEARCH_LIMIT 0.2

typedef enum {
	TRACK_1 = 0, TRACK_2, TRACK_UNKNOWN = 9
} TrackNum;

typedef struct decoder_object {
	PcmData *pcm;

	List *peaks;
	int *bitStream;
	int bitCount;
	int preFetchCharacters;
	int skipInitialPeaks;
	int deltaChange;
	int deltaChanges;
	int startDelta;

	double deltaVariation;
	double startSentinelSearchLimit;

	char *cardString;

	TrackNum trackNum;
	SwipeError lastError;
} SquareDecoder;

SquareDecoder* square_init(PcmData *pcm);
SquareDecoder* square_free(SquareDecoder *decoder);
void square_find_peaks(SquareDecoder *decoder, int delta);
void square_decode_peaks(SquareDecoder *decoder);
int square_determine_track_type(SquareDecoder *decoder);
int* square_reverse_bit_stream(int *bitStream, int bitCount);
boolean square_decode_bit_stream(SquareDecoder *decoder, int startIndex);
int square_check_lrc(int aStartIndex, int aEndIndex, int *bitStream, int bitCount, TrackNum trackNum, SwipeError *error);
char square_decode_character(int bitStreamIndex, int *bitStream, int bitCount, TrackNum trackNum, SwipeError *error);
SwipeError square_decode_swipe(SquareDecoder *decoder);

#endif /* SQUARE_H_ */
