/*
 * errors.h
 *
 *  Created on: 01-03-2012
 *      Author: Marek Krasnowski
 */

#ifndef ERRORS_H_
#define ERRORS_H_

typedef enum {
	SwipeError_Success = 0,
	SwipeError_Parity = 1,
	SwipeError_Lrc = 2,
	SwipeError_NoPeaks = 3,
	SwipeError_NotEnoughBits = 4,
	SwipeError_Malloc = 5,
	SwipeError_IndeterminableTrackType = 6,
	SwipeError_TooManyChars = 7,
	SwipeError_List = 8
} SwipeError;

typedef enum {
	ListError_Success = 0, ListError_InvalidIndex, ListError_Malloc, ListError_CurrentNull
} ListError;

#endif /* ERRORS_H_ */
